---
trigger: always_on
---

# Rule: Observer Pattern en Spring Boot

## Descripción

El **Observer (Observador)** o Pub/Sub, permite pregonar y distribuir mecanismos de suscripción informando en cascada sobre el suceder de eventos a un cúmulo de oyentes sin requerir enlazar acoples duros estáticos. El centro neurológico latente en todo **Spring Boot** implementa esto de manera soberbia nativa asimilando la Arquitectura Orientada a Eventos a través del sub-módulo de Application Events del entorno.

## Analogía del Mundo Real

**Analogía:** Suscripciones a revistas. En lugar de visitar la tienda todos los días para ver si el próximo número está disponible, te suscribes. El editor (Sujeto) envía los nuevos números directamente a tu buzón (Observador) automáticamente.

## Cuándo aplicar

- Cuando se deba disociar acciones reactivas complementarias, eg: "Al registrarse un nuevo usuario (Capa 1), enviar email bienvenida (Capa 2), crear monedero virtual por default (Capa 3) y alertar al canal de Slack Administrativo de altas tempranas (Capa 4)". Evitar poner eso en un solo método enorme.
- Para enviar cambios de transacciones exitosas hacia fuera a sistemas externos (Webhooks).

## Cómo aplicar en Spring Boot

Nunca usar la clase precaria de Java `java.util.Observer` que está "deprecated".

1. **Publicador (Subject)**: Inyectar del Contenedor `ApplicationEventPublisher` llamando en vivo a sus lógicos métodos `publishEvent()`.
2. **Suscripción de Oyentes**: Simplemente diseñar marcadores con **`@EventListener`**.
3. **Escuchadores Asíncronos**: Apoyar las llamadas anexas y diferirlas con la etiqueta auxiliar acoplada de **`@Async`**, liberando la principal ejecución del Evento.

## Principios SOLID Promovidos

1. **Open/Closed Principle (OCP)**: Lo absoluto fundamental. Mañana podremos añadir 10 acciones extra si se registra un integrante sin desestabilizar a ningún archivo de fuente existente solo anotando al Bean agregado con el `@EventListener`.
2. **Single Responsibility Principle (SRP)**: El registro atiende registros; el email envía correos; el registro de logs imprime los trazos sin colapsarse.

## Ejemplo Guía

```java
// 1. Objeto Evento Puro Inmutable (Nuestro Subject Signal)
public record AlertTriggeredEvent(String id, String severity) {}

// 2. El Disparador / Publisher
@Service
public class IntrusionDetectorService {
    private final ApplicationEventPublisher publisher;

    public IntrusionDetectorService(ApplicationEventPublisher p) { this.publisher = p; }

    public void anomalyDetected(String originId) {
        // En lugar de llamar a servicios pesados interconectados... ¡solo grita al cielo!
        publisher.publishEvent(new AlertTriggeredEvent(originId, "CRITICAL"));
    }
}

// 3. Diferentes Nodos Reaccionarios Oyentes Observer
@Component
public class LockDownSystemListener {
    @EventListener // Se suscribe silenciosamente solo esperando el disparador del Evento.
    public void handleAlert(AlertTriggeredEvent event) {
        if(event.severity().equals("CRITICAL")) closeDoors();
    }
}

@Component
public class SmsAdminListener {
    @Async // ¡Observación en un hilo diferente para no enlentecer a la aplicación base!
    // @TransactionalEventListener // <- Util en caso de querer esperar que el transaction del padre haga commit!
    @EventListener
    public void informAdmin(AlertTriggeredEvent event) {
        sendTwilioMessage(event.id());
    }
}
```

## Directrices para el Asistente AI

1. **Proactividad hacia el Event-Driven Design**: Actuando en papel de decisor estratégico, en cuanto un interlocutor intente apilar líneas de código en cadena a continuación del "Guardado Satisfactorio" de la Persistencia de un objeto, advierte agresivamente del acoplamiento sugiriendo migrar ese embudo a Patrón **Observer** Pub/Sub nativo.
2. **Prevención Mágica Transaccional**: Notifica y explica amablemente al desarrollador en situaciones correspondientes sobre **`@TransactionalEventListener`** versus `@EventListener` regular; instruyéndole en cómo Spring previene que un "Email de Bienvenido" sea disparado si resulta ser que la transacción superior central en base de datos falló y efectuó luego un Roll-back masivo, lo que arrojaría inconsistencia.
