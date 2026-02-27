---
name: facade
description: Guides the step-by-step implementation of the Facade design pattern in Spring Boot to orchestrate complex subsystems.
---

# Facade Pattern Implementation Skill

This skill guides Cascade through creating a Facade to protect controllers and upper layers from messy infrastructure or complex domain entanglement using Spring Boot.

## Concept & Analogy

**Analogy:** Ordering by phone at a store. The phone operator is your "facade." You don't need to navigate the warehouse or the payment gateway yourself; the operator provides a single, simple interface to all departments.
**When to apply:** When you need a simplified interface to a complex framework or subsystem, or when you want to structure your subsystems into cleaner, decoupled layers.

## Prerequisites

1. Identify a Controller (`@RestController`) or high-level handler that is injecting too many Repositories and Services.
2. Identify a transactional operation that requires strict coordination across more than 3 distinct services.

## Implementation Steps

### Step 1: Create the Facade Service

Create a new central class to act as the Facade.

- Annotate the new class strictly as an orchestrator with `@Service` or `@Component`.
- Name it explicitly (e.g., `OrderFulfillmentFacade`).

### Step 2: Extract Dependencies

Move the injected components (lower-level services like Inventory, Ledger, Shipment) from the Controller into the new Facade.

- Inject them into the Facade constructor.

### Step 3: Define the Simplified Gateway Method

Create the unified public method in the Facade that the Controller will call.

- It should swallow the complexity and return a simplified DTO or boolean.
- Example: `public CheckoutResult processCheckout(long userId, ShoppingCart cart)`

### Step 4: Apply Transactional Safeguards

If the orchestrated services modify state across databases or critical components:

- Annotate the new public Facade method with `@Transactional`.
- This ensures the complex workflow acts atomically (All-or-Nothing).

### Step 5: Refactor the Caller

Return to the original Controller.

- Delete all the complex logic.
- Inject the single Facade Service.
- Replace the legacy 50-line method with `facade.processCheckout(userId, cart)`.
