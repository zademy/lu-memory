---
name: memento
description: Guides the step-by-step implementation of the Memento design pattern in Spring Boot for states, audits and rollbacks.
---

# Memento Pattern Implementation Skill

This skill guides Cascade through preserving secure snapshots of object state using modern Java Records or delegating entirely to Hibernate Envers.

## Concept & Analogy

**Analogy:** Creating a snapshot or save-game. In a text editor, before you execute a risky format, the app saves a precise, private snapshot of the text, cursor, and scroll position. If you hit "Undo", the editor pulls that exact snapshot (the Memento) back without exposing its internal variables to the outside world.
**When to apply:** When you want to produce snapshots of an object's state to be able to restore it later, and when direct access to the object's fields would violate its encapsulation.

## Prerequisites

1. Identify a requirement to audit changes over time, support Undo/Redo operations, or implement SAGA compensation rollbacks.
2. Prevent writing the archaic GoF Memento pattern by hand with massive CareTaker/Originator boilerplate classes.

## Implementation Steps

### Option A: Database-Level Memento (Hibernate Envers)

#### Step A1: Add Dependencies

Ensure the project includes the `spring-data-envers` dependency (or equivalent Hibernate Envers library).

#### Step A2: Annotate the Entities

Locate the core Entity that needs its exact state history (mementos) preserved automatically on every transaction.

- Add the `@Audited` annotation to the class.
- Spring Boot will automatically generate shadow tables (`_AUD`) that act as a persistent CareTaker tracking every single historical snapshot perfectly encapsulated.

### Option B: In-Memory Memento (Java Records)

#### Step B1: Define the Immutable Memento

Create an immutable `record` to hold the snapshot data. It must contain only the fields required for restoration and no logic.
Example: `public record StateSnapshotMemento(String phase, Double balance) {}`

#### Step B2: Create the Memento (Originator)

Inside the stateful domain class:

- Create a `saveState()` method.
- Return a newly instantiated Record containing copies of the current internal properties: `return new StateSnapshotMemento(this.phase, this.balance);`

#### Step B3: Restore the Memento

Inside the same stateful domain class:

- Create a `restoreState(StateSnapshotMemento memento)` method.
- Re-map the internal fields exactly from the values provided invisibly via the Memento record: `this.phase = memento.phase();`.
- The CareTaker (the external service) simply holds the `StateSnapshotMemento` object without ever seeing or modifying its properties.
