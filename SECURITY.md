# Política de Seguridad

## Versiones Soportadas

A continuación se muestran las versiones de `lu-memory` que actualmente reciben actualizaciones de seguridad.

| Versión | Soporte            | Notas                        |
| ------- | ------------------ | ---------------------------- |
| 0.0.1.x | :white_check_mark: | Versión en desarrollo activo |
| < 0.0.1 | :x:                | Sin soporte                  |

## Consideraciones de Seguridad del Proyecto

Dado que `lu-memory` es un servidor MCP (Model Context Protocol), interactúa de manera local con clientes y agentes de Inteligencia Artificial. Es fundamental en la arquitectura del sistema contemplar las siguientes áreas:

- **Almacenamiento Local Seguro**: `lu-memory` utiliza una base de datos local SQLite (`lu-memory.db`) para persistir la información. Para proteger la confidencialidad de los prompts, historiales de sesión y contexto arquitectónico de los proyectos, asegúrate de aplicar permisos estrictos a nivel del sistema operativo. Considera el uso de cifrado en reposo para volúmenes sensibles.
- **Comunicación MCP (stdio)**: Se apoya en la entrada/salida estándar (`stdio`) para el protocolo. La seguridad del servidor depende en gran medida de que el cliente (ej. Claude Desktop, windsurf, u otros) se ejecute en un entorno confiable, limitando quién puede invocar dicho proceso.
- **Entorno de Ejecución**: El proyecto funciona sobre **Java 25** y **Spring Boot**. Como buena práctica, mantén siempre actualizadas las dependencias definidas en tu `pom.xml` para mitigar vulnerabilidades de la cadena de suministro, siguiendo el principio de _Security by Design_.

## Reportar una Vulnerabilidad

Tomamos la seguridad de `lu-memory` muy en serio. Si descubres una vulnerabilidad de seguridad, arquitectura o falla conceptual en este proyecto, te pedimos que la reportes de manera privada. **No la divulgues públicamente** (por ejemplo, creando un Issue público) hasta que se haya evaluado e implementado una solución.

Por favor, reporta cualquier vulnerabilidad:

- A través de la función de **Aviso de Seguridad Privado (Private Vulnerability Reporting)** en GitHub de este repositorio.
- O contactando al equipo directo de mantenimiento si tienes los correos habilitados.

Al crear el reporte, por favor incluye:

1. Una descripción detallada de la vulnerabilidad o falla de diseño de seguridad.
2. Pasos concretos para reproducir el problema.
3. Una evaluación del posible impacto en arquitecturas que utilicen este MCP.

El equipo se compromete a revisar el caso a la brevedad y proporcionar próximos pasos antes de cualquier divulgación.
