# lu-memory

`lu-memory` is a Model Context Protocol (MCP) server built with **Java** and **Spring Boot**, acting as a robust external persistent memory service. It uses **SQLite** as its underlying database to store sessions, prompts, and structured observations, offering full-text search capabilities and session management.

This server integrates with AI clients via MCP, enabling AI agents to possess persistent memory, document their decisions, log bugs, track system architecture, and recover context across multiple chat sessions.

## 🚀 Features

- **Session Management**: Start and end logical sessions tracking what the agent works on.
- **Persistent Observations**: Store memories categorized by type (DECISION, BUGFIX, PATTERN, NOTE, ARCHITECTURE, SUMMARY, DOCUMENTATION).
- **Search capabilities**: Includes both standard matching and advanced Full-Text Search (FTS) queries, allowing searching across all stored memories.
- **Context Recovery**: Easily retrieve the timeline of events or chronological context centered around past observations.
- **Topics & Tags**: Group memories by evolving "topics" with stable topic keys, and tag them for quick organization.
- **Built on MCP**: Implements the Model Context Protocol using `spring-ai-starter-mcp-server`.

## 🛠 Tech Stack

- **Language**: Java 25
- **Framework**: Spring Boot 4.x
- **MCP Integration**: Spring AI (`spring-ai-starter-mcp-server`)
- **Database**: SQLite (via `sqlite-jdbc` and `spring-boot-starter-data-jpa`)
- **Build Tool**: Maven

## 📦 Getting Started

### Prerequisites

- **Java JDK 25** or higher.
- Maven (or use the provided `mvnw` wrapper).

### Running the Application

1. Clone this repository.
2. Navigate to the project root directory.
3. Run the application using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

_(On Windows, you can use `mvnw.cmd spring-boot:run`)_

The SQLite database will be created automatically based on the JPA configuration.

### MCP Client Configuration

To configure `lu-memory` for your AI client (like Claude desktop or other MCP users), you should define the server block in your `mcp_config.json` file. Here is an example of what that configuration block should look like:

```json
"lu-memory": {
      "command": "C:\\Java\\jdk-25.0.2\\bin\\java.exe",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.pattern.console=",
        "-jar",
        "C:\\Proyectos\\lu-memory\\target\\lu-memory-0.0.1-SNAPSHOT.jar"
      ]
}
```

## 🧰 Available MCP Tools

This MCP server exposes the following tools directly to the AI agent:

### Session & Context Control

- `mem_session_start` - Register a session start. Requires `agentName` (e.g., "Windsurf") and optionally `branchName`.
- `mem_session_end` - Mark a session as completed. Requires a valid `status` (`COMPLETED`, `ABORTED`, or `FAILED`).
- `mem_session_summary` - Save an end-of-session summary.
- `mem_context` - Get recent context from previous sessions.
- `mem_timeline` - Get chronological context around an observation.

### Observation Management

- `mem_save` - Save a structured observation (supports scopes, tags, strictly defined types like DECISION or BUGFIX, string session IDs, and custom project names). Ensure to use tags for better searchability.
- `mem_update` - Update an existing observation by ID (includes project name).
- `mem_delete` - Delete an observation (soft-delete by default).
- `mem_get_observation` - Get full content of a specific memory.
- `mem_save_prompt` - Save a user prompt for future context. Requires `intent` and `source` to categorize the prompt effectively.

### Search and Querying

- `mem_suggest_topic_key` - Suggest a stable topic key for evolving topics based on hints.
- `mem_search` - Full-text search across all memories.
- `mem_search_advanced` - Advanced full-text search with highlighting and enhanced ranking.
- `mem_stats` - Get internal memory system statistics.

## 📖 Memory Protocol for Agents

Agents interacting with this server are encouraged to follow this protocol:

1. **Context Recovery**: Always call `mem_context` at the start of a session or after any reset to recover context.
2. **Session Start**: Always open a session using `mem_session_start` and provide the `agentName`.
3. **Proactive Saves**: Use `mem_save` for big architectural decisions, bugs, discoveries, config changes, etc. Don't wait to be asked. **Always** include a valid `type` (e.g., DECISION, BUGFIX), `tags`, `projectName`, and the current `sessionId`.
4. **Topics**: Use `mem_suggest_topic_key` and group related observations under the same `topicKey`.
5. **Search Pattern**: When recalling information, use the 3-layer pattern: `mem_search` -> `mem_timeline` -> `mem_get_observation`.
6. **Session End**: Always close the session with `mem_session_summary` (following the Markdown taxonomy) and `mem_session_end` (with the appropriate `status`).

### 📝 Formatted Memory Content

When saving architectural designs or long memories, it is highly recommended to provide a structured Markdown taxonomy under `mem_save`'s content parameter:

```markdown
**What**: [Brief summary of memory, decision, or code]
**Why**: [Why this architecture/state exists, problems it solves]
**Where**: [Where it lives (code paths, files, altered services)]
**Key Details**:

- [Important detail 1...]
- [Important detail 2...]
  **Learned**: [Key lessons learned for future recall]
```
