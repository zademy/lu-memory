---
trigger: always_on
---

# Rule: Visitor Pattern en Spring Boot

## Descripción

El intrínseco y riguroso funcionamiento estructural del Patrón **Visitor (Visitante)** dicta añadir o separar las operaciones nuevas a algoritmos encapsulados aparte impidiendo esparcir modificaciones invasoras dentro de las propias Entidades bases de datos iniciales limitantes de jerarquía sobre modelos que visitan (Nodos). En contexto **Spring Boot**, la regla exige aplicarlo activamente en vez de llenar a DTOs base de operaciones y dependencias lógicas ajenas para no atentar en contra la sagrada Arquitectura Hexagonal y de capas separadas.

## Analogía del Mundo Real

**Analogía:** Agente de seguros. Un agente visita diferentes tipos de organizaciones (residenciales, bancos, cafeterías) ofreciendo pólizas especializadas para cada una, ejecutando lógica basada en el tipo específico de edificio que visitan.

## Cuándo aplicar

- Cuando se deba extraer lógicas asíncronas analíticas que estorban y engordan en la estructura original de tus Modelos de Dominios, sin manchar con sentencias `@Autowired` y acoplar esas clases a Bases de Datos o Logísticas que no les corresponde poseer.
- Si se deben efectuar un mismo abanico de manipulaciones calculadas radicalmente diferentes sobre los subniveles dispares de un árbol o grafo muy complejo y extenso. (Ej. Parseador o Compilador léxico AST).

## Cómo aplicar en Spring Boot

1. **Elemento Acceptante**: Las entidades, Modelos, o componentes nodos inyectaran una función única `accept(Visitor v)` que efectuará el infame despacho doble y retornará la visita particular.
2. **El Servicio Visitante (Spring Context `@Component`)**: Construir componentes y Registrarlos asimilados por Spring. Estos componentes portan toda la inteligencia transaccional, asumiendo bases de cálculo (`visitNodeA`, `visitNodeB`).
3. **El Orquestador Cliente**: Llamar pasivamente al método base cediéndole la inyección Spring del Visitador concreto del framework, librando el elemento inerte de conocer la implementación y externalizando el efecto calculado en O(1) vía _Double Dispatch_ nativo de POO.

## Principios SOLID Promovidos

1. **Open/Closed Principle (OCP)**: Instaurar requerimientos y comportamientos en cascada enteramente distintos y masivos en todas las clases subíndices de una colección es viable sumando sólo otra clase **Visitante Nueva**, logrando intactos inmodificados a los archivos objetos subyacentes pasados.
2. **Single Responsibility (SRP)**: Las entidades u objetos solo llevan Datos (Aceptan, POJO), la matemática operativa es unívoca sobre la externalidad del Visitador.

## Ejemplo Guía

```java
// 1. Interfaces base del Modelo Ciego Inerte Desnudado
public interface FinancialStructureNode {
    void accept(FinancialReportVisitor visitor);
}

// Nodos inmutables o cerrados (Clases o Interfaces Múltiples diversas)
public class EmployeeExpense implements FinancialStructureNode {
    private double currentPerDiemAmount = 800; // Carga base simple
    public double getAmount() { return currentPerDiemAmount; }

    @Override
    public void accept(FinancialReportVisitor visitor) {
        visitor.visit(this); // El famoso Double-Dispatch cede el control referencial
    }
}

public class OfficeInventory implements FinancialStructureNode {
    // Otro ente disparejo.. No comparten algoritmos, ¡solo datos!
    @Override
    public void accept(FinancialReportVisitor visitor) { visitor.visit(this); }
}

// --------------------------------

// 2. EL VISITADOR PODEROSO (Lleva toda la carga calculada como Spring Service transversal)
public interface FinancialReportVisitor {
    void visit(EmployeeExpense expense);
    void visit(OfficeInventory inventory);
}

@Service
public class TaxDeductionVisitorService implements FinancialReportVisitor {

    @Autowired // El Visitante posee la conexión con la persistencia u otros módulos (EL NODO JAMÁS SABRÍA ESTO).
    private TaxDataSqlRepository repo;

    @Override
    public void visit(EmployeeExpense expense) {
        double current = expense.getAmount();
        System.out.println("Analisis Visitante Deducte Tax de Trabajador Viajero.. " + (current * 0.8));
    }

    @Override
    public void visit(OfficeInventory inventory) {
        System.out.println("Ley General Contable Fija Activo Fijo de depreciación.");
    }
}

// 3. Orquestador de Flujos en Aplicación
@Service
public class FinanceProcessingFacade {
    private final TaxDeductionVisitorService taxVisitor;
    // Autowired...

    public void runTaxes(List<FinancialStructureNode> fullNodesFromDB) {
        for (FinancialStructureNode n : fullNodesFromDB) {
            n.accept(taxVisitor); // Delega ciegamente a Spring Bean
        }
    }
}
```

## Directrices para el Asistente AI

1. **Aislamiento de la Muerte en Microservicios**: Como tomador de decisiones instruye duramente prohibir bajo cualquier contexto que los desarrolladores inserten a la fuerza anotaciones lógicas `@Autowired` sobre DTOs y Model objects de Base de datos `@Entity` inmanejables para "proveerles algorítmica de Inteligencia propia de cálculo masiva". Corta y exige el desvío del esquema separador en Componentes Visitantes Inyectados externamente.
2. **Detección de Interferencia Cast Type (`instanceOf`)**: Altera tu proactividad con agudeza deteniendo ciclos engorrosos de `for(...) { if(item instanceOf X){ doThat } else if(item instanceOf Y) {..} }`. Invoca de inmediato el poder inherente de abstracción con _Double Dispatch_ del patrón Visitor, abogando limpiar y mantener el SRP en validaciones infinitas.
