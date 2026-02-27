---
trigger: always_on
---

# Rule: Flyweight Pattern en Spring Boot

## Descripción

El patrón **Flyweight** persigue frenar el hiper-consumo y sangrado indiscriminado de memoria RAM compartiendo estado común entre grandes volúmenes de objetos minuciosos en tu sistema. Esta regla instruye al asistente a alertar al programador para delegar todo esto a los impresionantes engranajes internos del framework de **Spring Boot**. La herramienta por excelencia en este punto radica en sistemas integrales de Memoria y Caché (`@Cacheable`, Caffeine, map pooling) y la fuerte dependencia hacia Inmutabilidad y Beans Stateless.

## Analogía del Mundo Real

**Analogía:** Renderizar millones de árboles en un juego. En lugar de almacenar texturas masivas en millones de objetos Árbol, extraes los datos pesados "TipoArbol" en un solo objeto Flyweight compartido entre millones de instancias de coordenadas ligeras.

## Cuándo aplicar

- Al lidiar y crear colecciones excesivas (cientos de miles, billones) de objetos inyectados desde bases de datos pero que poseen propiedades comunes inmutables (ej. Tipos de Categoria en un catálogo gigante).
- Evitar múltiples accesos REST y DB duplicados idénticos usando el subsistema de caché.
- Creación de factorías inmutables persistentes en el arranque de la aplicación usando variables constantes estáticas de memoria compartida tipo Flyweight.

## Cómo aplicar en Spring Boot

1. **Spring Cache Management**: Configurar un manejador unificado de la caché, integrando memoria como `Caffeine` en el ámbito interno u optando con `Redis` en el externo para componentes o métodos pesados inyectando la meta-data en la etiqueta natural de la librería de Spring cache (`@Cacheable`).
2. **Factoría Estática por Referencias Hash (`Enum`/`Map`)**: Configurar y llenar clases POJO inmutables (Uso estricto del patron Builder) bajo Registros de Colecciones Singleton de las partes compartidas sin duplicarlas jamas vía un proveedor `ObjectFactory`.

## Principios SOLID / Clean Code Promovidos

1. **Eficiencia y Escalabilidad Estructural (Clean Architecture)**: Menos instanciaciones masivas en bucles o respuestas directas significa retrasar los paratiros paralizantes del _Garbage Collector (GC)_ y disminuir el footprint RAM general.
2. **Single Responsibility Principle (SRP) e Inmutabilidad**: Fomenta el fraccionamiento de los estados (Estado Intrínseco que se comparte sin posibilidad de cambio vs Estado Extrínseco que provee el cliente).

## Ejemplo Guía

```java
// 1. Objeto del estado Compartido Mínimo INMUTABLE!
public record ItemSharedData(String typeLabel, byte[] sharedIconBlob) { }

// 2. La Factory de referencias cacheada RAM (Flyweight Factory)
@Service
public class ItemTypeFlyweightFactory {

    // Almacena referencia compartida pura del tipo (Solo ocupando memoria 1 sola vez por llame en el Map)
    private final Map<String, ItemSharedData> cachePool = new ConcurrentHashMap<>();

    // Método para reusar o crear
    public ItemSharedData getSharedData(String keyType) {
        return cachePool.computeIfAbsent(keyType, k -> {
            // Operación muy pesada (acceder archivo disco, etc) la hacemos Solo UNA Vez y cachamos
            return new ItemSharedData(k, loadHeavyIcon(k));
        });
    }

    private byte[] loadHeavyIcon(String code) { return new byte[2000]; /* Costoso!! */ }
}

// 3. Método Transparente en Spring Boot Framework Natural
@Service
public class DictionaryService {

    // Al colocar esto, si me envían la misma Petición ID, no construyo nuevo objeto, comparto desde RAM Spring Proxy temporal el objeto serializado guardado.
    @Cacheable(value = "expensiveDbLookups", key = "#dictionaryId")
    public DictionaryData retrieveMassiveData(long dictionaryId) {
        return loadMassiveItemsDatabase(dictionaryId);
    }
}
```

## Directrices para el Asistente AI

1. **Detección de Instanciación Brutal**: Si notas que los bloques de lógica planean realizar búsquedas masivas e iterativas instanciando localmente un componente o DTO con variables enormes cada iteración `for` o `while` al iterar Listados de DB, bloquea y propón Flyweight mediante un Map o Factory temporal compartiendo la carga instanciada.
2. **Promoción de Spring Cache**: Abrazando tu rol como Arquitecto, si observas que se acceden datos paramétricos duros (por ejemplo: tipos de cambio diarios, configuración regional) directamente y sin pausa desde la Database sugiera activamente habilitar `@EnableCaching` e incorporar mecanismos Flyweight a través de `@Cacheable` de Spring Native.
