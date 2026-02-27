---
trigger: always_on
---

# Rule: Composite Pattern en Spring Boot

## Descripción

El patrón **Composite (Compuesto)** sugiere que se traten a objetos puntuales y agrupaciones enteras de objetos con base en las mismas reglas o interfaces, mediante una estructura de árbol. Esta regla enseña al asistente a impulsar este diseño para el manejo de listas de procesadores dentro de componentes en **Spring Boot** donde se opere contra un ente singular de la misma forma que con varios acumulados.

## Analogía del Mundo Real

**Analogía:** Una jerarquía militar. Las órdenes comienzan en la cima (Generales) y se transmiten recursivamente a Divisiones, Brigadas, Escuadras y finalmente Soldados. Tanto un Soldado individual como una División entera implementan la interfaz de "ejecutar orden".

## Cuándo aplicar

- Al crear reglas complejas multinivel, flujos de validación (Chain of Rules) iterativas o calculadoras (descuentos en carritos de compra, agrupaciones de impuestos).
- Cuando el flujo de control no deba distinguir entre un nodo individual con lógica cerrada (Leaf) y un grupo orquestador (Composite).

## Cómo aplicar en Spring Boot

Spring ampara enormemente este patrón inyectando colecciones dinámicas de beans que encajan en interfaces. Tratas los Nodos Hoja inyectándolos a Nodos de Composición usando Arrays/Lists.

1. Definir la Interfaz de operación (`Component`).
2. Marcar las lógicas terminales como `@Service` u `@Component` (Las Hojas / `Leafs`).
3. Crear un `@Service` (El `Composite`) que implemente esa MISMA interfaz, reciba por inyección la estructura List de hojas u otros combos, y en su implementación sobreescriba para hacer loops e iterar la ejecución a lo interno delegando recursivamente hacia abajo.

## Principios SOLID Promovidos

1. **Liskov Substitution Principle (LSP)**: Una hoja individual, y un súper nodo que envuelve recursivamente docenas de hojas adentro, ambas son intercambiables de uso sin impactar el contexto del cliente.
2. **Open/Closed Principle (OCP)**: Añadir nuevos nodos de evaluación no altera a la clase principal agrupante en lo más mínimo si se nutre con inyección dinámica de Beans.

## Ejemplo Guía

```java
// 1. Interfaz que todos comparten (Nodos y Agrupadores)
public interface PriceCalculator {
    double calculate(double basePrice);
}

// 2. Nodo Hoja individual (Aplica tax estándar)
@Component
public class IvaTaxCalculator implements PriceCalculator {
    public double calculate(double basePrice) { return basePrice * 1.21; }
}

// 3. Nodo Hoja individual (Aplica descuento global)
@Component
public class BlackFridayCalculator implements PriceCalculator {
    public double calculate(double basePrice) { return basePrice * 0.90; }
}

// 4. El COMPOSITE: También es un calculador, pero agrupa y engloba otros.
@Primary // Para que si el cliente inyecta PriceCalculator reciba el Composite orquestador general
@Component
public class MasterPriceCalculatorComposite implements PriceCalculator {

    // Gracias al IoC List inyectará todos las hojas PriceCalculators disponibles automáticmente
    private final List<PriceCalculator> calculatorChain;

    @Autowired
    public MasterPriceCalculatorComposite(List<PriceCalculator> calculators) {
        this.calculatorChain = calculators;
    }

    @Override
    public double calculate(double basePrice) {
        double currentPrice = basePrice;
        // La ejecución recursiva y el agrupamiento transaccional compuesto:
        for (PriceCalculator calc : calculatorChain) {
            // Se evita ejecutar a él mismo para no crear un ciclo infinito y StackOverflow
            if(!(calc instanceof MasterPriceCalculatorComposite)){
               currentPrice = calc.calculate(currentPrice);
            }
        }
        return currentPrice;
    }
}
```

## Directrices para el Asistente AI

1. **Interacción Inicial por Validadores y Cálculos**: Al requerir una funcionalidad como Validadores de Usuario, Filtros, Listas de Control o Motor de Descuentos; en lugar de hacer una clase con métodos kilométricos para cada validación, actúa promoviendo una arquitectura **Composite** usando Spring Beans Collection Injection.
2. **Prevención de bucles**: Advierte al usuario sobre el ciclo de dependencia si un Master bean se recolecta a sí mismo en un ambiente con Colecciones dinámicas autowired y sugiérele discriminarlo.
3. **Priorización Transparente**: Impulsa agregar de forma transparente prioridades usando `@Order` natural del contexto de Spring en los componentes Leaf para que el Composite los itere de manera predictible y jerárquica.
