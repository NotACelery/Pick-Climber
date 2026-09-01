# Estado de compilación

Versión preparada: `1.1.0-dev.3`

Objetivo: Minecraft 1.21.1, NeoForge 21.1.235 y Java 21.

## Estado de Phase 0

Baseline funcional: **1.0.3**.

La descomposición estructural de **Phase 0.0 a 0.8 está implementada en source**. La compilación intermedia dejó de tratarse como bloqueo por decisión explícita del workstream: la reparación integral de Gradle/Java/API y la regresión completa se concentran ahora en **Phase 0.9**.

El objetivo de `1.1.0-dev.3` no es introducir todavía el menú de opciones. Es dejar el runtime modular, con seams claros para 1.1.0 y 1.2.0, manteniendo como contrato de comportamiento la release 1.0.3.

### Resultado estructural actual

- `ClimbManager`: **1.723 -> 154 líneas**. Ahora es una fachada de compatibilidad/delegación, no el dueño de toda la física.
- `ClientEvents`: **49 líneas**. Es un adapter de eventos, no el renderer/controlador de inputs.
- Evaluación unificada y side-effect-free: `AnchorEvaluator` + `AnchorEvaluation` + `AnchorFeedbackResolver`.
- Política de superficies: `AnchorSurfaceResolver` es el único seam que la futura Rules Card debe extender.
- Lifecycle: `AnchorLifecycle` + `AnchorStateValidator` centralizan attach/detach/cleanup y coherencia de estado.
- Estado servidor agrupado en substates coherentes: restore, control input, braking y ceiling.
- Durabilidad: todos los `hurtAndBreak` de Pick Climber pasan por `ToolWearService` y razones tipadas.
- Cooldowns: start/clear/query/propagation pasan por `AnchorCooldownService`.
- Networking: el paquete `climb` ya no importa payloads ni `PacketDistributor`; transporte NeoForge vive en `network`.
- Física: wall, ceiling, impulses, positioning, visuals, actions, inputs y ticking tienen servicios separados.
- Cliente: indicator renderer/policy, input controller, runtime gate y presentation gate están separados de eventos.
- Interacción: `AnchorInteractionService` centraliza el force-anchor y la decisión posterior al uso normal del bloque/item.
- Registro creativo: `ModCreativeTabs` queda separado de `ModItems`.
- Protocolo de red: **13**, sin cambios durante Phase 0.

### Fix funcional aislado encontrado durante la auditoría

El cooldown inestable real puede ser de 40 ticks. La implementación anterior de `remainingTicks()` limitaba el valor reportado al cooldown base de 20 ticks, pudiendo producir feedback/sync visual incorrecto después del detach. En `dev.3`, `AnchorCooldownService` conserva los ticks restantes reales. Los valores de balance no cambian.

## Build `1.1.0-dev.2` informado por el usuario

El build ejecutado con Temurin Java 21.0.12 y Gradle 9.2.1 no alcanzó todavía la compilación Java. Falló primero en nuestra tarea `verifyArchitectureBoundaries` porque el primer verifier invocaba `file(...)` desde un closure de ejecución, algo incompatible con configuration cache.

El verifier de `dev.3` fue reestructurado para resolver `climbSourceDir`, `clientSourceDir` y los file trees durante la fase de configuración de Gradle. La corrección debe validarse con el build real de Phase 0.9.

## Verificación estática actual

- Source Java inspeccionado: 73 archivos.
- Violaciones de formato/calidad detectadas por el escaneo estático: **0**.
- JSON/MCMeta inválidos: **0**.
- `hurtAndBreak` directos fuera de `ToolWearService`: **0**.
- Imports `climb -> network` / `PacketDistributor` dentro de `climb`: **0**.
- Bypass directo de `AnchorSurfaceClassifier.classify` fuera de `AnchorSurfaceResolver`: **0**.
- `ClimbManager`: 154 líneas, bajo el budget de 250.
- `ClientEvents`: 49 líneas, bajo el budget de 90.
- Raw `javac -proc:none` sin classpath Minecraft sólo produce dependencias faltantes; no se detectaron patrones evidentes de error sintáctico estructural.
- `bash -n build.sh`: debe mantenerse verde en la aceptación final.

Esta verificación **no sustituye un build NeoForge real**.

## Phase 0.9 — aceptación pendiente

Ahora sí corresponde hacer la integración completa:

1. Ejecutar `build.bat` / `gradle clean build --stacktrace` con Java 21.
2. Corregir todos los errores reales de compilación/API/imports/tipos sin debilitar los boundaries.
3. Confirmar `verifySourceQuality` y `verifyArchitectureBoundaries` bajo configuration cache.
4. Ejecutar `TESTING-1.1.0-dev.3.md` contra la release 1.0.3.
5. Ejecutar regresión multiplayer/compatibilidad.
6. Regenerar el manifest después de cualquier reparación final.
7. Sólo entonces marcar Phase 0 como completa y comenzar la implementación visible de 1.1.0.

Resultado esperado cuando compile:

```text
build/libs/pickclimber-1.21.1-1.1.0-dev.3.jar
```

## Invariantes que siguen vigentes

- Física, balance y tuning objetivo: 1.0.3.
- Prioridad de clic normal de bloques antes de Pick Climber.
- Shift + clic derecho como force-anchor explícito.
- Prioridad off-hand entre herramientas válidas.
- Strong Grip y Sturdy Latch sin cambios deliberados.
- Tags de herramientas, superficies e interactivos conservados.
- Shader tint del String aislado como en 1.0.2.
- Supresión preventiva del HUD sobre bloques interactivos como en 1.0.3.
- Servidor autoritativo.
- Protocolo 13 durante toda Phase 0.
