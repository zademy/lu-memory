---
trigger: always_on
---

# Rule: Singleton Pattern en Spring Boot

## Descripción

Esta regla instruye al asistente a actuar como un **tomador de decisiones arquitectónicas** para orientar la implementación del patrón **Singleton** hacia una perspectiva nativa de **Spring Boot**. En un entorno de Inversión de Control (IoC), la implementación clásica "Gang of Four" (una clase con un constructor privado estricto, variables estáticas y el método `public static getInstance()`) no solo es obsoleta, sino considerada un **antipatrón** por causar acoplamiento fuerte y dificultar las pruebas unitarias.

En su lugar, la regla promueve fuertemente el uso natural de los **Spring Beans**, que ya son Singleness (Singleton) por defecto en su Scope, advirtiendo sobre su correcta configuración inmutable y orientada a _Stateless_ (Sin Estado).

## Analogía del Mundo Real

**Analogía:** El Gobierno de un país. Un país solo puede tener un gobierno oficial que actúa como un único punto de acceso global para las leyes y la representación.

## Cuándo aplicar

- Cuando la aplicación necesite usar un componente, procesador o un gestor de caché costoso de instanciar y se requiera que viva exactamente una vez por todo el ciclo de ejecución de la JVM.
- En Controladores REST, Servicios y Repositorios.
- Cuando se comparta configuración, pools de conexiones a la base de datos o utilitarios lógicos.

## Cómo aplicar en Spring Boot

A diferencia del Singleton clásico manual:

1. **Delegación de Instancia (IoC)**: Dejar la inicialización y el control del ciclo de vida al contenedor inyectando mediante `@Component`, `@Service`, `@Repository`, `@Controller` o exportándolo desde una clase `@Configuration` vía `@Bean`.
2. **Eliminar Bloqueos Antiguos**: Eliminar y desaconsejar el uso estricto de cláusulas `synchronized` y la comprobación Double-Checked Locking para la creación, puesto que Spring, mediante la preinstanciación _Eager_, ya nos asegura Thread-safety al arrancar el contexto.
3. **Manejo de Estado (Stateless)**: Una instancia Singleton transversal **nunca** debe poseer variables de estado mutables (como `private int counter = 0` o constructores que mantengan info de un request específico) relativas a transacciones o al usuario actual. Todo estado mutante debe ser manejado a nivel de bases de datos o en `ThreadLocal` abstractos definidos por el contexto (como `SecurityContextHolder`).

## Principios SOLID Promovidos y Aplicados

1. **Dependency Inversion Principle (DIP)**: Un Singleton inyectado como un simple Bean puede ser abstraído detrás de una Interfaz. Esto destruye la gran falencia del Singleton tradicional: el acoplamiento duro; permitiendo proveer _Mocks_ fácilmente en tiempos de pruebas en un solo contexto.
2. **Single Responsibility Principle (SRP)**: La clase Singleton original GoF tenía la responsabilidad de ser ella y de _manejar también su propia creación cíclica aislada_. En Spring, la creación recae en el Framework y la clase se limita unívocamente a lo suyo.

## Ejemplo Guía para Generación de Código

```java
// ANTIPATRÓN: EVITAR generar este Singleton del año 1999
public class ManualCacheManager {
    private static ManualCacheManager instance;
    private ManualCacheManager() {}
    public static synchronized ManualCacheManager getInstance() {
        if(instance == null) instance = new ManualCacheManager();
        return instance;
    }
}

// -----------------------------------------------------

// 1. Singleton Nativo Correcto de Spring Boot
@Service
public class MetricsCollectorService {
    // Correcto: Variables de solo lectura/Inyectadas inmutables
    private final MeterRegistry meterRegistry;

    // PELIGRO (Antipatrón Stateful, propenso a fallas concurrentes)
    // private int totalRequestsTemporal = 0;

    @Autowired
    public MetricsCollectorService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementRequestCounter() {
        meterRegistry.counter("requests.total").increment();
    }
}

// 2. Extrayendo un Singleton Externo vía configuración
@Configuration
public class UtilityConfig {

    // Por omisión, un Bean así entregado es un Singleton thread-safe en todo el inicio
    @Bean
    public ThirdPartyHeavyClient thirdPartyClient() {
        return new ThirdPartyHeavyClient("config-apiKeyxyz");
    }
}
```

## Directrices para el Asistente AI

1. **Interacción Inicial y Toma de Decisiones**: Si el usuario pide crear una clase Utils (como un formateador, generador de un identificador complejo, un caché manual, un `DateUtil`), objeta proactivamente sobre declarar métodos estáticos (antipatrón `public static void x()`) si esto va a necesitar base de datos, caché u otras conexiones. Sugiérele definirlo como un Componente (`@Component`) Singleton puro que deba ser inyectado.
2. **Caza de Antipatrón Clásico**: Si el asistente detecta o si se le pide construir explícitamente una clase usando las etiquetas `public static ... getInstance()` ó declaraciones `private static _instance`, el asistente se rehusará amablemente, educando al desarrollador sobre cómo Spring y sus Beans inyectables resuelven los problemas de concupiscencia y testeabilidad a los que condena el patrón heredado.
3. **Mantenimiento Crítico de Statelessness**: Al escanear una clase `@Service`, `@Component` o `@RestController` nueva, si se percata de la creación de un atributo modificable persistente como: `private String temporalUser;` advertirá con prioridad máxima (alerta roja) sobre una _Race Condition_, asegurando que es un componente Singleton que será embestido por múltiples hilos nativos asincrónicos procesando usuarios diferentes. Citará un requerimiento urgente de pasar ese estado al paradigma Scope/Request o en su defecto almacenarlo en el ThreadLocal de Spring de ser necesario.
