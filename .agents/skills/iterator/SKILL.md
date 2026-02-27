---
name: iterator
description: Guides the step-by-step implementation of the Iterator design pattern in Spring Boot using Data Streams and Pagination.
---

# Iterator Pattern Implementation Skill

This skill guides Cascade through protecting system memory bounds when iterating massive database payloads by transitioning to Spring Data Pagination or Streams.

## Concept & Analogy

**Analogy:** Visiting Rome. You can explore the city by wandering randomly, using a GPS app on your phone, or hiring a local human guide. All these act as different Iterators to traverse the exact same complex collection of attractions.
**When to apply:** When a collection has a complex internal data structure but you want to hide its complexity from clients, or to reduce duplicated traversal code across your app.

## Prerequisites

1. Identify a Spring Data JPA Repository method returning a massive `List<Entity>` (e.g., `findAll()`).
2. Reject any attempt to implement legacy GoF cursor-based `hasNext()` Iterators manually.

## Implementation Steps

### Option A: Chunked Iterator via Pagination (UI/REST focuses)

#### Step A1: Refactor the Repository

Change the heavy repository method signature.

- Change return type from `List<T>` to `Page<T>` or `Slice<T>`.
- Add a `Pageable` parameter: `Page<User> findAllByStatus(String status, Pageable pageable);`

#### Step A2: Update the Caller

In the calling service, initialize a standard PageRequest and iterate chunks.

- Calculate total pages or advance slices programmatically, processing a safe block of data and relinquishing memory in between chunks.

### Option B: Stream Iterator via DB Cursors (Batch processing focuses)

#### Step B1: Refactor the Repository

Change the method to return a native Java 8 Stream to lazily evaluate queries cursor-by-cursor.

- Change return type to `Stream<T>`.
- Add explicit cursor fetch limits: `@QueryHints(value = @QueryHint(name = org.hibernate.annotations.QueryHints.FETCH_SIZE, value = "100"))`.

#### Step B2: Establish the Transactional Context

**CRITICAL:** Stream iteration over a Database connection requires an active transaction.

- In the calling service, wrap the method with `@Transactional(readOnly = true)`.

#### Step B3: Implement Safe Stream Iteration

Because the Stream holds an active DB cursor, it MUST be closed exactly when iteration finishes to prevent connection leaks.

- Wrap the repository call in a `try-with-resources` block.

```java
try (Stream<User> userStream = repository.streamAllByStatus("ACTIVE")) {
    userStream.forEach(user -> process(user));
}
```
