package com.zademy.lu_memory.constants;

/**
 * Standardized error messages for the application.
 */
public final class ErrorMessages {
    private ErrorMessages() {
        // Prevent instantiation
    }

    public static final String SESSION_NOT_FOUND = "Session not found: ";
    public static final String OBSERVATION_NOT_FOUND = "Observation not found: ";
    public static final String CONTENT_REQUIRED = "content is required";
    public static final String PROMPT_REQUIRED = "prompt is required";
    public static final String INVALID_SCOPE = "Invalid scope. Allowed values: project, personal";
    public static final String INVALID_IMPORTANCE_LEVEL = "Invalid importance level. Allowed values: HIGH, MEDIUM, LOW";
    public static final String INVALID_STATUS = "Invalid status. Allowed values: STARTED, COMPLETED, FAILED, ABORTED";
    public static final String INVALID_QUERY = "query is required";
}
