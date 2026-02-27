package com.zademy.lu_memory.tools;

import com.zademy.lu_memory.models.ObservationRecord;
import com.zademy.lu_memory.models.PromptRecord;
import com.zademy.lu_memory.models.SessionRecord;
import com.zademy.lu_memory.services.MemoryService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Exposes the MemoryService capabilities as distinct tools.
 * These tools are annotated for use by Spring AI, enabling LLM models (agents)
 * to autonomously manage and retrieve their long-term memory via the Model
 * Context Protocol (MCP).
 */
@Service
public class MemoryTools {

        private final MemoryService memoryService;

        public MemoryTools(MemoryService memoryService) {
                this.memoryService = memoryService;
        }

        @Tool(name = "mem_session_start", description = "Register a session start")
        public Map<String, Object> memSessionStart(
                        @ToolParam(description = "Agent name") String agentName,
                        @ToolParam(description = "Branch name") String branchName) {
                SessionRecord session = memoryService.startSession(agentName, branchName);
                return Map.of(
                                "sessionId", session.id(),
                                "status", session.status(),
                                "startedAt", session.startedAt());
        }

        @Tool(name = "mem_session_end", description = "Mark a session as completed")
        public Map<String, Object> memSessionEnd(
                        @ToolParam(description = "Session ID") String sessionId,
                        @ToolParam(description = "Session status (COMPLETED, ABORTED, FAILED)") String status,
                        @ToolParam(description = "Session summary") String summary) {
                SessionRecord session = memoryService.endSession(UUID.fromString(sessionId), status, summary);
                return Map.of(
                                "sessionId", session.id(),
                                "status", session.status(),
                                "endedAt", session.endedAt());
        }

        @Tool(name = "mem_save", description = "Save a structured observation")
        public Map<String, Object> memSave(
                        @ToolParam(description = "Observation type: DECISION, BUGFIX, PATTERN, NOTE, ARCHITECTURE, SUMMARY, DOCUMENTATION") String type,
                        @ToolParam(description = "Stable topic key") String topicKey,
                        @ToolParam(description = "Short title") String title,
                        @ToolParam(description = "Observation content") String content,
                        @ToolParam(description = "Comma separated tags") String tags,
                        @ToolParam(description = "Session ID (UUID or String)") String sessionId,
                        @ToolParam(description = "Scope: project or personal") String scope,
                        @ToolParam(description = "Source of memory") String source,
                        @ToolParam(description = "Project name") String projectName,
                        @ToolParam(description = "Importance level (HIGH, MEDIUM, LOW)") String importanceLevel) {
                ObservationRecord observation = memoryService.saveObservation(
                                type,
                                topicKey,
                                title,
                                content,
                                tags,
                                sessionId == null || sessionId.isBlank() ? null : sessionId.trim(),
                                scope,
                                source,
                                projectName,
                                importanceLevel);

                return Map.of(
                                "id", observation.id(),
                                "topicKey", observation.topicKey(),
                                "type", observation.type(),
                                "createdAt", observation.createdAt());
        }

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
                                UUID.fromString(observationId),
                                type,
                                topicKey,
                                title,
                                content,
                                tags,
                                projectName,
                                importanceLevel);

                return Map.of(
                                "id", updated.id(),
                                "updatedAt", updated.updatedAt(),
                                "topicKey", updated.topicKey());
        }

        @Tool(name = "mem_delete", description = "Delete an observation; soft-delete by default")
        public Map<String, Object> memDelete(
                        @ToolParam(description = "Observation ID") String observationId,
                        @ToolParam(description = "Use true for hard delete") Boolean hardDelete) {
                boolean deleteHard = hardDelete != null && hardDelete;
                return memoryService.deleteObservation(UUID.fromString(observationId), deleteHard);
        }

        @Tool(name = "mem_suggest_topic_key", description = "Suggest a stable topic key")
        public Map<String, Object> memSuggestTopicKey(
                        @ToolParam(description = "Topic hint") String topicHint,
                        @ToolParam(description = "Additional content hint") String contentHint) {
                return Map.of("topicKey", memoryService.suggestTopicKey(topicHint, contentHint));
        }

        @Tool(name = "mem_search", description = "Full-text search across all memories")
        public Map<String, Object> memSearch(
                        @ToolParam(description = "Search query") String query,
                        @ToolParam(description = "Comma separated tags to filter by") String tags,
                        @ToolParam(description = "Max rows to return") Integer limit,
                        @ToolParam(description = "Include deleted observations") Boolean includeDeleted) {
                int maxRows = limit == null ? 20 : Math.max(1, Math.min(limit, 200));
                boolean includeDeletedRows = includeDeleted != null && includeDeleted;
                return Map.of(
                                "query", query,
                                "limit", maxRows,
                                "results", memoryService.searchMemories(query, tags, maxRows, includeDeletedRows));
        }

        @Tool(name = "mem_search_advanced", description = "Advanced full-text search with highlighting and enhanced ranking")
        public Map<String, Object> memSearchAdvanced(
                        @ToolParam(description = "Search query (supports AND, OR, NOT, \"exact phrases\")") String query,
                        @ToolParam(description = "Comma separated tags to filter by") String tags,
                        @ToolParam(description = "Max rows to return") Integer limit,
                        @ToolParam(description = "Include deleted observations") Boolean includeDeleted) {
                int maxRows = limit == null ? 20 : Math.max(1, Math.min(limit, 200));
                boolean includeDeletedRows = includeDeleted != null && includeDeleted;
                return Map.of(
                                "query", query,
                                "limit", maxRows,
                                "results",
                                memoryService.searchMemoriesAdvanced(query, tags, maxRows, includeDeletedRows));
        }

        @Tool(name = "mem_session_summary", description = "Save end-of-session summary")
        public Map<String, Object> memSessionSummary(
                        @ToolParam(description = "Session ID") String sessionId,
                        @ToolParam(description = "Summary") String summary,
                        @ToolParam(description = "Lessons learned") String lessonsLearned) {
                ObservationRecord observation = memoryService.saveSessionSummary(
                                UUID.fromString(sessionId),
                                summary,
                                lessonsLearned);

                return Map.of(
                                "observationId", observation.id(),
                                "sessionId", sessionId,
                                "topicKey", observation.topicKey());
        }

        @Tool(name = "mem_context", description = "Get recent context from previous sessions")
        public Map<String, Object> memContext(
                        @ToolParam(description = "Topic key") String topicKey,
                        @ToolParam(description = "Maximum rows") Integer limit,
                        @ToolParam(description = "Include saved prompts") Boolean includePrompts) {
                int maxRows = limit == null ? 20 : Math.max(1, Math.min(limit, 100));
                boolean includePromptRows = includePrompts == null || includePrompts;
                return memoryService.getContext(topicKey, maxRows, includePromptRows);
        }

        @Tool(name = "mem_timeline", description = "Chronological context around an observation")
        public Map<String, Object> memTimeline(
                        @ToolParam(description = "Observation ID") String observationId,
                        @ToolParam(description = "Window in minutes") Integer windowMinutes,
                        @ToolParam(description = "Maximum rows") Integer limit) {
                int window = windowMinutes == null ? 180 : windowMinutes;
                int maxRows = limit == null ? 40 : Math.max(1, Math.min(limit, 200));
                return memoryService.timeline(UUID.fromString(observationId), window, maxRows);
        }

        @Tool(name = "mem_get_observation", description = "Get full content of specific memory")
        public Map<String, Object> memGetObservation(
                        @ToolParam(description = "Observation ID") String observationId) {
                return memoryService.getObservation(UUID.fromString(observationId));
        }

        @Tool(name = "mem_save_prompt", description = "Save a user prompt for future context")
        public Map<String, Object> memSavePrompt(
                        @ToolParam(description = "Prompt text") String prompt,
                        @ToolParam(description = "Session ID") String sessionId,
                        @ToolParam(description = "Topic key") String topicKey,
                        @ToolParam(description = "Intent") String intent,
                        @ToolParam(description = "Source") String source) {
                PromptRecord saved = memoryService.savePrompt(
                                sessionId == null || sessionId.isBlank() ? null : sessionId.trim(),
                                prompt,
                                topicKey,
                                intent,
                                source);

                return Map.of(
                                "id", saved.id(),
                                "topicKey", saved.topicKey(),
                                "createdAt", saved.createdAt());
        }

        @Tool(name = "mem_stats", description = "Memory system statistics")
        public Map<String, Object> memStats() {
                return memoryService.stats();
        }
}
