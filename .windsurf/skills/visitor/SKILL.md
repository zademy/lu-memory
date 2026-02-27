---
name: visitor
description: Guides the step-by-step implementation of the Visitor design pattern in Spring Boot to externalize calculations via Double Dispatch.
---

# Visitor Pattern Implementation Skill

This skill guides Cascade pattern through extracting heavy analytical or calculation logic out of raw Database Entities and into isolated Spring Services using Double Dispatch.

## Concept & Analogy

**Analogy:** Insurance agent. An agent visits different kinds of organizations (residential, bank, coffee shop) offering specialized policies for each, executing logic based on the specific type of building they visit.
**When to apply:** When you need to perform an operation on all elements of a complex object structure (like an object tree), or to clean up business logic from auxiliary behaviors.

## Prerequisites

1. Identify Entity classes (`@Entity`) or raw domain Objects becoming bloated with operations (e.g., `calculateTax()`, `generateReport()`) that require injecting external Services.
2. Identify massive `instanceof` checks used to determine how to process elements of a common collection.

## Implementation Steps

### Step 1: Setup the Acceptor Interface

Create a generic interface that all the raw data objects must implement.

- Add a single method: `void accept(Visitor visitor);`

### Step 2: Implement Accept in Domain Entities

In every raw object class (e.g., `ProductA`, `ProductB`):

- Implement the interface.
- Implement the method utilizing Double Dispatch: `public void accept(Visitor visitor) { visitor.visit(this); }`
- Note: This must be identical in all classes. The compiler uses the `this` pointer type to resolve the exact method signature on the Visitor.

### Step 3: Define the Visitor Interface

Create the interface containing the heavy logic definitions.

- Create an overloaded method for each exact object type.
- Example: `void visit(ProductA a); void visit(ProductB b);`

### Step 4: Create the Visitor Service

Implement the heavy logic in a new, isolated class.

- Annotate with `@Service` or `@Component`.
- Inject repositories, APIs, or calculators here as needed via constructor (`@Autowired`).
- Implement the overloaded `visit` methods containing the specific mathematical or logistical operations for each respective class.

### Step 5: Refactor the Orchestrator

In the main execution flow processing the list of objects:

- Remove all `instanceof` checks.
- Inject the Visitor Service.
- Iterate the collection and call accept: `item.accept(mySpringVisitorService);`
- The system automatically routes the logic to the correct method keeping the domain objects completely free of dependencies.
