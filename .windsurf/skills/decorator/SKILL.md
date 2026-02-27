---
name: decorator
description: Guides the step-by-step implementation of the Decorator design pattern in Spring Boot using AOP or Primary Delegations.
---

# Decorator Pattern Implementation Skill

This skill guides Cascade through adding responsibilities dynamically to objects in Spring Boot avoiding monolithic classes.

## Concept & Analogy

**Analogy:** Wearing clothes. When it's cold, you put on a sweater (decorator). If it rains, you put on a raincoat (another decorator). The clothes "extend" your behavior dynamically without changing your base identity.
**When to apply:** When you need to add functionality to objects dynamically at runtime (like logging, caching, or notifications) without altering their code or relying on an exploding number of subclasses.

## Prerequisites

1. Identify a core service that needs transverse logic added (e.g., caching, logging, encrypting) without modifying its inner code.
2. Decide between Native Bean Wrapping (for business logic decoration) or Spring AOP (for purely transverse technical concerns).

## Implementation Steps

### Option A: Native Bean Wrapper (Business Decorator)

#### Step A1: Expose the Base Service

Ensure the core service implements an Interface.
Annotate the core service class with `@Component("baseQualifierName")`.

#### Step A2: Create the Decorator

Create a new class implementing the SAME Interface.

- Annotate with `@Component`.
- **CRITICAL:** Annotate with `@Primary` so Spring prioritizes the Decorator whenever the Interface is injected.

#### Step A3: Inject and Envelop

Inside the Decorator:

- Define a field for the Interface.
- Inject it via the constructor using `@Qualifier("baseQualifierName")` to ensure Spring injects the naked/base component, not the Decorator itself.

#### Step A4: Add Behavior

Implement the overridden methods.

- Execute the pre-actions (e.g., Log start time).
- Call the base class: `innerService.doWork()`.
- Execute post-actions (e.g., Save to cache).

### Option B: Aspect Oriented Programming (Technical Decorator)

#### Step B1: Enable AOP

Ensure `<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-aop</artifactId></dependency>` exists.

#### Step B2: Create the Aspect

Create a transverse aspect instead of a class wrapper.

- Annotate with `@Aspect` and `@Component`.

#### Step B3: Define the Pointcut and Around Advice

- Use `@Around("execution(* com.yourapp.services.TargetService.*(..))")` to intercept.
- Receive the `ProceedingJoinPoint`.
- Execute pre-logic, call `joinPoint.proceed()`, and execute post-logic.
