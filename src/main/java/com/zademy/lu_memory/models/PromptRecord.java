package com.zademy.lu_memory.models;

import java.time.Instant;
import java.util.UUID;

public record PromptRecord(
        UUID id,
        String sessionId,
        String topicKey,
        String intent,
        String source,
        String prompt,
        Instant createdAt) {
}
