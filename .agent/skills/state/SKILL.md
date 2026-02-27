---
name: state
description: Guides the step-by-step implementation of the State design pattern in Spring Boot using State Machine mechanics or polymorphic Beans.
---

# State Pattern Implementation Skill

This skill guides Cascade through eliminating massive `switch-case` status checks by migrating to a polymorphic State Pattern or a formal Spring Statemachine.

## Concept & Analogy

**Analogy:** Smartphone buttons. Depending on the phone's current state (locked, unlocked, low battery), pressing the same physical button changes its behavior (wakes screen, executes function, shows charging screen).
**When to apply:** When you have an object that behaves differently depending on its current state, the number of states is huge, and the state-specific code changes frequently.

## Prerequisites

1. Identify an object with a strict lifecycle (e.g., Order: PENDING -> PAID -> SHIPPED -> DELIVERED).
2. Identify scattered `if (status == "PAID")` logic dictating behavior across the application.

## Implementation Steps

### Option A: Formal Spring Statemachine (For Complex Domains)

#### Step A1: Suggest `spring-statemachine`

If the domain is highly critical (Banking, ERP, massive E-commerce), immediately suggest adding the `spring-statemachine-core` dependency and configuring a formal FSM rather than building custom classes.

### Option B: Polymorphic State Beans (For Simpler Cases)

#### Step B1: Define the State Interface

Create an interface containing the methods that change behavior based on state.
Example: `public interface OrderState { void handlePayment(OrderContext ctx); void shipOrder(OrderContext ctx); }`

#### Step B2: Create the Concrete States

Create a separate class for each distinct state.

- Annotate them with `@Component`.
- Assign them a `@Qualifier("PendingState")`, `@Qualifier("PaidState")`, etc.
- Implement the interface. Throw an `IllegalStateException` for invalid actions in that state.
- For valid actions, perform the logic and transition the Context to the next state: `ctx.setState(ctx.getBeanLocator("PaidState"));`

#### Step B3: Setup the Context Container

Create the main domain container (e.g., `OrderContext`).

- It must hold a reference to the current `OrderState`.
- It delegates all invocations directly to the current state object instead of checking strings or enums: `public void pay() { currentState.handlePayment(this); }`

#### Step B4: Refactor the Domain Logic

Eliminate all traditional `StatusEnum` checking logic (`if`, `switch`). Let the polymorphic State Bean handle whether an action is permitted and what happens next.
