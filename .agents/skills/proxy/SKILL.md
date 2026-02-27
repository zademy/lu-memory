---
name: proxy
description: Guides the step-by-step implementation of the Proxy design pattern and avoidance of the Self-Invocation Anti-Pattern in Spring Boot.
---

# Proxy Pattern Implementation Skill

This skill guides Cascade through understanding Spring Boot's internal proxies, fixing the Self-Invocation trap, and applying manual proxies.

## Concept & Analogy

**Analogy:** A credit card acts as a proxy for a bank account, which is a proxy for actual cash. Both the card and cash implement the same "payment" interface. The card delays the physical transfer of funds and adds security layers.
**When to apply:** When you need to control access to an object, such as delaying its initialization (Lazy Loading), adding security checks, logging requests, or managing remote service calls without modifying the target object.

## Prerequisites

1. Identify code relying on Spring Interceptor Proxies like `@Transactional`, `@Async`, `@Cacheable`, or `@PreAuthorize`.
2. Inspect for the "Self-Invocation" trap (calling these methods internally via `this`).

## Implementation Steps

### Option A: Fixing the Self-Invocation Proxy Trap (CRITICAL)

#### Step A1: Identify the Trap

Look inside a `@Service` for methods marked with `@Transactional` or `@Async` that are invoked from WITHIN the same class (e.g., `this.processInternal()`).

- Because Spring uses Dynamic Proxies, internal calls bypass the proxy, meaning the transaction, security, or cache will NOT trigger.

#### Step A2: Refactor to Prevent the Trap

Choose one of the following fixes:

1. **Extract to a new Service (Recommended):** Move the annotated method entirely to a new, separate `@Service` class and inject it into the original class. This forces the call to go through the public proxy boundary.
2. **Self-Injection:** Inject the proxy version of the current class into itself (if Spring Boot version >= 2.6, consider `ObjectProvider` or `Lazy` to avoid circular dependencies).
3. **ApplicationContext retrieval:** Use `AopContext.currentProxy()` (requires `exposeProxy=true`).

### Option B: Creating a Manual Virtual/Protection Proxy

#### Step B1: Create the Interface

Ensure the target heavy object implements an interface.
Example: `public interface MassiveFileParser { void parse(); }`

#### Step B2: Create the Proxy Class

Create a new class implementing the SAME interface.

- Annotate with `@Component` (or `@Primary` if injecting globally).
- Ensure it holds a reference to the Real Subject but DOES NOT instantly instantiate it.

#### Step B3: Apply Lazy Initialization

inside the Proxy:

- Do NOT inject the real subject directly via `@Autowired`. Inject `ObjectProvider<RealMassiveFileParser> provider`.
- In the overriden method, check if the real subject has been retrieved. If not, instantiate it `this.realParser = provider.getObject()`.
- Delegate the call: `this.realParser.parse()`.
