# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

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

- Refactored the internal code structure to adhere to **Clean Architecture** and **SOLID** principles, effectively abstracting SQLite persistence details.

### Deprecated

- N/A

### Removed

- N/A

### Fixed

- N/A

### Security

- N/A

## [0.1.0] - YYYY-MM-DD

_(Example template for the first official release once tagged)_

### Added

- Core memory storage capabilities for the agent.

[Unreleased]: https://github.com/zademy/lu-memory/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/zademy/lu-memory/releases/tag/v0.1.0
