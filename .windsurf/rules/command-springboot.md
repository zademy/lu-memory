---
trigger: always_on
---

# Rule: Command Pattern en Spring Boot

## Descripción

El patrón **Command (Comando)** convierte una solicitud u operación en un objeto independiente que contiene toda la información métrica sobre cómo ser ejecutado. Esta regla enseña al asistente a promover este patrón como base para mecanismos modernos en **Spring Boot**, fundamental para el procesamiento asíncrono en colas (vía JMS, RabbitMQ, o Spring Events) y el diferimiento de acciones.

## Analogía del Mundo Real

**Analogía:** Pedir comida en un restaurante. Le dices al mesero tu pedido. El mesero lo escribe en un papel (el Comando) y lo pone en la fila de la cocina. El Chef (Receptor) lee el papel y cocina la comida cuando está lista, desacoplándote del proceso de cocción.

## Cuándo aplicar

- Cuando se necesite diferir, encolar o agendar tareas (Sistemas de Jobs, Queues).
- Para implementar sistemas potentes de Deshacer/Rehacer (Undo/Redo).
- Cuando el invocador (ej. UN EndPoint REST) no deba tener idea de _cómo_ se ejecuta una lógica, y solo deba "emitir la acción" y liberarse de la carga.

## Cómo aplicar en Spring Boot

En Spring, el patrón Command alcanza su máximo esplendor al combinarse con eventos o brokers asíncronos en lugar de comandos seriales síncronos:

1. **ApplicationEventPublisher**: Implementar un Command como un Evento que viaja por el EventBus de Spring a receptores asíncronos.
2. **Ejecutores Asíncronos**: Despachar enrutamientos que implementen `Runnable` o Interfaces Command hacia hilos o `@Async`.
3. Para interfaces sincronas o transaccionales duras, el típico Command Bus pattern (Suele aplicarse bajo CQRS con Axon o lógicas propias).

## Principios SOLID Promovidos

1. **Single Responsibility Principle (SRP)**: El emisor invoca, el receptor ejecuta de verdad.
2. **Open/Closed Principle (OCP)**: Es posible inyectar nuevos Receptores en un Event Bus añadiendo un comando distinto nuevo sin tocar jamás al componente emisor.

## Ejemplo Guía

```java
// 1. El Objeto "Command" como abstracción pura portadora de datos
public record ProcessPaymentCommand(Long userId, Double amount) {}

// 2. El INVOCADOR (No procesa, sólo emite el comando a un bus asíncrono)
@RestController
public class PaymentController {

    private final ApplicationEventPublisher eventBus;

    public PaymentController(ApplicationEventPublisher eventBus) {
        this.eventBus = eventBus;
    }

    @PostMapping("/pay")
    public ResponseEntity<String> pay(@RequestBody PaymentDTO dto) {
        ProcessPaymentCommand cmd = new ProcessPaymentCommand(dto.user(), dto.amount());
        eventBus.publishEvent(cmd); // Emite y se olvida! Desacoplamiento total
        return ResponseEntity.ok("Payment encolado");
    }
}

// 3. El RECEPTOR (Execution Listener)
@Component
public class PaymentCommandHandler {

    @Async // Procesa en hilo separado
    @EventListener
    public void execute(ProcessPaymentCommand command) {
        System.out.println("Comando Procesado: Cobrando " + command.amount());
        // ... Lógica bancaria
    }
}
```

## Directrices para el Asistente AI

1. **Evitar Acoplamiento Asíncrono**: Si el usuario intenta llamar métodos increíblemente pesados (Ej. Enviar 10,000 emails masivos) bloqueando el hilo de un `@RestController`, propón agresivamente el patrón Command, donde un Objeto Comando detalla la operativa y se envía a un Publisher o Cola (RabbitMQ, Kafka) liberando el thread de Web Mvc.
2. **Propulsión CQRS**: Promueve este padrón bajo su derivado CQRS (_Command Query Responsibility Segregation_) si percibes que el dominio muta la DB. Fija la instrucción para diferenciar Comandos (Acciones que cambian estado) de Queries (Consultas Get puras).
