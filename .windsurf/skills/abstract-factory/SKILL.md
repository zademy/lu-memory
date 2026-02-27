---
name: abstract-factory
description: Guides the step-by-step implementation of the Abstract Factory design pattern in Spring Boot, creating product families using IoC orchestration.
---

# Abstract Factory Implementation Skill

This skill guides Cascade through implementing an Abstract Factory pattern natively in Spring Boot.

## Concept & Analogy

**Analogy:** A furniture store selling Chairs, Sofas, and Coffee Tables in various matching styles (Modern, Victorian, Art Deco) where products from different families shouldn't be mixed.
**When to apply:** When the system must produce families of related products, but shouldn't depend on their concrete classes to allow future extensibility and ensure products always integrate properly.

## Prerequisites

1. Identify the family of related interfaces (e.g., `StorageService`, `AnalyticsService`).
2. Identify the variants or providers (e.g., `AWS`, `GCP`).

## Implementation Steps

### Step 1: Define Product Interfaces

Create the abstract interfaces for each product in your family. Do not define `@Component` here.
Example: `public interface StorageService { void save(); }`

### Step 2: Define the Abstract Factory Interface

Create the interface that will group the creation of your products. It MUST include an identifier method.
Example:

```java
public interface ProviderFactory {
    String getProviderName(); // Essential for Spring Registry mapping
    StorageService createStorageService();
    AnalyticsService createAnalyticsService();
}
```

### Step 3: Implement Concrete Factories

For each variant (e.g., AWS, GCP), create a class implementing the Abstract Factory interface.

- Annotate the class with `@Component`.
- Implement `getProviderName()` to return a unique constant matching the variant.
- Return the specific concrete product instances in the creation methods.

### Step 4: Create the Factory Orchestrator

Create the central dispatcher class that clients will use to retrieve the correct factory.

- Annotate with `@Component`.
- Inject `List<ProviderFactory>` using the `@Autowired` constructor.
- Initialize an internal `Map<String, ProviderFactory>` mapping the provider name to its factory instance.
- Provide a `getFactory(String providerName)` method that fetches from the Map in O(1).

### Step 5: Update the Client Code

Locate the code that previously relied on `switch/if-else` blocks to choose variants.

- Inject the Orchestrator.
- Request the required Factory giving the variant name.
- Call the Factory to create the family of objects securely.
