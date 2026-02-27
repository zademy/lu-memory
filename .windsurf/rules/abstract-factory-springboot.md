---
trigger: always_on
---

# Rule: Abstract Factory en Spring Boot

## Descripción

Esta regla instruye al asistente a actuar como un **tomador de decisiones arquitectónicas** proactivo para identificar cuándo se deben construir "familias" de objetos o servicios relacionados usando el patrón **Abstract Factory**, especialmente en entornos de **Spring Boot**. La regla promueve el uso del IoC container de Spring para gestionar este patrón, garantizando que todo el código sea testable, extensible y que aplique rigurosamente los principios **SOLID**.

## Analogía del Mundo Real

**Analogía:** Imagina una tienda de muebles que vende sillas, sofás y mesas de centro en varios estilos que combinan entre sí (Moderno, Victoriano, Art Decó). Los productos de diferentes familias nunca deben mezclarse. El patrón Abstract Factory garantiza que siempre obtengas muebles del mismo estilo.

## Cuándo aplicar

- Cuando el sistema necesite configurar múltiples variantes de un producto complejo o una familia entera de implementaciones que están agrupadas bajo un mismo contexto tecnológico o de negocio.
- Cuando una simple `Factory Method` se quede corta porque el sistema no devuelve usar un solo servicio o clase, sino _varios servicios u objetos acoplados lógicamente_.
- Ejemplos comunes: Familias de UI (dark/light mode components), diferentes integraciones de Cloud (AWS vs GCP vs Azure: StorageService + ComputeService + DatabaseService), o pasarelas de pago complejas que necesitan instanciar tanto el calculador de comisiones como el validador de identidad de forma conjunta.

## Cómo aplicar en Spring Boot

A diferencia de la creación estática del Abstract Factory clásico usando `new`, Spring Boot simplifica su instrumentación mediante:

1. Declarar una Interfaz Base (la "Fábrica Abstracta") que defina la creación de múltiples servicios.
2. Crear un Componente central (`@Component` o `@Service`) que implemente esta Interfaz Base, o, mejor aún, aprovechar un `BeanFactory` o configuración en `@Configuration` inyectando propiedades de control.
3. Usar inyección de colecciones (`List` o `Map`) o un **Registry Pattern** avanzado para agrupar las "Factories Particulares".

## Principios SOLID Promovidos y Aplicados

1. **Open/Closed Principle (OCP)**: Añadir una nueva variante/familia de productos en el futuro consiste únicamente en implementar una nueva "Fábrica Concreta" y registrarla como un Bean, con un impacto nulo en el cliente que invoca la Factory principal.
2. **Dependency Inversion Principle (DIP)**: El cliente que usa la familia de productos y creadores solo interactúa a través de abstracciones puras, desconociendo completamente qué Framework/API física está detrás de la compilación de la lógica provista.
3. **Liskov Substitution Principle (LSP)**: Cualquier Familia Concreta retornada por la Factoría se comportará consistentemente dentro de las expectativas del componente controlador, siendo intercambiables en tiempo de ejecución de manera segura y libre de Side-effects no documentados.

## Ejemplo Guía para Generación de Código

```java
// 1. Interfaces de los Productos
public interface StorageService { void save(byte[] data); }
public interface AnalyticsService { void analyze(String event); }

// 2. Interfaz del Abstract Factory (La Factoría de Familias)
public interface CloudProviderFactory {
    String getProviderName(); // Identificador: "AWS", "GCP", etc.
    StorageService createStorageService();
    AnalyticsService createAnalyticsService();
}

// 3. Implementación Específica por Familia (Fábricas Concretas como Beans)
@Component
public class AwsCloudFactory implements CloudProviderFactory {
    @Override
    public String getProviderName() { return "AWS"; }

    @Override
    public StorageService createStorageService() {
        // Lógica de S3
        return new AwsS3StorageService();
    }

    @Override
    public AnalyticsService createAnalyticsService() {
        // Lógica de Kinesis/CloudWatch
        return new AwsAnalyticsService();
    }
}

@Component
public class GcpCloudFactory implements CloudProviderFactory {
    // Implementaciones análogas para GCP...
}

// 4. Factory Provider Selector (El punto de entrada unificado para Spring)
@Component
public class CloudServicesOrchestrator {
    private final Map<String, CloudProviderFactory> factories;

    @Autowired
    public CloudServicesOrchestrator(List<CloudProviderFactory> factoryList) {
        this.factories = factoryList.stream()
            .collect(Collectors.toMap(CloudProviderFactory::getProviderName, f -> f));
    }

    public CloudProviderFactory getCloudFactory(String provider) {
        CloudProviderFactory factory = factories.get(provider.toUpperCase());
        if (factory == null) {
            throw new IllegalArgumentException("Cloud Provider no soportado: " + provider);
        }
        return factory;
    }
}
```

## Directrices para el Asistente AI

1. **Interacción Inicial y Toma de Decisiones**: Al plantear la arquitectura durante el inicio de conversación, si el usuario requiere implementar un ecosistema de varios servicios/productos que operan interdependientes (Familias), **sugiere de inmediato el patrón Abstract Factory**.
2. **Detección de anti-patrones relacionales**: Si observas que el usuario está creando interfaces separadas pero tiene un condicional gigante `switch` esparcido en múltiples ubicaciones buscando definir si usar "AWS Storage o GCP Storage" y, tres líneas abajo, otra vez el `switch` para "AWS Auth o GCP Auth", alerta sobre la vulneración del SRP y falta de cohesión.
3. **Escalamiento vía Inyección**: Impulsa estructurar el código utilizando `List` a `Map` para orquestar las Fábricas usando las bondades del contexto IoC de Spring nativo.
4. **Claridad Arquitectónica**: Recuerda siempre al desarrollador que el uso de Abstract Factory puede aumentar temporalmente la "cantidad de clases", pero sus dividendos a largo plazo en aislamiento y pruebas unitarias seguras (`Mockito`) justificarán totalmente el esfuerzo.
