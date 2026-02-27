package com.zademy.lu_memory.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable data record representing a saved user prompt template.
 * 
 * <p>This record encapsulates user-defined prompts that can be saved and reused
 * across different sessions. Prompts are typically instructions or templates
 * for common development tasks that agents frequently encounter, such as
 * code generation, documentation, refactoring, or debugging patterns.</p>
 * 
 * <p>Prompt templates help:
 * <ul>
 *   <li>Reduce repetitive typing for common tasks</li>
 *   <li>Maintain consistency in code generation patterns</li>
 *   <li>Preserve effective prompts for future reuse</li>
 *   <li>Enable prompt discovery and sharing across sessions</li>
 * </ul>
 * </p>
 * 
 * @param id the unique identifier for this prompt template
 * @param sessionId the ID of the session where this prompt was originally saved
 * @param topicKey the topic key for categorizing and grouping related prompts
 * @param intent the intent category of the prompt (e.g., "scaffolding", "refactor", "bugfix", "documentation")
 * @param source the source of the prompt (e.g., "user-prompt", "agent-template")
 * @param prompt the actual prompt text content that can be reused
 * @param createdAt the timestamp when this prompt was first saved
 * 
 * @author lu-memory team
 * @since 1.0.0
 */
public record PromptRecord(
        UUID id,
        String sessionId,
        String topicKey,
        String intent,
        String source,
        String prompt,
        Instant createdAt) {
}
