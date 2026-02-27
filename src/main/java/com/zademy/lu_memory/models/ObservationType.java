package com.zademy.lu_memory.models;

import java.util.Locale;

/**
 * Represents the canonical categories used to classify stored observations.
 * <p>
 * These values are persisted and used by search/filter operations, so new values
 * should be introduced carefully to avoid breaking compatibility.
 */
public enum ObservationType {
    /** Architectural or implementation decision records. */
    DECISION,
    /** Bug reports, root causes, and fix notes. */
    BUGFIX,
    /** Reusable implementation or design patterns. */
    PATTERN,
    /** General-purpose note when no stronger category applies. */
    NOTE,
    /** End-of-session summaries generated for continuity. */
    SESSION_SUMMARY,
    /** User prompts saved as reusable templates. */
    PROMPT,
    /** System design and high-level architecture observations. */
    ARCHITECTURE,
    /** High-level summary items not tied to a full session summary. */
    SUMMARY,
    /** Documentation-related updates or guidance notes. */
    DOCUMENTATION;

    /**
     * Converts an arbitrary raw string into an {@link ObservationType}.
     * <p>
     * Input is normalized using {@link Locale#ROOT}. Blank or {@code null}
     * values default to {@link #NOTE}.
     *
     * @param raw the user-provided type value
     * @return the parsed enum value, or {@link #NOTE} when input is empty
     * @throws IllegalArgumentException if {@code raw} is non-empty but not a valid type
     */
    public static ObservationType fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return NOTE;
        }

        return ObservationType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
