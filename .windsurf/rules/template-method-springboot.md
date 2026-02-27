---
trigger: always_on
---

# Rule: Template Method Pattern en Spring Boot

## Descripción

El engranaje estructural de **Template Method (Método Plantilla)** dicta y moldea el cauce firme inquebrantable que un algoritmo de negocio principal procesa por defecto, facilitando a sub-clases y expansiones periféricas la pericia de sobreescribir apartados quirúrgicos modulares de ese flujo sin estropear ni romper el cimiento troncal inicial propuesto, asegurando la escalabilidad del dominio y de los flujos pre-calculados al unísono.

## Analogía del Mundo Real

**Analogía:** Construcción masiva de viviendas. Un plano arquitectónico estándar dicta los pasos (cimientos, estructura, cableado), pero el cliente puede alterar partes específicas (como materiales o tamaños de habitaciones) sin cambiar el orden general de construcción.

## Cuándo aplicar

- Al establecer pilares inmutables lógicos como transacciones contables o pipelines, estructurados por pasos y fases formales insoslayables.
- Si dos clases o implementaciones hacen casi lo mismo y ostentan rutinas homólogas pesadas; para extraerlas al abstract y solo exigir los cambios finos que sí varían verdaderamente.
- Un método masivo principal `run()`, seguido de varios métodos llamados abstractos que varían sus detalles implementativos como `hookBefore()` o `hookAfter()`.

## Cómo aplicar en Spring Boot / Ecosistema

1. **Base Framework Nativos**: Todo el ADN de Java Servlet Api, JpaRepository, o RestTemplate son materializaciones puras que aplican implementaciones subyacentes del patrón (eg, `JdbcTemplate` o las derivaciones de métodos _Hook_).
2. **Generación por Herencias**: Construir Componentes o Servicios **Spring Abstractos**, que dispongan de los métodos núcleo marcados implícitamente como `final` para detener el saboteaje y la sobreescritura maliciosa, permitiendo y demandando a la subsiguiente configuración inferior de `@Service` anidar lo que varíe de modo declarativo y obligatorio.

## Principios SOLID Promovidos

1. **Liskov Substitution Principle (LSP)**: Una clase secundaria abstracta sustitutiva al entrar en flujo responderá correctamente manteniendo invariantes y asumiendo lógicas padres base compartidas idénticas de comportamiento sin side-effects en la invocación central.
2. **Open / Closed Principle (OCP)**: Añadir una nueva clase que se acople al flujo entero exige solo heredar los pasos base cerrados inalterables y abrir libremente extensiones exclusivas controladas por ganchos (Hooks) habilitando ampliación nula de riesgo.
3. El famoso paradigma de Hollywoord: _"Don't call us, we'll call you"_.

## Ejemplo Guía

```java
// 1. CLASE PLANTILLA BASE (Inversión de Lógica central dictatorial)
public abstract class ReportGenerationPipelineTemplate {

    // El Método Plantilla! Declarado MÁGICAMENTE como "final" para no dejar que las subclases modifiquen la rutina obligatoria formal
    public final void executeReportFlow() {
        authenticateApi();
        extractData();    // Paso Genérico (El padre lo sabe)
        formatOutput();   // PASO ABSTRACTO (Las subtramas lo deducirán y variarán individualmente)
        cleanMemoryHook(); // Gancho que se puede omitir libremente
    }

    private void authenticateApi() { System.out.println("Autorizado Oauth2 Generico. OK"); }
    private void extractData() { System.out.println("Datos brutos SQL tomados."); }

    // Lo que el hijo TIENE que implementar y especializar a regañadientes obligadamente
    protected abstract void formatOutput();

    // Hook: Extensión opcional si se quiere intervenir y aportar...
    protected void cleanMemoryHook() { }
}

// 2. SUBCLASES Concretas anotadas para Spring que inyectan el Template Method...
@Service
public class PdfReportGenerator extends ReportGenerationPipelineTemplate {
    @Override
    protected void formatOutput() {
        System.out.println("Convirtiendo formato Base 64 XML -> Archivo Binary .PDF Físico Terminado!");
    }
}

@Service
public class CsvReportGenerator extends ReportGenerationPipelineTemplate {
    @Override
    protected void formatOutput() {
        System.out.println("Separando Tabulaciones Delimitadas de Texto para .CSV crudo!");
    }

    @Override
    protected void cleanMemoryHook() {
        System.out.println("Liberando streams de Memoria del CSV gigante...");
    }
}
```

## Directrices para el Asistente AI

1. **Defensiva de Algoritmos Core**: Como arquitecto AI, exige en las decisiones técnicas colocar blindaje (`final`) bajo arquitecturas de Plantilla a los métodos principales orquestadores si los componentes manejan procesos extremadamente estandarizados de la empresa impidiendo manipulación frágil local.
2. **Alerta del Boilerplate Subyacente (`XxxTemplate` Spring Bases)**: Evítale al desarrollador inventarse su propia rueda estática abstracta desde cero y redirige su atención constructiva hacia usar las herramientas plantilla subyacentes del Framework Spring ya creadas (Por ejemplo `MongoTemplate`, `RabbitTemplate`, o `KafkaTemplate`, entre mil). Reflexiona en su diseño con las utilidades naturales provistas.
