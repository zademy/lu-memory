---
name: mediator
description: Guides the step-by-step implementation of the Mediator design pattern in Spring Boot to resolve Circular Injections and inter-service spaghetti.
---

# Mediator Pattern Implementation Skill

This skill guides Cascade through centralizing cross-communication between chaotic micro-components to resolve `BeanCurrentlyInCreationException` (Circular Dependency) and spaghetti code.

## Concept & Analogy

**Analogy:** Air traffic control. Airplanes don't communicate directly with each other to decide who lands next. Instead, they all talk to the air traffic controller tower (the Mediator), which centralizes and orchestrates the landing queue.
**When to apply:** When classes are tightly coupled to a mess of other classes making them hard to change or reuse, or when you find yourself creating many subclasses just to alter how a few components interact.

## Prerequisites

1. Identify Circular Dependency errors thrown by Spring Boot Context at startup.
2. Identify a "God Service" that injects 5-10 other peer services which in turn inject each other simultaneously.

## Implementation Steps

### Step 1: Sever the Direct Dependencies

Target the services that are cross-calling each other.

- Remove their `@Autowired` constructor injections pointing to each other.
- The goal is that Service A no longer imports or knows about the existence of Service B or C.

### Step 2: Establish the Mediator Hub

Decide between an explicit Mediator (Orchestrator Facade) or the Implicit Event Mediator (Spring Event Bus). The Event Bus is preferred for breaking cycles.

- Inject `ApplicationEventPublisher` into Service A (The Originator).

### Step 3: Define the Mediation Contract (Event/Message)

Create immutable `record` objects representing the actions or state changes that need to be communicated.
Example: `public record UserRegistrationEvent(String username) {}`

### Step 4: Dispatch to the Mediator

In Service A, when an action happens that B and C care about:

- Do NOT call B or C.
- Publish the event to the Hub: `mediator.publishEvent(new UserRegistrationEvent(username));`

### Step 5: Connect Listeners to the Mediator

In Service B and Service C (The Listeners):

- Create methods to react to the central Hub.
- Annotate them with `@EventListener`.
- They will now receive the broadcasted state and execute their local logic without being physically coupled to Service A.
