---
trigger: always_on
---

Use `mem_context` **siempre al inicio de la sesión** (y después de cualquier reset de contexto) para recuperar el estado de trabajo.
You have access to long‑term memory via `lu-memory` MCP tools (mem\_\*).

## Core Principles

1. **Context first**
   - Inicial de sesión o tras un reset: llama a `mem_context` antes de tomar decisiones importantes.
   - Para profundizar en un tema: usa el patrón de 3 capas (ver más abajo).
2. **Save proactively**  
   Usa `mem_save` sin esperar a que el usuario lo pida cuando:
   - Se cierre un tema importante.
   - Se llegue a una decisión de arquitectura o diseño.
   - Se descubra una causa raíz o un aprendizaje clave.
   - Se cambie una configuración importante o un flujo de trabajo.
3. **Topic keys para temas que evolucionan**
   - Usa `mem_suggest_topic_key` para temas que tendrán muchas revisiones (arquitectura, features grandes, proyectos largos).
   - Reutiliza siempre el mismo `topic_key` para ese tema.
4. **Sesiones explícitas (Tabla `memory_sessions`)**
   - Abre sesiones con `mem_session_start`. **Importante:** Incluye el `agentName` (ej. "Windsurf") y el `branchName` si aplica.
   - Antes de cerrar, guarda el resumen con `mem_session_summary`.
   - Cierra siempre con `mem_session_end`, indicando el `status` correcto (`COMPLETED`, `ABORTED` o `FAILED`).

### Convención de Formato para Memorias Extendidas

Cuando uses `mem_save` para registrar una decisión, patrón complejo o diseño de arquitectura, DEBES estructurar el parámetro `content` en formato Markdown usando la siguiente taxonomía (basada en el OpenCode Gentleman Agent):

**What**: [Resumen muy abreviado de la memoria, decisión o código]
**Why**: [El por qué de esta arquitectura/estado. Los problemas que resuelve y metas.]
**Where**: [Dónde vive (rutas de código, archivos, servicios alterados)]
**Key Details**:

- [Punto detallado importante 1...]
- [Punto detallado importante 2...]
  **Learned**: [Lecciones clave aprendidas para recordar a futuro]

- Usa siempre un **`type` válido**: `DECISION`, `BUGFIX`, `PATTERN`, `NOTE`, `ARCHITECTURE`, `SUMMARY`, o `DOCUMENTATION`.
- Usa el argumento **`tags`** con palabras clave separadas por comas (ej. "frontend,react,auth") para mejorar la búsqueda.
- Utiliza siempre **`projectName`** apuntando al repositorio o dominio que corresponde ("cpancode", "lu-memory", "app-frontend", etc.).
- Incluye el **`sessionId`** actual y el **`topicKey`** correspondiente para mantener la trazabilidad.
- Si el usuario lo pide explícitamente, puedes pasar "manual-save" en el parámetro _source_ o _sessionId_.
- Especifica el **`scope`** ("project" o "personal") según corresponda.

---

## Recommended Workflow

### 1. Start / Resume Work

- Al principio de la sesión:
  - `mem_session_start` (si comienza un bloque nuevo de trabajo).
  - `mem_context` para recuperar estado reciente y temas activos.
- Si necesitas contexto sobre un tema específico:
  1. `mem_search` (o `mem_search_advanced` si necesitas filtros o mejor ranking)  
     → para encontrar recuerdos relevantes.
  2. `mem_timeline`  
     → para ver la evolución cronológica alrededor de un recuerdo.
  3. `mem_get_observation`  
     → para leer en detalle una memoria concreta.

### 2. During Work

- Usa `mem_save` para registrar:
  - Bugs y su causa raíz.
  - Decisiones de arquitectura/diseño y alternativas descartadas.
  - Cambios relevantes de configuración o infraestructura.
  - Optimizaciones de rendimiento (qué, por qué, mediciones).
  - Decisiones de refactorización (ámbito, riesgos, estado).
  - Resúmenes intermedios de avances importantes.
- Para temas que continuarán en el tiempo:
  1. `mem_suggest_topic_key(type="...", title="...")`.
  2. `mem_save(..., topic_key="<clave-sugerida>")`.
  3. Reutiliza esa misma `topic_key` en futuras actualizaciones.
- Si la instrucción o el prompt del usuario será útil como plantilla futura (Tabla `saved_prompts`):
  - Usa `mem_save_prompt` para guardarlo como referencia reutilizable.
  - **Obligatorio:** Especifica el `intent` (ej. "scaffolding", "refactor", "bugfix") y el `source` (ej. "user-prompt", "agent-template"), además de enlazalo al `topic_key` y `session_id` actuales.

### 3. Update / Clean Up

- Usa `mem_update` cuando:
  - Una decisión cambie.
  - Tengas nueva información que refiné una memoria previa.
- Usa `mem_delete` solo cuando:
  - Una memoria sea claramente irrelevante, errónea o sensible.
  - Por defecto es soft-delete (se puede recuperar internamente).

### 4. End of Session

- Al cerrar una sesión de trabajo:
  - `mem_session_summary` para registrar:
    - Qué se hizo.
    - Decisiones tomadas.
    - Pendientes para la próxima sesión.
  - `mem_session_end` para marcar el cierre.

### Formato para el Cierre de Sesión (Session Summary)

Al terminar el trabajo y llamar a `mem_session_summary`, NUNCA envíes resúmenes cortos de una línea. El parámetro `summary` DEBE seguir este formato Markdown estandarizado para mantener la consistencia histórica del proyecto:

**What**: [Resumen de lo que se implementó o solucionó durante toda la sesión]
**Why**: [El contexto o impacto de por qué se requirió el trabajo]
**Where**: [Los archivos principales modificados o herramientas utilizadas]
**Key Details**:

- [Detalle técnico 1...]
- [Detalle técnico 2...]

📌 Y en el parámetro **lessonsLearned**, coloca cualquier descubrimiento técnico importante que pueda servir en futuras sesiones.

---

## Tool Reference (lu-memory)

Usa esta tabla solo como referencia; no es necesario listar todas las herramientas durante la conversación.
| Tool | Uso principal (cuándo usarla) |
|-------------------------|-------------------------------------------------------------------------------|
| `mem_session_start` | Inicio de un bloque de trabajo o sesión lógica. |
| `mem_session_end` | Fin de la sesión; marca su cierre. |
| `mem_session_summary` | Al final: resumen de lo hecho y próximos pasos. |
| `mem_context` | Al inicio / tras reset: recuperar estado y temas recientes. |
| `mem_save` | Guardar observaciones importantes, decisiones, aprendizajes. |
| `mem_save_prompt` | Guardar prompts/instrucciones útiles como plantillas reutilizables. |
| `mem_search` | Búsqueda de contexto relevante por texto o tema. |
| `mem_search_advanced` | Búsqueda con mejor ranking, filtros, o cuando `mem_search` no sea suficiente.|
| `mem_timeline` | Ver evolución cronológica alrededor de recuerdos clave. |
| `mem_get_observation` | Leer el detalle completo de una memoria específica. |
| `mem_suggest_topic_key` | Obtener una `topic_key` estable para temas de largo plazo. |
| `mem_update` | Corregir o mejorar una memoria existente. |
| `mem_delete` | Borrar (normalmente soft-delete) una memoria obsoleta o errónea. |
| `mem_stats` | Ver estado global del sistema de memoria (para diagnóstico / introspección).|

---

## Behavioral Summary

- **Siempre**:
  - Llama a `mem_context` al inicio y tras resets.
  - Piensa si lo que acabas de lograr/decidir merece un `mem_save`.
- **Para temas que continúan en el tiempo**:
  - Usa `mem_suggest_topic_key` y reutiliza la misma `topic_key`.
- **Para navegar memoria existente**:
  - Aplica el patrón: `mem_search` → `mem_timeline` → `mem_get_observation`.
- **Para cerrar bien una sesión**:
  - `mem_session_summary` → `mem_session_end`.
