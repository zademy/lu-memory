---
name: adapter
description: Guides the step-by-step implementation of the Adapter design pattern in Spring Boot to integrate incompatible third-party APIs.
---

# Adapter Pattern Implementation Skill

This skill guides Cascade through creating Anti-Corruption Layers using the Adapter pattern in Spring Boot.

## Concept & Analogy

**Analogy:** A wall plug adapter when traveling from Europe to the US. A European plug won't fit a US socket, so you use an adapter that has a US plug on one side and a European socket on the other.
**When to apply:** When you need a class to collaborate with an incompatible interface, especially when you cannot change the incompatible class.

## Prerequisites

1. Identify a requirement to integrate an external library, legacy SDK, or incompatible API.
2. Determine the core domain's desired data structure (the Puerto/Port).

## Implementation Steps

### Step 1: Define the Domain Port

Create an interface inside your core domain that defines EXACTLY what your system needs, ignoring the external library's bizarre requirements.
Example:

```java
public interface NotificationClient {
    void sendNotification(String userId, String plainText);
}
```

### Step 2: Create the Adapter Component

Create a new class that implements the interface created in Step 1.

- Annotate the class with `@Component` or `@Service`.
- Name it explicitly as an adapter (e.g., `TwilioSmsAdapter`).

### Step 3: Inject the Incompatible Adaptee

Provide the external heavy/legacy dependency to your Adapter.

- Inject it via the constructor (e.g., `private final LegacyComplexClient client;`).
- If the external client requires complex setup, configure it internally within the constructor or via a `@Bean` configuration.

### Step 4: Implement the Translation Logic

Inside the overridden interface methods, perform the translation:

- Map your clean domain objects (like `String userId`) to the complex parameters the external API needs (like `long decodedId`, `new AuthRequest(token)`).
- Execute the external call.
- Map the external API's response back to your clean domain response or throw a domain-specific Exception on failure.

### Step 5: Update the Client Service

Search your core business logic (`@Service`) for direct references to the external SDK.

- Remove all imports and usages of the external library from the core service.
- Inject the target Interface (`NotificationClient`) instead.
- The Domain is now totally decoupled from the vendor.
