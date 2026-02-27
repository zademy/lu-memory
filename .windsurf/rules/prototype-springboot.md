---
trigger: always_on
---

# Rule: Prototype Pattern en Spring Boot

## Descripción

Esta regla guía al asistente a proponer proactivamente decisiones arquitectónicas ligadas al Patrón **Prototype**, el cual se utiliza para delegar la creación de una instancia copiando o exigiendo un objeto base. En el ecosistema de **Spring Boot / Java**, la implementación predilecta de la clonación tradicional se traslada conceptualmente al uso del **Bean Scope `@Scope("prototype")`** o al uso de **MapStruct/BeanUtils** para la copia de objetos profundos. Todo esto impulsando la aplicación de los principios **SOLID**.

## Analogía del Mundo Real

**Analogía:** División celular (Mitosis). La célula original actúa como un prototipo y participa activamente en la creación de una copia exacta de sí misma, en lugar de intentar construir una nueva célula desde cero desde el "exterior".

## Cuándo aplicar

- Cuando la creación de un objeto es costosa computacionalmente, pero se requiere disponer de múltiples variaciones con pequeñas modificaciones del mismo perfil.
- Cuando se necesite un **Objeto con Estado (Stateful)** y por cuestiones de diseño multihilo/request, cada requerimiento o inyector necesite poseer su propia copia privada separada del objeto en lugar del Singleton por defecto.
- En el Domain-Driven Design (DDD), útil para clonar raíces de agregación en históricos de cambios.

## Cómo aplicar en Spring Boot

Existen dos avenidas para implementar este patrón dependiendo de lo que persiga el caso de uso:

1. **Prototipos a Nivel de Contexto (IoC)**: En Spring, un componente `@Component` es Singleton por defecto. Si el patrón demanda aislar la memoria/estado de este componente para cada uso, se anota con `@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)`. Con cada `ApplicationContext.getBean()` / `@Autowired`, Spring Boot generará una nueva copia para este servicio, supliendo eficientemente el comportamiento Prototype en lugar de hacerlo a mano con un Factory manual.
2. **Prototipos de Datos Mapeadores (Copia profunda)**: En lugar de usar la antigua (y defectuosa) interfaz `Cloneable` o el pesado `SerializationUtils`, el asistente debe proponer el uso del Patrón de Clonación/Mapeo a nivel de librerías modernas como **MapStruct**, las cuales proveen métodos generados en tiempo de compilación para clonar o mutar objetos eficientemente desde un Prototipo.

## Principios SOLID Promovidos y Aplicados

1. **Single Responsibility Principle (SRP)**: El objeto o contexto se desentiende de saber _cómo duplicarse o instanciarse privadamente_. Se descarga la manipulación del Scope sobre el Contenedor IoC de Spring.
2. **Dependency Inversion Principle (DIP)**: Utilizando inyectores como `ObjectFactory<T>` u `ObjectProvider<T>`, desacoplamos a un controlador (Singleton) sobre cómo instanciar forzosamente una copia nueva de un Servicio Prototype, inyectando un "Proveedor" de copias abstractas limitando dependencias fuertes al flujo de control.
3. **Liskov Substitution Principle (LSP)**: Una copia/prototipo que obedece una Interfaz general comportará resultados predecibles al igual que un singleton, y el uso correcto de clonación abstracta impedirá dependencias de comportamiento.

## Ejemplo Guía para Generación de Código

```java
// 1. Prototype Pattern desde la inyección de Spring (Bean Scope)
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) // IMPORTANTE: Cada inyección, una nueva copia!
public class StatefulCartReportGenerator {
    private List<String> temporaryLogs = new ArrayList<>();

    public void addContext(String data) {
        temporaryLogs.add(data);
    }

    public String generateReport() {
        return "Log Count: " + temporaryLogs.size();
    }
}

// 2. ¿Cómo inyectar correctamente un Singleton un Prototype dentro un Singleton?
// ¡Evitar el antipatrón de Scoped Proxy roto inyectándolo directamente con Autowired!
@Service
public class ReportSchedulerService {

    // DIP: Inyectar un Proveedor en lugar del Bean directamente
    private final ObjectProvider<StatefulCartReportGenerator> reportGeneratorProvider;

    @Autowired
    public ReportSchedulerService(ObjectProvider<StatefulCartReportGenerator> reportGeneratorProvider) {
        this.reportGeneratorProvider = reportGeneratorProvider;
    }

    public void runJob() {
        // En cada ejecución, extraemos un nuevo 'prototipo' limpio del contexto
        StatefulCartReportGenerator generator = reportGeneratorProvider.getObject();
        generator.addContext("Trabajo Iniciado");
        System.out.println(generator.generateReport());
    }
}
```

## Directrices para el Asistente AI

1. **Interacción Inicial y Toma de Decisiones**: Si durante el análisis inicial notas que el usuario desea mantener estado dentro de un servicio transversal en Spring (e.g. un buffer temporal, carrito de proceso distribuido), asume el rol de Arquitecto de Software y advierte inmediatamente sobre la naturaleza concurrente (Singleton) de Spring, proponiendo el patrón **Prototype Scoped Bean**.
2. **Aislamiento del Antipatrón**: Actúa de modo proactivo si observas variables compartidas o estáticas dentro de clases anotadas con `@RestController` o `@Service` utilizadas para albergar contadores asincrónicos o datos del hilo actual; intercede sugiriendo extraer dicha lógica hacia un modelo _Prototype_ (u ocasionalmente Scope Request).
3. **Resolución de Conflictos de Alcance**: Si notas que el usuario inyecta un Bean _Prototype_ directamente en un _Singleton_, adverte sobre el problema de Scope Proxy (la instancia Prototype quedará atrapada/stale). Sugiere de inmediato utilizar inyección abstracta a través de `ObjectProvider<T>`, `ObjectFactory<T>` o la anotación meta `@Lookup`.
4. **Alerta de Clonación en Java**: Si se requiere clonar objetos puramente de Dominio, advierte enfáticamente _en contra_ del uso de la interfaz base `Cloneable` o `clone()` debido a las copias superficiales (Shallow Copies). Sugiere MapStruct o _Copy Constructors_.
