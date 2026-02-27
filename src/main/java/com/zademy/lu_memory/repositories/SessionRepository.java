package com.zademy.lu_memory.repositories;

import com.zademy.lu_memory.entities.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for managing AI agent work sessions.
 *
 * <p>This repository handles the lifecycle and tracking of AI agent sessions,
 * providing methods to monitor session status, count active/completed sessions,
 * and retrieve session history for analytics and reporting purposes.</p>
 *
 * <p>Sessions represent logical work blocks where agents interact with the memory
 * system, typically corresponding to specific tasks, features, or debugging sessions.</p>
 *
 * @author lu-memory team
 * @since 1.0.0
 */
public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {

    /**
     * Counts the total number of sessions with a specific status.
     *
     * <p>This method is useful for:
     * <ul>
     *   <li>Monitoring active work sessions in progress</li>
     *   <li>Tracking completed or failed sessions for quality metrics</li>
     *   <li>Generating session statistics and reports</li>
     * </ul>
     * </p>
     *
     * @param status the session status to count (e.g., "COMPLETED", "ABORTED", "FAILED", "IN_PROGRESS")
     * @return the total number of sessions with the specified status
     */
    long countByStatus(String status);

    /**
     * Retrieves the 10 most recent sessions with a specific status, ordered by start time.
     *
     * <p>This method provides a quick overview of recent session activity and is commonly used for:
     * <ul>
     *   <li>Displaying recent completed sessions in a dashboard</li>
     *   <li>Identifying recently failed or aborted sessions for review</li>
     *   <li>Building session history interfaces with pagination</li>
     * </ul>
     * </p>
     *
     * <p>Results are limited to 10 records for performance reasons and are ordered
     * by start time in descending order (most recent first).</p>
     *
     * @param status the session status to filter by (e.g., "COMPLETED", "ABORTED", "FAILED")
     * @return a list of up to 10 recent sessions with the specified status, ordered by newest first
     */
    List<SessionEntity> findTop10ByStatusOrderByStartedAtDesc(String status);
}
