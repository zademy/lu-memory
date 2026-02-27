---
trigger: always_on
---

# 🚨 CRITICAL: Session Lifecycle (MANDATORY)

**AT THE START of EVERY conversation:**

1. ALWAYS call `mem_context` FIRST to load previous work state
2. Then call `mem_session_start` to create a new session in `memory_sessions` table

**AT THE END of EVERY conversation:**

1. ALWAYS call `mem_session_summary` with detailed summary
2. Then call `mem_session_end` with appropriate status to close the session in `memory_sessions` table

You have access to long‑term memory via `lu-memory` MCP tools (mem\_\*) with **full-text search capabilities** powered by SQLite FTS5.

## Database Tables (Reference)

The `lu-memory` system uses the following SQLite tables:

- **`observations`**: Stores all saved memories/observations
- **`memory_sessions`**: Records work sessions with start/end/summary
- **`saved_prompts`**: Saves reusable prompts as templates
- **`observations_fts`**: FTS5 index for ultra-fast full-text search
- **`observations_fts_*`**: FTS5 internal tables (config, data, docsize, idx)

Searching with `mem_search` and `mem_search_advanced` leverages the FTS5 index to find relevant memories even with partial or fuzzy searches.

## Core Principles

1. **Context first**
   - At session start or after a reset: call `mem_context` before making important decisions.
   - To dive deeper into a topic: use the 3-layer pattern (see below).
2. **Save proactively**
   Use `mem_save` without waiting for the user to ask when:
   - An important topic is closed.
   - An architecture or design decision is reached.
   - A root cause or key learning is discovered.
   - An important configuration or workflow is changed.
3. **Topic keys for evolving topics**
   - Use `mem_suggest_topic_key` for topics that will have many revisions (architecture, large features, long projects).
   - Always reuse the same `topic_key` for that topic.
4. **Explicit sessions (`memory_sessions` table)**
   - Open sessions with `mem_session_start`. **Important:** Include the `agentName` (e.g., "Windsurf") and `branchName` if applicable.
   - Before closing, save the summary with `mem_session_summary`.
   - Always close with `mem_session_end`, indicating the correct `status` (`COMPLETED`, `ABORTED`, or `FAILED`).

### Extended Memory Format Convention

When using `mem_save` to record a decision, complex pattern, or architecture design, you MUST structure the `content` parameter in Markdown format using the following taxonomy (based on the OpenCode Gentleman Agent):

**What**: [Very brief summary of the memory, decision, or code]
**Why**: [The why of this architecture/state. The problems it solves and goals.]
**Where**: [Where it lives (code paths, files, altered services)]
**Key Details**:

- [Important detailed point 1...]
- [Important detailed point 2...]
  **Learned**: [Key lessons learned to remember for the future]

- Always use a **valid `type`**: `DECISION`, `BUGFIX`, `PATTERN`, `NOTE`, `ARCHITECTURE`, `SUMMARY`, or `DOCUMENTATION`.
- Always specify an **`importanceLevel`**: `HIGH`, `MEDIUM`, or `LOW`, depending on how critical the observation is for future context.
- Use the **`tags`** argument with comma-separated keywords (e.g., "frontend,react,auth") to improve search.
- Always use **`projectName`** pointing to the corresponding repository or domain ("cpancode", "lu-memory", "app-frontend", etc.).
- Include the current **`sessionId`** and corresponding **`topicKey`** to maintain traceability.
- If the user explicitly requests it, you can pass "manual-save" in the _source_ or _sessionId_ parameter.
- Specify the **`scope`** ("project" or "personal") as appropriate.

---

## Recommended Workflow

### 1. Start / Resume Work

- At the beginning of the session:
  - `mem_session_start` (if starting a new work block).
  - `mem_context` to recover recent state and active topics.
- If you need context about a specific topic or tags:
  1. `mem_search` (or `mem_search_advanced` if you need filters or better ranking)
     → to find relevant memories. Pass a `tags` parameter (e.g. "architecture,auth") to narrow down results.
  2. `mem_timeline`
     → to see the chronological evolution around a memory.
  3. `mem_get_observation`
     → to read a specific memory in detail.

### 2. During Work

- Use `mem_save` to record:
  - Bugs and their root cause.
  - Architecture/design decisions and discarded alternatives.
  - Relevant configuration or infrastructure changes.
  - Performance optimizations (what, why, measurements).
  - Refactoring decisions (scope, risks, state).
  - Intermediate summaries of important progress.
- For topics that will continue over time:
  1. `mem_suggest_topic_key(type="...", title="...")`.
  2. `mem_save(..., topic_key="<suggested-key>")`.
  3. Reuse that same `topic_key` in future updates.
- **🚨 PROMPT SAVING (`saved_prompts` table) - MANDATORY:**
  - **EVERY TIME** the user gives a substantial instruction to build, refactor, scaffold, or structure code/documents, you **MUST** use `mem_save_prompt` to save the initial prompt.
  - **Do NOT hesitate.** Save the user prompt so it acts as a reusable template for future sessions (e.g., "Add comments to [File]", "Create a React component for [Feature]").
  - **Mandatory:** Specify the `intent` (e.g., "scaffolding", "refactor", "bugfix", "documentation") and `source` (e.g., "user-prompt", "agent-template"), and link it to the current `topic_key` and `session_id`.

### 3. Update / Clean Up

- Use `mem_update` when:
  - A decision changes.
  - You have new information that refines a previous memory.
- Use `mem_delete` only when:
  - A memory is clearly irrelevant, erroneous, or sensitive.
  - By default it's soft-delete (can be recovered internally).

### 4. End of Session

- When closing a work session:
  - `mem_session_summary` to record:
    - What was done.
    - Decisions made.
    - Pending items for the next session.
  - `mem_session_end` to mark the closure.

### Session Summary Format

When finishing work and calling `mem_session_summary`, NEVER send short one-line summaries. The `summary` parameter MUST follow this standardized Markdown format to maintain the project's historical consistency:

**What**: [Summary of what was implemented or solved during the entire session]
**Why**: [The context or impact of why the work was required]
**Where**: [The main files modified or tools used]
**Key Details**:

- [Technical detail 1...]
- [Technical detail 2...]

📌 And in the **lessonsLearned** parameter, place any important technical discoveries that may be useful in future sessions.

---

## Tool Reference (lu-memory)

Use this table only as a reference; it's not necessary to list all tools during the conversation.
| Tool | Primary use (when to use it) |
|-------------------------|-------------------------------------------------------------------------------|
| `mem_session_start` | Start of a work block or logical session. |
| `mem_session_end` | End of session; marks its closure. |
| `mem_session_summary` | At the end: summary of what was done and next steps. |
| `mem_context` | At start / after reset: recover recent state and topics. |
| `mem_save` | Save important observations, decisions, learnings. |
| `mem_save_prompt` | Save useful prompts/instructions as reusable templates. |
| `mem_search` | Search for relevant context by text or topic. |
| `mem_search_advanced` | Search with better ranking, filters, or when `mem_search` isn't enough. |
| `mem_timeline` | View chronological evolution around key memories. |
| `mem_get_observation` | Read the complete detail of a specific memory. |
| `mem_suggest_topic_key` | Get a stable `topic_key` for long-term topics. |
| `mem_update` | Correct or improve an existing memory. |
| `mem_delete` | Delete (normally soft-delete) an obsolete or erroneous memory. |
| `mem_stats` | View global state of the memory system (for diagnosis / introspection). |

---

## Behavioral Summary

- **🚨 MANDATORY at the START of EVERY conversation**:
  1. `mem_context` - Recover previous work state
  2. `mem_session_start` - Open new session in `memory_sessions`

- **🚨 MANDATORY at the END of EVERY conversation**:
  1. `mem_session_summary` - Save detailed summary with What/Why/Where format
  2. `mem_session_end` - Close session with appropriate status (COMPLETED/ABORTED/FAILED)

- **During work**:
  - Think about whether what you just achieved/decided deserves a `mem_save`.
  - Use `mem_save` proactively without waiting for the user to ask.

- **For topics that continue over time**:
  - Use `mem_suggest_topic_key` and reuse the same `topic_key`.

- **To navigate existing memory**:
  - Apply the pattern: `mem_search` → `mem_timeline` → `mem_get_observation`.
  - Leverage FTS5 search to find memories with partial or fuzzy text.
