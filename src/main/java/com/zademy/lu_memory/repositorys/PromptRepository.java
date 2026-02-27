package com.zademy.lu_memory.repositorys;

import com.zademy.lu_memory.entitys.PromptEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for persisting and retrieving user-defined prompts.
 *
 * <p>This repository manages the storage and retrieval of prompt templates that users
 * can save and reuse across different sessions. Prompts are typically user instructions
 * for common tasks like code generation, documentation, refactoring, etc.</p>
 *
 * <p>The repository supports topic-based organization and chronological ordering
 * to help users find relevant prompts quickly.</p>
 *
 * @author lu-memory team
 * @since 1.0.0
 */
public interface PromptRepository extends JpaRepository<PromptEntity, UUID> {

    /**
     * Retrieves recent prompts, optionally filtered by topic key.
     * Results are ordered by creation date in descending order (newest first).
     *
     * <p>This method is useful for:
     * <ul>
     *   <li>Finding recently saved prompts for quick access</li>
     *   <li>Retrieving all prompts related to a specific topic</li>
     *   <li>Building a prompt discovery interface with pagination</li>
     * </ul>
     * </p>
     *
     * <p>If topicKey is null, returns all recent prompts across all topics.
     * If topicKey is provided, returns only prompts associated with that specific topic.</p>
     *
     * @param topicKey the topic key to filter by (null for all topics)
     * @param pageable pagination and sorting information for large result sets
     * @return a list of recent prompts matching the criteria, ordered by newest first
     */
    @Query("""
            select p from PromptEntity p
            where (:topicKey is null or p.topicKey = :topicKey)
            order by p.createdAt desc
            """)
    List<PromptEntity> findRecentByTopicKey(String topicKey, Pageable pageable);
}
