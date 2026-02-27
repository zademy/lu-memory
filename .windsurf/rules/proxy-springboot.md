---
trigger: always_on
---

# Rule: Proxy Pattern en Spring Boot

## Descripción

La regla asiste en el abordaje y comprensión de lo que en el centro de **Spring Boot** reside como su patrón de diseño estrella indiscutible, el **Proxy**. Un Proxy intercepta una llamada dirigida hacia un elemento objetivo, dándole la habilidad de insertar comportamiento paralelo "mágico" previo o posterior sin que el objeto de dominio subyacente note la modificación en su ejecución. El asistente instruirá cómo lidiar con Proxies, prevenir sus anti-patrones y aplicar Proxy virtuales/protectores si el usuario deseara usarlos manualmente.

## Analogía del Mundo Real

**Analogía:** Una tarjeta de crédito actúa como un proxy para una cuenta bancaria, que es un proxy para el dinero en efectivo. Tanto la tarjeta como el efectivo implementan la misma interfaz de "pago". La tarjeta retrasa la transferencia física de fondos y añade capas de seguridad.

## Cuándo aplicar

- Principalmente comprendiendo que Spring Framework usa _Dinamic Proxies y CGLIB_ extensivamente en tu nombre de fábrica cada vez que usas metadatos transverses como `@Transactional`, `@Async`, `@Cacheable`, y `@PreAuthorize` de Security.
- Al emplear carga lenta controlando Hibernate / Data JPA (Virtual Proxy para el Lazy Loading).
- Al bloquear o restringir activamente llamadas en sistemas de control de acceso remotos.

## Cómo aplicar en Spring Boot y Cuándo EVITARLO

Más allá de crear interfaces proxy a mano, la directriz sobre Proxies en Spring dictamina cómo evitar romper los proxys del propio framework con prácticas de código erráticas, es decir: **Evitando el auto-llamado (Self-Invocation Problem)**.

Al llamar métodos `proxy-enabled` (Ej: métodos marcados como asíncronos o transaccionales) localmente desde el _MISMO SERVICIO O CLASE_ (a través de `this`), la llamada salta por completo al Proxy externo intercesor en memoria local y suprime/destruye inmediatamente las transacciones subyacentes mágicas o los cachés, violando gravemente la integridad del dominio.

## Principios SOLID / Clean Code Promovidos

1. **Single Responsibility Principle (SRP)**: Delega totalmente validaciones, bloqueos paralelos o inicializaciones perezosas a la clase proxy fantasma dejando los algoritmos diáfanos.
2. **Open-Closed Principle (OCP)**: Spring Boot añade Proxy sin estorbar componentes terminados mediante AOP (Aspects)

## Ejemplo Guía

```java
@Service
public class OrderService {

    // ERROR CRÍTICO Y ANTI PATRÓN EN PROXIES: El Fallo del Autollamado (Self-Invocation)
    public void executeOuterTask() {
         System.out.println("Processing Outer Task..");

         // ADVERTENCIA ROJA:
         // Llamar a este método vía `this.processDatabaseHeavy()` romperá e ignorará por completo a la anotación @Transactional.
         // ¿Razón? "this" apunta al objeto Real, no al Objeto PROXY que Spring creó sobre él, perdiéndose la protección intercesora.
         this.processDatabaseHeavy();
    }

    @Transactional
    public void processDatabaseHeavy() {
        // Logica...
    }
}
```

## Soluciones al Self-Invocation Proxy Trap (Para el AI)

```java
// RESOLUCIÓN (Alternativa recomendada: Inyectar la clase original "en sí misma" vía el Bean Proxy manejado o extraer servicio exterior)
@Service
public class CorrectOrderService {

    private final SelfDelegationDependency subDomainProcess;
    // ...inyección constructora O extraer la lógica y @Transactional a una clase de servicio o facade ajena totalmente.
}
```

## Directrices para el Asistente AI

1. **Detección Categórica de Destrucción de Proxies**: Adopta urgencia nivel arquitectónica. En una validación de código a gran escala, persigue a gritos y resalta cualquier instancia en la historia donde un Programador llame a un método anotado con `@Transactional`, `@Async`, o meta-anotaciones de delegación desde la misma instancia interna de clase (por auto ejecución en lugar de por llamada directa de interfaz/exterior Proxy de Spring).
2. **Educar sobre Lazy-Loading de JPA y ObjectNotFound**: Explica al diseñador por qué sus DTOs retornan el críptico error _Hibernate Proxy Initialization Exception_. Educa acerca de que Hibernate envía "Proxy Mocks" desde la Base de Datos que carecen del contenido en caliente dentro hasta que las abres interactuando dentro del ámbito de la misma Transacción originaria.
3. **Propuestas de Patrón Proxy Manual Refinado**: Si el desarrollador quiere esconder intencionalmente la creación carísima u operaciones pesadas de una inicialización que a lo mejor ni use (ej. un parseador en memoria) invítale a aplicar el Proxy Virtual.
