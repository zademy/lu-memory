---
name: command
description: Guides the step-by-step implementation of the Command design pattern in Spring Boot for asynchronous event orchestration.
---

# Command Pattern Implementation Skill

This skill guides Cascade through deferring execution and freeing up active threads using the Command pattern coupled with Spring's ApplicationEventPublisher.

## Concept & Analogy

**Analogy:** Ordering food at a restaurant. You tell the waiter your order. The waiter writes it on a piece of paper (the Command) and puts it in the kitchen queue. The Chef (Receiver) reads the paper and cooks the meal when ready, decoupling you from the cooking process.
**When to apply:** When you want to parameterize objects with operations, put operations in a queue, schedule their execution, execute them remotely, or implement reversible (undo/redo) operations.

## Prerequisites

1. Identify an endpoint or service calling heavy, slow cross-domain logic (e.g., sending 10,000 emails, heavy calculations).
2. The caller does not need an immediate synchronous response regarding the heavy task's completion.

## Implementation Steps

### Step 1: Define the Command Object

Create a purely data-holding object (Command/Event) containing the precise parameters needed to execute the action.

- Use a Java `record` for automatic immutability.
- Example: `public record ProcessPaymentCommand(Long userId, Double amount) {}`

### Step 2: Establish the Receiver (Handler)

Create the class that actually executes the heavy logic.

- Annotate with `@Component`.
- Create a method that receives the Command object created in Step 1.
- Annotate the method with `@EventListener` so Spring maps it automatically.

### Step 3: Enable Asynchronous Execution (Optional but Recommended)

If the task is truly heavy:

- Ensure the main application class has `@EnableAsync`.
- Add `@Async` above the `@EventListener` method in the Receiver so it executes on a separate worker thread.

### Step 4: Refactor the Invoker

Return to the original Controller or Service creating the bottleneck.

- Delete the direct call to the heavy service.
- Inject `ApplicationEventPublisher`.
- Create the Command/Record instance with the required parameters.
- Dispatch the command: `eventPublisher.publishEvent(myCommand);`.
- Return a successful HTTP 202 (Accepted) or 200 immediately, achieving total decoupling.
