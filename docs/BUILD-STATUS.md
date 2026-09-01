# Estado de compilación

Versión preparada: `1.1.0-dev.11`

Objetivo: Minecraft 1.21.1, NeoForge 21.1.235 y Java 21.

## Estado de Phase 0

Baseline funcional: **1.0.3**.

La descomposición estructural de **Phase 0.0 a 0.8 está implementada en source**. La compilación intermedia dejó de tratarse como bloqueo por decisión explícita del workstream: la reparación integral de Gradle/Java/API y la regresión completa se concentran ahora en **Phase 0.9**.

La descomposición estructural de Phase 0 sigue siendo la base, pero por decisión explícita del workstream la reparación total de compilación queda aplazada hasta terminar la primera pasada funcional de 1.1.0. `dev.8` continúa consumiendo los seams creados durante Phase 0 para cerrar personalización visual y hot-disable sin volver a centralizar el runtime.

### Resultado estructural actual

- `ClimbManager`: **1.723 -> 154 líneas**. Ahora es una fachada de compatibilidad/delegación, no el dueño de toda la física.
- `ClientEvents`: **64 líneas**. Es un adapter de eventos/keybind, no el renderer ni el controlador de inputs.
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
- Protocolo de red: **14** desde `dev.5`, exclusivamente por el nuevo estado runtime/presentación por jugador de 1.1.0.
- Opciones cliente: `PickClimberClientOptionsStore` persiste el HUD local en `config/pickclimber-client.json`.
- Entrada GUI: `PickClimberOptionsEntry` sólo inyecta el botón cuando existe mundo y jugador cargados.
- Hot-disable: `PlayerClimbRuntimePreferences` + `RuntimePreferencePayload` usan el lifecycle ya extraído para desacoplar una sesión activa.

### Fix funcional aislado encontrado durante la auditoría

El cooldown inestable real puede ser de 40 ticks. La implementación anterior de `remainingTicks()` limitaba el valor reportado al cooldown base de 20 ticks, pudiendo producir feedback/sync visual incorrecto después del detach. En `dev.3`, `AnchorCooldownService` conserva los ticks restantes reales. Los valores de balance no cambian.

## Build `1.1.0-dev.2` informado por el usuario

El build ejecutado con Temurin Java 21.0.12 y Gradle 9.2.1 no alcanzó todavía la compilación Java. Falló primero en nuestra tarea `verifyArchitectureBoundaries` porque el primer verifier invocaba `file(...)` desde un closure de ejecución, algo incompatible con configuration cache.

El verifier de `dev.3` fue reestructurado para resolver `climbSourceDir`, `clientSourceDir` y los file trees durante la fase de configuración de Gradle. La corrección debe validarse con el build real de Phase 0.9.

## Build `1.1.0-dev.3` informado por el usuario

El build de `dev.3` superó `verifyArchitectureBoundaries`, creó los artefactos de Minecraft y llegó correctamente a `compileJava`. Allí falló con **4 errores del mismo origen**: `ClientClimbInputController` almacenaba `minecraft.player` en una variable declarada como `Player`, pero luego accedía a `player.input.forwardImpulse` y `player.input.leftImpulse`, campos propios de `LocalPlayer`.

`dev.4` conserva el controlador cliente tipado como `LocalPlayer` en los puntos que leen input. `dev.5` y `dev.6` continúan sobre esa corrección, pero no se exige un build verde intermedio: todos los errores Java/API acumulados se repararán juntos al terminar esta pasada de implementación.

El build también reportó dos advertencias de deprecación en `ClientModEvents` y una nota de API deprecated usada por `AnchorVisualService`. No rompen la compilación; quedan registradas como limpieza de integración de Phase 0.9 y no deben mezclarse con cambios de gameplay.

### Reorganización documental de `dev.4`–`dev.8`

La raíz del repositorio conserva únicamente `README.md` como documentación visible. `BUILD-STATUS.md`, `CHANGELOG.md`, `ROADMAP.md`, `SOURCE-MANIFEST.json` y todos los checklists `TESTING-*` se movieron bajo `docs/` (`docs/testing/` para los checklists). `MIGRATE-DOCS-DEV8.bat` elimina de forma explícita las copias antiguas de raíz y los migradores anteriores al aplicar el parche sobre árboles anteriores; el snapshot dev.8 ya no contiene esas copias.


## HUD styles in `1.1.0-dev.6`

- `IndicatorStyle` is persisted independently from indicator visibility mode.
- `String` remains the compatibility/default renderer and preserves the 1.0.2 shader isolation rule.
- `Pickaxe` uses a dedicated monochrome 16x16 texture mask with a recognizable horizontal head and diagonal handle; it is tinted through the same isolated shader path as String and does not render a vanilla item.
- Style selection is isolated behind `IndicatorIconRenderer`/`AnchorIndicatorIconRenderer`, so future styles do not couple to anchor evaluation or NeoForge event adapters.
- Old client configs without `indicatorStyle` defensively fall back to `String`.


## UX/config finalization in `1.1.0-dev.9`

- The 1.1.0 options surface is treated as source-complete pending integration/QA.
- Visual controls now become inactive when their parent mode makes them irrelevant: indicator controls while Off, box opacity while the box is disabled, and Show Unclimbable outside Contextual mode.
- The options footer now exposes one `Reset to Defaults` action, which restores the full client-options record and immediately requests runtime preference resync.
- Client config now writes `configVersion: 1`, continues to load legacy files without the field, and tolerates newer versions by reading known fields only.
- Redundant option writes are skipped and temporary files are cleaned after non-atomic save fallback.
- Footer layout now adapts between one and two rows on narrow windows.

## Build `1.1.0-dev.7` informado por el usuario

El build de `dev.7` superó los verificadores y llegó a `compileJava`. Falló con **2 errores del mismo origen**: `PickClimberOptionsEntry` importaba `OptionsScreen` desde `net.minecraft.client.gui.screens`, mientras que en Minecraft 1.21.1 Mojmap la clase vive en `net.minecraft.client.gui.screens.options`. El mismo build reportó además las deprecaciones ya conocidas del selector explícito de bus en `ClientModEvents` y del lookup no contextual de `SoundType` en `AnchorVisualService`.

`dev.8` corrige esa capa: usa el package correcto de `OptionsScreen`, deja que `@EventBusSubscriber` enrute automáticamente los eventos de mod bus y consulta el `SoundType` contextual con nivel/posición/jugador. El usuario confirmó posteriormente un build verde de dev.8 en el entorno Windows de release con Java 21.

## Verificación estática actual

- Source Java inspeccionado: 92 archivos.
- Violaciones de formato/calidad detectadas por el escaneo estático: **0**.
- JSON/MCMeta inválidos: **0**.
- `docs/SOURCE-MANIFEST.json`: debe corresponder al snapshot dev.11 y validarse por tamaño y SHA-256.
- Markdown en la raíz del repositorio: únicamente `README.md`.
- `hurtAndBreak` directos fuera de `ToolWearService`: **0**.
- Imports `climb -> network` / `PacketDistributor` dentro de `climb`: **0**.
- Bypass directo de `AnchorSurfaceClassifier.classify` fuera de `AnchorSurfaceResolver`: **0**.
- `ClimbManager`: 154 líneas, bajo el budget de 250.
- `ClientEvents`: 64 líneas, bajo el budget de 90.
- Raw `javac -proc:none` sin classpath Minecraft sólo produce dependencias faltantes; no se detectaron patrones evidentes de error sintáctico estructural.
- `bash -n build.sh`: debe mantenerse verde en la aceptación final.

Esta verificación **no sustituye un build NeoForge real**.

## Integración final pendiente

La integración/compilación se realizará en una pasada concentrada al finalizar la implementación source-side de esta etapa:

1. Ejecutar `build.bat` / `gradle clean build --stacktrace` con Java 21.
2. Corregir todos los errores reales de compilación/API/imports/tipos sin debilitar los boundaries.
3. Confirmar `verifySourceQuality` y `verifyArchitectureBoundaries` bajo configuration cache.
4. Ejecutar `docs/testing/TESTING-1.1.0-dev.11.md` y la regresión 1.0.3.
5. Ejecutar regresión multiplayer/compatibilidad.
6. Regenerar el manifest después de cualquier reparación final.
7. Corregir además cualquier incompatibilidad introducida por la primera pasada visible de 1.1.0 antes de QA final.

Resultado esperado cuando compile:

```text
build/libs/pickclimber-1.21.1-1.1.0-dev.11.jar
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
- Protocolo 14 desde el inicio de las funciones runtime de 1.1.0; el gameplay base sigue siendo servidor-autoritativo.

## Resultado dev.8, fallo dev.9 y objetivo dev.10

El usuario confirmó que `1.1.0-dev.8` compiló correctamente en el entorno Windows de release con Java 21. La primera prueba in-game detectó problemas de UX: entrada descentrada en Options, preview mezclado con el HUD detrás del backdrop, transparencia del String inefectiva, outline de picota poco reconocible y orden/dependencias de controles mejorables.

`1.1.0-dev.9` implementó la corrección UX y alcanzó nuevamente `compileJava`, donde quedó un único error: la fábrica compartida de sliders recibía `Consumer<Double>` y `DoubleOptionSlider` exige `DoubleConsumer`.

`1.1.0-dev.10` corrige exclusivamente esa frontera de tipos. No se incluye ningún migrador ni BAT de limpieza nuevo.

## dev.10 integration-repair status

- Keybind-based options entry and revised GUI remain unchanged from dev.9.
- Default key: `K`, rebindable through Minecraft Controls.
- `pickclimber-client.json` remains on config format version 2.
- The transparency-slider callback now uses `DoubleConsumer` end-to-end.
- Static scan found no remaining `Consumer<Double>` use in the client options package.
- Exact build and in-game QA are pending for this snapshot.

## dev.11 pickaxe/reset refinement status

- The user confirmed dev.10 options behavior works in-game, but the procedural Pickaxe Outline remained visually ambiguous and resembled a hook/scythe.
- dev.11 replaces those plotted pixels with `textures/gui/pickaxe_indicator.png`, a dedicated monochrome 16x16 tool-slot pickaxe mask.
- The user-facing style is now simply `Pickaxe`; config v3 maps the legacy serialized value `pickaxe_outline` to `PICKAXE`.
- The redundant `Reset HUD` and `Reset All` pair is replaced by a single `Reset to Defaults` action with immediate runtime-preference resync.
- Exact build and in-game visual QA are pending for dev.11.
