package com.zademy.lu_memory.constants;

/**
 * Standardized keys used in Maps and JSON responses.
 */
public final class ResponseKeys {
    private ResponseKeys() {
        // Prevent instantiation
    }

    // Common keys
    public static final String ID = "id";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";

    // Observation keys
    public static final String TYPE = "type";
    public static final String TOPIC_KEY = "topicKey";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String SNIPPET = "snippet";
    public static final String SCORE = "score";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";
    public static final String DELETED = "deleted";
    public static final String DELETED_AT = "deletedAt";
    public static final String TAGS = "tags";
    public static final String SOURCE = "source";
    public static final String SESSION_ID = "sessionId";
    public static final String PROJECT_NAME = "projectName";
    public static final String PROJECT_KEY = "projectKey";
    public static final String SCOPE = "scope";
    public static final String IMPORTANCE_LEVEL = "importanceLevel";
    public static final String HIGHLIGHTED_CONTENT = "highlightedContent";

    // Session keys
    public static final String STARTED_AT = "startedAt";
    public static final String ENDED_AT = "endedAt";
    public static final String SUMMARY = "summary";

    // Prompt keys
    public static final String INTENT = "intent";
    public static final String PROMPT = "prompt";

    // Timeline keys
    public static final String CENTER = "center";
    public static final String TIMELINE = "timeline";
    public static final String FROM = "from";
    public static final String TO = "to";

    // Stats keys
    public static final String TOTAL_OBSERVATIONS = "totalObservations";
    public static final String ACTIVE_OBSERVATIONS = "activeObservations";
    public static final String DELETED_OBSERVATIONS = "deletedObservations";
    public static final String TOTAL_DUPLICATES = "totalDuplicates";
    public static final String TOTAL_REVISIONS = "totalRevisions";
    public static final String SAVED_PROMPTS = "savedPrompts";
    public static final String SESSIONS = "sessions";
    public static final String OPEN_SESSIONS = "openSessions";
    public static final String TOP_TOPICS = "topTopics";
    public static final String COUNT = "count";
}
