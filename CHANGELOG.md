# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- New scoped MCP tools: `mem_context_scoped`, `mem_search_scoped`, and `mem_search_advanced_scoped` for project/scope-isolated memory retrieval.
- Response payloads now include `scope`, `projectKey`, and normalized `importanceLevel` fields for stronger multi-tenant context clarity.
- Validation error messages for invalid `scope`, `importanceLevel`, `status`, and empty search `query` inputs.
- SQLite performance indexes for hot paths on `observations`, `memory_sessions`, and `saved_prompts`.
- Maintained and authored by **zademy**.
- Initial project architecture inspired by the Gentleman-Programming/engram concept.
- Setup Model Context Protocol (MCP) server core for Java/Spring Boot.
- Integration with SQLite for long-term memory management using FTS5 (Full-Text Search).
- Basic CRUD operations for handling sessions, observations, and saved prompts.
- Foundational standard community files: `README.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, `CONTRIBUTING.adoc`, and dual-attributed `LICENSE`.
- Maven Wrapper (`mvnw`) configuration for ease of building the project.
- 22 comprehensive Windsurf Skills (`.windsurf/skills/`) for Spring Boot design patterns with step-by-step implementation guides and Refactoring Guru analogies.
- 22 Windsurf Rules (`.windsurf/rules/`) for Spring Boot design patterns including Real-World Analogies.
- `software-architect` Windsurf Skill for architectural decision-making, SOLID enforcement, and Context7 MCP usage.

### Changed

- Documented MCP tool count updated from **14** to **17** in `README.md`.
- Refactored `MemoryService` search flows to use parameterized `NamedParameterJdbcTemplate` with optional scope/project/tag filters.
- Context retrieval now supports explicit scope/project filtering and normalized `topicKey` handling.
- Session status handling now enforces valid enum values (`STARTED`, `COMPLETED`, `FAILED`, `ABORTED`).
- Spring bean method name corrected from `weatherTools` to `memoryToolsProvider` for semantic accuracy.
- Refactored the internal code structure to adhere to **Clean Architecture** and **SOLID** principles, effectively abstracting SQLite persistence details.
- Updated default SQLite datasource configuration to use a deterministic file URL with `busy_timeout` and single-connection pooling for safer runtime behavior.
- Updated README architecture documentation to reflect datasource/runtime behavior and include latest MCP smoke validation status.

### Deprecated

- N/A

### Removed

- N/A

### Fixed

- Soft-deleted observations are now excluded from `mem_get_observation`.
- Timeline queries are now constrained by observation scope/project to prevent cross-context leakage.
- Private content redaction now uses a compiled regex path for consistent sanitization behavior.
- Fixed SQL query assembly boundaries in search paths (`mem_search`, `mem_search_advanced`, and fallback search) to prevent malformed statements such as `falseORDER` and `:tag0ORDER`.
- Added explicit dynamic-clause spacing in tag filters to avoid named-parameter token collisions during query construction.

### Security

- Dynamic SQL tag filtering now uses bound parameters instead of string concatenation to reduce injection risk.

## [0.1.0] - YYYY-MM-DD

_(Example template for the first official release once tagged)_

### Added

- Core memory storage capabilities for the agent.

[Unreleased]: https://github.com/zademy/lu-memory/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/zademy/lu-memory/releases/tag/v0.1.0
