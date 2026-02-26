package com.zademy.lu_memory.services;

import com.zademy.lu_memory.entitys.ObservationEntity;
import com.zademy.lu_memory.entitys.PromptEntity;
import com.zademy.lu_memory.entitys.SessionEntity;
import com.zademy.lu_memory.models.ObservationRecord;
import com.zademy.lu_memory.models.PromptRecord;
import com.zademy.lu_memory.models.SessionRecord;
import com.zademy.lu_memory.models.ObservationType;
import com.zademy.lu_memory.repositorys.ObservationRepository;
import com.zademy.lu_memory.repositorys.PromptRepository;
import com.zademy.lu_memory.repositorys.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * <b>Core Domain Service</b> for managing the lifecycle of sessions,
 * observations, and prompts.
 * <p>
 * This service acts as an <b>Orchestrator</b> (Application Service layer) that
 * encapsulates
 * business logic for memory retention, de-duplication, and search.
 * It follows <b>SOLID</b> principles:
 * <ul>
 * <li><b>Single Responsibility (SRP):</b> Manages domain logic while delegating
 * persistence to Repositories.</li>
 * <li><b>Open/Closed (OCP):</b> Search capabilities are extensible via FTS5 and
 * fallback mechanisms.</li>
 * <li><b>Dependency Inversion (DIP):</b> Depends on Repository abstractions
 * rather than concrete implementations.</li>
 * </ul>
 */
@Service
public class MemoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryService.class);

    private final ObservationRepository observationRepository;
    private final PromptRepository promptRepository;
    private final SessionRepository sessionRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructs the MemoryService with required repositories and JDBC template.
     * Follows the <b>Dependency Inversion Principle (DIP)</b> by injecting
     * abstractions.
     *
     * @param observationRepository Repository for observation persistence.
     * @param promptRepository      Repository for prompt templates persistence.
     * @param sessionRepository     Repository for session tracking.
     * @param jdbcTemplate          JDBC Template for advanced FTS5 queries.
     */
    public MemoryService(
            ObservationRepository observationRepository,
            PromptRepository promptRepository,
            SessionRepository sessionRepository,
            JdbcTemplate jdbcTemplate) {
        this.observationRepository = observationRepository;
        this.promptRepository = promptRepository;
        this.sessionRepository = sessionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Initializes a new tracking session for an AI agent.
     *
     * @param agentName  The name of the agent (e.g., "Windsurf").
     * @param branchName The specific branch or context string.
     * @return The newly created {@link SessionEntity}.
     */
    @Transactional
    public SessionRecord startSession(String agentName, String branchName) {
        SessionEntity session = new SessionEntity();
        session.setAgentName(normalize(agentName));
        session.setBranchName(normalize(branchName));
        session.setStatus("STARTED");
        return toSessionRecord(sessionRepository.save(session));
    }

    /**
     * Completes and saves an ongoing session, registering the outcome status and a
     * detailed summary.
     *
     * @param sessionId The UUID of the session to end.
     * @param status    The status to set (e.g., "COMPLETED", "FAILED").
     * @param summary   The narrative summary of tasks performed during the session.
     * @return The updated {@link SessionEntity}.
     */
    @Transactional
    public SessionRecord endSession(UUID sessionId, String status, String summary) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        session.setStatus(normalize(status) == null ? "COMPLETED" : status.trim().toUpperCase(Locale.ROOT));

        String normSummary = normalize(summary);
        if (normSummary != null) {
            if (session.getSummary() == null || session.getSummary().isBlank()) {
                session.setSummary(normSummary);
            } else if (!session.getSummary().contains(normSummary)) {
                // Append the new text instead of destroying the previous summary.
                session.setSummary(session.getSummary() + "\n\n--- End Note ---\n" + normSummary);
            }
        }

        session.setEndedAt(Instant.now());
        return toSessionRecord(sessionRepository.save(session));
    }

    /**
     * Stores a granular piece of memory (observation) within the persistence layer.
     * Evaluates whether to create a new entry or update/increment a duplicate.
     *
     * @param type        The classification type of the observation (e.g.,
     *                    DECISION, NOTE).
     * @param topicKey    The logical grouping key for the memory.
     * @param title       A brief descriptive title.
     * @param content     The comprehensive text payload to store (will be redacted
     *                    if private).
     * @param tags        Comma-separated values acting as lookup metadata.
     * @param sessionId   The current active session UUID string associated with
     *                    this memory.
     * @param scope       Visibility boundary (e.g., "project" or "personal").
     * @param source      The mechanism that originated this memory.
     * @param projectName The domain name for multi-project isolation.
     * @return The saved or updated {@link ObservationEntity}.
     */
    @Transactional
    public ObservationRecord saveObservation(
            String type,
            String topicKey,
            String title,
            String content,
            String tags,
            String sessionId,
            String scope,
            String source,
            String projectName) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }

        // Sanitize content (redact <private>...</private>)
        String sanitizedContent = content.replaceAll("(?s)<private>.*?</private>", "[REDACTED]");
        String actualScope = normalize(scope) == null ? "project" : scope.trim().toLowerCase(Locale.ROOT);
        String actualTopicKey = resolveTopicKey(topicKey, title, sanitizedContent);
        String projectKey = "default";
        String contentHash = computeHash(sanitizedContent);

        // Check for existing observations to handle de-duplication or revisions
        Optional<ObservationEntity> existingOpt = observationRepository
                .findByScopeAndProjectKeyAndTopicKeyAndDeletedFalse(actualScope, projectKey, actualTopicKey);

        if (existingOpt.isPresent()) {
            ObservationEntity existing = existingOpt.get();
            existing.setLastSeenAt(Instant.now());

            // If content matches exactly, increment duplicate count (De-duplication
            // pattern)
            if (contentHash.equals(existing.getContentHash())) {
                existing.setDuplicateCount(existing.getDuplicateCount() + 1);
            } else {
                // If content changed, update it and increment revision count (Version tracking)
                existing.setContent(sanitizedContent.trim());
                if (normalize(title) != null) {
                    existing.setTitle(normalize(title));
                }
                if (normalize(tags) != null) {
                    existing.setTagsText(normalize(tags));
                }
                existing.setRevisionCount(existing.getRevisionCount() + 1);
                existing.setContentHash(contentHash);
            }
            return toObservationRecord(observationRepository.save(existing));
        }

        // New observation creation
        ObservationEntity observation = new ObservationEntity();
        observation.setType(ObservationType.fromString(type).name());
        observation.setTopicKey(actualTopicKey);
        observation.setTitle(normalize(title));
        observation.setContent(sanitizedContent.trim());
        observation.setTagsText(normalize(tags));
        observation.setSessionId(sessionId);
        observation.setSource(normalize(source));
        observation.setDeleted(false);
        observation.setScope(actualScope);
        observation.setProjectKey(projectKey);
        observation.setProjectName(normalize(projectName) != null ? normalize(projectName) : "default");
        observation.setContentHash(contentHash);
        observation.setDuplicateCount(0);
        observation.setRevisionCount(1);
        observation.setLastSeenAt(Instant.now());
        return toObservationRecord(observationRepository.save(observation));
    }

    /**
     * Computes a SHA-256 hash of the given input string.
     * Used for content de-duplication and version tracking.
     *
     * @param input The raw string content.
     * @return A hexadecimal representation of the SHA-256 hash.
     */
    private String computeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }

    /**
     * Updates an existing observation with new data.
     * Increments revision count if content significantly changes.
     *
     * @param observationId The unique identifier of the observation.
     * @param type          The new type classification (optional).
     * @param topicKey      The new topic key (optional).
     * @param title         The new title (optional).
     * @param content       The new content payload (optional).
     * @param tags          The new tags metadata (optional).
     * @param projectName   The new project name (optional).
     * @return The updated {@link ObservationRecord}.
     * @throws IllegalArgumentException if the observation is not found.
     */
    @Transactional
    public ObservationRecord updateObservation(
            UUID observationId,
            String type,
            String topicKey,
            String title,
            String content,
            String tags,
            String projectName) {
        ObservationEntity observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found: " + observationId));

        if (normalize(type) != null) {
            observation.setType(ObservationType.fromString(type).name());
        }
        if (normalize(topicKey) != null) {
            observation.setTopicKey(slugify(topicKey));
        }
        if (normalize(title) != null) {
            observation.setTitle(title.trim());
        }
        if (normalize(content) != null) {
            observation.setContent(content.trim());
        }
        if (tags != null) {
            observation.setTagsText(normalize(tags));
        }
        if (normalize(projectName) != null) {
            observation.setProjectName(projectName.trim());
        }

        return toObservationRecord(observationRepository.save(observation));
    }

    /**
     * Removes an observation from the system.
     * Supports both soft-delete (flagging) and hard-delete (physical removal).
     *
     * @param observationId The unique identifier of the observation.
     * @param hardDelete    True for physical removal, false for soft-delete.
     * @return A map containing the ID and the operation status.
     * @throws IllegalArgumentException if the observation is not found.
     */
    @Transactional
    public Map<String, Object> deleteObservation(UUID observationId, boolean hardDelete) {
        ObservationEntity observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found: " + observationId));

        if (hardDelete) {
            observationRepository.delete(observation);
            return Map.of(
                    "id", observationId,
                    "status", "HARD_DELETED");
        }

        observation.setDeleted(true);
        observation.setDeletedAt(Instant.now());
        observationRepository.save(observation);
        return Map.of(
                "id", observationId,
                "status", "SOFT_DELETED");
    }

    /**
     * Suggests a stable and SEO-friendly topic key based on hints.
     * Applies a semantic heuristic to categorize keys into namespaces (bug,
     * architecture, etc.).
     *
     * @param topicHint   Initial hint for the topic name.
     * @param contentHint Secondary hint derived from content.
     * @return A unique slugified topic key.
     */
    @Transactional(readOnly = true)
    public String suggestTopicKey(String topicHint, String contentHint) {
        String base = slugify(normalize(topicHint) != null ? topicHint : contentHint);
        if (base.isBlank()) {
            base = "general-memory";
        }

        // Heuristic by family
        String combined = ((topicHint != null ? topicHint : "") + " " + (contentHint != null ? contentHint : ""))
                .toLowerCase(Locale.ROOT);
        if (!base.contains("/")) {
            if (combined.contains("error") || combined.contains("exception") || combined.contains("bug")
                    || combined.contains("fix")) {
                base = "bug/" + base.replace("bug-", "").replace("error-", "");
            } else if (combined.contains("architecture") || combined.contains("design")
                    || combined.contains("database")) {
                base = "architecture/" + base;
            } else if (combined.contains("deploy") || combined.contains("ci/cd") || combined.contains("docker")) {
                base = "devops/" + base;
            } else if (combined.contains("pattern") || combined.contains("refactor")) {
                base = "pattern/" + base;
            }
        }

        if (!observationRepository.existsByTopicKeyAndDeletedFalse(base)) {
            return base;
        }

        int suffix = 2;
        while (observationRepository.existsByTopicKeyAndDeletedFalse(base + "-" + suffix)) {
            suffix++;
        }

        return base + "-" + suffix;
    }

    /**
     * Searches for memories using SQLite FTS5 full-text search capabilities.
     * Provides high-performance ranked retrieval based on relevance (BM25).
     *
     * @param query          The search string or FTS query.
     * @param limit          Maximum number of results to return.
     * @param includeDeleted Whether to search within soft-deleted observations.
     * @return A list of matching memory records with relevance scores.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchMemories(String query, int limit, boolean includeDeleted) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        try {
            // Use SQLite FTS5 for full-text search
            String sql = """
                    SELECT o.id,
                           o.type,
                           o.topic_key,
                           o.title,
                           o.content,
                           o.created_at,
                           o.deleted,
                           bm25(observations_fts) AS score
                    FROM observations o
                    JOIN observations_fts fts ON o.rowid = fts.rowid
                    WHERE (%s)
                      AND observations_fts MATCH ?
                    ORDER BY score ASC, o.created_at DESC
                    LIMIT ?
                    """.formatted(includeDeleted ? "1=1" : "o.deleted = false");

            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getString("id"));
                row.put("type", rs.getString("type"));
                row.put("topicKey", rs.getString("topic_key"));
                row.put("title", rs.getString("title"));
                row.put("snippet", toSnippet(rs.getString("content")));
                row.put("score", rs.getDouble("score"));
                row.put("createdAt", rs.getTimestamp("created_at").toInstant());
                row.put("deleted", rs.getBoolean("deleted"));
                return row;
            }, query, limit);
        } catch (Exception e) {
            LOGGER.error("FTS5 search error: " + e.getMessage(), e);
            // Fallback to simple search without FTS
            return fallbackSearch(query, limit, includeDeleted);
        }
    }

    /**
     * Fallback search mechanism using SQL LIKE operators when FTS5 is unavailable
     * or fails.
     *
     * @param query          The search term.
     * @param limit          Maximum results to return.
     * @param includeDeleted Whether to include soft-deleted records.
     * @return A list of matching memory records.
     */
    private List<Map<String, Object>> fallbackSearch(String query, int limit, boolean includeDeleted) {
        String sql = """
                SELECT id, type, topic_key, title, content, created_at, deleted
                FROM observations
                WHERE (%s)
                  AND (type LIKE ? OR topic_key LIKE ? OR title LIKE ? OR content LIKE ?)
                ORDER BY created_at DESC
                LIMIT ?
                """.formatted(includeDeleted ? "1=1" : "deleted = false");

        String searchTerm = "%" + query + "%";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getString("id"));
            row.put("type", rs.getString("type"));
            row.put("topicKey", rs.getString("topic_key"));
            row.put("title", rs.getString("title"));
            row.put("snippet", toSnippet(rs.getString("content")));
            row.put("score", 0.0);
            row.put("createdAt", rs.getTimestamp("created_at").toInstant());
            row.put("deleted", rs.getBoolean("deleted"));
            return row;
        }, searchTerm, searchTerm, searchTerm, searchTerm, limit);
    }

    /**
     * performs an advanced full-text search with highlighting and query
     * enhancement.
     * Uses enhanced query syntax to improve recall.
     *
     * @param query          The natural language query.
     * @param limit          Maximum results.
     * @param includeDeleted Include soft-deleted records.
     * @return Memory records with highlighted snippets.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchMemoriesAdvanced(String query, int limit, boolean includeDeleted) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        // Advanced search with FTS5 using different operators
        String ftsQuery = enhanceFtsQuery(query);

        String sql = """
                SELECT o.id,
                       o.type,
                       o.topic_key,
                       o.title,
                       o.content,
                       o.tags_text,
                       o.created_at,
                       o.deleted,
                       bm25(observations_fts) AS score,
                        snippet(observations_fts, 2, '<mark>', '</mark>', '...', 64) AS highlighted_content
                FROM observations o
                JOIN observations_fts fts ON o.rowid = fts.rowid
                WHERE (%s)
                  AND observations_fts MATCH ?
                ORDER BY score ASC, o.created_at DESC
                LIMIT ?
                """.formatted(includeDeleted ? "1=1" : "o.deleted = false");

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getObject("id", UUID.class));
            row.put("type", rs.getString("type"));
            row.put("topicKey", rs.getString("topic_key"));
            row.put("title", rs.getString("title"));
            row.put("content", rs.getString("content"));
            row.put("highlightedContent", rs.getString("highlighted_content"));
            row.put("tags", rs.getString("tags_text"));
            row.put("score", rs.getDouble("score"));
            row.put("createdAt", rs.getTimestamp("created_at").toInstant());
            row.put("deleted", rs.getBoolean("deleted"));
            return row;
        }, ftsQuery, limit);
    }

    /**
     * Enhances a plain query with FTS5 boolean operators for better search
     * performance.
     *
     * @param query The raw query string.
     * @return An optimized FTS5 query.
     */
    private String enhanceFtsQuery(String query) {
        // Enhance FTS query with advanced operators
        String enhanced = query.trim();

        // If it does not contain FTS operators, add prefix search
        if (!enhanced.contains("AND") && !enhanced.contains("OR") && !enhanced.contains("NOT")
                && !enhanced.contains("\"")) {
            String[] words = enhanced.split("\\s+");
            enhanced = String.join(" OR ", words);
        }

        return enhanced;
    }

    /**
     * Saves a high-level summary of the current session as a permanent observation.
     * Integrates lessons learned for future architectural reference.
     *
     * @param sessionId      The active session UUID.
     * @param summary        Narrative summary of progress.
     * @param lessonsLearned Key takeaways or technical discoveries.
     * @return The created {@link ObservationRecord}.
     */
    @Transactional
    public ObservationRecord saveSessionSummary(UUID sessionId, String summary, String lessonsLearned) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        String merged = summary;
        if (normalize(lessonsLearned) != null) {
            merged = (normalize(summary) == null ? "" : summary.trim() + "\n\n")
                    + "Lessons learned:\n"
                    + lessonsLearned.trim();
        }

        session.setSummary(merged);
        sessionRepository.save(session);

        return saveObservation(
                ObservationType.SESSION_SUMMARY.name(),
                "session-" + sessionId,
                "Session Summary",
                merged,
                "session,summary",
                sessionId.toString(),
                "project",
                "mem_session_summary",
                "default");
    }

    /**
     * Retrieves the comprehensive context for a specific topic or recent activity.
     * Consolidates observations, recent sessions, and prompt templates.
     *
     * @param topicKey       The group key to filter by (optional).
     * @param limit          Maximum number of items per category.
     * @param includePrompts Whether to include associated prompt templates.
     * @return A unified context map.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getContext(String topicKey, int limit, boolean includePrompts) {
        int cappedLimit = Math.max(1, Math.min(limit, 100));
        List<ObservationEntity> observations = observationRepository.findRecentByTopicKey(
                normalize(topicKey) == null ? null : topicKey,
                PageRequest.of(0, cappedLimit));

        List<Map<String, Object>> observationRows = observations.stream()
                .map(this::toObservationRow)
                .toList();

        List<Map<String, Object>> sessions = sessionRepository.findTop10ByStatusOrderByStartedAtDesc("COMPLETED")
                .stream()
                .map(session -> Map.<String, Object>of(
                        "id", session.getId(),
                        "status", session.getStatus(),
                        "startedAt", session.getStartedAt(),
                        "endedAt", session.getEndedAt(),
                        "summary", session.getSummary() == null ? "" : session.getSummary()))
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("observations", observationRows);
        response.put("recentSessions", sessions);

        if (includePrompts) {
            List<Map<String, Object>> prompts = promptRepository.findRecentByTopicKey(
                    normalize(topicKey) == null ? null : topicKey,
                    PageRequest.of(0, cappedLimit)).stream()
                    .map(prompt -> Map.<String, Object>of(
                            "id", prompt.getId(),
                            "sessionId", prompt.getSessionId(),
                            "topicKey", prompt.getTopicKey(),
                            "intent", prompt.getIntent(),
                            "prompt", prompt.getPrompt(),
                            "createdAt", prompt.getCreatedAt()))
                    .toList();
            response.put("prompts", prompts);
        }

        return response;
    }

    /**
     * Generates a chronological timeline around a specific observation point.
     * Useful for understanding the sequence of events or debugging.
     *
     * @param observationId The anchor observation ID.
     * @param windowMinutes Time range in minutes (before and after) to include.
     * @param limit         Maximum records to return.
     * @return A timeline representation including the center point.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> timeline(UUID observationId, int windowMinutes, int limit) {
        ObservationEntity center = observationRepository.findByIdAndDeletedFalse(observationId)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found: " + observationId));

        int effectiveWindowMinutes = Math.max(1, windowMinutes);
        Instant from = center.getCreatedAt().minus(effectiveWindowMinutes, ChronoUnit.MINUTES);
        Instant to = center.getCreatedAt().plus(effectiveWindowMinutes, ChronoUnit.MINUTES);

        List<ObservationEntity> around = observationRepository.findTimeline(from, to,
                PageRequest.of(0, Math.max(1, limit)));

        return Map.of(
                "center", toObservationRow(center),
                "timeline", around.stream().map(this::toObservationRow).toList(),
                "from", from,
                "to", to);
    }

    /**
     * Fetches detailed information for a single observation.
     *
     * @param observationId The unique ID.
     * @return Data map of the observation.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getObservation(UUID observationId) {
        ObservationEntity observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found: " + observationId));
        return toObservationRow(observation);
    }

    /**
     * Saves a reusable prompt template for future agent context.
     * Follows the <b>Strategy Pattern</b> by allowing categorized intent and
     * source.
     *
     * @param sessionId The current session ID.
     * @param prompt    The template text.
     * @param topicKey  Logical grouping key.
     * @param intent    The purpose of the prompt (e.g., "refactor", "debug").
     * @param source    Origin of the template.
     * @return The saved {@link PromptRecord}.
     */
    @Transactional
    public PromptRecord savePrompt(String sessionId, String prompt, String topicKey, String intent, String source) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt is required");
        }

        PromptEntity promptEntity = new PromptEntity();
        promptEntity.setSessionId(sessionId);
        promptEntity.setTopicKey(normalize(topicKey) == null ? suggestTopicKey(topicKey, prompt) : slugify(topicKey));
        promptEntity.setIntent(normalize(intent));
        promptEntity.setSource(normalize(source));
        promptEntity.setPrompt(prompt.trim());
        return toPromptRecord(promptRepository.save(promptEntity));
    }

    /**
     * Aggregates global statistics for the memory system.
     * Includes counts, de-duplication metrics, and top active topics.
     *
     * @return A map of diagnostic metrics.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> stats() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalObservations", observationRepository.count());
        response.put("activeObservations", observationRepository.countByDeletedFalse());
        response.put("deletedObservations", observationRepository.countByDeletedTrue());

        Long dupCount = observationRepository.sumDuplicateCountByDeletedFalse();
        Long revCount = observationRepository.sumRevisionCountByDeletedFalse();
        response.put("totalDuplicates", dupCount != null ? dupCount : 0L);
        response.put("totalRevisions", revCount != null ? revCount : 0L);

        response.put("savedPrompts", promptRepository.count());
        response.put("sessions", sessionRepository.count());
        response.put("openSessions", sessionRepository.countByStatus("STARTED"));

        String topTopicsSql = """
                SELECT topic_key, count(*) AS total
                FROM observations
                WHERE deleted = false
                GROUP BY topic_key
                ORDER BY total DESC
                LIMIT 5
                """;

        List<Map<String, Object>> topTopics = jdbcTemplate.query(topTopicsSql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("topicKey", rs.getString("topic_key"));
            row.put("count", rs.getLong("total"));
            return row;
        });
        response.put("topTopics", topTopics);

        return response;
    }

    /**
     * Retrieves a specific session record by its UUID.
     *
     * @param sessionId The session identifier.
     * @return An Optional containing the {@link SessionRecord} if found.
     */
    @Transactional(readOnly = true)
    public Optional<SessionRecord> getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId).map(this::toSessionRecord);
    }

    /**
     * Resolves the final topic key, prioritizing user input or generating one from
     * content.
     */
    private String resolveTopicKey(String provided, String title, String content) {
        if (normalize(provided) != null) {
            return slugify(provided);
        }

        String source = normalize(title) != null ? title : content;
        return suggestTopicKey(source, source);
    }

    /**
     * Transforms raw strings into URL-safe, lowercase slugs for consistent topic
     * tracking.
     */
    private String slugify(String raw) {
        if (raw == null) {
            return "";
        }

        String slug = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "")
                .trim();

        if (slug.length() > 120) {
            slug = slug.substring(0, 120).replaceAll("-$", "");
        }

        return slug;
    }

    /**
     * Trims strings and returns null if empty, ensuring clean data persistence.
     */
    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Creates a short preview of the content for list views.
     */
    private String toSnippet(String content) {
        if (content == null) {
            return "";
        }

        if (content.length() <= 220) {
            return content;
        }

        return content.substring(0, 220) + "...";
    }

    /**
     * Mapping helper to convert JPA Entity to an API-friendly Map representation.
     */
    private Map<String, Object> toObservationRow(ObservationEntity observation) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", observation.getId());
        row.put("type", observation.getType());
        row.put("topicKey", observation.getTopicKey());
        row.put("title", observation.getTitle());
        row.put("content", observation.getContent());
        row.put("tags", observation.getTagsText());
        row.put("source", observation.getSource());
        row.put("sessionId", observation.getSessionId());
        row.put("projectName", observation.getProjectName());
        row.put("createdAt", observation.getCreatedAt());
        row.put("updatedAt", observation.getUpdatedAt());
        row.put("deleted", observation.isDeleted());
        row.put("deletedAt", observation.getDeletedAt());
        return row;
    }

    private SessionRecord toSessionRecord(com.zademy.lu_memory.entitys.SessionEntity entity) {
        if (entity == null)
            return null;
        return new SessionRecord(
                entity.getId(), entity.getAgentName(), entity.getBranchName(),
                entity.getSummary(), entity.getStatus(), entity.getStartedAt(),
                entity.getEndedAt());
    }

    private ObservationRecord toObservationRecord(com.zademy.lu_memory.entitys.ObservationEntity entity) {
        if (entity == null)
            return null;
        return new ObservationRecord(
                entity.getId(), entity.getType(), entity.getTopicKey(),
                entity.getTitle(), entity.getContent(), entity.getTagsText(),
                entity.getSource(), entity.getSessionId(), entity.getScope(),
                entity.getProjectKey(), entity.getProjectName(), entity.getContentHash(),
                entity.getDuplicateCount(), entity.getRevisionCount(), entity.getLastSeenAt(),
                entity.isDeleted(), entity.getDeletedAt(), entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private PromptRecord toPromptRecord(com.zademy.lu_memory.entitys.PromptEntity entity) {
        if (entity == null)
            return null;
        return new PromptRecord(
                entity.getId(), entity.getSessionId(), entity.getTopicKey(),
                entity.getIntent(), entity.getSource(), entity.getPrompt(),
                entity.getCreatedAt());
    }
}
