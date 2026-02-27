package com.zademy.lu_memory.utils;

import com.zademy.lu_memory.entitys.ObservationEntity;
import com.zademy.lu_memory.entitys.PromptEntity;
import com.zademy.lu_memory.entitys.SessionEntity;
import com.zademy.lu_memory.models.ObservationRecord;
import com.zademy.lu_memory.models.PromptRecord;
import com.zademy.lu_memory.models.SessionRecord;
import com.zademy.lu_memory.constants.ResponseKeys;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for converting between JPA entities and API-friendly
 * records/maps.
 * 
 * <p>
 * This class follows the <b>Mapper Pattern</b> to provide clean separation
 * between
 * persistence layer entities and API layer representations. It handles the
 * conversion
 * of complex entity structures to simpler, more user-friendly data transfer
 * objects.
 * </p>
 * 
 * <p>
 * Key responsibilities:
 * </p>
 * <ul>
 * <li>Entity to Record conversions for API responses</li>
 * <li>Entity to Map conversions for generic API responses</li>
 * <li>Null-safe conversions following defensive programming</li>
 * </ul>
 * 
 * @author lu-memory team
 * @since 1.0.0
 */
public final class EntityMapperUtils {

    private EntityMapperUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Converts a SessionEntity to a SessionRecord.
     * 
     * @param entity The session entity to convert
     * @return SessionRecord, or null if entity is null
     */
    public static SessionRecord toSessionRecord(SessionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new SessionRecord(
                entity.getId(),
                entity.getAgentName(),
                entity.getBranchName(),
                entity.getSummary(),
                entity.getStatus(),
                entity.getStartedAt(),
                entity.getEndedAt());
    }

    /**
     * Converts an ObservationEntity to an ObservationRecord.
     * 
     * @param entity The observation entity to convert
     * @return ObservationRecord, or null if entity is null
     */
    public static ObservationRecord toObservationRecord(ObservationEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ObservationRecord(
                entity.getId(),
                entity.getType(),
                entity.getTopicKey(),
                entity.getTitle(),
                entity.getContent(),
                entity.getTagsText(),
                entity.getSource(),
                entity.getSessionId(),
                entity.getScope(),
                entity.getProjectKey(),
                entity.getProjectName(),
                entity.getContentHash(),
                entity.getDuplicateCount(),
                entity.getRevisionCount(),
                entity.getLastSeenAt(),
                entity.isDeleted(),
                entity.getDeletedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    /**
     * Converts a PromptEntity to a PromptRecord.
     * 
     * @param entity The prompt entity to convert
     * @return PromptRecord, or null if entity is null
     */
    public static PromptRecord toPromptRecord(PromptEntity entity) {
        if (entity == null) {
            return null;
        }
        return new PromptRecord(
                entity.getId(),
                entity.getSessionId(),
                entity.getTopicKey(),
                entity.getIntent(),
                entity.getSource(),
                entity.getPrompt(),
                entity.getCreatedAt());
    }

    /**
     * Converts an ObservationEntity to a Map representation for API responses.
     * 
     * <p>
     * This method is useful for generic API responses where a flexible
     * data structure is preferred over strongly-typed records.
     * </p>
     * 
     * @param observation The observation entity to convert
     * @return Map containing all observation fields with proper keys
     */
    public static Map<String, Object> toObservationRow(ObservationEntity observation) {
        if (observation == null) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> row = new LinkedHashMap<>();
        row.put(ResponseKeys.ID, observation.getId());
        row.put(ResponseKeys.TYPE, observation.getType());
        row.put(ResponseKeys.TOPIC_KEY, observation.getTopicKey());
        row.put(ResponseKeys.TITLE, observation.getTitle());
        row.put(ResponseKeys.CONTENT, observation.getContent());
        row.put(ResponseKeys.TAGS, observation.getTagsText());
        row.put(ResponseKeys.SOURCE, observation.getSource());
        row.put(ResponseKeys.SESSION_ID, observation.getSessionId());
        row.put(ResponseKeys.PROJECT_NAME, observation.getProjectName());
        row.put(ResponseKeys.CREATED_AT, observation.getCreatedAt());
        row.put(ResponseKeys.UPDATED_AT, observation.getUpdatedAt());
        row.put(ResponseKeys.DELETED, observation.isDeleted());
        row.put(ResponseKeys.DELETED_AT, observation.getDeletedAt());
        return row;
    }

    /**
     * Converts a SessionEntity to a Map representation for API responses.
     * 
     * @param session The session entity to convert
     * @return Map containing all session fields with proper keys
     */
    public static Map<String, Object> toSessionRow(SessionEntity session) {
        if (session == null) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> row = new LinkedHashMap<>();
        row.put(ResponseKeys.ID, session.getId());
        row.put("agentName", session.getAgentName());
        row.put("branchName", session.getBranchName());
        row.put(ResponseKeys.STATUS, session.getStatus());
        row.put(ResponseKeys.SUMMARY, session.getSummary());
        row.put(ResponseKeys.STARTED_AT, session.getStartedAt());
        row.put(ResponseKeys.ENDED_AT, session.getEndedAt());
        return row;
    }

    /**
     * Converts a PromptEntity to a Map representation for API responses.
     * 
     * @param prompt The prompt entity to convert
     * @return Map containing all prompt fields with proper keys
     */
    public static Map<String, Object> toPromptRow(PromptEntity prompt) {
        if (prompt == null) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> row = new LinkedHashMap<>();
        row.put(ResponseKeys.ID, prompt.getId());
        row.put(ResponseKeys.SESSION_ID, prompt.getSessionId());
        row.put(ResponseKeys.TOPIC_KEY, prompt.getTopicKey());
        row.put(ResponseKeys.INTENT, prompt.getIntent());
        row.put(ResponseKeys.SOURCE, prompt.getSource());
        row.put(ResponseKeys.PROMPT, prompt.getPrompt());
        row.put(ResponseKeys.CREATED_AT, prompt.getCreatedAt());
        return row;
    }

    /**
     * Safely extracts a string value from a Map, with null fallback.
     * 
     * @param map The map to extract value from
     * @param key The key to extract
     * @return The string value, or null if not found or null
     */
    public static String extractString(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return null;
        }
        Object value = map.get(key);
        return value instanceof String string ? string : null;
    }
}
