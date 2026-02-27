package com.zademy.lu_memory.tools;

import java.util.Map;
import java.util.UUID;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import com.zademy.lu_memory.models.ObservationRecord;
import com.zademy.lu_memory.models.PromptRecord;
import com.zademy.lu_memory.models.SessionRecord;
import com.zademy.lu_memory.services.MemoryService;

/**
 * Memory management tools for AI agents using the Model Context Protocol (MCP).
 * 
 * This service exposes MemoryService capabilities as distinct Spring AI tools, enabling Large
 * Language Models (LLMs) and AI agents to autonomously manage their long-term memory. Each tool is
 * annotated with @Tool for automatic discovery and registration with Spring AI.
 * 
 * <p>
 * The tools provide comprehensive memory operations including:
 * </p>
 * <ul>
 * <li>Session lifecycle management (start, end, summary)</li>
 * <li>Observation storage, retrieval, and manipulation</li>
 * <li>Full-text search with advanced filtering and ranking</li>
 * <li>Context retrieval and timeline analysis</li>
 * <li>Prompt management for reusable templates</li>
 * <li>System statistics and monitoring</li>
 * </ul>
 * 
 * <p>
 * All operations are backed by SQLite with FTS5 (Full-Text Search) for efficient memory retrieval
 * and management.
 * </p>
 * 
 * @author LuMemory Team
 * @version 1.0
 * @since 2025
 * @see MemoryService
 * @see com.zademy.lu_memory.models.ObservationRecord
 * @see com.zademy.lu_memory.models.SessionRecord
 */
@Service
public class MemoryTools {

        /** The underlying memory service that handles all database operations */
        private final MemoryService memoryService;

        /**
         * Constructs a new MemoryTools instance with the required MemoryService dependency.
         * 
         * @param memoryService The service responsible for all memory storage and retrieval
         *        operations
         */
        public MemoryTools(MemoryService memoryService) {
                this.memoryService = memoryService;
        }

        /**
         * Starts a new memory session for tracking agent work.
         * 
         * This tool creates a new session record in the database, allowing agents to track their
         * work periods and associate observations with specific sessions. Each session represents a
         * distinct work block or conversation period.
         * 
         * @param agentName The name or identifier of the AI agent starting the session
         * @param branchName The branch or context name (e.g., "main", "feature-branch")
         * @return Map containing session details: sessionId, status, and startedAt timestamp
         */
        @Tool(name = "mem_session_start", description = "Register a session start")
        public Map<String, Object> memSessionStart(
                        @ToolParam(description = "Agent name") String agentName,
                        @ToolParam(description = "Branch name") String branchName) {
                SessionRecord session = memoryService.startSession(agentName, branchName);
                return Map.of("sessionId", session.id(), "status", session.status(), "startedAt",
                                session.startedAt());
        }

        /**
         * Ends a memory session with final status and summary.
         * 
         * This tool marks a session as completed, aborted, or failed and stores a summary of the
         * work accomplished during the session. This is essential for maintaining a complete audit
         * trail of agent activities.
         * 
         * @param sessionId The unique identifier of the session to end
         * @param status The final session status: "COMPLETED", "ABORTED", or "FAILED"
         * @param summary A concise summary of what was accomplished during the session
         * @return Map containing session details: sessionId, status, and endedAt timestamp
         */
        @Tool(name = "mem_session_end", description = "Mark a session as completed")
        public Map<String, Object> memSessionEnd(
                        @ToolParam(description = "Session ID") String sessionId,
                        @ToolParam(description = "Session status (COMPLETED, ABORTED, FAILED)") String status,
                        @ToolParam(description = "Session summary") String summary) {
                SessionRecord session = memoryService.endSession(UUID.fromString(sessionId), status,
                                summary);
                return Map.of("sessionId", session.id(), "status", session.status(), "endedAt",
                                session.endedAt());
        }

        /**
         * Saves a structured observation to the memory database.
         * 
         * This is the primary tool for storing important information, decisions, learnings, and
         * other observations that the agent wants to remember for future reference. Observations
         * are indexed for full-text search and can be categorized by type, tags, and importance
         * level.
         * 
         * @param type The observation type: "DECISION", "BUGFIX", "PATTERN", "NOTE",
         *        "ARCHITECTURE", "SUMMARY", or "DOCUMENTATION"
         * @param topicKey A stable identifier for grouping related observations
         * @param title A brief, descriptive title for the observation
         * @param content The detailed content of the observation (supports Markdown formatting)
         * @param tags Comma-separated tags for categorization and filtering
         * @param sessionId Optional session ID to associate with this observation
         * @param scope The scope of the observation: "project" or "personal"
         * @param source The source of the memory (e.g., "user-prompt", "agent-template")
         * @param projectName The name of the project this observation relates to
         * @param importanceLevel The importance level: "HIGH", "MEDIUM", or "LOW"
         * @return Map containing the saved observation details: id, topicKey, type, and createdAt
         *         timestamp
         */
        @Tool(name = "mem_save", description = "Save a structured observation")
        public Map<String, Object> memSave(@ToolParam(
                        description = "Observation type: DECISION, BUGFIX, PATTERN, NOTE, ARCHITECTURE, SUMMARY, DOCUMENTATION") String type,
                        @ToolParam(description = "Stable topic key") String topicKey,
                        @ToolParam(description = "Short title") String title,
                        @ToolParam(description = "Observation content") String content,
                        @ToolParam(description = "Comma separated tags") String tags,
                        @ToolParam(description = "Session ID (UUID or String)") String sessionId,
                        @ToolParam(description = "Scope: project or personal") String scope,
                        @ToolParam(description = "Source of memory") String source,
                        @ToolParam(description = "Project name") String projectName,
                        @ToolParam(description = "Importance level (HIGH, MEDIUM, LOW)") String importanceLevel) {
                // Handle optional session ID parameter - trim whitespace and handle empty/null
                // values
                String processedSessionId =
                                sessionId == null || sessionId.isBlank() ? null : sessionId.trim();

                ObservationRecord observation = memoryService.saveObservation(type, topicKey, title,
                                content, tags, processedSessionId, scope, source, projectName,
                                importanceLevel);

                return Map.of("id", observation.id(), "topicKey", observation.topicKey(), "type",
                                observation.type(), "createdAt", observation.createdAt());
        }

        /**
         * Updates an existing observation with new information.
         * 
         * This tool allows modification of previously saved observations, enabling agents to
         * correct errors, add additional details, or update information as their understanding
         * evolves.
         * 
         * @param observationId The unique identifier of the observation to update
         * @param type The updated observation type (optional)
         * @param topicKey The updated topic key (optional)
         * @param title The updated title (optional)
         * @param content The updated content (optional)
         * @param tags The updated comma-separated tags (optional)
         * @param projectName The updated project name (optional)
         * @param importanceLevel The updated importance level (optional)
         * @return Map containing the updated observation details: id, updatedAt timestamp, and
         *         topicKey
         */
        @Tool(name = "mem_update", description = "Update an existing observation by ID")
        public Map<String, Object> memUpdate(
                        @ToolParam(description = "Observation ID") String observationId,
                        @ToolParam(description = "Observation type") String type,
                        @ToolParam(description = "Topic key") String topicKey,
                        @ToolParam(description = "Title") String title,
                        @ToolParam(description = "Content") String content,
                        @ToolParam(description = "Comma separated tags") String tags,
                        @ToolParam(description = "Project name") String projectName,
                        @ToolParam(description = "Importance level (HIGH, MEDIUM, LOW)") String importanceLevel) {
                ObservationRecord updated = memoryService.updateObservation(
                                UUID.fromString(observationId), type, topicKey, title, content,
                                tags, projectName, importanceLevel);

                return Map.of("id", updated.id(), "updatedAt", updated.updatedAt(), "topicKey",
                                updated.topicKey());
        }

        /**
         * Deletes an observation from the memory database.
         * 
         * This tool removes observations, with support for both soft-delete (default) and
         * hard-delete operations. Soft-delete marks the observation as deleted but retains it for
         * historical reference, while hard-delete permanently removes it from the database.
         * 
         * @param observationId The unique identifier of the observation to delete
         * @param hardDelete If true, permanently delete; if false or null, soft-delete
         * @return Boolean indicating success of the deletion operation
         */
        @Tool(name = "mem_delete", description = "Delete an observation; soft-delete by default")
        public Map<String, Object> memDelete(
                        @ToolParam(description = "Observation ID") String observationId,
                        @ToolParam(description = "Use true for hard delete") Boolean hardDelete) {
                // Default to soft-delete unless explicitly requested for hard-delete
                boolean deleteHard = hardDelete != null && hardDelete;
                return memoryService.deleteObservation(UUID.fromString(observationId), deleteHard);
        }

        /**
         * Suggests a stable topic key for organizing related observations.
         * 
         * This tool generates consistent, machine-readable topic keys that can be used to group
         * related observations together. Topic keys are essential for maintaining organized memory
         * structure and enabling efficient retrieval of related information.
         * 
         * @param topicHint A brief hint about the topic (e.g., "authentication",
         *        "database-migration")
         * @param contentHint Additional context about the content to help generate a more specific
         *        key
         * @return Map containing the suggested topic key
         */
        @Tool(name = "mem_suggest_topic_key", description = "Suggest a stable topic key")
        public Map<String, Object> memSuggestTopicKey(
                        @ToolParam(description = "Topic hint") String topicHint,
                        @ToolParam(description = "Additional content hint") String contentHint) {
                return Map.of("topicKey", memoryService.suggestTopicKey(topicHint, contentHint));
        }

        /**
         * Performs full-text search across all stored memories.
         * 
         * This tool leverages SQLite's FTS5 (Full-Text Search) engine to efficiently search through
         * all observations. It supports basic text matching with optional filtering by tags and can
         * include or exclude deleted observations.
         * 
         * @param query The search query string to match against observation content
         * @param tags Optional comma-separated tags to filter results by
         * @param limit Maximum number of results to return (default: 20, max: 200)
         * @param includeDeleted If true, include soft-deleted observations in results
         * @return Map containing the search query, limit used, and list of matching observations
         */
        @Tool(name = "mem_search", description = "Full-text search across all memories")
        public Map<String, Object> memSearch(@ToolParam(description = "Search query") String query,
                        @ToolParam(description = "Comma separated tags to filter by") String tags,
                        @ToolParam(description = "Max rows to return") Integer limit,
                        @ToolParam(description = "Include deleted observations") Boolean includeDeleted) {
                // Validate and normalize limit parameter
                int maxRows = limit == null ? 20 : Math.max(1, Math.min(limit, 200));
                boolean includeDeletedRows = includeDeleted != null && includeDeleted;
                return Map.of("query", query, "limit", maxRows, "results", memoryService
                                .searchMemories(query, tags, maxRows, includeDeletedRows));
        }

        /**
         * Performs advanced full-text search with enhanced features.
         * 
         * This tool provides sophisticated search capabilities including boolean operators (AND,
         * OR, NOT), exact phrase matching, result highlighting, and enhanced ranking algorithms for
         * more relevant results.
         * 
         * @param query Advanced search query supporting operators: AND, OR, NOT, and "exact
         *        phrases"
         * @param tags Optional comma-separated tags to filter results by
         * @param limit Maximum number of results to return (default: 20, max: 200)
         * @param includeDeleted If true, include soft-deleted observations in results
         * @return Map containing the search query, limit used, and list of matching observations
         *         with highlighting
         */
        @Tool(name = "mem_search_advanced",
                        description = "Advanced full-text search with highlighting and enhanced ranking")
        public Map<String, Object> memSearchAdvanced(@ToolParam(
                        description = "Search query (supports AND, OR, NOT, \"exact phrases\")") String query,
                        @ToolParam(description = "Comma separated tags to filter by") String tags,
                        @ToolParam(description = "Max rows to return") Integer limit,
                        @ToolParam(description = "Include deleted observations") Boolean includeDeleted) {
                // Validate and normalize limit parameter
                int maxRows = limit == null ? 20 : Math.max(1, Math.min(limit, 200));
                boolean includeDeletedRows = includeDeleted != null && includeDeleted;
                return Map.of("query", query, "limit", maxRows, "results", memoryService
                                .searchMemoriesAdvanced(query, tags, maxRows, includeDeletedRows));
        }

        /**
         * Saves an end-of-session summary with key learnings.
         * 
         * This tool is essential for capturing the outcomes and lessons learned from each session.
         * It creates a special observation that summarizes what was accomplished and what insights
         * were gained, providing valuable context for future sessions.
         * 
         * @param sessionId The unique identifier of the session being summarized
         * @param summary A comprehensive summary of what was accomplished during the session
         * @param lessonsLearned Key insights and learnings that will be useful in future sessions
         * @return Map containing the saved observation details: observationId, sessionId, and
         *         topicKey
         */
        @Tool(name = "mem_session_summary", description = "Save end-of-session summary")
        public Map<String, Object> memSessionSummary(
                        @ToolParam(description = "Session ID") String sessionId,
                        @ToolParam(description = "Summary") String summary,
                        @ToolParam(description = "Lessons learned") String lessonsLearned) {
                ObservationRecord observation = memoryService.saveSessionSummary(
                                UUID.fromString(sessionId), summary, lessonsLearned);

                return Map.of("observationId", observation.id(), "sessionId", sessionId, "topicKey",
                                observation.topicKey());
        }

        /**
         * Retrieves recent context from previous sessions.
         * 
         * This tool provides agents with relevant historical context by retrieving recent
         * observations and optionally saved prompts related to a specific topic. It's essential for
         * maintaining continuity across sessions and building upon previous work.
         * 
         * @param topicKey The topic key to retrieve context for (optional, retrieves all if null)
         * @param limit Maximum number of recent observations to return (default: 20, max: 100)
         * @param includePrompts If true, include saved prompts in the context (default: true)
         * @return Map containing the context data including observations and optionally prompts
         */
        @Tool(name = "mem_context", description = "Get recent context from previous sessions")
        public Map<String, Object> memContext(@ToolParam(description = "Topic key") String topicKey,
                        @ToolParam(description = "Maximum rows") Integer limit,
                        @ToolParam(description = "Include saved prompts") Boolean includePrompts) {
                // Validate and normalize parameters with appropriate defaults
                int maxRows = limit == null ? 20 : Math.max(1, Math.min(limit, 100));
                boolean includePromptRows = includePrompts == null || includePrompts;
                return memoryService.getContext(topicKey, maxRows, includePromptRows);
        }

        /**
         * Retrieves chronological context around a specific observation.
         * 
         * This tool provides temporal context by showing what happened before and after a specific
         * observation. It's useful for understanding the sequence of events and decisions that led
         * to particular outcomes.
         * 
         * @param observationId The unique identifier of the observation to center the timeline on
         * @param windowMinutes The time window in minutes before and after the observation
         *        (default: 180)
         * @param limit Maximum number of observations to return (default: 40, max: 200)
         * @return Map containing the timeline data with observations ordered chronologically
         */
        @Tool(name = "mem_timeline", description = "Chronological context around an observation")
        public Map<String, Object> memTimeline(
                        @ToolParam(description = "Observation ID") String observationId,
                        @ToolParam(description = "Window in minutes") Integer windowMinutes,
                        @ToolParam(description = "Maximum rows") Integer limit) {
                // Set reasonable defaults for timeline parameters
                int window = windowMinutes == null ? 180 : windowMinutes;
                int maxRows = limit == null ? 40 : Math.max(1, Math.min(limit, 200));
                return memoryService.timeline(UUID.fromString(observationId), window, maxRows);
        }

        /**
         * Retrieves the complete content of a specific observation.
         * 
         * This tool provides full access to a single observation, including all its metadata and
         * content. It's used when agents need to examine the complete details of a previously saved
         * memory.
         * 
         * @param observationId The unique identifier of the observation to retrieve
         * @return Map containing the complete observation data including all fields
         */
        @Tool(name = "mem_get_observation", description = "Get full content of specific memory")
        public Map<String, Object> memGetObservation(
                        @ToolParam(description = "Observation ID") String observationId) {
                return memoryService.getObservation(UUID.fromString(observationId));
        }

        /**
         * Saves a user prompt as a reusable template.
         * 
         * This tool captures user prompts for future reuse, allowing agents to maintain a library
         * of effective prompt templates. Saved prompts can be retrieved later to provide context
         * and maintain consistency across sessions.
         * 
         * @param prompt The actual prompt text to save
         * @param sessionId Optional session ID to associate with this prompt
         * @param topicKey Optional topic key for categorizing the prompt
         * @param intent The intent or purpose of the prompt (e.g., "scaffolding", "refactor",
         *        "bugfix")
         * @param source The source of the prompt (e.g., "user-prompt", "agent-template")
         * @return Map containing the saved prompt details: id, topicKey, and createdAt timestamp
         */
        @Tool(name = "mem_save_prompt", description = "Save a user prompt for future context")
        public Map<String, Object> memSavePrompt(
                        @ToolParam(description = "Prompt text") String prompt,
                        @ToolParam(description = "Session ID") String sessionId,
                        @ToolParam(description = "Topic key") String topicKey,
                        @ToolParam(description = "Intent") String intent,
                        @ToolParam(description = "Source") String source) {
                // Handle optional session ID parameter with proper validation
                String processedSessionId =
                                sessionId == null || sessionId.isBlank() ? null : sessionId.trim();

                PromptRecord saved = memoryService.savePrompt(processedSessionId, prompt, topicKey,
                                intent, source);

                return Map.of("id", saved.id(), "topicKey", saved.topicKey(), "createdAt",
                                saved.createdAt());
        }

        /**
         * Retrieves comprehensive statistics about the memory system.
         * 
         * This tool provides system-wide statistics including total observations, session counts,
         * storage usage, and other metrics useful for monitoring the health and usage of the memory
         * system.
         * 
         * @return Map containing various statistics about the memory system
         */
        @Tool(name = "mem_stats", description = "Memory system statistics")
        public Map<String, Object> memStats() {
                return memoryService.stats();
        }
}
