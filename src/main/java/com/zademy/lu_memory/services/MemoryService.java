package com.zademy.lu_memory.services;

import com.zademy.lu_memory.entities.ObservationEntity;
import com.zademy.lu_memory.entities.PromptEntity;
import com.zademy.lu_memory.entities.SessionEntity;
import com.zademy.lu_memory.constants.AppConstants;
import com.zademy.lu_memory.constants.ErrorMessages;
import com.zademy.lu_memory.constants.ResponseKeys;
import com.zademy.lu_memory.constants.SessionStatus;
import com.zademy.lu_memory.models.ObservationRecord;
import com.zademy.lu_memory.models.PromptRecord;
import com.zademy.lu_memory.models.SessionRecord;
import com.zademy.lu_memory.models.ObservationType;
import com.zademy.lu_memory.repositories.ObservationRepository;
import com.zademy.lu_memory.repositories.PromptRepository;
import com.zademy.lu_memory.repositories.SessionRepository;
import com.zademy.lu_memory.services.search.AdvancedMemorySearchQueryStrategy;
import com.zademy.lu_memory.services.search.BasicMemorySearchQueryStrategy;
import com.zademy.lu_memory.utils.EntityMapperUtils;
import com.zademy.lu_memory.utils.TextProcessingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

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
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final BasicMemorySearchQueryStrategy basicSearchQueryStrategy;
    private final AdvancedMemorySearchQueryStrategy advancedSearchQueryStrategy;

    private static final Pattern PRIVATE_BLOCK_PATTERN = Pattern.compile("(?s)<private>.*?</private>");

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
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            BasicMemorySearchQueryStrategy basicSearchQueryStrategy,
            AdvancedMemorySearchQueryStrategy advancedSearchQueryStrategy) {
        this.observationRepository = observationRepository;
        this.promptRepository = promptRepository;
        this.sessionRepository = sessionRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.basicSearchQueryStrategy = basicSearchQueryStrategy;
        this.advancedSearchQueryStrategy = advancedSearchQueryStrategy;
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
        session.setAgentName(TextProcessingUtils.normalize(agentName));
        session.setBranchName(TextProcessingUtils.normalize(branchName));
        session.setStatus(SessionStatus.STARTED.name());
        return EntityMapperUtils.toSessionRecord(sessionRepository.save(session));
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
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.SESSION_NOT_FOUND + sessionId));

        session.setStatus(resolveSessionStatus(status));

        String normSummary = TextProcessingUtils.normalize(summary);
        if (normSummary != null) {
            if (session.getSummary() == null || session.getSummary().isBlank()) {
                session.setSummary(normSummary);
            } else if (!session.getSummary().contains(normSummary)) {
                // Append the new text instead of destroying the previous summary.
                session.setSummary(session.getSummary() + "\n\n--- End Note ---\n" + normSummary);
            }
        }

        session.setEndedAt(Instant.now());
        return EntityMapperUtils.toSessionRecord(sessionRepository.save(session));
    }

    /**
     * Stores a granular piece of memory (observation) within the persistence layer.
     * Evaluates whether to create a new entry or update/increment a duplicate.
     *
     * @param type            The classification type of the observation (e.g.,
     *                        DECISION, NOTE).
     * @param topicKey        The logical grouping key for the memory.
     * @param title           A brief descriptive title.
     * @param content         The comprehensive text payload to store (will be
     *                        redacted
     *                        if private).
     * @param tags            Comma-separated values acting as lookup metadata.
     * @param sessionId       The current active session UUID string associated with
     *                        this memory.
     * @param scope           Visibility boundary (e.g., "project" or "personal").
     * @param source          The mechanism that originated this memory.
     * @param projectName     The domain name for multi-project isolation.
     * @param importanceLevel The configured importance level (HIGH, MEDIUM, LOW)
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
            String projectName,
            String importanceLevel) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(ErrorMessages.CONTENT_REQUIRED);
        }

        String sanitizedContent = redactPrivateContent(content);
        String actualScope = normalizeScope(scope);
        String projectKey = resolveProjectKey(projectName);
        String actualTopicKey = resolveTopicKey(topicKey, title, sanitizedContent);
        String contentHash = TextProcessingUtils.computeHash(sanitizedContent);
        String actualImportanceLevel = normalizeImportanceLevel(importanceLevel);

        // Check for existing observations to handle de-duplication or revisions
        Optional<ObservationEntity> existingOpt = observationRepository
                .findTopByScopeAndProjectKeyAndTopicKeyAndDeletedFalseOrderByUpdatedAtDesc(
                        actualScope,
                        projectKey,
                        actualTopicKey);

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
                if (TextProcessingUtils.normalize(title) != null) {
                    existing.setTitle(TextProcessingUtils.normalize(title));
                }
                if (TextProcessingUtils.normalize(tags) != null) {
                    existing.setTagsText(TextProcessingUtils.normalize(tags));
                }
                if (TextProcessingUtils.normalize(importanceLevel) != null) {
                    existing.setImportanceLevel(actualImportanceLevel);
                }
                existing.setRevisionCount(existing.getRevisionCount() + 1);
                existing.setContentHash(contentHash);
            }
            return EntityMapperUtils.toObservationRecord(observationRepository.save(existing));
        }

        // New observation creation
        ObservationEntity observation = new ObservationEntity();
        observation.setType(ObservationType.fromString(type).name());
        observation.setTopicKey(actualTopicKey);
        observation.setTitle(TextProcessingUtils.normalize(title));
        observation.setContent(sanitizedContent.trim());
        observation.setTagsText(TextProcessingUtils.normalize(tags));
        observation.setSessionId(sessionId);
        observation.setSource(TextProcessingUtils.normalize(source));
        observation.setDeleted(false);
        observation.setScope(actualScope);
        observation.setProjectKey(projectKey);
        observation.setProjectName(
                TextProcessingUtils.normalize(projectName) != null ? TextProcessingUtils.normalize(projectName)
                        : AppConstants.DEFAULT_PROJECT_NAME);
        observation.setImportanceLevel(actualImportanceLevel);
        observation.setContentHash(contentHash);
        observation.setDuplicateCount(0);
        observation.setRevisionCount(1);
        observation.setLastSeenAt(Instant.now());
        return EntityMapperUtils.toObservationRecord(observationRepository.save(observation));
    }

    /**
     * Updates an existing observation with new data.
     * Increments revision count if content significantly changes.
     *
     * @param observationId   The unique identifier of the observation.
     * @param type            The new type classification (optional).
     * @param topicKey        The new topic key (optional).
     * @param title           The new title (optional).
     * @param content         The new content payload (optional).
     * @param tags            The new tags metadata (optional).
     * @param projectName     The new project name (optional).
     * @param importanceLevel The new importance level (optional).
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
            String projectName,
            String importanceLevel) {
        ObservationEntity observation = observationRepository.findByIdAndDeletedFalse(observationId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.OBSERVATION_NOT_FOUND + observationId));

        if (TextProcessingUtils.normalize(type) != null) {
            observation.setType(ObservationType.fromString(type).name());
        }
        if (TextProcessingUtils.normalize(topicKey) != null) {
            observation.setTopicKey(TextProcessingUtils.slugify(topicKey));
        }
        if (TextProcessingUtils.normalize(title) != null) {
            observation.setTitle(title.trim());
        }
        if (TextProcessingUtils.normalize(content) != null) {
            String sanitizedContent = redactPrivateContent(content);
            String newHash = TextProcessingUtils.computeHash(sanitizedContent);
            if (!Objects.equals(newHash, observation.getContentHash())) {
                observation.setContent(sanitizedContent.trim());
                observation.setContentHash(newHash);
                observation.setRevisionCount(observation.getRevisionCount() + 1);
            }
        }
        if (tags != null) {
            observation.setTagsText(TextProcessingUtils.normalize(tags));
        }
        if (TextProcessingUtils.normalize(projectName) != null) {
            observation.setProjectName(projectName.trim());
            observation.setProjectKey(resolveProjectKey(projectName));
        }
        if (TextProcessingUtils.normalize(importanceLevel) != null) {
            observation.setImportanceLevel(normalizeImportanceLevel(importanceLevel));
        }
        observation.setLastSeenAt(Instant.now());

        return EntityMapperUtils.toObservationRecord(observationRepository.save(observation));
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
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.OBSERVATION_NOT_FOUND + observationId));

        if (hardDelete) {
            observationRepository.delete(observation);
            return Map.of(
                    ResponseKeys.ID, observationId,
                    ResponseKeys.STATUS, AppConstants.STATUS_HARD_DELETED);
        }

        observation.setDeleted(true);
        observation.setDeletedAt(Instant.now());
        observationRepository.save(observation);
        return Map.of(
                ResponseKeys.ID, observationId,
                ResponseKeys.STATUS, AppConstants.STATUS_SOFT_DELETED);
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
        String base = TextProcessingUtils
                .slugify(TextProcessingUtils.normalize(topicHint) != null ? topicHint : contentHint);
        if (base.isBlank()) {
            base = AppConstants.DEFAULT_TOPIC_KEY;
        }

        // Heuristic by family
        String combined = ((topicHint != null ? topicHint : "") + " " + (contentHint != null ? contentHint : ""))
                .toLowerCase(Locale.ROOT);
        if (!base.contains("/")) {
            if (combined.contains("error") || combined.contains("exception") || combined.contains("bug")
                    || combined.contains("fix")) {
                base = AppConstants.NAMESPACE_BUG + base.replace("bug-", "").replace("error-", "");
            } else if (combined.contains("architecture") || combined.contains("design")
                    || combined.contains("database")) {
                base = AppConstants.NAMESPACE_ARCHITECTURE + base;
            } else if (combined.contains("deploy") || combined.contains("ci/cd") || combined.contains("docker")) {
                base = AppConstants.NAMESPACE_DEVOPS + base;
            } else if (combined.contains("pattern") || combined.contains("refactor")) {
                base = AppConstants.NAMESPACE_PATTERN + base;
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
     * @param tags           Comma separated tags to filter by.
     * @param limit          Maximum number of results to return.
     * @param includeDeleted Whether to search within soft-deleted observations.
     * @return A list of matching memory records with relevance scores.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchMemories(String query, String tags, int limit, boolean includeDeleted) {
        return searchMemoriesScoped(query, tags, limit, includeDeleted, null, null);
    }

    /**
     * Searches memories with optional scope/project filtering.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchMemoriesScoped(
            String query,
            String tags,
            int limit,
            boolean includeDeleted,
            String scope,
            String projectKey) {
        String ftsQuery = basicSearchQueryStrategy.buildMatchQuery(query);
        if (ftsQuery.isBlank()) {
            return List.of();
        }

        String normalizedScope = normalizeScopeOrNull(scope);
        String normalizedProjectKey = normalizeProjectKeyOrNull(projectKey);
        List<String> tagFilters = normalizeTagFilters(tags);

        try {
            StringBuilder sql = new StringBuilder("""
                    SELECT o.id,
                           o.type,
                           o.topic_key,
                           o.title,
                           o.content,
                           o.importance_level,
                           o.created_at,
                           o.deleted,
                           bm25(observations_fts) AS score
                    FROM observations o
                    JOIN observations_fts fts ON o.rowid = fts.rowid
                    WHERE observations_fts MATCH :query
                    """);

            if (!includeDeleted) {
                sql.append(" AND o.deleted = false");
            }
            if (normalizedScope != null) {
                sql.append(" AND o.scope = :scope");
            }
            if (normalizedProjectKey != null) {
                sql.append(" AND o.project_key = :projectKey");
            }

            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("query", ftsQuery);
            parameters.addValue("limit", limit);

            if (normalizedScope != null) {
                parameters.addValue("scope", normalizedScope);
            }
            if (normalizedProjectKey != null) {
                parameters.addValue("projectKey", normalizedProjectKey);
            }

            appendTagFilters(sql, parameters, tagFilters);

            sql.append("""
                    ORDER BY score ASC, o.created_at DESC
                    LIMIT :limit
                    """);

            return namedParameterJdbcTemplate.query(sql.toString(), parameters, (rs, rowNum) -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put(ResponseKeys.ID, rs.getString("id"));
                row.put(ResponseKeys.TYPE, rs.getString("type"));
                row.put(ResponseKeys.TOPIC_KEY, rs.getString("topic_key"));
                row.put(ResponseKeys.TITLE, rs.getString("title"));
                row.put(ResponseKeys.SNIPPET, TextProcessingUtils.toSnippet(rs.getString("content")));
                row.put(ResponseKeys.IMPORTANCE_LEVEL, rs.getString("importance_level"));
                row.put(ResponseKeys.SCORE, rs.getDouble("score"));
                row.put(ResponseKeys.CREATED_AT, rs.getTimestamp("created_at").toInstant());
                row.put(ResponseKeys.DELETED, rs.getBoolean("deleted"));
                return row;
            });
        } catch (Exception e) {
            LOGGER.error("FTS5 search error: " + e.getMessage(), e);
            // Fallback to simple search without FTS
            return fallbackSearch(ftsQuery, tags, limit, includeDeleted, normalizedScope, normalizedProjectKey);
        }
    }

    /**
     * performs an advanced full-text search with highlighting and query
     * enhancement.
     * Uses enhanced query syntax to improve recall.
     *
     * @param query          The natural language query.
     * @param tags           Comma separated tags to filter by.
     * @param limit          Maximum results.
     * @param includeDeleted Include soft-deleted records.
     * @return Memory records with highlighted snippets.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchMemoriesAdvanced(String query, String tags, int limit,
            boolean includeDeleted) {
        return searchMemoriesAdvancedScoped(query, tags, limit, includeDeleted, null, null);
    }

    /**
     * Performs advanced full-text search with optional scope/project filtering.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchMemoriesAdvancedScoped(
            String query,
            String tags,
            int limit,
            boolean includeDeleted,
            String scope,
            String projectKey) {
        String ftsQuery = advancedSearchQueryStrategy.buildMatchQuery(query);
        if (ftsQuery.isBlank()) {
            return List.of();
        }

        String normalizedScope = normalizeScopeOrNull(scope);
        String normalizedProjectKey = normalizeProjectKeyOrNull(projectKey);
        List<String> tagFilters = normalizeTagFilters(tags);

        StringBuilder sql = new StringBuilder("""
                SELECT o.id,
                       o.type,
                       o.topic_key,
                       o.title,
                       o.content,
                       o.tags_text,
                       o.importance_level,
                       o.created_at,
                       o.deleted,
                       bm25(observations_fts) AS score,
                        snippet(observations_fts, 2, '<mark>', '</mark>', '...', 64) AS highlighted_content
                FROM observations o
                JOIN observations_fts fts ON o.rowid = fts.rowid
                WHERE observations_fts MATCH :query
                """);

        if (!includeDeleted) {
            sql.append(" AND o.deleted = false");
        }
        if (normalizedScope != null) {
            sql.append(" AND o.scope = :scope");
        }
        if (normalizedProjectKey != null) {
            sql.append(" AND o.project_key = :projectKey");
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("query", ftsQuery);
        parameters.addValue("limit", limit);
        if (normalizedScope != null) {
            parameters.addValue("scope", normalizedScope);
        }
        if (normalizedProjectKey != null) {
            parameters.addValue("projectKey", normalizedProjectKey);
        }
        appendTagFilters(sql, parameters, tagFilters);

        sql.append("""
                ORDER BY score ASC, o.created_at DESC
                LIMIT :limit
                """);

        return namedParameterJdbcTemplate.query(sql.toString(), parameters, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(ResponseKeys.ID, rs.getString("id"));
            row.put(ResponseKeys.TYPE, rs.getString("type"));
            row.put(ResponseKeys.TOPIC_KEY, rs.getString("topic_key"));
            row.put(ResponseKeys.TITLE, rs.getString("title"));
            row.put(ResponseKeys.CONTENT, rs.getString("content"));
            row.put(ResponseKeys.HIGHLIGHTED_CONTENT, rs.getString("highlighted_content"));
            row.put(ResponseKeys.TAGS, rs.getString("tags_text"));
            row.put(ResponseKeys.IMPORTANCE_LEVEL, rs.getString("importance_level"));
            row.put(ResponseKeys.SCORE, rs.getDouble("score"));
            row.put(ResponseKeys.CREATED_AT, rs.getTimestamp("created_at").toInstant());
            row.put(ResponseKeys.DELETED, rs.getBoolean("deleted"));
            return row;
        });
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
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.SESSION_NOT_FOUND + sessionId));

        String merged = summary;
        if (TextProcessingUtils.normalize(lessonsLearned) != null) {
            merged = (TextProcessingUtils.normalize(summary) == null ? "" : summary.trim() + "\n\n")
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
                AppConstants.SCOPE_PROJECT,
                "mem_session_summary",
                AppConstants.DEFAULT_PROJECT_NAME,
                AppConstants.DEFAULT_IMPORTANCE_LEVEL);
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
        return getContextScoped(topicKey, limit, includePrompts, null, null);
    }

    /**
     * Retrieves context with optional scope/project filtering.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getContextScoped(
            String topicKey,
            int limit,
            boolean includePrompts,
            String scope,
            String projectKey) {
        int cappedLimit = Math.max(1, Math.min(limit, 100));
        String normalizedScope = normalizeScopeOrNull(scope);
        String normalizedProjectKey = normalizeProjectKeyOrNull(projectKey);
        String normalizedTopicKey = TextProcessingUtils.normalize(topicKey) == null ? null : TextProcessingUtils.slugify(topicKey);

        List<ObservationEntity> observations = observationRepository.findRecent(
                normalizedScope,
                normalizedProjectKey,
                normalizedTopicKey,
                PageRequest.of(0, cappedLimit));

        List<Map<String, Object>> observationRows = observations.stream()
                .map(EntityMapperUtils::toObservationRow)
                .toList();

        List<Map<String, Object>> sessions = sessionRepository
                .findTop10ByStatusOrderByStartedAtDesc(SessionStatus.COMPLETED.name())
                .stream()
                .map(session -> Map.<String, Object>of(
                        ResponseKeys.ID, session.getId(),
                        ResponseKeys.STATUS, session.getStatus(),
                        ResponseKeys.STARTED_AT, session.getStartedAt(),
                        ResponseKeys.ENDED_AT, session.getEndedAt(),
                        ResponseKeys.SUMMARY, session.getSummary() == null ? "" : session.getSummary()))
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("observations", observationRows);
        response.put("recentSessions", sessions);
        response.put(ResponseKeys.SCOPE, normalizedScope);
        response.put(ResponseKeys.PROJECT_KEY, normalizedProjectKey);

        if (includePrompts) {
            List<Map<String, Object>> prompts = promptRepository.findRecentByTopicKey(
                    normalizedTopicKey,
                    PageRequest.of(0, cappedLimit)).stream()
                    .map(prompt -> Map.<String, Object>of(
                            ResponseKeys.ID, prompt.getId(),
                            ResponseKeys.SESSION_ID, prompt.getSessionId(),
                            ResponseKeys.TOPIC_KEY, prompt.getTopicKey(),
                            ResponseKeys.INTENT, prompt.getIntent(),
                            ResponseKeys.PROMPT, prompt.getPrompt(),
                            ResponseKeys.CREATED_AT, prompt.getCreatedAt()))
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
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.OBSERVATION_NOT_FOUND + observationId));

        int effectiveWindowMinutes = Math.max(1, windowMinutes);
        Instant from = center.getCreatedAt().minus(effectiveWindowMinutes, ChronoUnit.MINUTES);
        Instant to = center.getCreatedAt().plus(effectiveWindowMinutes, ChronoUnit.MINUTES);

        List<ObservationEntity> around = observationRepository.findTimeline(
                center.getScope(),
                center.getProjectKey(),
                from,
                to,
                PageRequest.of(0, Math.max(1, limit)));

        return Map.of(
                ResponseKeys.CENTER, EntityMapperUtils.toObservationRow(center),
                ResponseKeys.TIMELINE, around.stream().map(EntityMapperUtils::toObservationRow).toList(),
                ResponseKeys.FROM, from,
                ResponseKeys.TO, to);
    }

    /**
     * Fetches detailed information for a single observation.
     *
     * @param observationId The unique ID.
     * @return Data map of the observation.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getObservation(UUID observationId) {
        ObservationEntity observation = observationRepository.findByIdAndDeletedFalse(observationId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.OBSERVATION_NOT_FOUND + observationId));
        return EntityMapperUtils.toObservationRow(observation);
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
            throw new IllegalArgumentException(ErrorMessages.PROMPT_REQUIRED);
        }

        PromptEntity promptEntity = new PromptEntity();
        promptEntity.setSessionId(sessionId);
        promptEntity.setTopicKey(TextProcessingUtils.normalize(topicKey) == null ? suggestTopicKey(topicKey, prompt)
                : TextProcessingUtils.slugify(topicKey));
        promptEntity.setIntent(TextProcessingUtils.normalize(intent));
        promptEntity.setSource(TextProcessingUtils.normalize(source));
        promptEntity.setPrompt(prompt.trim());
        return EntityMapperUtils.toPromptRecord(promptRepository.save(promptEntity));
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
        response.put(ResponseKeys.TOTAL_OBSERVATIONS, observationRepository.count());
        response.put(ResponseKeys.ACTIVE_OBSERVATIONS, observationRepository.countByDeletedFalse());
        response.put(ResponseKeys.DELETED_OBSERVATIONS, observationRepository.countByDeletedTrue());

        Long dupCount = observationRepository.sumDuplicateCountByDeletedFalse();
        Long revCount = observationRepository.sumRevisionCountByDeletedFalse();
        response.put(ResponseKeys.TOTAL_DUPLICATES, dupCount != null ? dupCount : 0L);
        response.put(ResponseKeys.TOTAL_REVISIONS, revCount != null ? revCount : 0L);

        response.put(ResponseKeys.SAVED_PROMPTS, promptRepository.count());
        response.put(ResponseKeys.SESSIONS, sessionRepository.count());
        response.put(ResponseKeys.OPEN_SESSIONS, sessionRepository.countByStatus(SessionStatus.STARTED.name()));

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
            row.put(ResponseKeys.TOPIC_KEY, rs.getString("topic_key"));
            row.put(ResponseKeys.COUNT, rs.getLong("total"));
            return row;
        });
        response.put(ResponseKeys.TOP_TOPICS, topTopics);

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
        return sessionRepository.findById(sessionId).map(EntityMapperUtils::toSessionRecord);
    }

    /**
     * Resolves the final topic key, prioritizing user input or generating one from
     * content.
     */
    private String resolveTopicKey(String provided, String title, String content) {
        if (TextProcessingUtils.normalize(provided) != null) {
            return TextProcessingUtils.slugify(provided);
        }

        String source = TextProcessingUtils.normalize(title) != null ? title : content;
        return suggestTopicKey(source, source);
    }

    /**
     * Fallback search implementation when FTS5 is unavailable or fails.
     * Uses standard SQL LIKE operator for basic substring matching.
     */
    private List<Map<String, Object>> fallbackSearch(
            String query,
            String tags,
            int limit,
            boolean includeDeleted,
            String scope,
            String projectKey) {
        String normalizedScope = normalizeScopeOrNull(scope);
        String normalizedProjectKey = normalizeProjectKeyOrNull(projectKey);
        List<String> tagFilters = normalizeTagFilters(tags);
        String likeQuery = "%" + query.toLowerCase(Locale.ROOT) + "%";
        StringBuilder sql = new StringBuilder("""
                SELECT o.id,
                       o.type,
                       o.topic_key,
                       o.title,
                       o.content,
                       o.importance_level,
                       o.created_at,
                       o.deleted
                FROM observations o
                WHERE (LOWER(o.title) LIKE :likeQuery OR LOWER(o.content) LIKE :likeQuery)
                """);

        if (!includeDeleted) {
            sql.append(" AND o.deleted = false");
        }
        if (normalizedScope != null) {
            sql.append(" AND o.scope = :scope");
        }
        if (normalizedProjectKey != null) {
            sql.append(" AND o.project_key = :projectKey");
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("likeQuery", likeQuery);
        parameters.addValue("limit", limit);
        if (normalizedScope != null) {
            parameters.addValue("scope", normalizedScope);
        }
        if (normalizedProjectKey != null) {
            parameters.addValue("projectKey", normalizedProjectKey);
        }
        appendTagFilters(sql, parameters, tagFilters);

        sql.append("""
                ORDER BY o.created_at DESC
                LIMIT :limit
                """);

        return namedParameterJdbcTemplate.query(sql.toString(), parameters, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(ResponseKeys.ID, rs.getString("id"));
            row.put(ResponseKeys.TYPE, rs.getString("type"));
            row.put(ResponseKeys.TOPIC_KEY, rs.getString("topic_key"));
            row.put(ResponseKeys.TITLE, rs.getString("title"));
            row.put(ResponseKeys.SNIPPET, TextProcessingUtils.toSnippet(rs.getString("content")));
            row.put(ResponseKeys.IMPORTANCE_LEVEL, rs.getString("importance_level"));
            row.put(ResponseKeys.SCORE, 0.0); // No semantic scoring available
            row.put(ResponseKeys.CREATED_AT, rs.getTimestamp("created_at").toInstant());
            row.put(ResponseKeys.DELETED, rs.getBoolean("deleted"));
            return row;
        });
    }

    private String resolveSessionStatus(String status) {
        if (TextProcessingUtils.normalize(status) == null) {
            return SessionStatus.COMPLETED.name();
        }

        try {
            return SessionStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_STATUS);
        }
    }

    private String normalizeScope(String scope) {
        if (TextProcessingUtils.normalize(scope) == null) {
            return AppConstants.DEFAULT_SCOPE;
        }

        String normalized = scope.trim().toLowerCase(Locale.ROOT);
        if (!AppConstants.SCOPE_PROJECT.equals(normalized) && !AppConstants.SCOPE_PERSONAL.equals(normalized)) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_SCOPE);
        }
        return normalized;
    }

    private String normalizeScopeOrNull(String scope) {
        if (TextProcessingUtils.normalize(scope) == null) {
            return null;
        }
        return normalizeScope(scope);
    }

    private String resolveProjectKey(String projectName) {
        if (TextProcessingUtils.normalize(projectName) == null) {
            return AppConstants.DEFAULT_PROJECT_KEY;
        }

        String key = TextProcessingUtils.slugify(projectName);
        return key.isBlank() ? AppConstants.DEFAULT_PROJECT_KEY : key;
    }

    private String normalizeProjectKeyOrNull(String projectKey) {
        if (TextProcessingUtils.normalize(projectKey) == null) {
            return null;
        }

        String normalized = TextProcessingUtils.slugify(projectKey);
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeImportanceLevel(String importanceLevel) {
        if (TextProcessingUtils.normalize(importanceLevel) == null) {
            return AppConstants.DEFAULT_IMPORTANCE_LEVEL;
        }

        String normalized = importanceLevel.trim().toUpperCase(Locale.ROOT);
        if (!AppConstants.IMPORTANCE_HIGH.equals(normalized)
                && !AppConstants.DEFAULT_IMPORTANCE_LEVEL.equals(normalized)
                && !AppConstants.IMPORTANCE_LOW.equals(normalized)) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_IMPORTANCE_LEVEL);
        }
        return normalized;
    }

    private String redactPrivateContent(String rawContent) {
        return PRIVATE_BLOCK_PATTERN.matcher(rawContent).replaceAll("[REDACTED]");
    }

    private List<String> normalizeTagFilters(String tags) {
        if (TextProcessingUtils.normalize(tags) == null) {
            return List.of();
        }

        return Arrays.stream(tags.split(","))
                .map(TextProcessingUtils::normalize)
                .filter(Objects::nonNull)
                .map(tag -> tag.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    private void appendTagFilters(StringBuilder sql, MapSqlParameterSource parameters, List<String> tags) {
        for (int i = 0; i < tags.size(); i++) {
            String key = "tag" + i;
            sql.append(" AND LOWER(COALESCE(o.tags_text, '')) LIKE :").append(key);
            parameters.addValue(key, "%" + tags.get(i) + "%");
        }
    }

}
