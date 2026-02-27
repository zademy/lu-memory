---
name: chain-of-responsibility
description: Guides the step-by-step implementation of the Chain of Responsibility design pattern in Spring Boot using ordered collection injection.
---

# Chain of Responsibility Pattern Implementation Skill

This skill guides Cascade through breaking down massive `if-else` validation blocks into autonomous, ordered handlers natively orchestrated by Spring Boot.

## Concept & Analogy

**Analogy:** Calling tech support. You first talk to a robot (Handler 1). If it can't solve your issue, it passes you to a human operator (Handler 2). If they can't help, they pass you to an engineer (Handler 3) who finally resolves it.
**When to apply:** When you need to process various requests in sequence and the exact types/sequences aren't known beforehand, or when you need to execute handlers in a specific dynamic order.

## Prerequisites

1. Identify a large block of sequential validations, filters, or rules that can abort a process (e.g., Security Checks, Order Validation).
2. Look for hardcoded "next" pointer variables (legacy Java approach) that should be removed.

## Implementation Steps

### Step 1: Define the Handler Interface

Create a common abstract interface for all validation steps.

- Decide the contract: e.g., `void validate(Object target);` (throws an Exception if validation fails) or `boolean process(Object target);`.

### Step 2: Extract Individual Handlers

For each `if` block in the monolithic code, create a new class implementing the shared interface.

- Annotate each class with `@Component`.
- Ensure each class has a Single Responsibility (SRP).

### Step 3: Enforce Execution Order

Annotate each individual handler with `@Order(N)`.

- Use `@Order(1)` for the first check, `@Order(2)` for the second logically required check, and so on.
- This replaces the need to manually link objects together.

### Step 4: Create the Orchestrator Chain

Create a central Service to act as the Execution Chain.

- Annotate it with `@Service`.
- Inject all handlers dynamically via the constructor: `@Autowired public ValidationChain(List<HandlerInterface> steps)`.
- Spring Boot will automatically inject the handlers and sort them according to their `@Order` values.

### Step 5: Implement the Sequential Loop

Inside the Orchestrator's exposed method:

- Create a `for` loop iterating over the injected `List<HandlerInterface>`.
- Call the validation/process method on each step.
- If a step throws an Exception or returns `false`, the chain naturally breaks and bubbles up.

### Step 6: Refactor the Caller

Replace the massive 100-line validation block in the original client with a single call to the Orchestrator Chain.
