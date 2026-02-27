---
trigger: always_on
---

# Rule: State Pattern en Spring Boot

## Descripción

El patrón **State (Estado)** le concede la soberanía de alterar el compartamiento absoluto de ejecución interna de un algoritmo u objeto cuando el contexto que lo cobija cambia de etapa vitalicia. En la concepción moderna y pesada de **Spring Boot**, si la cantidad de variables cíclicas son demasiadas y de peso industrial logístico, la regla promueve abandonar polimorfismo básico y mudarse velozmente a **Spring Statemachine**, consolidando así el flujo de transición formal entre Estados bajo las protecciones técnicas del framework.

## Analogía del Mundo Real

**Analogía:** Botones de teléfonos inteligentes. Dependiendo del estado actual del teléfono (bloqueado, desbloqueado, batería baja), presionar el mismo botón físico cambia su comportamiento (despierta la pantalla, ejecuta una función, muestra la pantalla de carga).

## Cuándo aplicar

- Al programar entes con flujos de vida o etapas marcadas fuertemente restringidas y dispares (p. ej: Un Pedido - Creado, Pagado, Empacado, Enviado, Cancelado, Devuelto -).
- Si cada estado restringe operaciones de diferentes lógicas, y te abrumas bajo una red inmensa de validadores `switch-case` o "State == X/ State == Y" para dejar pasar acciones.

## Cómo aplicar en Spring Boot

1. **Diseño Básico**: Para casos pequeños, inyectar dinámicamente el Set/List de clases interceptoras usando el modelo parecido a _Factory / Strategy_ mapeado en un StateContext inyectado en un Bean.
2. **Uso Avanzado / Oficial (Spring Statemachine)**: Emplear librerías satélite de Spring orientadas específicamente para crear autómata finitos (FSM); configurando formalmente Transiciones y Estados enumerables con inyección nativa del autómata, salvaguardado y resguardando contexto persistentemente con Redis o la Base de un plumazo.

## Principios SOLID Promovidos

1. **Single Responsibility Principle (SRP)**: La lógica de la transición y los eventos específicos dentro de un evento (e.g., qué sucede cuando está Pagado) reside íntegramente de la clase propia del Estado, no del Orquestador General.
2. **Open/Closed Principle (OCP)**: Alterar un nuevo Flujo y Transición solo necesita definir una declaración de máquina añadida o una Interfaz adjunta al contexto FSM.

## Ejemplo Guía

```java
// EJEMPLO CONCEPTUAL BÁSICO BASADO EN INYECCIONES / STRATEGY (Para escenarios pequeños/puros)

// 1. Estados como Enums o Interfaces compartidas
public interface DocumentStateRole {
    void handleApproval(DocumentContext context);
    void handleRejection(DocumentContext context);
}

@Service
@Qualifier("DraftState")
public class DraftState implements DocumentStateRole {
    public void handleApproval(DocumentContext context) {
        // Valida que todo sea verde, avanza estado
        context.setCurrentState(context.getBeanLocator("ReviewState"));
    }
    public void handleRejection(DocumentContext context) { /* Ignorar / Error */ }
}

@Service
@Qualifier("ReviewState")
public class ReviewState implements DocumentStateRole {
    public void handleApproval(DocumentContext context) {
        context.setCurrentState(context.getBeanLocator("PublishedState"));
    }
    public void handleRejection(DocumentContext context) {
        context.setCurrentState(context.getBeanLocator("DraftState"));
    }
}

// 2. El Contexto del Objeto mutable (Usa prototypos y se le delega el estado actual al Contexto general)
public class DocumentContext {
    private DocumentStateRole currentState;

    // Su estado dictamina el cómo responde a invocaciones.
    public void approve() { currentState.handleApproval(this); }
    public void reject() { currentState.handleRejection(this); }
}
```

## Directrices para el Asistente AI

1. **Recomendación FSM Profesional**: Si adviertes que el usuario te requiere codificar máquinas de estados de dominios principales (Sistemas ERP, Ciclo de Transacciones Bancarias, Lógicas completas de E-commerce Carts), propón la incorporación sólida de **`Spring Statemachine`** como primera sugerencia en la fase de Toma de Decisiones del Diseño, educando en favor de un FSM formalizado con persistencia incorporada y manejadores de guardias asíncronos en las transacciones.
2. **Alergia a Switch-Case infinitos**: Si te enfrentas a una refactorización donde observes _code-smell_ tipo `if(status == "CREATED") {..} else if(status == "SHIPPED"){..}`, levanta una alerta y aconseja proactivamente aislar el motor de negocio transicionando a un Patrón State puro delegando el comportamiento al valor dinámico inyectado en una propiedad Interface de Estado.
