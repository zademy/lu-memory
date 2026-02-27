---
trigger: always_on
---

# Rule: Adapter Pattern en Spring Boot

## Descripción

Esta regla instruye al asistente a aplicar el patrón **Adapter (Adaptador)** dentro del ecosistema de **Spring Boot**. Como **arquitecto de software**, el asistente propondrá este patrón cuando deba resolverse una incompatibilidad de interfaces, típicamente al integrar el sistema con librerías de terceros antiguas, SDKs legados o APIs externas que no encajan en el modelo de dominio core (Hexagonal Architecture).

## Analogía del Mundo Real

**Analogía:** Un adaptador de enchufe de pared cuando viajas de Europa a EE.UU. Un enchufe europeo no encaja en una toma de corriente de EE.UU., así que usas un adaptador que tiene un conector de EE.UU. en un lado y una toma europea en el otro.

## Cuándo aplicar

- Cuando se necesita integrar un componente existente cuyas interfaces/contratos no son compatibles con lo que el código central espera.
- Al implementar arquitecturas limpias (Hexagonal/Ports and Adapters) donde el dominio define "Puertos" (Interfaces) y los detalles de infraestructura externa se integran mediante "Adaptadores".
- Cuando se usan librerías pesadas de legacy code (Ej. clientes SOAP viejos, librerías de encriptación cerradas) que ensucian la lógica de negocio si se ligan directamente.

## Cómo aplicar en Spring Boot

1. **Definir el Puerto (Interfaz Interna)**: Declarar la interfaz limpia dentro del paquete de dominio que exprese lo que el sistema necesita.
2. **Crear el Adaptador como Bean**: Implementar dicha interfaz pura en una clase anotada con `@Component` o `@Service`.
3. **Inyectar la dependencia externa**: Inyectar la clase/SDK incompatible en el Adaptador, envolviéndola y traduciendo sus entradas y salidas raras a los DTOs y objetos de dominio deseables.

## Principios SOLID Promovidos

1. **Dependency Inversion Principle (DIP)**: El core del negocio no interactúa con sistemas de terceros. Interactúa únicamente con nuestra propia interfaz (Puerto).
2. **Open/Closed Principle (OCP)**: Se pueden conectar varios adaptadores nuevos (Ej: Migrar de proveedor de correos de Mailgun a Sendgrid) añadiendo una clase sin tocar la lógica original.
3. **Single Responsibility Principle (SRP)**: Separa el código de traducción y mapeo de datos externos de la lógica operacional.

## Ejemplo Guía

```java
// 1. Lo que espera nuestro sistema (Target/Puerto)
public interface NotificationClient {
    void send(String recipient, String message);
}

// 2. La clase incompatible de terceros (Adaptee / Legacy)
public class LegacySmsLibrary {
    public boolean triggerSmsToPhone(long phoneNo, String textMessage, int priority) {
        // ... external code
        return true;
    }
}

// 3. El Adaptador mapeador anotado como Bean de Spring Boot
@Component
public class LegacySmsAdapter implements NotificationClient {
    private final LegacySmsLibrary legacyClient;

    public LegacySmsAdapter() {
        this.legacyClient = new LegacySmsLibrary(); // o inyectado
    }

    @Override
    public void send(String recipient, String message) {
        long phoneNumber = Long.parseLong(recipient);
        // Traducimos los requerimientos de la lib externa y enmascaramos la complejidad
        boolean success = legacyClient.triggerSmsToPhone(phoneNumber, message, 1);
        if(!success) throw new RuntimeException("Error en SMS legado");
    }
}
```

## Directrices para el Asistente AI

1. **Decisión proactiva en Integraciones**: Cuando el usuario requiera consumir un API externa (AWS, SDKs propietarios) desde un `Service`, objeta usar el cliente extraño directamente en el servicio de negocio e insiste en aislarlo creando una Interfaz pura y un `@Component` Adaptador (Ports & Adapters).
2. **Capa Anticorrupción**: Educa al desarrollador argumentando que envolver el código de terceros en Adaptadores establece una _Anti-Corruption Layer_ que librará al microservicio principal de enfermarse con modelos intrusivos de terceros.
3. **Refactorización Limpia**: Si ves importaciones o variables de clientes de dependencias en medio de reglas de negocio (`@Service`), propón extraer y encapsular esas llamadas a una clase Adaptadora apartada.
