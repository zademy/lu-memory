package com.zademy.lu_memory.models;

import java.time.Instant;
import java.util.UUID;

public record ObservationRecord(
        UUID id,
        String type,
        String topicKey,
        String title,
        String content,
        String tagsText,
        String source,
        String sessionId,
        String scope,
        String projectKey,
        String projectName,
        String contentHash,
        int duplicateCount,
        int revisionCount,
        Instant lastSeenAt,
        boolean deleted,
        Instant deletedAt,
        Instant createdAt,
        Instant updatedAt) {
}
