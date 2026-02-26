package com.zademy.lu_memory.models;

import java.time.Instant;
import java.util.UUID;

public record SessionRecord(
        UUID id,
        String agentName,
        String branchName,
        String summary,
        String status,
        Instant startedAt,
        Instant endedAt) {
}
