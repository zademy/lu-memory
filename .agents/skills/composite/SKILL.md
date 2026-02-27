---
name: composite
description: Guides the step-by-step implementation of the Composite design pattern in Spring Boot for recursive multi-level processing.
---

# Composite Pattern Implementation Skill

This skill guides Cascade through implementing the Composite pattern using Spring Boot's automatic collection injection features.

## Concept & Analogy

**Analogy:** A military hierarchy. Orders start at the top (Generals) and are recursively passed down to Divisions, Brigades, Squads, and finally Soldiers. Both an individual Soldier and an entire Division implement the "execute order" interface.
**When to apply:** When you need to represent part-whole hierarchies as trees, and you want clients to treat individual objects and compositions of objects uniformly without knowing their concrete classes.

## Prerequisites

1. Identify a requirement to process single rules, calculations, or filters interchangeably with massive groups of them (e.g., chain of rules, tax calculators).

## Implementation Steps

### Step 1: Define the Shared Component Interface

Create the interface that both Single Items (Leaves) and Groups (Composites) will share.
Example: `public interface PriceCalculator { double calc(double base); }`

### Step 2: Create the Leaf Processors

Implement the individual, atomic rules.

- Annotate each with `@Component`.
- Implement the interface strictly with their specific logic.
- Consider adding `@Order(1)`, `@Order(2)` to define execution priority naturally.

### Step 3: Create the Composite Master Node

Create the class that groups the leaves.

- Must implement the exact same Interface (`PriceCalculator`).
- Annotate with `@Component`.
- CRÍTICAL: Annotate with `@Primary` so clients using `@Autowired PriceCalculator` receive the full orchestrated chain, not a random leaf.

### Step 4: Inject the Tree Dynamically

Inside the Master Node:

- Inject a `List<SharedInterface>` via the constructor. Spring Boot will automatically locate and wire all Leaf Beans inside this list.

### Step 5: Implement the Recursive Loop

Inside the Master Node overridden method:

1. Iterate over the injected `List<>`.
2. **Infinite Loop Protection:** Check `if (!(item instanceof MasterCompositeClass))` before calling its logic to prevent the Composite from calling itself recursively until StackOverflow.
3. Keep track of the rolling state (e.g., updating the price variable) and return the aggregated result.

### Step 6: Client Usage

Ensure clients only inject the single Interface without `List`. Since the Composite is `@Primary`, the client will trigger the whole tree seamlessly.
