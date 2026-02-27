---
name: strategy
description: Guides the step-by-step implementation of the Strategy design pattern in Spring Boot using dynamic Map injection.
---

# Strategy Pattern Implementation Skill

This skill guides Cascade through eliminating type-checking `if-else` cascades by using Spring Boot's O(1) Map injection to dynamically select algorithms.

## Concept & Analogy

**Analogy:** Getting to the airport. You can ride a bike, take a bus, or hail a taxi. These are all different transportation strategies to achieve the same goal, chosen dynamically based on budget or time constraints.
**When to apply:** When you want to use different variants of an algorithm within an object and be able to switch from one algorithm to another at runtime.

## Prerequisites

1. Identify a method deciding which algorithm to run based on an identifier (e.g., `if (type.equals("CREDIT")) calcCredit() else if (type.equals("PAYPAL")) calcPaypal()`).

## Implementation Steps

### Step 1: Define the Shared Strategy Interface

Create a common interface for the algorithms.

- **CRITICAL:** Include an identification method, e.g., `String getStrategyType();`
- Define the operational method, e.g., `double calculate(double amount);`

### Step 2: Implement the Concrete Strategies

Create separate classes for each branch of the inner logic.

- Annotate each with `@Component` or `@Service`.
- Implement the interface completely.
- Return the unique identifier string in the `getStrategyType()` method (e.g., "CREDIT", "PAYPAL").

### Step 3: Setup the Strategy Context (The Orchestrator)

Create the Service that previously held the massive `if-else` block.

- Declare a Map dictionary: `private final Map<String, PaymentStrategy> strategyMap;`

### Step 4: Inject via Map Conversion

Create a constructor injecting all strategies and transforming them into the Map.

```java
@Autowired
public PaymentContext(List<PaymentStrategy> strategies) {
    this.strategyMap = strategies.stream()
        .collect(Collectors.toMap(PaymentStrategy::getStrategyType, s -> s));
}
```

### Step 5: Execute in O(1) Time

Inside the main operational method:

- Retrieve the chosen strategy using the incoming identifier.
- `PaymentStrategy target = strategyMap.get(paymentType);`
- Handle nulls (e.g., throw `IllegalArgumentException`).
- Execute the algorithm blindly: `target.calculate(amount);`
- Delete all legacy conditional statements.
