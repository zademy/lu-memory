---
trigger: always_on
---

# Rule: Memento Pattern en Spring Boot

## Descripción

El patrón **Memento (Recuerdo)** permite resguardar metadatos cruciales o el estado íntegro y exacto de un objeto en un lapso en particular para revertirlo de ser necesario, conservando a cabalidad el principio clave de **Encapsulamiento** (sin exponer detalles internos a un guardián). En **Spring Boot**, orientamos este patrón de comportamiento especialmente para transacciones complejas baseadas en persistencia que carezcan de un rollback natural, así como en auditorias inmutables, empleando a fondo el framework JPA (Hibernate Envers) o copias de DTO inmutables transitorias.

## Analogía del Mundo Real

**Analogía:** Crear una instantánea o guardar un juego. En un editor de texto, antes de ejecutar un formato arriesgado, la aplicación guarda una copia privada y precisa del texto, el cursor y la posición de desplazamiento. Si presionas "Deshacer", el editor recupera esa copia exacta (el Memento) sin exponer sus variables internas al mundo exterior.

## Cuándo aplicar

- Al aplicar funcionalidades masivas de Deshacer/Rehacer.
- Operaciones a base de datos de control de versiones y auditoria temporal de entidades (Audit Logs / History snapshots).
- Salvaguardado de estado global distribuido donde fallen partes y deba restaurarse un momento original y exacto.

## Cómo aplicar en Spring Boot

El Memento artesanal creando clases CareTaker es largo y pesado. En Spring y su fuerte lazo con Hibernate, delegamos la responsabilidad a las soluciones robustas del framework:

1. **Hibernate Envers**: Spring Data integra Envers proporcionando mecanismos listos para emplear auditoría global de bases de datos insertando Mementos automáticos (`@Audited`) por detrás interceptando eventos.
2. **Clones Inmutables Records**: Para resguardar un "Snapshot in-memory" usar el tipo de dato Java 14+ `record`, siendo objetos finales e inmutables ideales para mementos.

## Principios SOLID Promovidos

1. **Single Responsibility (SRP)**: El responsable de realizar los snapshots guarda la memoria paralela y se exime de entender el algoritmo de negocio de la clase, siendo la clase quien cede únicamente representaciones cerradas.
2. **Encapsulamiento de Modificaciones Estructurales**: Un memento debe permanecer invisible ante toda posible interrupción extraña y ser readaptado con inmutabilidad de extremo a extremo.

## Ejemplo Guía

```java
// 1. Uso de Records modernos para establecer un Snapshot (Memento) temporal puramente Inmutable
public record StateSnapshotMemento(String currentStatePhase, Double balanceTemp) {}

// La Originator
public class FinancialProcessor {
    private String currentPhase = "INITIALIZED";
    private Double currentBalance = 150.0;

    // Crea y empaqueta cediendo datos puros, sin pasar atributos o objetos con setters.
    public StateSnapshotMemento saveState() {
        return new StateSnapshotMemento(this.currentPhase, this.currentBalance);
    }

    // Restaura basándose solamente en lectura, sin inmutar el record.
    public void restoreState(StateSnapshotMemento memento) {
        this.currentPhase = memento.currentStatePhase();
        this.currentBalance = memento.balanceTemp();
    }
}

// 2. LA FORMA NATIVA DE SPRING BOOT: Base de datos.
// Hibernate Envers genera el Caretaker Memento en el motor DB.
@Entity
@Audited // ¡Hace todo el proceso del patrón Memento insertando historial _AUD!
public class FinancialApplicationState {
    @Id private Long id;
    private String currentStatePhase;
    //...
}
```

## Directrices para el Asistente AI

1. **Evitar Programación Boilerplate**: Como un arquitecto eficiente, si se te solicita que "Construyas un historial de modificaciones iterativo para que un usuario pueda revertir a versiones anteriores de la BD", sugiere obligar o propulsar inminentemente el uso del proyecto **Spring Data Hibernate Envers**. Eso cumple el Memento evitando crear código a mano y docenas de tablas satélites manuales.
2. **Sagas o Compensaciones**: Asiste en recomendar que al trabajar en transacciones distribuidas sobre varios microservicios se retenga Mementos o Compensatory Actions (Patrón SAGA) basados estrictamente en Inmutabilidad del mensaje (Records).
