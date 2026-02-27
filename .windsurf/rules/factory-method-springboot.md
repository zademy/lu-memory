---
trigger: always_on
---

# Rule: Factory Method en Spring Boot

## Descripción

Esta regla instruye al asistente a identificar oportunidades de diseño para aplicar el patrón **Factory Method** en proyectos basados en **Spring Boot** y a implementarlo utilizando las mejores prácticas del framework (Inyección de Dependencias, `@Component`, `@Service`, y colecciones inyectadas automáticamente), respetando los principios **SOLID** (especialmente Open/Closed y Dependency Inversion).

## Analogía del Mundo Real

**Analogía:** Una app de logística que inicialmente solo maneja Camiones. Cuando las operaciones se expanden a la logística marítima, inyectar directamente un `new Barco()` rompe el código base. La Fábrica delega la construcción a creadores especializados.

## Cuándo aplicar

- Cuando se deba crear un objeto, elegir una implementación de una estrategia, o seleccionar un servicio delegado de manera dinámica basándose en condiciones, parámetros de entrada o enumeradores.
- Cuando exista un "código espagueti" o alta complejidad ciclomática (`switch` o múltiples `if-else` encadenados) determinando qué clase instanciar o qué servicio llamar.
- Cuando la creación dependa de otros Beans inyectados de Spring, haciendo inviable el simple uso de un operador `new`.
- Cuando el sistema necesite ser fácilmente extensible (agregar nuevas reglas de negocio o implementaciones sin modificar el cliente).

## Cómo aplicar en Spring Boot

En Spring Boot, **NO** se recomienda instanciar un Factory manual con `new` o con lógicas estáticas si los productos del factory tienen dependencias externas. En su lugar, se aprovecha el **Contenedor IoC (Inversión de Control)**.

### Implementación Recomendada: Inyección Dinámica vía `List` -> `Map` (Registry Pattern + Factory Pattern)

Se recomienda el uso conjunto de inyección de lista (`List<T>`) dentro de un Bean factoría y transformar dicha lista internamente a un `Map<String, T>` para buscar la implementación correcta de manera eficiente (O(1)).

## Principios SOLID Promovidos y Aplicados

1. **Open/Closed Principle (OCP)**: Nuevas estrategias de resolución se pueden agregar sumando simplemente un nuevo `@Component` que implemente la interfaz, sin alterar ni una línea del `Factory`, cumpliendo al 100% que el código está abierto a la extensión y cerrado a la modificación.
2. **Dependency Inversion Principle (DIP)**: Los clientes ya no dependen de un puñado de servicios ni instancian componentes sucios; todo está abstraído tras una sola interfaz.
3. **Single Responsibility Principle (SRP)**: La Factoría se encarga solamente de orquestar y entregar el componente correcto. El consumidor solo procesa el flujo general y las implementaciones dedican exclusividad a su lógica particular.

## Ejemplo Guía para Generación de Código

```java
// 1. Interfaz Común (Producto del Factory)
public interface PaymentProcessor {
    String getPaymentType();
    void process(double amount);
}

// 2. Implementaciones Específicas
@Service
public class CreditCardProcessor implements PaymentProcessor {
    @Override
    public String getPaymentType() {
        return "CREDIT_CARD";
    }

    @Override
    public void process(double amount) {
        // Lógica de Tarjeta de Crédito
    }
}

@Service
public class PaypalProcessor implements PaymentProcessor {
    @Override
    public String getPaymentType() {
        return "PAYPAL";
    }

    @Override
    public void process(double amount) {
        // Lógica de Paypal
    }
}

// 3. Spring Boot Factory Component
@Component
public class PaymentProcessorFactory {
    // Registry interno para búsqueda O(1)
    private final Map<String, PaymentProcessor> processorsMap;

    @Autowired
    public PaymentProcessorFactory(List<PaymentProcessor> processors) {
        // Al inyectar List<T>, Spring pasa dinámicamente todos los Beans que implementan la interfaz.
        this.processorsMap = processors.stream()
            .collect(Collectors.toMap(PaymentProcessor::getPaymentType, p -> p));
    }

    public PaymentProcessor getProcessor(String type) {
        PaymentProcessor processor = processorsMap.get(type);
        if (processor == null) {
            throw new IllegalArgumentException("Tipo de procesador no soportado: " + type);
        }
        return processor;
    }
}
```

## Directrices para el Asistente AI

1. **Interacción Inicial y Toma de Decisiones**: Al inicio de una conversación o sesión de diseño (chat), actúa proactivamente como **tomador de decisiones arquitectónicas**. Si el usuario plantea un requerimiento donde se deban manejar múltiples condiciones, integraciones o variantes, **sugiere de inmediato el patrón Factory Method** como la mejor estrategia de diseño, antes incluso de que se escriba el primer código.
2. **Detección proactiva de código heredado**: Al ver un Service que contiene muchos `if-else` o un gran `switch` basado en Strings/Enums para procesar diferentes datos, sugiere un **Factory Method de Spring Boot**.
3. **Refactorización Limpia**: Ofrece convertir la lógica engorrosa al modelo anterior, usando un `Map` para instanciar en tiempo real la clase precisa.
4. **Justificación**: Muestra siempre al desarrollador por qué la solución con OCP (Open/Closed) favorece la escalabilidad del sistema, y los hace más amigables a las pruebas unitarias (Unit Tests).
