package com.zademy.lu_memory.repositorys;

import com.zademy.lu_memory.entitys.ObservationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for semantic access to {@link ObservationEntity}.
 * Extends CRUD functionality with specific lookup and aggregation queries.
 * 
 * <p>This repository provides specialized methods for accessing observations
 * with soft-delete support, topic-based filtering, and timeline queries.</p>
 * 
 * @author lu-memory team
 * @since 1.0.0
 */
public interface ObservationRepository extends JpaRepository<ObservationEntity, UUID> {

  /**
   * Finds an observation by its unique identifier, excluding soft-deleted records.
   * 
   * @param id the UUID of the observation to find
   * @return an Optional containing the active observation if found, empty otherwise
   */
  Optional<ObservationEntity> findByIdAndDeletedFalse(UUID id);

  /**
   * Counts all active (non-deleted) observations in the system.
   * 
   * @return the total number of observations that are not marked as deleted
   */
  long countByDeletedFalse();

  /**
   * Counts all soft-deleted observations in the system.
   * 
   * @return the total number of observations that are marked as deleted
   */
  long countByDeletedTrue();

  /**
   * Checks if an active observation exists for the given topic key.
   * This is useful for determining if a topic has been established before.
   * 
   * @param topicKey the topic key to search for
   * @return true if at least one active observation exists with the given topic key
   */
  boolean existsByTopicKeyAndDeletedFalse(String topicKey);

  /**
   * Finds the most recent active observation for a specific scope, project, and topic combination.
   * This method is typically used to retrieve the latest state of a particular topic within a project context.
   * 
   * @param scope the scope of the observation (e.g., "project" or "personal")
   * @param projectKey the project identifier
   * @param topicKey the topic key within the project
   * @return an Optional containing the matching observation if found, empty otherwise
   */
  Optional<ObservationEntity> findByScopeAndProjectKeyAndTopicKeyAndDeletedFalse(String scope, String projectKey,
      String topicKey);

  /**
   * Calculates the total number of duplicate observations across all active records.
   * This helps identify content redundancy in the memory system.
   * 
   * @return the sum of duplicateCount for all non-deleted observations, or null if no records exist
   */
  @Query("SELECT sum(o.duplicateCount) FROM ObservationEntity o WHERE o.deleted = false")
  Long sumDuplicateCountByDeletedFalse();

  /**
   * Calculates the total number of revisions across all active observations.
   * This provides insight into how much content has been modified over time.
   * 
   * @return the sum of revisionCount for all non-deleted observations, or null if no records exist
   */
  @Query("SELECT sum(o.revisionCount) FROM ObservationEntity o WHERE o.deleted = false")
  Long sumRevisionCountByDeletedFalse();

  /**
   * Retrieves recent observations, optionally filtered by topic key.
   * Results are ordered by creation date in descending order (newest first).
   * 
   * <p>If topicKey is null, returns all recent active observations.
   * If topicKey is provided, returns only observations matching that topic.</p>
   * 
   * @param topicKey the topic key to filter by (null for all topics)
   * @param pageable pagination and sorting information
   * @return a list of recent observations matching the criteria
   */
  @Query("""
      select o from ObservationEntity o
      where o.deleted = false
        and (:topicKey is null or o.topicKey = :topicKey)
      order by o.createdAt desc
      """)
  List<ObservationEntity> findRecentByTopicKey(String topicKey, Pageable pageable);

  /**
   * Retrieves observations within a specific time range for timeline generation.
   * Results are ordered by creation date in ascending order (chronological).
   * 
   * <p>This method is essential for creating chronological views of memory evolution
   * and for generating timelines around specific observations.</p>
   * 
   * @param from the start of the time range (inclusive)
   * @param to the end of the time range (inclusive)
   * @param pageable pagination information for large result sets
   * @return a list of observations within the specified time range, ordered chronologically
   */
  @Query("""
      select o from ObservationEntity o
      where o.deleted = false
        and o.createdAt between :from and :to
      order by o.createdAt asc
      """)
  List<ObservationEntity> findTimeline(Instant from, Instant to, Pageable pageable);
}
