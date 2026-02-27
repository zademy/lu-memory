---
name: builder
description: Guides the step-by-step implementation of the Builder design pattern in Spring Boot to construct complex objects securely.
---

# Builder Pattern Implementation Skill

This skill guides Cascade through implementing the Builder pattern for complex objects, DTOs, or configurations in Spring Boot.

## Concept & Analogy

**Analogy:** Building a custom house step-by-step. A basic house needs walls and a roof, but a luxury house needs a pool, garage, and statues. You don't want a "telescopic constructor" with 20 parameters where 15 are null.
**When to apply:** When you need to create varying representations of a complex object, and want to avoid an anti-pattern constructor bloated with optional parameters.

## Prerequisites

1. Identify a class with a telescoping constructor or many setter calls.
2. Determine if the object should be strictly immutable.

## Implementation Steps

### Step 1: Define the Target Class

Ensure the class (e.g., an external API request DTO) has all fields marked as `final` to enforce immutability.
Remove any public setters.

### Step 2: Apply Lombok (Preferred Route)

If Lombok is available in the project `pom.xml` / `build.gradle`:

1. Annotate the class with `@Builder`.
2. Annotate the class with `@Getter`.
3. If fields have default values, annotate them with `@Builder.Default`.

### Step 3: Create Manual Builder (Alternative Route)

If Lombok is strictly forbidden:

1. Create a `public static class Builder` inside the target class.
2. Duplicate all fields inside the builder.
3. Provide fluent setter methods in the Builder returning `this`.
4. Create a `public TargetClass build()` method that calls a private constructor: `return new TargetClass(this);`.
5. Mark the target class constructor as `private TargetClass(Builder builder)` and map the fields.

### Step 4: Refactor Instantiations

Search the codebase for places where the target class was instantiated using `new` or `setXYZ()`.
Replace them with the newly created builder chain (`TargetClass.builder().field(val).build();`).

### Step 5: Use Native Spring Builders (For Framework Components)

If constructing `RestTemplate`, `WebClient`, or `MockMvc`:
DO NOT use custom builders. Inject `RestTemplateBuilder` or `WebClient.Builder` manually and chain the configuration calls.
