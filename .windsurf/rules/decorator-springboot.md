---
trigger: always_on
---

# Rule: Decorator Pattern en Spring Boot

## Descripción

El patrón **Decorator (Decorador)** proporciona una manera maleable de añadir comportamiento a objetos terminales sin la necesidad de subclasificarlos masivamente y sin alterarlos. En aplicaciones **Spring Boot**, el asistente debe orientar la decisión arquitectónica ya sea hacia envoltorios de delegación (`@Primary` o `@Qualifier`) para lógica fuertemente enraizada al Dominio o invadir drásticamente la metaprogramación sugiriendo **Spring AOP (Aspectos)** si los comportamientos son transversales técnicos (Cross-cutting concerns).

## Analogía del Mundo Real

**Analogía:** Usar ropa. Cuando hace frío, te pones un suéter (decorador). Si llueve, te pones un impermeable (otro decorador). La ropa "extiende" tu comportamiento dinámicamente sin cambiar tu identidad base.

## Cuándo aplicar

- Al agregar múltiples responsabilidades aditivas en combinaciones diversas (Ej: Añadir una capa de caché, luego cifrado, y luego logs) a un objeto en tiempo de ejecución de su servicio.
- Al necesitar envolver componentes de terceros y no poder o no querer acceder a su lógica física.

## Cómo aplicar en Spring Boot

Existen dos avenidas para aplicarlo eficientemente sin usar código boilerplate explícito:

1. **Decorador Bean Nativo (Wrapper `@Primary`)**: Se anotan beans originales con algo explícito (`@Qualifier("baseService")`) y el el nuevo decorador implementa la interfaz, envuelve al inyectado y devuelve anotado con `@Primary`. Todo cliente que lo inyecte usará ahora al decorador de interceptor por omisión.
2. **Aspect Orientated Programming (Spring AOP)**: Enviar responsabilidades transversales extra-dominio al contexto de `@Aspect` / `@Around`, que tejen decoradores Proxy-transparentes en tiempo de ejecución en métodos target permitiendo no enmarañar la arquitectura de clases.

## Principios SOLID Promovidos

1. **Single Responsibility (SRP)**: La autenticación, el caché o el log permanecen aislados afuera de la lógica cruda de dominio del componente central mediante la delegación o las anotaciones.
2. **Open/Closed Principle (OCP)**: Permite la amalgama iterativa de comportamientos anidando decoradores unos en otros a medida que lo requiera el sistema, sin mutar las clases concretas o iniciales.

## Ejemplo Guía

```java
public interface DocumentService { void generate(String doc); }

// Servicio "Desnudo" central
@Component("baseDocumentService")
public class BasicDocumentService implements DocumentService {
    @Override
    public void generate(String doc) { System.out.println("Processing: " + doc); }
}

// DECORADOR ESTILO DELEGATE
// Al colocar @Primary se engaña al Contexto de Spring para que priorice cargar este
@Primary
@Component
public class CacheAndLogDocumentDecorator implements DocumentService {

    private final DocumentService innerService;

    // Se inyecta explícitamente y se captura en el Scope la implementación base "nativa"
    @Autowired
    public CacheAndLogDocumentDecorator(@Qualifier("baseDocumentService") DocumentService inner) {
        this.innerService = inner;
    }

    @Override
    public void generate(String doc) {
        // Lógica agregada pre-decorador
        System.out.println("Logger pre-generación o Cache Check...");

        // Llamada central al envuelto!
        innerService.generate(doc);

        // Lógica post-decorador
        System.out.println("Sincronizando asíncrono e-mail de respuesta ok!");
    }
}
```

## Directrices para el Asistente AI

1. **Abogacía sobre Aspectos (AOP)**: Al detectarse si un usuario pretende colocar capas de validación en bruto, cronometraje (`StopWatch`), manejo de logs invasivos o retries en métodos puros de dominio, intercede proponiendo la vertiente extrema del Decorator (Spring AOP / `@Aspect`).
2. **Identificación de Wrappers**: Cuando el usuario solicite encadenar operaciones que envuelven lógicas (Cachear sobre BuscarSobreBD sobre EncriptarResultado), sugiere el ecosistema Wrapper `Decorator` empujando por anotaciones explícitas (`@Primary`, `@Qualifier`) para evitar conflictos _NoUniqueBeanDefinitionException_ en la carga de variables del contenedor de Spring.
