package com.zademy.lu_memory.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable data record representing an AI agent work session.
 *
 * <p>This record encapsulates the complete lifecycle information of a session,
 * from its initiation by an agent through its completion or termination. Sessions
 * represent logical work blocks where agents interact with the memory system to
 * accomplish specific tasks, develop features, or debug issues.</p>
 *
 * <p>Session records are used for:
 * <ul>
 *   <li>Tracking agent activity and work patterns</li>
 *   <li>Generating session summaries and reports</li>
 *   <li>Analyzing agent productivity and session duration</li>
 *   <li>Maintaining audit trails of memory system interactions</li>
 * </ul>
 * </p>
 *
 * @param id the unique identifier for this session
 * @param agentName the name of the AI agent that initiated the session (e.g., "Windsurf", "Claude")
 * @param branchName the git branch or feature context during the session (e.g., "feature/user-auth")
 * @param summary a detailed summary of what was accomplished during the session
 * @param status the final status of the session (e.g., "COMPLETED", "ABORTED", "FAILED")
 * @param startedAt the timestamp when the session was initiated
 * @param endedAt the timestamp when the session was concluded (may be null for active sessions)
 *
 * @author lu-memory team
 * @since 1.0.0
 */
public record SessionRecord(
        UUID id,
        String agentName,
        String branchName,
        String summary,
        String status,
        Instant startedAt,
        Instant endedAt) {
}
