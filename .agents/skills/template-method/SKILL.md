---
name: template-method
description: Guides the step-by-step implementation of the Template Method design pattern in Spring Boot to protect core pipelines.
---

# Template Method Pattern Implementation Skill

This skill guides Cascade through protecting central business algorithms by creating unbreakable Abstract Base templates with optional/required Hooks.

## Concept & Analogy

**Analogy:** Mass housing construction. A standard architectural plan dictates the steps (foundation, framing, wiring), but the client can alter specific parts (like materials or room sizes) without changing the overall construction order.
**When to apply:** When you want to let clients extend only particular steps of an algorithm, but not the whole algorithm or its structure.

## Prerequisites

1. Identify a multi-step process where 80% of the steps are identical across variations, but 20% must be customized (e.g., Report Generation: Fetch data -> **Format Data (varies)** -> Save file).

## Implementation Steps

### Step 1: Create the Abstract Base Class

Create an `abstract class` representing the overall pipeline or workflow.

### Step 2: Define and Seal the Core Template

Create the main public execution method coordinating the steps.

- **CRITICAL:** Mark this method as `final` so extending classes cannot overwrite the execution order or skip mandatory steps.
- Example: `public final void runReportPipeline() { authenticate(); extract(); format(); save(); }`

### Step 3: Provide Abstract Methods (Required Overrides)

For the steps that MUST change depending on the scenario:

- Define them as `protected abstract void format();`
- Subclasses will be forced by the compiler to implement these.

### Step 4: Provide Hook Methods (Optional Overrides)

For steps that are usually blank but provide an extension point:

- Define them as non-abstract methods with empty bodies.
- Example: `protected void afterSaveHook() { // Default empty }`

### Step 5: Implement Native Spring Subclasses

Create the specific variations.

- Annotate the derived classes with `@Service` or `@Component`.
- Extend the `abstract class`.
- Implement the required `abstract` methods with specific logic (e.g., format as PDF, CSV).
- Optionally override Hook methods if specific cleanup is needed.

### Step 6: Favor existing Spring Templates

Always remind the user to check if an existing Spring Template exists (e.g., `JdbcTemplate`, `RestTemplate`) before building complex generic interaction code from scratch.
