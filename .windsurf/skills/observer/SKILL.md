---
name: observer
description: Guides the step-by-step implementation of the Observer design pattern in Spring Boot using native Application Events.
---

# Observer Pattern Implementation Skill

This skill guides Cascade through decoupling reactive side-effects (like sending emails or creating logs after a core action) using Spring Boot's Event-Driven Architecture.

## Concept & Analogy

**Analogy:** Magazine subscriptions. Instead of visiting the store every day to check if the next issue is available, you subscribe. The publisher (Subject) sends new issues directly to your mailbox (Observer) automatically.
**When to apply:** When changes to the state of one object may require changing other objects, and the actual set of objects is unknown beforehand or changes dynamically.

## Prerequisites

1. Identify a Service method doing too many lateral tasks (e.g., save User -> send Welcome Email -> setup Billing -> notify Admin Slack).
2. The core action (saving User) should not fail or be delayed by the lateral tasks.

## Implementation Steps

### Step 1: Define the Event Payload

Create a purely data-holding object (Event) to broadcast what happened.

- Use a Java `record` for automatic immutability.
- Example: `public record UserRegisteredEvent(Long userId, String email) {}`

### Step 2: Set up the Publisher (Subject)

Refactor the core Service where the main action happens.

- Inject `ApplicationEventPublisher`.
- After the core logic succeeds, broadcast the Event: `publisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail()));`
- Remove all the injected lateral services (EmailService, BillingService) from this core class.

### Step 3: Set up the Listeners (Observers)

Create separate classes for every reactive task.

- Annotate them with `@Component`.
- Create a method receiving the Event payload.
- Annotate the method with `@EventListener`.

### Step 4: Configure Asynchronous Execution (Optional)

If a listener does heavy IO tasks (like calling an external Email API):

- Add `@Async` above its `@EventListener`.
- Ensure the main Spring Boot Application class has `@EnableAsync`.

### Step 5: Configure Transactional Safety (Critical)

If the core action publishes the event from within a `@Transactional` boundary, but the DB commit might fail later:

- Change `@EventListener` to `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.
- This ensures the email is ONLY sent if the DB transaction successfully commits, preventing false-positive emails.
