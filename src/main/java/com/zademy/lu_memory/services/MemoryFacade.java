package com.zademy.lu_memory.services;

import com.zademy.lu_memory.models.ObservationRecord;
import com.zademy.lu_memory.models.PromptRecord;
import com.zademy.lu_memory.models.SessionRecord;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Facade that exposes a simplified, cohesive API for memory operations used by
 * tool adapters.
 */
@Service
public class MemoryFacade {

    /** Core domain service that implements all memory/session business logic. */
    private final MemoryService memoryService;

    /**
     * Creates the facade with its required domain service dependency.
     *
     * @param memoryService memory service used to execute persistence and query operations.
     */
    public MemoryFacade(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    /**
     * Starts a new logical memory session.
     *
     * @param agentName  agent identifier that opened the session.
     * @param branchName optional branch or working context identifier.
     * @return persisted session record.
     */
    public SessionRecord startSession(String agentName, String branchName) {
        return memoryService.startSession(agentName, branchName);
    }

    /**
     * Marks an existing memory session as finished.
     *
     * @param sessionId unique session identifier.
     * @param status    final status (for example COMPLETED, ABORTED, FAILED).
     * @param summary   optional closing summary.
     * @return updated session record.
     */
    public SessionRecord endSession(UUID sessionId, String status, String summary) {
        return memoryService.endSession(sessionId, status, summary);
    }

    /**
     * Saves a new observation entry.
     *
     * @param type            observation category/type.
     * @param topicKey        stable topic key used for grouping related observations.
     * @param title           short observation title.
     * @param content         observation body/content.
     * @param tags            comma-separated tags.
     * @param sessionId       identifier of the related session.
     * @param scope           scope of visibility (for example project/personal).
     * @param source          source identifier of the observation.
     * @param projectName     logical project name.
     * @param importanceLevel observation importance level.
     * @return created observation record.
     */
    public ObservationRecord saveObservation(
            String type,
            String topicKey,
            String title,
            String content,
            String tags,
            String sessionId,
            String scope,
            String source,
            String projectName,
            String importanceLevel) {
        return memoryService.saveObservation(type, topicKey, title, content, tags, sessionId, scope, source, projectName,
                importanceLevel);
    }

    /**
     * Updates an existing observation entry.
     *
     * @param observationId   identifier of the observation to update.
     * @param type            observation category/type.
     * @param topicKey        stable topic key used for grouping related observations.
     * @param title           updated title.
     * @param content         updated content.
     * @param tags            updated comma-separated tags.
     * @param projectName     updated project name.
     * @param importanceLevel updated importance level.
     * @return updated observation record.
     */
    public ObservationRecord updateObservation(
            UUID observationId,
            String type,
            String topicKey,
            String title,
            String content,
            String tags,
            String projectName,
            String importanceLevel) {
        return memoryService.updateObservation(observationId, type, topicKey, title, content, tags, projectName,
                importanceLevel);
    }

    /**
     * Deletes an observation permanently or marks it as deleted.
     *
     * @param observationId observation identifier.
     * @param hardDelete    true for permanent deletion, false for soft-delete.
     * @return operation metadata/result map.
     */
    public Map<String, Object> deleteObservation(UUID observationId, boolean hardDelete) {
        return memoryService.deleteObservation(observationId, hardDelete);
    }

    /**
     * Suggests a normalized topic key from user-provided hints.
     *
     * @param topicHint   short topic hint.
     * @param contentHint optional additional content hint.
     * @return suggested topic key.
     */
    public String suggestTopicKey(String topicHint, String contentHint) {
        return memoryService.suggestTopicKey(topicHint, contentHint);
    }

    /**
     * Performs standard full-text memory search.
     *
     * @param query          search query text.
     * @param tags           optional comma-separated tag filter.
     * @param limit          maximum number of rows to return.
     * @param includeDeleted whether soft-deleted rows should be included.
     * @return list of search results.
     */
    public List<Map<String, Object>> searchMemories(String query, String tags, int limit, boolean includeDeleted) {
        return memoryService.searchMemories(query, tags, limit, includeDeleted);
    }

    /**
     * Performs scoped memory search with additional project/scope filters.
     *
     * @param query          search query text.
     * @param tags           optional comma-separated tag filter.
     * @param limit          maximum number of rows to return.
     * @param includeDeleted whether soft-deleted rows should be included.
     * @param scope          logical scope filter.
     * @param projectKey     project key filter.
     * @return list of scoped search results.
     */
    public List<Map<String, Object>> searchMemoriesScoped(
            String query,
            String tags,
            int limit,
            boolean includeDeleted,
            String scope,
            String projectKey) {
        return memoryService.searchMemoriesScoped(query, tags, limit, includeDeleted, scope, projectKey);
    }

    /**
     * Performs advanced full-text search with improved ranking/highlighting.
     *
     * @param query          search query text.
     * @param tags           optional comma-separated tag filter.
     * @param limit          maximum number of rows to return.
     * @param includeDeleted whether soft-deleted rows should be included.
     * @return list of advanced search results.
     */
    public List<Map<String, Object>> searchMemoriesAdvanced(String query, String tags, int limit,
            boolean includeDeleted) {
        return memoryService.searchMemoriesAdvanced(query, tags, limit, includeDeleted);
    }

    /**
     * Performs advanced full-text search constrained by scope and project.
     *
     * @param query          search query text.
     * @param tags           optional comma-separated tag filter.
     * @param limit          maximum number of rows to return.
     * @param includeDeleted whether soft-deleted rows should be included.
     * @param scope          logical scope filter.
     * @param projectKey     project key filter.
     * @return list of advanced scoped search results.
     */
    public List<Map<String, Object>> searchMemoriesAdvancedScoped(
            String query,
            String tags,
            int limit,
            boolean includeDeleted,
            String scope,
            String projectKey) {
        return memoryService.searchMemoriesAdvancedScoped(query, tags, limit, includeDeleted, scope, projectKey);
    }

    /**
     * Stores the end-of-session summary as an observation.
     *
     * @param sessionId      identifier of the session being summarized.
     * @param summary        markdown/plaintext summary content.
     * @param lessonsLearned lessons learned captured for future context.
     * @return created summary observation record.
     */
    public ObservationRecord saveSessionSummary(UUID sessionId, String summary, String lessonsLearned) {
        return memoryService.saveSessionSummary(sessionId, summary, lessonsLearned);
    }

    /**
     * Retrieves recent context for a specific topic.
     *
     * @param topicKey       topic key to filter context.
     * @param limit          maximum number of context entries.
     * @param includePrompts whether saved prompts should be included.
     * @return context payload map.
     */
    public Map<String, Object> getContext(String topicKey, int limit, boolean includePrompts) {
        return memoryService.getContext(topicKey, limit, includePrompts);
    }

    /**
     * Retrieves recent context with extra scope/project constraints.
     *
     * @param topicKey       topic key to filter context.
     * @param limit          maximum number of context entries.
     * @param includePrompts whether saved prompts should be included.
     * @param scope          logical scope filter.
     * @param projectKey     project key filter.
     * @return scoped context payload map.
     */
    public Map<String, Object> getContextScoped(
            String topicKey,
            int limit,
            boolean includePrompts,
            String scope,
            String projectKey) {
        return memoryService.getContextScoped(topicKey, limit, includePrompts, scope, projectKey);
    }

    /**
     * Returns chronological entries around a reference observation.
     *
     * @param observationId reference observation identifier.
     * @param windowMinutes time window around the reference entry.
     * @param limit         maximum number of timeline entries.
     * @return timeline payload map.
     */
    public Map<String, Object> timeline(UUID observationId, int windowMinutes, int limit) {
        return memoryService.timeline(observationId, windowMinutes, limit);
    }

    /**
     * Retrieves a single observation by id.
     *
     * @param observationId observation identifier.
     * @return observation payload map.
     */
    public Map<String, Object> getObservation(UUID observationId) {
        return memoryService.getObservation(observationId);
    }

    /**
     * Saves a reusable prompt template tied to session/topic metadata.
     *
     * @param sessionId session identifier.
     * @param prompt    raw prompt text.
     * @param topicKey  topic key for grouping prompts.
     * @param intent    prompt intent/category.
     * @param source    prompt source identifier.
     * @return created prompt record.
     */
    public PromptRecord savePrompt(String sessionId, String prompt, String topicKey, String intent, String source) {
        return memoryService.savePrompt(sessionId, prompt, topicKey, intent, source);
    }

    /**
     * Returns aggregate statistics for the memory subsystem.
     *
     * @return map with high-level counters and health metadata.
     */
    public Map<String, Object> stats() {
        return memoryService.stats();
    }
}
