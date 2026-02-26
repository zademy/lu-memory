package com.zademy.lu_memory.constants;

/**
 * General application configuration constants and default values.
 */
public final class AppConstants {
    private AppConstants() {
        // Prevent instantiation
    }

    public static final String DEFAULT_SCOPE = "project";
    public static final String DEFAULT_PROJECT_KEY = "default";
    public static final String DEFAULT_PROJECT_NAME = "default";
    public static final String DEFAULT_TOPIC_KEY = "general-memory";

    // Status keys for operations
    public static final String STATUS_HARD_DELETED = "HARD_DELETED";
    public static final String STATUS_SOFT_DELETED = "SOFT_DELETED";

    // Topic namespaces
    public static final String NAMESPACE_BUG = "bug/";
    public static final String NAMESPACE_ARCHITECTURE = "architecture/";
    public static final String NAMESPACE_DEVOPS = "devops/";
    public static final String NAMESPACE_PATTERN = "pattern/";
}
