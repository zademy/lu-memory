---
name: factory-method
description: Guides the step-by-step implementation of the Factory Method design pattern dynamically via Spring Boot Dependency Injection.
---

# Factory Method Implementation Skill

This skill guides Cascade through replacing scattered conditional logic (`switch`, `if-else`) with a clean Factory Method leveraging Spring's IoC container registry.

## Concept & Analogy

**Analogy:** A logistics app that initially only handles Trucks. When operations expand to Sea logistics, directly injecting a `new Ship()` breaks the codebase. The Factory delegates construction to specialized creators.
**When to apply:** When you don't know the exact types and dependencies of objects your code should work with in advance, or when you want to save system resources by reusing existing objects.

## Prerequisites

1. Identify a service or processor doing multiple specific tasks based on an Enum or String type (e.g., Payment processors, Notification senders).
2. Look for large conditional blocks.

## Implementation Steps

### Step 1: Create the Common Interface

Create a unified interface that all variants will implement.

- Define the business logic contract (e.g., `void process();`).
- CRITICAL: Define an identifier contract method (e.g., `String getType();`).

### Step 2: Extract Implementations

Extract each branch of the existing conditional logic into its own independent class.

- Make them implement the common interface.
- Annotate each new class with `@Service` or `@Component`.
- Return the unique identifier matching the old conditional branch in `getType()`.

### Step 3: Implement the Spring Registry Factory

Create the Factory class that orchestrates the beans dynamically.

- Annotate with `@Component`.
- Inject `List<CommonInterface> implementations` via the constructor.
- Add a private `Map<String, CommonInterface> registry`.
- In the constructor, stream the list and collect it into the map: `implementations.stream().collect(Collectors.toMap(CommonInterface::getType, i -> i));`

### Step 4: Add the Retrieval Method

In the Factory, create `public CommonInterface getInstance(String type)`.

- Retrieve from the map using the type.
- Add safety checks: if the retrieved instance is `null`, throw an `IllegalArgumentException` indicating the unspported type.

### Step 5: Refactor the Caller

Locate the caller that previously contained the `if-else` logic.

- Remove the conditional block entirely.
- Inject the Factory.
- Replace the logic with: `factory.getInstance(type).process();`
