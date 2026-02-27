---
trigger: always_on
---

# Rule: Strategy Pattern en Spring Boot

## Descripción

El patrón **Strategy (Estrategia)** impulsa declarar una base formal de algoritmos operables análogamente, blindándolos independientemente y logrando hacerlos reemplazables entre sí programáticamente en tiempo real dependiente del contexto imperante del negocio. Adaptado a **Spring Boot**, la regla exige exprimir la poderosa sinergia de **Strategy + Inversión de Control**, auto inyectando todas las resoluciones a una tabla de Hash (Map) donde se instancian condicional e instantáneamente.

## Analogía del Mundo Real

**Analogía:** Ir al aeropuerto. Puedes ir en bicicleta, tomar un autobús o pedir un taxi. Todas estas son diferentes estrategias de transporte para lograr el mismo objetivo, elegidas dinámicamente según el presupuesto o las limitaciones de tiempo.

## Cuándo aplicar

- Al momento que la algoritmia o mecánica del dominio cambie fuertemente con un identificador, por ejemplo: Sistema de Rutas para mapas (Caminando, Auto, Metro), o Sistemas de Cálculo Taxista (Normal, Nocturno, Fronterizo).
- Alternativa por antonomasia a `switch` repetitivos, aliviando drásticamente refactorizaciones pesadas para cada condicional que surja en un futuro.

## Cómo aplicar en Spring Boot

Muy relacionado a cómo explicamos _Abstract Factory Method_, la Estrategia se consuma utilizando Beans dinámicos:

1. Define un Componente Central (`StrategyRegistry` o `StrategyContext`) inyectable como Servicio.
2. Recibir vía Autowired Inyección todas las clases anotadas con `@Service`/`@Component` que comparten e instrumentan idénticamente a la Interfaz Strategy Principal dentro de una estructura `List<Strategy>`.
3. Mapear y llamar en **O(1)** desde un diccionario a la estrategia requerida eludiendo IFs.

## Principios SOLID Promovidos

1. **Open/Closed Principle (OCP)**: Añadir una Nueva variante/Estrategia solamente equivale a sumar un nuevo y solitario Componente Bean Spring que la incorpore al ecosistema; la clase principal invocadora NO se modificara, cumpliéndose a cabalidad el paradigma Abierto.
2. **Dependency Inversion (DIP)**: Todos confían ciegamente hacia una Interface inalterable.

## Ejemplo Guía

```java
// 1. Interfaces a ejecutar (La Estrategia Abstracta)
public interface ShippingPricingStrategy {
    String getMode();
    double computeCosts(double distanceKm);
}

// 2. Estrategias Concretas de algoritmos totalmente distintos y aislados
@Service
public class StandardShipping implements ShippingPricingStrategy {
    public String getMode() { return "STANDARD"; }
    public double computeCosts(double km) { return km * 5.0; } // Costo Plano
}

@Service
public class ExpressAereoShipping implements ShippingPricingStrategy {
    public String getMode() { return "EXPRESS_AIR"; }
    public double computeCosts(double km) { return km * 25.0 + 1000; } // Sobrecargo tarifa
}

// 3. El Contexto que Orquesta Dinámicamente - Autowired List -> Map Conversion
@Service
public class ShippingContextManager {
    private final Map<String, ShippingPricingStrategy> strategies;

    // Al Inyectar el framework te suministra una lista con el Standard y el Express
    @Autowired
    public ShippingContextManager(List<ShippingPricingStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(ShippingPricingStrategy::getMode, s -> s));
    }

    // Selección Dinámica y Ejecución
    public double quoteShipping(String shipmentRouteMode, double dist) {
        ShippingPricingStrategy targetStrategy = strategies.get(shipmentRouteMode);
        if(targetStrategy == null) throw new IllegalArgumentException("Ruta Inválida");

        // ¡Se ejecuta limpiamente sin Switches o IF-Elses interminables!
        return targetStrategy.computeCosts(dist);
    }
}
```

## Directrices para el Asistente AI

1. **Agresividad contra Condicionales Lógicos**: Adquiere el hábito proactivo de transformar todo acercamiento condicional o _if/else_ en cascada basado en la tipología de un Objeto (por ejemplo if user es VIP entonces...) en un esquema puro **Strategy**. Propón activamente esta conversión del código usando Maps auto-inyectables en Colecciones `List<T>`.
2. **Unificación Factory-Strategy**: Demuestra entendimiento superior de patrones advirtiendo al programador que en Spring Boot los mecanismos nativos y la configuración por dependencias logran difuminar positivamente la brecha entre Strategy y Factory en implementaciones de Orquestadores Contextuales en O(1) tiempo de resolución.
