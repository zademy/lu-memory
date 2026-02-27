package com.zademy.lu_memory.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable data record representing a memory observation stored in the system.
 * 
 * <p>This record encapsulates all metadata and content for observations that agents
 * create to maintain long-term context. Observations can represent architectural decisions,
 * bug fixes, patterns, notes, or any other significant information that needs to be
 * preserved across sessions for future reference.</p>
 * 
 * <p>Observations support:
 * <ul>
 *   <li>Content deduplication through SHA-256 hashing</li>
 *   <li>Revision tracking for content evolution</li>
 *   <li>Soft-delete functionality for data retention</li>
 *   <li>Topic-based organization and tagging</li>
 *   <li>Project and scope isolation</li>
 * </ul>
 * </p>
 * 
 * @param id the unique identifier for this observation
 * @param type the observation type (e.g., "DECISION", "BUGFIX", "PATTERN", "NOTE", "ARCHITECTURE", "SUMMARY", "DOCUMENTATION")
 * @param topicKey the stable topic key for grouping related observations
 * @param title a concise title describing the observation
 * @param content the detailed content following the What/Why/Where format
 * @param tagsText comma-separated tags for search and categorization
 * @param source the source of the observation (e.g., "agent-decision", "user-prompt")
 * @param sessionId the ID of the session that created this observation
 * @param scope the scope of the observation ("project" or "personal")
 * @param projectKey the project identifier this observation belongs to
 * @param projectName the human-readable project name
 * @param contentHash SHA-256 hash for content deduplication
 * @param duplicateCount the number of times this content has been duplicated
 * @param revisionCount the number of times this observation has been revised
 * @param lastSeenAt the timestamp when this observation was last accessed
 * @param deleted soft-delete flag (true if observation is marked as deleted)
 * @param deletedAt the timestamp when the observation was soft-deleted
 * @param createdAt the timestamp when the observation was first created
 * @param updatedAt the timestamp when the observation was last modified
 * 
 * @author lu-memory team
 * @since 1.0.0
 */
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
