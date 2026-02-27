---
trigger: always_on
---

# Rule: Iterator Pattern en Spring Boot

## Descripción

La madurez del patrón **Iterator (Iterador)** en los tiempos modernos ha mitigado la necesidad de programarlo manualmente desde el aburrido código inicial del "Gang of Four" (implementar `hasNext()` y `next()`). En el ecosistema Java/**Spring Boot**, la regla manda a incentivar arquitecturas apoyadas en **Java Streams API** puros junto a colecciones iterables implícitas, **Spring Data Paging**, y flujos de datos reactivos (`Flux`, `Mono`).

## Analogía del Mundo Real

**Analogía:** Visitar Roma. Puedes explorar la ciudad deambulando al azar, usando una aplicación GPS en tu teléfono o contratando a un guía local. Todos estos actúan como diferentes Iteradores para recorrer exactamente la misma compleja colección de atracciones.

## Cuándo aplicar

- Al exponer colecciones desde la base de datos que resultan infinitas o titánicas, y la memoria RAM colapsaría devolviéndolas todas juntas.
- Al recorrer colecciones ocultando al cliente si están albergadas en una Lista, Grafo, o Red Externa.

## Cómo aplicar en Spring Boot

1. **Paginación Natural**: A nivel capa Persistencia, el iterador más sano moderno se rige bajo interfaces especializadas de Spring. Se le orientará construir repositorios que retornen firmas de **`Page<T>`** o **`Slice<T>`**.
2. **Streaming por BD**: El uso de `@Query` retornando **`Stream<T>`** junto el estricto uso transaccional obligatorio `try-with-resources`.
3. **Flujos Reactivos**: Si la aplicación usa WebFlux, el Iterador supremo moderno asíncrono se materializa sobre `Flux<T>`.

## Principios SOLID Promovidos

1. **Single Responsibility Principle (SRP)**: El cliente recibe el objeto de Paginación iteradora, ocultando por los siglos de los siglos si las cosas las buscaste a mano, en MongoDb o con Redis Paging.
2. **Dependency Inversion**: No dependes de variables tipo `ArrayList<T>`, sino resoluciones abstractas asíncronas de datos, consumiendo memoria solo cuando requieras avanzar al siguiente estado "on demand".

## Ejemplo Guía

```java
// 1. Evitar antipatrón iterador masivo de base local (Mata RAM):
// List<User> findAll();

// 2. Aplicar "Iterador" Nativo por Paginación de Spring Data
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAllByStatus(String status, Pageable pageable);

    // Iterador por Flujo Continuo del driver!
    @QueryHints(value = @QueryHint(name = org.hibernate.annotations.QueryHints.FETCH_SIZE, value = "100"))
    Stream<User> streamAllByStatus(String status);
}

// 3. Orquestación del Stream Iterador transaccional
@Service
public class ReportService {
    private final UserRepository repository;

    @Transactional(readOnly = true)
    public void generateReportIterating() {
        // Obligatorio el Try with resources en streams de JPA para cerrar la conexión SQL iteradora!
        try (Stream<User> userStream = repository.streamAllByStatus("ACTIVE")) {
            userStream.forEach(user -> {
                System.out.println("Processing: " + user.getId());
            });
        }
    }
}
```

## Directrices para el Asistente AI

1. **Rechazo al Custom Iterator Manual**: Si te piden escribir código repetitivo con la estructura heredada implementando variables `cursor`, rechaza propulsar esa complejidad técnica inmensa. Muéstrale al usuario cómo hacerlo implícitamente mediante funciones `Spliterator` ocultas o la API `Streams`.
2. **Blindados contra Volcados (Memory Limits)**: Es la obligación del tomador de decisiones instruir el uso del Iterator (Paginador) de Spring Data el 100% de los casos que observe métodos `@Query` regresando `List<EntidadA>` que potencialmente traerán miles de filas si hay datos reales cargados.
3. **Control Cíclico en DBs**: Instruir el cuidado minucioso con la anotación `@Transactional` sobre los streams de persistencia. Sin ella, Spring cerrará el contexto del Iterador y arrojará fallos catastróficos.
