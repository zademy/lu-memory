---
name: bridge
description: Guides the step-by-step implementation of the Bridge design pattern in Spring Boot, prioritizing composition over inheritance.
---

# Bridge Pattern Implementation Skill

This skill guides Cascade through breaking down massive inheritance trees by injecting orthogonal implementations (Bridges) using Spring Boot.

## Concept & Analogy

**Analogy:** A universal remote control (abstraction) and devices like a TV or Radio (implementation). The remote's interface can be developed independently of the TV's internal circuitry.
**When to apply:** When you want to divide a monolithic class with multiple variants into two separate hierarchies that can be developed and extended independently.

## Prerequisites

1. Identify a combinatorial explosion of subclasses (e.g., `DesktopDirectXRenderer`, `DesktopOpenGLRenderer`, `MobileDirectXRenderer`).
2. Identify the two orthogonal dimensions: The Abstraction (the high-level control) and the Implementor (the low-level specific platform/tool).

## Implementation Steps

### Step 1: Define the Implementor Base

Create an interface that dictates the primitive operations that the specific platforms will execute.

```java
public interface MessagingProvider {
    void pushMessage(String text);
}
```

### Step 2: Create Concrete Implementors

Create the classes for the specific tools.

- Annotate them with `@Component`.
- Add a `@Qualifier("toolName")` to easily distinguish them during injection.
  Example: `@Component @Qualifier("twilioProvider") public class TwilioProvider implements MessagingProvider`

### Step 3: Define the Abstraction (The Bridge)

Create an `abstract class` representing the high-level logic (The Domain).

- Define a `protected final MessagingProvider provider;` field (This is the Bridge).
- Create a constructor requiring this interface.
- Provide `abstract` high-level operational methods.

### Step 4: Create Refined Abstractions

Create the high-level variants extending the `abstract class`.

- Annotate them with `@Service`.
- In their constructor, inject the specific required `@Qualifier` and pass it to `super()`.

```java
@Service
public class UrgentNotificationService extends NotificationService {
    @Autowired
    public UrgentNotificationService(@Qualifier("twilioProvider") MessagingProvider provider) {
        super(provider);
    }
    // Implement high-level logic using provider.pushMessage()
}
```

### Step 5: Eliminate the Old Inheritance Tree

Delete the combinatorial legacy classes. The application now uses Composition (Bridge via Spring DI) instead of deepest-level inheritance.
