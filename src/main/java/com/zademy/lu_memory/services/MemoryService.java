package com.zademy.lu_memory.services;

import com.zademy.lu_memory.entitys.ObservationEntity;
import com.zademy.lu_memory.entitys.PromptEntity;
import com.zademy.lu_memory.entitys.SessionEntity;
import com.zademy.lu_memory.models.ObservationType;
import com.zademy.lu_memory.repositorys.ObservationRepository;
import com.zademy.lu_memory.repositorys.PromptRepository;
import com.zademy.lu_memory.repositorys.SessionRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class MemoryService {

    private final ObservationRepository observationRepository;
    private final PromptRepository promptRepository;
    private final SessionRepository sessionRepository;
    private final JdbcTemplate jdbcTemplate;

    public MemoryService(
            ObservationRepository observationRepository,
            PromptRepository promptRepository,
            SessionRepository sessionRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.observationRepository = observationRepository;
        this.promptRepository = promptRepository;
        this.sessionRepository = sessionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public SessionEntity startSession(String agentName, String branchName) {
        SessionEntity session = new SessionEntity();
        session.setAgentName(normalize(agentName));
        session.setBranchName(normalize(branchName));
        session.setStatus("STARTED");
        return sessionRepository.save(session);
    }

    @Transactional
    public SessionEntity endSession(UUID sessionId, String status, String summary) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        session.setStatus(normalize(status) == null ? "COMPLETED" : status.trim().toUpperCase(Locale.ROOT));
        session.setSummary(normalize(summary));
        session.setEndedAt(Instant.now());
        return sessionRepository.save(session);
    }

    @Transactional
    public ObservationEntity saveObservation(
            String type,
            String topicKey,
            String title,
            String content,
            String tags,
            UUID sessionId,
            String source
    ) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }

        ObservationEntity observation = new ObservationEntity();
        observation.setType(ObservationType.fromString(type).name());
        observation.setTopicKey(resolveTopicKey(topicKey, title, content));
        observation.setTitle(normalize(title));
        observation.setContent(content.trim());
        observation.setTagsText(normalize(tags));
        observation.setSessionId(sessionId);
        observation.setSource(normalize(source));
        observation.setDeleted(false);
        return observationRepository.save(observation);
    }

    @Transactional
    public ObservationEntity updateObservation(
            UUID observationId,
            String type,
            String topicKey,
            String title,
            String content,
            String tags
    ) {
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

        return observationRepository.save(observation);
    }

    @Transactional
    public Map<String, Object> deleteObservation(UUID observationId, boolean hardDelete) {
        ObservationEntity observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found: " + observationId));

        if (hardDelete) {
            observationRepository.delete(observation);
            return Map.of(
                    "id", observationId,
                    "status", "HARD_DELETED"
            );
        }

        observation.setDeleted(true);
        observation.setDeletedAt(Instant.now());
        observationRepository.save(observation);
        return Map.of(
                "id", observationId,
                "status", "SOFT_DELETED"
        );
    }

    @Transactional(readOnly = true)
    public String suggestTopicKey(String topicHint, String contentHint) {
        String base = slugify(normalize(topicHint) != null ? topicHint : contentHint);
        if (base.isBlank()) {
            base = "general-memory";
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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchMemories(String query, int limit, boolean includeDeleted) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String sql = """
                SELECT id,
                       type,
                       topic_key,
                       title,
                       content,
                       created_at,
                       deleted,
                       ts_rank_cd(
                           to_tsvector('simple',
                               coalesce(topic_key,'') || ' ' ||
                               coalesce(title,'') || ' ' ||
                               coalesce(content,'') || ' ' ||
                               coalesce(tags_text,'')),
                           plainto_tsquery('simple', ?)
                       ) AS score
                FROM observations
                WHERE (%s)
                  AND to_tsvector('simple',
                      coalesce(topic_key,'') || ' ' ||
                      coalesce(title,'') || ' ' ||
                      coalesce(content,'') || ' ' ||
                      coalesce(tags_text,'')) @@ plainto_tsquery('simple', ?)
                ORDER BY score DESC, created_at DESC
                LIMIT ?
                """.formatted(includeDeleted ? "1=1" : "deleted = false");

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getObject("id", UUID.class));
            row.put("type", rs.getString("type"));
            row.put("topicKey", rs.getString("topic_key"));
            row.put("title", rs.getString("title"));
            row.put("snippet", toSnippet(rs.getString("content")));
            row.put("score", rs.getDouble("score"));
            row.put("createdAt", rs.getTimestamp("created_at").toInstant());
            row.put("deleted", rs.getBoolean("deleted"));
            return row;
        }, query, query, limit);
    }

    @Transactional
    public ObservationEntity saveSessionSummary(UUID sessionId, String summary, String lessonsLearned) {
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
                sessionId,
                "mem_session_summary"
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getContext(String topicKey, int limit, boolean includePrompts) {
        int cappedLimit = Math.max(1, Math.min(limit, 100));
        List<ObservationEntity> observations = observationRepository.findRecentByTopicKey(
                normalize(topicKey) == null ? null : topicKey,
                PageRequest.of(0, cappedLimit)
        );

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
                        "summary", session.getSummary() == null ? "" : session.getSummary()
                ))
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("observations", observationRows);
        response.put("recentSessions", sessions);

        if (includePrompts) {
            List<Map<String, Object>> prompts = promptRepository.findRecentByTopicKey(
                            normalize(topicKey) == null ? null : topicKey,
                            PageRequest.of(0, cappedLimit)
                    ).stream()
                    .map(prompt -> Map.<String, Object>of(
                            "id", prompt.getId(),
                            "sessionId", prompt.getSessionId(),
                            "topicKey", prompt.getTopicKey(),
                            "intent", prompt.getIntent(),
                            "prompt", prompt.getPrompt(),
                            "createdAt", prompt.getCreatedAt()
                    ))
                    .toList();
            response.put("prompts", prompts);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> timeline(UUID observationId, int windowMinutes, int limit) {
        ObservationEntity center = observationRepository.findByIdAndDeletedFalse(observationId)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found: " + observationId));

        int effectiveWindowMinutes = Math.max(1, windowMinutes);
        Instant from = center.getCreatedAt().minus(effectiveWindowMinutes, ChronoUnit.MINUTES);
        Instant to = center.getCreatedAt().plus(effectiveWindowMinutes, ChronoUnit.MINUTES);

        List<ObservationEntity> around = observationRepository.findTimeline(from, to, PageRequest.of(0, Math.max(1, limit)));

        return Map.of(
                "center", toObservationRow(center),
                "timeline", around.stream().map(this::toObservationRow).toList(),
                "from", from,
                "to", to
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getObservation(UUID observationId) {
        ObservationEntity observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found: " + observationId));
        return toObservationRow(observation);
    }

    @Transactional
    public PromptEntity savePrompt(UUID sessionId, String prompt, String topicKey, String intent, String source) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt is required");
        }

        PromptEntity promptEntity = new PromptEntity();
        promptEntity.setSessionId(sessionId);
        promptEntity.setTopicKey(normalize(topicKey) == null ? suggestTopicKey(topicKey, prompt) : slugify(topicKey));
        promptEntity.setIntent(normalize(intent));
        promptEntity.setSource(normalize(source));
        promptEntity.setPrompt(prompt.trim());
        return promptRepository.save(promptEntity);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> stats() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalObservations", observationRepository.count());
        response.put("activeObservations", observationRepository.countByDeletedFalse());
        response.put("deletedObservations", observationRepository.countByDeletedTrue());
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

    @Transactional(readOnly = true)
    public Optional<SessionEntity> getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId);
    }

    private String resolveTopicKey(String provided, String title, String content) {
        if (normalize(provided) != null) {
            return slugify(provided);
        }

        String source = normalize(title) != null ? title : content;
        return suggestTopicKey(source, source);
    }

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

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String toSnippet(String content) {
        if (content == null) {
            return "";
        }

        if (content.length() <= 220) {
            return content;
        }

        return content.substring(0, 220) + "...";
    }

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
        row.put("createdAt", observation.getCreatedAt());
        row.put("updatedAt", observation.getUpdatedAt());
        row.put("deleted", observation.isDeleted());
        row.put("deletedAt", observation.getDeletedAt());
        return row;
    }
}
