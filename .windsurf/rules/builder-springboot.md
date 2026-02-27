---
trigger: always_on
---

# Rule: Builder Pattern en Spring Boot

## Descripción

Esta regla instruye al asistente a actuar de manera proactiva proponiendo y aplicando el patrón de diseño **Builder** al construir objetos complejos en un entorno de **Spring Boot / Java**. El objetivo es evitar el uso de constructores telescópicos ("Telescoping Constructor Anti-Pattern") o depender masivamente de _setters_, logrando en última instancia objetos inmutables y código mucho más legible, aplicando los principios **SOLID**.

## Analogía del Mundo Real

**Analogía:** Construir una casa a medida paso a paso. Una casa básica necesita paredes y un techo, pero una casa de lujo necesita piscina, garaje y estatuas. No quieres un "constructor telescópico" con 20 parámetros donde 15 son nulos.

## Cuándo aplicar

- Al instanciar objetos de Dominio (Entidades, Modelos, DTOs) que tienen más de tres campos, o campos opcionales que complican la creación.
- Cuando la creación del objeto necesita varias etapas o cálculos parciales progresivos.
- Al emplear clases de configuración o construcción de clientes como `RestTemplate`, `WebClient`, o construyendo `URI`s.

## Cómo aplicar en Spring Boot / Ecosistema Java

1. **Uso de Librerías (Recomendado - Lombok)**: Lo más idiomático en proyectos Spring modernos es usar firmemente la anotación `@Builder` del proyecto Lombok, que auto-genera la clase constructora acoplada con inmutabilidad pura.
2. **Builders Nativos de Spring**: Spring Boot suministra de fábrica sus propios Builders altamente configurables. El asistente debe priorizar sugerir y usar `RestTemplateBuilder`, `WebClient.Builder`, o `MockMvcBuilders` en lugar de usar `new RestTemplate()` que se desentiende del ecosistema de métricas y traceo del framework.
3. **Builder Clásico (Sin Lombok)**: Si no se usa Lombok, implementarlo como una clase estática anidada (`static class Builder { ... }`) que retorne la propia instancia de configuración, con métodos que retornen `this`, y un constructor privado en la clase exterior para forzar instanciar vía `Builder`.

## Principios SOLID Promovidos y Aplicados

1. **Single Responsibility Principle (SRP)**: La lógica para orquestar cada paso de validación y de configuración se encapsula y segrega de la lógica de negocio final que expone la clase creada. La representación y la construcción son totalmente inconexas.
2. **Open/Closed Principle (OCP)**: Añadir nuevos atributos es fácil. Se pueden agregar métodos constructores o decoradores (usando Herencia/Abstract Builders o `@SuperBuilder`) sin romper el código cliente existente.
3. **Inmutabilidad Segura**: Ayuda a evitar el antipatrón de crear objetos mal inicializados debido al abuso de Setters o dependencias del contexto que mutan estado inesperadamente (hilos concurrentes). Un objeto `Builder.build()` es siempre consistente.

## Ejemplo Guía para Generación de Código

```java
// Ejemplo 1: Lombok Builder Puro (El Estándar Spring DTO)
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder // Automáticamente genera la inmutabilidad y patrón Builder completo asociado
public class ExternalApiRequestDTO {
    private final String url;
    private final String apiKey;
    private final Integer timeoutMs;
    @Builder.Default
    private final boolean retry = true;
}

// Ejemplo Invocación
ExternalApiRequestDTO req = ExternalApiRequestDTO.builder()
    .url("https://api.example.com")
    .apiKey("ABCD")
    .timeoutMs(5000)
    .build();

// Ejemplo 2: Builders nativos sugeridos por el Agente para clientes HTTP
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient externalWebClient(WebClient.Builder webClientBuilder) {
        // Usa el pre-configurado de Spring Boot! No use "WebClient.create()"
        return webClientBuilder
            .baseUrl("https://api.external.com")
            .defaultHeader("Authorization", "Bearer X")
            .build();
    }
}
```

## Directrices para el Asistente AI

1. **Interacción Inicial y Toma de Decisiones**: Como tomador de decisiones, si el usuario requiere mapear estructuras de datos o respuestas para integración con múltiples campos y condiciones de validación (Diferentes DTOs, Respuestas complejas REST) sugiere usar el patrón Builder antes de generar código con constructores masivos de `Record` u objetos anémicos.
2. **Caza de Antipatrones (Anti-Setter)**: Cuando se analice o revise código donde un Objeto se instancie con `new` y le sigan docenas de llamadas al método `.setXYZ(...)`, el asistente sugerirá refactorizar el código adoptando el patrón Builder local.
3. **Alineamiento con Spring Boot**: En la concepción de componentes de sistema (Clients, Security, Web configuration), alerta proactivamente en usar los builders que provee el framework (eg. `SecurityFilterChain` build via `HttpSecurity`).
4. **Verificación de dependencias**: Primero sugiere usar u ofrece instalar `Lombok` por su gran utilidad evitando código repetitivo (Boilerplate) antes de crear una clase interna estática Builder a mano que abulte la clase con cientos de líneas.
