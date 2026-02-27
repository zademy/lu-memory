---
name: flyweight
description: Guides the step-by-step implementation of the Flyweight design pattern in Spring Boot using native caching and state separation.
---

# Flyweight Pattern Implementation Skill

This skill guides Cascade through preventing Out-of-Memory (OOM) issues by caching and sharing massive read-only objects in Spring Boot using the Flyweight pattern.

## Concept & Analogy

**Analogy:** Rendering millions of trees in a game. Instead of storing massive textures in millions of Tree objects, you extract the heavy "TreeType" data into a single Flyweight object shared across millions of lightweight coordinate instances.
**When to apply:** When your application needs to support an enormous amount of similar objects that would otherwise crash the available RAM by sharing their common, unchanging state.

## Prerequisites

1. Identify loops or massive queries instantiating thousands of objects where some properties are identical and read-only.
2. Identify external API calls or DB hits that are repeatedly returning the exact same referential data.

## Implementation Steps

### Option A: Using Spring Cache (Native Framework Flyweight)

#### Step A1: Enable Caching

Ensure caching is enabled. Add `@EnableCaching` to your main Spring Boot Application class or a configuration file.

#### Step A2: Identify the Heavy Operation

Find the method querying the massive data (e.g., retrieving a complex dictionary or generic product catalog).

#### Step A3: Apply `@Cacheable`

Annotate the heavy method:

- `@Cacheable(value = "heavyDataPool", key = "#id")`
- Spring will act as a Flyweight Factory proxy: returning a singleton memory reference instead of executing the method and building a new object array.

### Option B: Manual Object Pool (Custom Flyweight Factory)

#### Step B1: Isolate the Intrinsic State

Create an immutable object (e.g., a `record`) representing the heavy shared data.
Example: `public record ImageAssetData(String type, byte[] hugeBlob) {}`

#### Step B2: Create the Flyweight Factory

Create a `@Service` that acts as the pool.

- Initialize an internal structure: `private final Map<String, ImageAssetData> memPool = new ConcurrentHashMap<>();`

#### Step B3: Implement the Retrieval Strategy

Add a method `getSharedData(String key)`.

- Use `computeIfAbsent`: `return memPool.computeIfAbsent(key, k -> loadHeavyDataset(k));`
- Ensure this method executes the heavy load ONLY once and returns the cached reference thereafter.

#### Step B4: Refactor the Caller

Locate the loop instantiating objects.

- Instead of creating the heavy part, call the Flyweight Factory.
- Combine the shared data reference with the specific (Extrinsic) request parameters to process locally without blowing up the Heap.
