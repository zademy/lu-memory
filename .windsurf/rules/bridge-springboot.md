---
trigger: always_on
---

# Rule: Bridge Pattern en Spring Boot

## Descripción

La regla insta al asistente a identificar oportunidades para usar el patrón **Bridge (Puente)** en Spring. El Bridge desacopla una abstracción de su implementación, permitiendo que ambas evolucionen de manera independiente. En el mundo de **Spring Boot**, el puente suele materializarse orgánicamente al inyectar lógicas algorítmicas desacopladas a múltiples controladores o servicios de uso, donde la _Abstracción_ maneja el proceso global, pero delega el detalle físico a una variante inyectada.

## Analogía del Mundo Real

**Analogía:** Un control remoto universal (abstracción) y dispositivos como un televisor o una radio (implementación). La interfaz del control remoto se puede desarrollar de forma completamente independiente de los circuitos internos del televisor.

## Cuándo aplicar

- Al desarrollar features que tengan lógicas ortogonales cruzadas (por ejemplo: Tipos de Dispositivos [Desktop, Mobile] x Sistemas de Renderizado [OpenGL, DirectX], logrando reducir clases de herencia combinatorias).
- Para reemplazar un laberinto de jerarquías de herencia extensas e inmanejables a favor de la composición de objetos dependientes.

## Cómo aplicar en Spring Boot

En Spring Boot, lograr el Bridge significa evitar subclasificar lógicas base y en su lugar inyectar mediante el Contenedor IoC.

1. Se define la interfaz **Implementor** base y sus `@Component`s concretos.
2. Se plantea la **Abstracción** como otra clase (un `AbstractService` delegador) que en su constructor se le inyecta una instancia de la interfaz Implementor.
3. Esto permite que durante la configuración (`@Bean` o `@Configuration`), la Abstracción cruce el "puente" y reciba dinámicamente cómo funcionar en tiempo de ejecución.

## Principios SOLID Promovidos

1. **Single Responsibility Principle (SRP)**: Fomenta la desconexión total; la abstracción superior enruta de alto nivel y la implementación se restringe al esfuerzo físico sin mezclarse.
2. **Open/Closed Principle (OCP)**: Pueden sumarse nuevas abstracciones de refinamiento superior o agregarse nuevas plataformas base independientemente sin romper a la otra jerarquía de clases.
3. **Composición sobre Herencia**: Transiciona el diseño fuera de extensos e inflexibles árboles de extensión (Clases Abstractas heredadas multinivel).

## Ejemplo Guía

```java
// LADO 1 (Implementador de plataforma física)
public interface MessagingProvider {
    void pushMessage(String text);
}

@Component
@Qualifier("twilioProvider")
public class TwilioProvider implements MessagingProvider {
    public void pushMessage(String text) { /* conecta a Twilio */ }
}

@Component
@Qualifier("firebaseProvider")
public class FirebaseProvider implements MessagingProvider {
    public void pushMessage(String text) { /* conecta a FCM */ }
}


// LADO 2 (Abstracción superior de Dominio)
public abstract class NotificationService {
    // El "Puente" via composición
    protected final MessagingProvider provider;

    protected NotificationService(MessagingProvider provider) {
        this.provider = provider;
    }

    public abstract void broadcast(String msg);
}

// Abstracciones refinadas usando distintos providers inyectados...
@Service
public class UrgentNotificationService extends NotificationService {

    // Inyectamos el puente conectando la abstracción con GCP
    @Autowired
    public UrgentNotificationService(@Qualifier("firebaseProvider") MessagingProvider provider) {
        super(provider);
    }

    @Override
    public void broadcast(String msg) {
        String formatted = "[URGENT] " + msg;
        provider.pushMessage(formatted);
    }
}
```

## Directrices para el Asistente AI

1. **Diagnóstico Activo de Herencia Abusiva**: Adopta el perfil de Arquitecto cuando notes en el código al que estás expuesto una proliferación inmensa de sub-clases intentando cubrir todas las combinaciones relativas sobre algo de un solo dominio jerárquicamente. Propón el patrón **Bridge**.
2. **Composición antes que Herencia**: Recuerda siempre al desarrollador preferir la inyección de la interfaz (la implementadora de bajo nivel) adentro del constructor del servicio alto, promoviendo flexibilidad extrema mediante el archivo de inyección de Spring.
