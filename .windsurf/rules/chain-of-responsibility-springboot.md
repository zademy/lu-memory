---
trigger: always_on
---

# Rule: Chain of Responsibility Pattern en Spring Boot

## Descripción

La regla dicta al asistente cómo implementar el patrón **Chain of Responsibility** (Cadena de Responsabilidad) utilizando las facilidades de inyección de colecciones y ordenamiento del framework **Spring Boot**. En lugar de acoplar estáticamente cada "eslabón" llamando manualmente al siguiente vía una variable interna `next`, Spring permite registrar handlers independientes como `@Component` y orquestarlos automáticamente, aplicando fuertemente los principios **SOLID**.

## Analogía del Mundo Real

**Analogía:** Llamar a soporte técnico. Primero hablas con un robot (Manejador 1). Si no puede resolver tu problema, te pasa a un operador (Manejador 2). Si no puede ayudar, te pasa a un ingeniero (Manejador 3) que finalmente lo resuelve.

## Cuándo aplicar

- Al validar datos complejos en múltiples fases donde cualquiera puede abortar el proceso (Filtros de Seguridad, Validadores de Formulario).
- Cuando existen varios manejadores que pueden procesar una petición, pero el escogido se determina dinámicamente o actúan secuencialmente.
- _Nota:_ Frameworks como Spring Security (Filter Chain) usan intensivamente esto bajo el capó.

## Cómo aplicar en Spring Boot

1. **Definir la Interfaz Esencial**: Crear una interfaz `Handler` o `Validator` que retorne información de continuación (e.g., `boolean` o arroje excepciones).
2. **Implementar los Eslabones Automáticos**: Crear múltiples clases como `@Component` que implementen la interfaz. Se recomienda anotarlos con `@Order(1)`, `@Order(2)`, etc.
3. **Orquestación mediante Inyección `List`**: Declarar la "Cadena" en un Servicio Inyector usando `List<Handler>`. Spring inyectará automáticamente todos los componentes existentes respetando la jerarquía `@Order`.

## Principios SOLID Promovidos

1. **Open/Closed Principle (OCP)**: Se pueden añadir nuevas validaciones a la cadena entera agregando simplemente un nuevo archivo `@Component` con `@Order`, sin tocar NUNCA el código de la orquestación principal ni otros eslabones.
2. **Single Responsibility Principle (SRP)**: Divide una avalancha de "if-else" en validadores encapsulados unitarios independientes.

## Ejemplo Guía

```java
// 1. Interfaz base para cada eslabón
public interface OrderValidationStep {
    void validate(Order order); // Lanza Excepción si falla
}

// 2. Un eslabón concreto (Prioridad Alta)
@Component
@Order(1)
public class InventoryValidationStep implements OrderValidationStep {
    public void validate(Order order) {
        if (!checkStock(order)) throw new ValidationException("Sin inventario");
    }
}

// 3. Otro eslabón concreto (Prioridad Media)
@Component
@Order(2)
public class FraudValidationStep implements OrderValidationStep {
    public void validate(Order order) {
        if (order.getAmount() > 10000) throw new ValidationException("Fraude Detectado");
    }
}

// 4. El Orquestador de la Cadena
@Service
public class OrderProcessingChain {
    // Spring inyecta y ORDENA automáticamente los eslabones gracias a @Order
    private final List<OrderValidationStep> validationSteps;

    @Autowired
    public OrderProcessingChain(List<OrderValidationStep> steps) {
        this.validationSteps = steps;
    }

    public void processOrder(Order order) {
        // Ejecución de la cadena secuencial
        for (OrderValidationStep step : validationSteps) {
            step.validate(order);
        }
        // Si nadie tiró excepción, avanzamos...
    }
}
```

## Directrices para el Asistente AI

1. **Destrucción de Bloques de Flujo Ciclomático**: Como Arquitecto, si al ayudar a un usuario detectas un bloque de 10 sentencias `if (condicion A) return falso; if (condicion B) return falso;` seguidas dentro de un mismo método, objeta proponiendo encapsular esa complejidad en un **Chain of Responsibility**.
2. **Integración Spring Autowired**: Advierte siempre al programador de no construir dependencias enlazadas tipo Puntero (`this.next = otherNode`) estilo Java estándar. Oblígalo a pasar a inyección dinámica de Colecciones con la directiva `@Order`.
