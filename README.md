# 🧠 lu-memory - MCP Memory Server

![Java Version](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-brightgreen.svg)
![Protocol](https://img.shields.io/badge/MCP-Supported-blue.svg)
![Database](https://img.shields.io/badge/SQLite-JPA-blueviolet.svg)

**lu-memory** is a robust, lightweight **Model Context Protocol (MCP)** server built with **Java** and **Spring Boot**. It acts as an external persistent memory service, allowing AI assistants and agents to build and maintain context across multiple sessions, document architectural decisions, log bugs, and store structured observations seamlessly.

---

## 🚀 Features & Capabilities

- **Session Management**: Explicitly start and end logical chat sessions, keeping a clean timeline of tasks.
- **Categorized Observations**: Save memories by granular types: `DECISION`, `BUGFIX`, `PATTERN`, `NOTE`, `ARCHITECTURE`, `SUMMARY`, and `DOCUMENTATION`.
- **Full-Text Context Recovery**: Powered by SQLite FTS, instantly retrieve chronological timelines around past observations.
- **Advanced Search**: Advanced search capabilities with result highlighting and relevance ranking.
- **Topic & Tag Tracking**: Group related memories by dynamic, evolving "topics" with stable topic keys, and use custom tags for high search precision.
- **Multi-Tenant / Scopes**: Differentiate local project memory from personal/global memory scopes.

## 🏗️ Architecture & Design Principles

By acting as a dedicated MCP daemon, `lu-memory` decouples context persistence from the LLM context window itself, embracing a **Modular Monolith** and **Clean Architecture** approach.

- **CQRS / Domain-Driven Data**: Writes (observations, summaries) are handled defensively, while Reads (timeline, FTS search) leverage optimized SQLite indices.
- **Single Responsibility Principle (SOLID)**: Each MCP tool encapsulates exactly one action against the persistence layer.

## 🛠 Tech Stack

- **Core & Runtime**: Java 25
- **Framework & MCP Integration**: Spring Boot 4.x, Spring AI (`spring-ai-starter-mcp-server`)
- **Persistence**: SQLite (via `sqlite-jdbc` and `spring-boot-starter-data-jpa`)
- **Build Server**: Maven

---

## 📦 Getting Started

### Prerequisites

- **Java JDK 25** or higher configured in your environment.
- **Maven** (optional, as the repository includes `mvnw`).

### Installation & Execution

1. Clone this repository.
2. Navigate to the project root directory.
3. Start the application using the Maven wrapper:

```bash
# On Linux / macOS
./mvnw spring-boot:run

# On Windows
mvnw.cmd spring-boot:run
```

The embedded SQLite database (`lu-memory.db`) will be created automatically in the root directory and managed via Hibernate/JPA auto-DDL.

### MCP Client Configuration

To expose `lu-memory` to your AI client (e.g., Claude Desktop, Windsurf, or Cursor), add the server definition to your `mcp_config.json`:

```json
{
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
}
```

_(Adjust the path to your compiled `.jar` and Java runtime executable accordingly)._

---

## 🧰 Available MCP Tools Reference

The server directly provides these tools via MCP. Agents should leverage them based on the standard **Memory Protocol**:

| Capability Area        | Tool Name               | Description & Usage                                                                          |
| ---------------------- | ----------------------- | -------------------------------------------------------------------------------------------- |
| **Session Control**    | `mem_session_start`     | Opens a new session with an `agentName` (e.g., "Windsurf") and `branchName`.                 |
|                        | `mem_session_end`       | Closes an ongoing session. Requires a `status` (`COMPLETED`, `ABORTED`, `FAILED`).           |
|                        | `mem_session_summary`   | Saves an end-of-session summary reflecting what was accomplished.                            |
| **Memory Extraction**  | `mem_context`           | Fetches the most recent context from previous sessions natively upon boot/reset.             |
|                        | `mem_timeline`          | Retrieves a chronological timeline around a specific observation.                            |
| **Observation Mgmt**   | `mem_save`              | Saves grouped observations. Requires `type`, `tags`, `projectName`, and current `sessionId`. |
|                        | `mem_save_prompt`       | Saves user-specific prompts as templates. Requires `intent` and `source`.                    |
|                        | `mem_update`            | Revises an existing observation.                                                             |
|                        | `mem_delete`            | Performs a soft-delete of an observation.                                                    |
|                        | `mem_get_observation`   | Expands and returns the full content payload of a specific memory ID.                        |
| **Search & Discovery** | `mem_suggest_topic_key` | Derives a stable key for evolving topics.                                                    |
|                        | `mem_search`            | Standard Full-Text Search across the datastore.                                              |
|                        | `mem_search_advanced`   | FTS query mechanism returning highlighted insights.                                          |

---

## 📖 Memory Protocol for Agents

Agents interacting with this server **MUST** follow this protocol to maximize consistency:

1. **Context Recovery**: Invoke `mem_context` immediately at the start of a session or after any context reset.
2. **Lifecycle Management**: Start work with `mem_session_start` and strictly finalize interactions using `mem_session_summary` alongside `mem_session_end`.
3. **Proactive Saves**: Autonomously leverage `mem_save` for consequential architecture decisions, bug resolutions, and configuration updates.
4. **Topic Continuity**: For long-spanning concepts, fetch a consistent topic with `mem_suggest_topic_key` and group observations beneath it.
5. **Drill-Down Context**: Use the 3-layer drill-down pattern: `mem_search` -> `mem_timeline` -> `mem_get_observation`.

### 📝 Formatted Memory Taxonomy

When committing large or architectural content via `mem_save`, utilize the following required Markdown schema within the content payload:

```markdown
**What**: [Summarized action, bug, or decision]
**Why**: [Domain or technical justification; problems solved]
**Where**: [Modified files, architecture sectors, tools]
**Key Details**:

- [Critical Implementation detail 1]
- [Critical Implementation detail 2]

**Learned**: [Important insights preventing future friction]
```

---

## 📚 Further Reading

- [Security Policy](SECURITY.md) guidelines and vulnerability reporting.
- [Help Document](HELP.md) extended application resources and operations.
