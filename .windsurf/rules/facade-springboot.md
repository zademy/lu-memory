---
trigger: always_on
---

# Rule: Facade Pattern en Spring Boot

## Descripción

La regla asiste en la creación de una capa superficial organizativa aplicando el patrón **Facade (Fachada)**. Funciona dotando de una interfaz simplificada, de alto nivel y cohesiva a subsistemas excesivamente complejos, clases enmarañadas y APIs con microservicios. En **Spring Boot**, una interfaz Facade toma la forma de un `@Service` Maestro Orquestador que oculta toda la pesada y fea complejidad estructural y comunicativa para exponer llamadas simplista a un cliente de capa de presentación o Controlador REST.

## Analogía del Mundo Real

**Analogía:** Hacer un pedido por teléfono en una tienda. El operador telefónico es tu "fachada". No necesitas navegar por el almacén o la pasarela de pago tú mismo; el operador proporciona una interfaz única y sencilla para todos los departamentos.

## Cuándo aplicar

- Al exponer una API limpia y fácil de consumir desde un `Controller` que carezca de dependencias enmarañadas de bajo nivel.
- Cuando una interacción de usuario requiere consultar más de 3 servicios interdependientes en un orden muy estricto obligatoriamente transaccional.
- Para proveer una envoltura digerible al envolver y llamar librerías/bibliotecas externas con flujos enormes y obligatorios de setup.

## Cómo aplicar en Spring Boot

En Spring Boot las fachadas son componentes de capa de servicios `@Service` que agregan llamadas pero sin aplicar lógicas del negocio particulares sobre los datos más que enrumbarlos u orquestarlos.

1. La capa superior (e.g. `@RestController`) dependerá única y estrechamente de la "Fachada" en lugar de inyectar los Múltiples servicios de dominio.
2. Anotar el uso de Fachadas transaccionales (`@Transactional`) para agrupar operativas en base de datos.

## Principios SOLID / Clean Code Promovidos

1. **Ley de Demeter (Least Knowledge)**: Protege a los clientes (`Controllers`, eventos) que usan la API previniéndolos de interactuar con componentes muy remotos del modelo de dominio del sistema. Solo conversan a su puerta cercana (la Fachada).
2. **Single Responsibility Principle (SRP)**: Abstracción a nivel orquestación. Los servicios continúan con algoritmos, la fachada con ruteos logísticos sin intervenir.

## Ejemplo Guía

```java
// Entidades muy ruidosas
@Service public class InventoryService { public boolean check(long pId) { return true; } }
@Service public class LedgerService { public void debitAmount(double cst) { } }
@Service public class ShipmentService { public String initShip(long pId) { return "OK"; } }

// ¡LA FACHADA! - Simplificación total de subsistemas acoplados para el Consumidor
@Service
public class OrderFulfillmentFacade {

    private final InventoryService inventory;
    private final LedgerService ledger;
    private final ShipmentService shipment;

    @Autowired
    public OrderFulfillmentFacade(InventoryService i, LedgerService l, ShipmentService s) {
        this.inventory = i;
        this.ledger = l;
        this.shipment = s;
    }

    // Método Simplificado que orquesta la transacción sin exponer detalles feos a los Controllers
    @Transactional
    public String checkoutProcess(long productId, double price) {
        if (!inventory.check(productId)) { throw new RuntimeException("No Inventory"); }
        ledger.debitAmount(price);
        return shipment.initShip(productId);
    }
}
```

## Directrices para el Asistente AI

1. **Orquestación Limpia**: Si al inicio del diseño detectas que el usuario en un `@RestController` planea autoinyectarse (`@Autowired`) cuatro o cinco Repositorios, clientes REST y dos Servicios, objeta deteniendo ese antipatrón y exígele usar el Patrón **Facade**. El mapeo general deberá enrutarse fuera del alcance de la Web Layer.
2. **Definición de Límites Transaccionales**: Promueve enérgicamente colocar `@Transactional` en las Fachadas de Spring cuando el motor involucre a más de dos llamadas de estado sobre Bases de Datos distintas para evitar inconstencias frente a caídas temporales del servidor de aplicación.
