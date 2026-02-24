package com.zademy.lu_memory.models;

import java.util.Locale;

public enum ObservationType {
    DECISION,
    BUGFIX,
    PATTERN,
    NOTE,
    SESSION_SUMMARY,
    PROMPT;

    public static ObservationType fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return NOTE;
        }

        return ObservationType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
