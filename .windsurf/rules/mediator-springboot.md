---
trigger: always_on
---

# Rule: Mediator Pattern en Spring Boot

## Descripción

El patrón **Mediator (Mediador)** abroga por centralizar las interacciones enmarañadas de forma cruzada (espagueti) entre muchos objetos u orígenes dispares en un solo hub de coordinación, logrando que los objetos pierdan dependencias bidireccionales destructivas apuntando a una sola vía central. En **Spring Boot**, el rol de Mediador recae de manera arquitectónica absoluta en los Orquestadores, el Application Context y típicamente librerías estilo Event-Bus/CQRS (Ej. Bus de eventos de Spring).

## Analogía del Mundo Real

**Analogía:** Control de tráfico aéreo. Los aviones no se comunican directamente entre sí para decidir quién aterriza a continuación. Todos hablan con la torre de control (el Mediador), que centraliza y orquesta la cola de aterrizaje.

## Cuándo aplicar

- Se requiere cuando docenas de componentes conversan el uno con el otro haciendo circular referencias de llamadas (Inyección Circular).
- Cuando en vez del Componente A llamar al B y C, le cede la llamada de acción al Controlador Mediador que redirige según políticas internas.
- Para reducir el acoplamiento directo entre Micro-funcionalidades.

## Cómo aplicar en Spring Boot

1. **Spring Internal Event Bus**: El ejemplo clásico puro y duro es el uso de `ApplicationEventPublisher` donde el contenedor IoC funge la función entera del Mediador. Los objetos publican y leen al mediador (el Event Context) sin citar bibliotecas externas jamás.
2. **Orchestrator Services**: Al tener muchos micro y micro-componentes, un Servicio Central asume el puente entre dos contextos pesados, forzando la "Ley de Demeter".

## Principios SOLID Promovidos

1. **Single Responsibility (SRP)**: Saca el código lógico y caótico logístico comunicacional entre la capa de módulos y lo recluye en un solo Mediador especializado.
2. **Dependency Inversion (DIP)**: Todos los componentes solo traen una inyección y referencian pasivamente al EventBus o la Facade/Mediator asomada sin apuntes específicos a bases de datos directas.

## Ejemplo Guía

```java
// Antipatrón: Servicio dependiente que inyecta a A llamando a B y a C... Un caos de red.
// ENFOQUE CQRS / MEDIATOR

public record UserRegistrationEvent(String username) {}

// A) COMUNICADOR PURO CIEGO
@Service
public class UserService {
    private final ApplicationEventPublisher mediator;

    public UserService(ApplicationEventPublisher mediator) { this.mediator = mediator; }

    public void register(String username) {
        // Componentes no saben quién, ni dónde se guarda o se envían correos. Hablan solo al Mediador.
        mediator.publishEvent(new UserRegistrationEvent(username));
    }
}

// B) LOS OYENTES AISLADOS
@Component
public class EmailNotificationMediatedListener {
    @EventListener
    public void onUserRegistration(UserRegistrationEvent event) {
        // Reacciona al mediador
        sendEmail(event.username());
    }
}

@Component
public class AuditLogMediatedListener {
    @EventListener
    public void onUserRegistration(UserRegistrationEvent event) {
        saveLog(event.username() + " registrado");
    }
}
```

## Directrices para el Asistente AI

1. **Circular Dependency Error Solver**: El AI tomará inmediata precaución y presentará la solución de la arquitectura **Mediator / Event** inmediatamente que el framework arroje la fatal excepción **`BeanCurrentlyInCreationException`** (Inyección Circular: El ServicioA necesita a Servicio B, y el B inyecta al C, y el C inyecta al A). Si el AI detecta este error sugerirá migrar el control a un Bus Mediador de dependencias.
2. **Caza de Arquitectura Mono-Servicio**: Si observas servicios "Todopoderosos" inyectando de docenas de fuentes, promueve su disosiación guiando las conversaciones del control bajo el patrón Mediator (A menudo convergiendo hacia pautas Observer orientadas a asincronía).
