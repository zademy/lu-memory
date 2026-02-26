# Security Policy

## Supported Versions

Below are the versions of `lu-memory` that currently receive security updates.

| Version | Supported          | Notes                      |
| ------- | ------------------ | -------------------------- |
| 0.0.1.x | :white_check_mark: | Active development version |
| < 0.0.1 | :x:                | Unsupported                |

## Project Security Considerations

Since `lu-memory` is a Model Context Protocol (MCP) server designed by **zademy**, it interacts locally with AI clients and agents. It is critical to consider the following areas within the system's architecture:

- **Secure Local Storage**: `lu-memory` uses a local SQLite database (`lu-memory.db`) to persist information. To protect the confidentiality of prompts, session history, and architectural context of projects, ensure you apply strict permissions at the OS level. Consider using encryption at rest for sensitive volumes.
- **MCP Communication (stdio)**: It relies on standard input/output (`stdio`) for the protocol. The server's security heavily depends on the client (e.g., Claude Desktop, Windsurf, or others) running in a trusted environment, limiting who can invoke the process.
- **Runtime Environment**: The project runs on **Java 25** and **Spring Boot**. As a best practice, always keep dependencies defined in your `pom.xml` updated to mitigate supply chain vulnerabilities, following the principle of _Security by Design_.

## Reporting a Vulnerability

We at **zademy** take the security of `lu-memory` very seriously. If you discover a security vulnerability, architectural flaw, or conceptual failure in this project, we ask that you report it privately. **Do not disclose it publicly** (e.g., by creating a public Issue) until it has been evaluated and a solution has been implemented.

Please report any vulnerability:

- Through the **Private Vulnerability Reporting** feature on this repository's GitHub.
- Or by directly contacting the maintenance team at **security@zademy.com**.

When creating the report, please include:

1. A detailed description of the vulnerability or security design flaw.
2. Concrete steps to reproduce the issue.
3. An assessment of the potential impact on architectures using this MCP.

The **zademy** team commits to reviewing the case promptly and providing next steps prior to any disclosure.
