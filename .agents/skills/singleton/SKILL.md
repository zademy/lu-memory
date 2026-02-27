---
name: singleton
description: Guides the step-by-step validation and implementation of the Singleton design pattern in Spring Boot using IoC.
---

# Singleton Pattern Implementation Skill

This skill guides Cascade through enforcing the Spring Boot native Singleton pattern and destroying legacy GoF Singleton implementations.

## Concept & Analogy

**Analogy:** The Government of a country. A country can only have one official government acting as a single global access point for laws and representation.
**When to apply:** When a class must have exclusively one instance available to all clients (e.g., a shared Database Connection Pool) and you need stricter control over global variables.

## Prerequisites

1. Identify components that are expensive to create, hold shared configurations, or orchestrate logic.
2. Check for legacy `public static getInstance()` blocks.

## Implementation Steps

### Step 1: Destroy Legacy Implementations

Locate any class manually implementing the Singleton pattern.

- Delete the `private static Type instance;` field.
- Delete the `synchronized public static Type getInstance()` method.
- Change the `private` constructor to `public` (or allow Lombok to generate it).

### Step 2: Delegate to Spring IoC

Transfer the lifecycle management to Spring.

- Annotate the target class with `@Component`, `@Service`, or `@Repository`.
- If the class comes from a third-party library, create a `@Configuration` class and export the singleton via a `@Bean` method.

### Step 3: Purge Stateful Fields (Thread-Safety Check)

**This is the most critical step.**

- Scan the newly created Singleton for any mutable state (e.g., `private int counter = 0;`, `private User currentRequestUser;`, `private List<String> tempLogs;`).
- If mutable fields exist that are NOT thread-safe collections (like `ConcurrentHashMap`), they will cause Race Conditions.
- Move stateful variables to local method scopes, to a Database, or use `ThreadLocal` / `Request Scope` if specifically tied to a user session.

### Step 4: Inject the Singleton

Update all clients that previously called `LegacyClass.getInstance()`.

- Use constructor injection (`@Autowired` is optional on constructors in modern Spring) to receive the Singleton natively.
- Example: `public MyService(TargetClass target) { this.target = target; }`
