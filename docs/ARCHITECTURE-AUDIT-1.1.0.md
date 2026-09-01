# Pick Climber — Auditoría arquitectónica y hoja de ruta 1.1.0 / 1.2.0

> Phase 0 implementation status — `1.1.0-dev.3`
>
> This document preserves the original audit/baseline findings. Source-side structural passes 0.0–0.8 are now implemented: `ClimbManager` has fallen from 1,723 to 154 lines, evaluation/lifecycle/wear/cooldown/network/physics/client/event responsibilities have focused boundaries, and `AnchorSurfaceResolver` remains the extension seam reserved for future 1.2.0 world rules. The validated 1.0.3 gameplay behavior and protocol 13 remain the comparison baseline; full build/API repair and regression are consolidated in Phase 0.9.

Fecha de auditoría: 2026-09-01  
Baseline: Pick Climber 1.0.3 / Minecraft 1.21.1 / NeoForge 21.1.235 / Java 21  
Repositorio: `NotACelery/Pick-Climber`, rama `main`, commit auditado `6ac560e3d5cc5e806ce64771d89db3ee7f2f39b3` (`Anchor indicator fix`).

---

## 1. Objetivo de esta hoja de ruta

La línea 1.0.x estabilizó la mecánica principal. A partir de aquí el objetivo cambia:

- **1.1.0 — Player Customization & Runtime Control**: dar control real al usuario sobre HUD, feedback y activación local de Pick Climber.
- **1.2.0 — Mapmaker Rules / Climbing Rules Jukebox**: permitir a creadores de mapas definir las reglas de escalada del mundo mediante una estación creativa, tarjetas y perfiles JSON.

Antes de implementar cualquiera de las dos versiones se requiere una **Phase 0 estructural**. Pick Climber funciona, pero su implementación actual concentra demasiadas responsabilidades en una única clase y no ofrece puntos de extensión seguros para configuración runtime o reglas persistentes por mundo.

Principio rector ya documentado por el proyecto y que debe mantenerse:

> Nunca combinar una extracción estructural con un cambio de física, balance o gameplay en la misma pasada.

---

# 2. Baseline auditado

## 2.1 Métricas actuales

- 26 archivos Java.
- 3.481 líneas Java totales.
- `ClimbManager.java`: 1.723 líneas, 78 métodos, ~49,5 % del Java total.
- `ServerClimbState`: 25 componentes de estado en un único record.
- `ClientEvents`: 233 líneas y 6 campos `static` mutables dedicados a latches/input local.
- 5 payloads de red.
- Protocolo de red actual: `13`.
- 4 tags de bloques de Pick Climber.
- 17 archivos JSON/MCMeta validados sintácticamente.
- `SOURCE-MANIFEST.json`: 60 entradas, tamaños y SHA-256 correctos para la baseline auditada.
- Los tres idiomas (`en_us`, `es_es`, `es_cl`) tienen el mismo conjunto de 21 claves.

## 2.2 Integridad y formato

El source actual cumple bastante bien las reglas de limpieza definidas:

- UTF-8 y LF correctos en source.
- Sin tabs en Java.
- Sin trailing whitespace relevante.
- Sin `TODO`, `FIXME`, `XXX`, `HACK`, `printStackTrace` o `System.out` en runtime.
- Runtime Java sin comentarios explicativos, respetando la política de mantener la justificación técnica en `docs/DEVELOPMENT.md`.
- JSON y `.mcmeta` válidos.
- Manifest íntegro.

Hallazgo de formato concreto:

- `ClimbManager.java:448` tiene una línea de 132 caracteres y viola el `max_line_length = 120` de `.editorconfig`.

Esto es menor. El problema principal no es de estilo sino de **cohesión y acoplamiento**.

## 2.3 Documentación desactualizada

`ROADMAP.md` todavía declara la línea estable como **1.0.2**, pide ejecutar `TESTING-1.0.2.md` y habla de mantener `ClimbManager` intacto para 1.0.2. Esto ya no representa el estado 1.0.3.

`TESTING-1.0.3.md` contiene 163 checks, de los cuales sólo 6 están marcados como validados. Además conserva lenguaje de etapas anteriores del HUD que ya no coincide exactamente con la presentación actual.

Antes de comenzar 1.1.0 conviene reemplazar el roadmap vigente por una versión equivalente a este documento y separar:

- regression baseline 1.0.3;
- regression de Phase 0;
- QA específico 1.1.0;
- QA específico 1.2.0.

---

# 3. Auditoría arquitectónica

## 3.1 `ClimbManager` es el cuello de botella principal

Actualmente contiene, dentro de la misma clase:

- selección y consulta de herramienta activa;
- estado cliente;
- estado servidor;
- pose remota;
- autorización de salto real;
- validación de anclajes;
- evaluación del indicador HUD;
- generación de mensajes de error;
- cálculo de targets;
- comprobación de colisiones;
- attach;
- detach;
- cleanup;
- reparación/transferencia de mano;
- frenado de caída;
- descenso inestable;
- movimiento lateral;
- swing de techo;
- cálculo de wall jump;
- cálculo de impulso adicional;
- durabilidad;
- cooldown;
- grietas;
- sonidos;
- sincronización de anclaje;
- sincronización de pose remota;
- aplicación de payloads en cliente.

Agregar configs directamente aquí haría que 1.1.0 y 1.2.0 multiplicaran los `if`, valores globales y dependencias cruzadas.

### Riesgo

Un cambio aparentemente visual puede terminar afectando validación, red o física porque las rutas comparten métodos y estado global.

### Decisión

Phase 0 debe extraer responsabilidades en pequeñas pasadas verificables antes de introducir comportamiento nuevo.

---

## 3.2 La evaluación de un anchor está duplicada

Actualmente existen rutas relacionadas pero separadas:

- `canAttemptAnchor(...)`
- `anchorIndicatorStatus(...)`
- `anchorAttemptFailureMessage(...)`
- `ClimbingHandSelector.preferred(...)`

`anchorIndicatorStatus()` vuelve a ejecutar múltiples comprobaciones que ya existen en `canAttemptAnchor()` y a su vez llama a `ClimbingHandSelector.preferred()`, que vuelve a llamar a `canAttemptAnchor()`.

`useClimbingTool()` valida y posteriormente `performClimbingBoost()` o `attach()` vuelven a validar.

### Problema para 1.1.0

El HUD contextual necesita saber **por qué** una acción es válida o inválida sin duplicar reglas.

### Problema para 1.2.0

Una Climbing Rules Card debe alterar la clasificación base de superficie, pero esa decisión tiene que propagarse idénticamente al HUD, al clic real y al servidor.

### Objetivo

Crear una evaluación única, side-effect-free, por ejemplo:

```text
AnchorEvaluation
- outcome / failureReason
- canAnchor
- surface
- selectedHand
- resolvedTarget
- ceilingAttempt
- requiresStrongGrip
- requiresSturdyLatch
- coolingDown
```

El HUD, los mensajes y el attach deben consumir esa evaluación en vez de reconstruirla por separado.

---

## 3.3 `canAttemptAnchor()` no es completamente puro

En servidor, la validación puede reparar una identidad UUID duplicada de una herramienta:

```text
ToolIdentity.assign(...)
ToolIdentity.clearCooldown(...)
```

Una función con nombre `canAttempt...` no debería mutar el ItemStack.

Esto dificulta:

- hacer previews;
- reutilizar la evaluación en GUI/HUD;
- testear reglas;
- cachear resultados;
- garantizar que una consulta no modifique inventario.

### Objetivo

Separar:

1. **Evaluate** — no modifica nada.
2. **Commit / Prepare Tool Identity** — sólo al ejecutar realmente un anchor.

---

## 3.4 Acoplamiento circular `climb <-> network`

Actualmente:

- `ClimbManager` importa payloads y `PacketDistributor`.
- `ModNetworking` importa `ClimbManager`.
- `AnchorSyncPayload` importa directamente `ServerClimbState`.

Esto crea dependencia bidireccional entre dominio y transporte.

### Objetivo

`ClimbManager`/runtime no debería saber cómo se serializa un paquete.

Crear un `AnchorSyncService` que traduzca estado de dominio a payload y mantener los records de red como DTOs independientes.

También conviene centralizar:

```text
NetworkProtocol.VERSION = "14" // 1.1.0
```

para no mantener el número de protocolo como literal perdido dentro de `ModNetworking`.

---

## 3.5 Estado runtime global estático

`ClimbManager` mantiene varios `HashMap<UUID, ...>` globales:

- servidor;
- cliente;
- pose remota;
- salto real;
- salto consumido.

Funciona en 1.0.3, pero 1.2.0 necesita además **estado persistente por mundo** y 1.1.0 necesita un **estado transient por jugador** para apagar Pick Climber en caliente.

No deben mezclarse estas tres categorías:

### Runtime por jugador

Anclaje actual, input, cooldown visual, etc.

### Preferencia cliente

HUD, opacity, tamaño, icon style, mensajes.

### Regla del mundo

Clasificación custom, mining allowed, durability multiplier, perfil activo.

Separarlas desde Phase 0 evita que una config cliente pueda accidentalmente convertirse en regla del servidor.

---

## 3.6 `ServerClimbState` tiene demasiada aridad

Tiene 25 componentes y varios métodos `with...` que reconstruyen el record completo pasando todos los parámetros nuevamente.

Es inmutable y seguro conceptualmente, pero agregar un nuevo campo obliga a modificar todos esos constructores y aumenta el riesgo de intercambiar posiciones.

### Recomendación

No convertirlo en mutable. Mantener inmutabilidad, pero considerar agruparlo en subestados durante Phase 0 avanzada:

```text
ServerClimbState
- AnchorAttachment
- ToolBinding
- MotionRuntime
- RestoreAbilities
- timestamps / visual IDs
```

Esto puede esperar hasta que las extracciones principales estén estables; no es requisito para el primer dev build de 1.1.0.

---

## 3.7 Los valores de balance están hardcodeados

Ejemplos dentro de `ClimbManager`:

- anchor durability: 15;
- ceiling initial durability: 20;
- ceiling sustain interval: 20 ticks;
- braking durability: 10 por bloque;
- cooldown: 20;
- unstable cooldown: 40;
- distancias;
- velocidades;
- aceleraciones;
- damping;
- radius.

No todos deben hacerse configurables.

Para 1.2.0 sólo necesitamos inicialmente modificar **el desgaste**, no la física.

### Recomendación

Introducir `ClimbTuning`/`ClimbRulesView` con defaults actuales, pero exponer a la tarjeta sólo un `durabilityMultiplier`.

Ejemplo:

```text
1.0 = balance vanilla de Pick Climber
0.0 = sin desgaste
0.5 = 50 %
2.0 = 200 %
```

El multiplicador se aplica de forma central a:

- coste normal;
- coste inicial de techo;
- desgaste de braking;
- desgaste sostenido de techo;
- herramienta de soporte.

Esto conserva las proporciones actuales y evita una GUI con cinco números de durabilidad distintos.

---

## 3.8 Hallazgo de sincronización de cooldown

`cooldownTicksRemaining()` limita el cooldown enviado al cliente usando `ANCHOR_COOLDOWN_TICKS` (20).

Sin embargo, una superficie inestable puede iniciar un cooldown de 40 ticks.

Esto significa que un detach temprano desde un anchor inestable puede enviar al cliente como máximo 20 ticks aunque el servidor conserve un cooldown superior.

El servidor sigue siendo autoridad, por lo que no necesariamente permite un exploit, pero puede producir feedback visual temporalmente incorrecto.

### Recomendación

Corregir durante Phase 0 usando la duración real almacenada en `ToolIdentity` o el valor restante real compatible con el mask de 16 bits del payload.

No mezclar esta corrección con refactors de física.

---

## 3.9 `ModItems` no escala a 1.2.0

Hoy registra:

- item de icono;
- creative tab.

1.2.0 requerirá:

- block;
- BlockItem;
- BlockEntityType;
- MenuType;
- DataComponentType para la tarjeta;
- Climbing Rules Card;
- creative-only Jukebox/Station;
- probablemente screen registration.

### Objetivo

Antes de 1.2.0 separar registries:

```text
registry/
  ModItems
  ModBlocks
  ModBlockEntities
  ModMenus
  ModDataComponents
  ModCreativeTabs
```

No es necesario crear registries vacíos en 1.1.0 salvo `ModCreativeTabs` si queremos comenzar la limpieza; puede hacerse justo al iniciar 1.2.0.

---

## 3.10 No hay tests automáticos ni formatter gate

Actualmente `.editorconfig` describe estilo, pero nada en Gradle obliga a respetarlo.

No existe `src/test` ni workflow de GitHub Actions.

### Recomendación Phase 0

Agregar al menos:

- formatter verificable por Gradle (`spotlessCheck` o equivalente);
- `check` que falle si el source no está formateado;
- tests puros para nuevas clases de reglas/evaluación;
- CI Java 21 en GitHub para `build` + tests + formatting.

La física completa seguirá necesitando QA in-game, pero la lógica de configuración y precedencia sí debe automatizarse.

---

# 4. Arquitectura objetivo antes de 1.1.0

No hace falta convertir Pick Climber en un framework enorme. La meta es crear puntos de extensión concretos.

Propuesta de responsabilidades:

```text
climb/
  AnchorSurface
  AnchorMotion
  AnchorEvaluation
  AnchorFailureReason
  AnchorEvaluator
  AnchorSurfaceResolver
  ClimbingHandSelector
  ClimbingToolClassifier
  ModEnchantments
  ToolIdentity

runtime/
  ClimbSessionStore
  AnchorLifecycle
  AnchorPhysics
  ClimbMovementCalculator
  ToolWearService

network/
  NetworkProtocol
  ModNetworking
  AnchorSyncService
  payloads...

client/
  ClientEvents
  ClientInputState
  config/
    PickClimberClientOptions
    ClientOptionsStore
  hud/
    AnchorIndicatorRenderer
    AnchorIndicatorPolicy
    AnchorIconStyle
  screen/
    PickClimberOptionsScreen

mapmaker/            // 1.2.0
  ClimbingRulesProfile
  ClimbingRulesCodec
  WorldClimbingRules
  WorldRulesSavedData
  RulesProfileValidator
  RulesStation...
```

Nombres son provisionales. Las fronteras importan más que el nombre exacto.

---

# 5. Phase 0 — Desrigidizar 1.0.3 sin cambiar gameplay

## Phase 0.0 — Freeze y documentación

- Congelar 1.0.3 como baseline.
- Confirmar build exacto de 1.0.3 con Java 21.
- Guardar snapshot recuperable.
- Actualizar `ROADMAP.md` desde 1.0.2 a 1.0.3.
- Corregir referencia stale a `TESTING-1.0.2.md`.
- Corregir única línea >120.
- Documentar el hash/commit baseline.

**Prohibido:** tocar balance, física o comportamiento.

## Phase 0.1 — Quality gate

- Añadir formatter automático.
- Añadir tarea de verificación.
- Añadir CI Java 21.
- Añadir tests puros mínimos.
- Mantener build scripts existentes.

**Salida:** mismo JAR funcional, más garantías de integridad.

## Phase 0.2 — Unified Anchor Evaluation

Extraer de `ClimbManager`:

- resolución de superficie;
- requirements de encantamientos;
- range;
- obstruction;
- cooldown;
- hand selection;
- reason/outcome.

Crear `AnchorEvaluation` inmutable.

Eliminar mutaciones de UUID desde funciones de consulta.

`canAttemptAnchor()`, HUD y mensajes pasan a ser wrappers sobre el mismo resultado.

**QA crítico:** todos los casos del indicador + clic real deben seguir coincidiendo.

## Phase 0.3 — Estado runtime

Extraer:

- server state map;
- client state map;
- remote pose state;
- jump authorization state.

No introducir persistencia todavía.

Crear stores con cleanup explícito.

## Phase 0.4 — Lifecycle / wear / sync

Extraer en pasadas separadas:

1. `ToolWearService`.
2. `AnchorSyncService`.
3. `AnchorLifecycle`.

Eliminar dependencia directa de `ClimbManager` hacia payloads.

Corregir sincronización de cooldown inestable en una pasada específica.

## Phase 0.5 — Physics extraction

Sólo cuando las fases anteriores estén validadas:

- braking;
- unstable slide;
- lateral movement;
- ceiling swing;
- movement calculators.

**No cambiar ningún número.**

El objetivo es que `ClimbManager` termine como fachada/coordinador pequeño o desaparezca gradualmente.

## Phase 0 exit criteria

Antes de implementar 1.1.0:

- todas las mecánicas 1.0.3 funcionan igual;
- evaluación es única y side-effect-free;
- HUD no tiene lógica de gameplay propia;
- sync no vive dentro del manager de física;
- wear puede consultar un rules/tuning provider;
- existe un lugar claro para preferencias cliente;
- existe un lugar claro para una futura regla por mundo.

---

# 6. Pick Climber 1.1.0 — Player Customization & Runtime Control

Nombre funcional provisional: **Customization Update**.

## 6.1 Acceso a opciones

Añadir botón **Pick Climber Options** dentro de la pantalla vanilla `Options` únicamente cuando existe una partida cargada.

- Desde menú principal: no aparece.
- Desde ESC -> Options dentro de mundo/servidor: aparece.
- Evitar hardcodes que colisionen con GUI scales o mods de UI.
- Pantalla propia responsive y con scroll cuando sea necesario.

## 6.2 Configuración del indicador

Opciones objetivo:

### Indicator mode

- `Contextual` — default nuevo.
- `Always` — comportamiento diagnóstico completo.
- `Off` — sin indicador, climbing sigue activo.

### Show unclimbable indicator

- Default: `Off`.
- Si está apagado, una superficie simplemente no escalable no ocupa HUD.

### Icon style

- `String` — actual.
- `Pickaxe Outline` — nuevo sprite monocromático teñible.

### Icon size

- Slider, recomendado: 50–200 %.
- Default: 100 %.

### Icon opacity

- 0–100 %.
- Independiente de border.

### Show border / box

- On/Off.
- Default: On.

### Border opacity

- 0–100 %.

### Status/failure messages

Actualmente el runtime no dibuja un texto permanente debajo del String. La información textual vigente aparece principalmente como mensajes de action bar al fallar un intento.

Por lo tanto, la opción debería llamarse algo como:

- `Show anchor status messages`

más que prometer un texto permanente inexistente.

Podemos reintroducir un label persistente más adelante si realmente aporta valor.

## 6.3 Política contextual default

Mostrar:

- `READY`;
- `UNSTABLE`;
- `REQUIRES_STRONG_GRIP`;
- `REQUIRES_STURDY_LATCH`;
- `COOLDOWN`;
- `OUT_OF_RANGE`;
- `OBSTRUCTED` en rojo.

Ocultar:

- `UNCLIMBABLE` por default;
- interactivos sin Shift;
- MISS/entity/fuera del radio visual;
- cualquier estado sin información útil.

Principio:

> No escalable no es un error. Obstruido sí es una condición útil que explicar.

## 6.4 Render flexible

Extraer de `ClientEvents` un `AnchorIndicatorRenderer`.

Debe recibir una estructura visual, no decidir gameplay:

```text
IndicatorPresentation
- visible
- iconStyle
- color
- iconScale
- iconAlpha
- borderVisible
- borderAlpha
```

Mantener el aislamiento de shader validado en 1.0.2.

Nunca volver a teñir batch compartido de Jade u otros overlays.

## 6.5 Hot-disable del mod por jugador

Opción:

- `Enable Pick Climber interactions: On/Off`.

Al pasar a `Off` mientras está attached:

1. solicitar detach;
2. servidor termina la sesión;
3. se limpian grietas/pose/input;
4. se restaura gravedad/flight state;
5. servidor marca a ese jugador como Pick Climber disabled;
6. eventos de anchor y boost dejan de ejecutarse para él;
7. HUD desaparece.

### Importante en multiplayer

No basta con un boolean sólo cliente. El servidor recibe los eventos de interacción y podría volver a iniciar climbing.

1.1.0 debe añadir un payload cliente->servidor para sincronizar esta preferencia transient por jugador.

No es una regla del mundo; es un **opt-out personal**.

Se limpia al logout y vuelve al valor guardado por el cliente al entrar.

## 6.6 Persistencia de preferencias

Separar alcance de UI y alcance de almacenamiento.

Recomendación:

- el botón sólo existe con una partida cargada;
- HUD/preferences se guardan localmente para que el usuario no tenga que configurar cada mundo;
- world rules siguen siendo completamente separadas y llegarán en 1.2.0.

Si se desea literalmente una configuración visual diferente por world save, debe decidirse antes de programar el store, porque en multiplayer no existe una ruta local de world save equivalente.

## 6.7 Reset

Añadir:

- `Reset HUD to Defaults`.
- Confirmación únicamente si hay cambios relevantes.

## 6.8 Network 1.1.0

Bump recomendado:

- protocolo 13 -> 14.

Payload nuevo mínimo:

- `SetPlayerClimbingEnabledPayload`.

No sincronizar opacity/size/icon style porque son puramente cliente.

## 6.9 QA 1.1.0

Además de regresión completa 1.0.3:

- Options button sólo en partida cargada.
- GUI escala pequeña/grande.
- ventanas pequeñas.
- String / Pickaxe Outline.
- opacity 0 / 50 / 100.
- scale min/default/max.
- border on/off y alpha.
- messages on/off.
- indicator contextual.
- Always conserva unclimbable si se pide.
- Off no afecta climbing.
- hot-disable detached y unattached.
- hot-enable sin reiniciar.
- singleplayer.
- dedicated server con dos clientes y preferencias distintas.
- Jade/EMI/JourneyMap/otros overlays conservan color.

---

# 7. Pick Climber 1.2.0 — Mapmaker Rules / Climbing Rules Jukebox

Nombre del bloque aún por definir. En diseño puede llamarse **Climbing Rules Jukebox** o **Rules Station**.

Visual objetivo:

- bloque tipo tocadiscos/jukebox;
- skin blanca + gris;
- sólo obtenible en Creative;
- sin receta survival;
- control administrativo del mundo.

No conviene heredar comportamiento real de `JukeboxBlock` si no se necesita música. Debe ser un bloque propio con apariencia/UX inspirada en un jukebox.

---

## 7.1 Autoridad y permisos

- reglas aplicadas en servidor;
- persistentes dentro del world save;
- visibles/sincronizadas a clientes;
- edición sólo para mapmaker autorizado;
- recomendado: Creative + permission level >= 2.

El bloque debería ser indestructible para jugadores normales y removible en creativo.

---

## 7.2 Flujo con papel

1. Insertar `minecraft:paper`.
2. La GUI habilita:
   - `Create New Card`;
   - `Import JSON`.
3. Se abre editor.
4. Configurar reglas.
5. Nombrar perfil.
6. Crear **Climbing Rules Card**.
7. Opcionalmente:
   - Apply to World;
   - Export JSON;
   - seguir editando.

Una tarjeta existente insertada debe abrir su configuración para edición/exportación/aplicación.

---

## 7.3 Editor de bloques

Tabs principales:

- **Climbable / Stable**;
- **Unstable**;
- **Unclimbable**.

Características:

- buscador;
- grid tipo creative inventory;
- scroll;
- selección múltiple;
- `Select All Visible`;
- `Clear Selection`;
- `Set Stable`;
- `Set Unstable`;
- `Set Unclimbable`;
- `Restore Selected to Default`.

Mover un bloque de categoría debe retirarlo automáticamente de la anterior.

### Lista de bloques

Por default mostrar sólo bloques apropiados para una superficie de anchor, preferentemente bloques completos con Item representable.

No enumerar estados individuales.

En modpacks grandes puede haber miles de bloques; la lista debe cachearse y renderizar sólo elementos visibles.

---

## 7.4 Semántica de clasificación

El perfil no debe reemplazar encantamientos ni físicas.

Orden:

```text
World rule classification
    -> physical face/collision validation
    -> Strong Grip / Sturdy Latch requirements
    -> tool / cooldown validation
    -> anchor physics
```

Por lo tanto:

- marcar un techo Stable NO elimina Strong Grip;
- marcar un techo Unstable sigue requiriendo Strong Grip + Sturdy Latch;
- un bloque Unclimbable siempre rechaza;
- physics/cooldowns permanecen como en Pick Climber salvo opciones explícitas del perfil.

---

## 7.5 Política para bloques no seleccionados

Campo del perfil:

```text
Unlisted blocks:
- Unclimbable
- Use Pick Climber Defaults
```

Para mapas de parkour, default recomendado de una nueva tarjeta:

- `Unclimbable`.

Esto implementa la allowlist que originó la solicitud de CurseForge.

Para perfiles de compatibilidad parcial:

- `Use Pick Climber Defaults`.

---

## 7.6 Durabilidad configurable

No exponer inicialmente cada constante interna.

Agregar:

```text
Pickaxe durability multiplier
```

Default: `1.0`.

Se aplica centralmente mediante `ToolWearService`.

Rango recomendado inicial:

- 0.0–10.0.

La UI puede usar porcentaje:

- 0 %;
- 50 %;
- 100 %;
- 200 %;
- custom.

El resultado final sigue pasando por `ItemStack.hurtAndBreak`, por lo que **Unbreaking y comportamiento vanilla de daño deben mantenerse**.

---

## 7.7 Disable mining

Campo world-authoritative del perfil:

```text
playerMiningEnabled: true / false
```

Objetivo: impedir que un jugador rompa la ruta de parkour para hacer trampas.

### Reglas

- cancelar mining en servidor;
- cancelar feedback/ataque de bloque en cliente cuando sea posible;
- no romper el detach de Pick Climber con clic izquierdo;
- Creative/OP puede tener bypass administrativo;
- comandos de administración no se bloquean;
- no bloquear colocación salvo feature futura separada;
- no prometer bloquear máquinas automáticas o explosiones si la opción se llama específicamente “player mining”.

QA especial con mods de minería como hammers/vein mining si están presentes.

---

## 7.8 Aplicar reglas al mundo

`Apply to World` debe copiar un snapshot validado del perfil a SavedData del servidor.

La tarjeta física NO debe ser necesaria para mantener las reglas activas.

Esto permite distribuir un mapa ya configurado.

Al abrir el mundo en otra instalación con Pick Climber:

- reglas siguen dentro del save;
- archivo JSON externo no es obligatorio.

---

## 7.9 Restore World Defaults

Botón visible en la Rules Jukebox:

**Restore World Defaults**

Debe requerir confirmación.

Al ejecutarlo:

- elimina perfil custom activo;
- restaura clasificación normal por tags/fallback;
- restaura durability multiplier 1.0;
- restaura player mining enabled;
- elimina cualquier otra restricción introducida por la tarjeta;
- fuerza sync actualizado a clientes.

Debe existir separado de:

- `Eject Paper/Card`;
- `Clear Editor`.

Nunca hacer que retirar el papel de la GUI restaure accidentalmente el mundo.

---

## 7.10 Persistencia del mundo

Usar `SavedData` almacenado por el servidor, idealmente desde el Overworld/server principal para representar reglas globales del save.

1.2.0: reglas globales para todo el mundo y dimensiones.

Diferir a futuro:

- per-dimension;
- region/zones;
- múltiples jukebox con radios distintos.

---

## 7.11 Sync de reglas a clientes

El HUD cliente debe conocer la misma clasificación que el servidor.

Cuando:

- jugador entra;
- profile cambia;
- defaults se restauran;

el servidor envía un `WorldRulesSyncPayload`.

No consultar servidor cada frame mirando un bloque.

El profile se sincroniza una vez y se resuelve localmente para HUD.

Limitar tamaño/cantidad de entradas por seguridad.

---

## 7.12 Formato JSON

Usar formato versionado.

Ejemplo conceptual:

```json
{
  "format_version": 1,
  "name": "Castle Parkour",
  "unlisted_blocks": "unclimbable",
  "durability_multiplier": 1.0,
  "player_mining_enabled": false,
  "blocks": {
    "stable": [
      "minecraft:stone",
      "minecraft:cobblestone",
      "minecraft:prismarine"
    ],
    "unstable": [
      "minecraft:gravel"
    ],
    "unclimbable": []
  }
}
```

Posible extensión avanzada futura:

```text
#namespace:tag
```

pero no es requisito de 1.2.0 inicial si complica la GUI.

---

## 7.13 Nombre de archivo y seguridad

La GUI debe permitir escribir el nombre antes de exportar.

Nunca aceptar rutas arbitrarias.

Sanitizar a un filename seguro:

```text
Castle Parkour -> castle_parkour.json
```

- longitud máxima;
- sin `..`;
- sin `/` o `\\`;
- extensión `.json` impuesta por Pick Climber.

Carpeta recomendada:

```text
config/pickclimber/profiles/
```

Los perfiles exportados son portables; el world save almacena su propia copia activa.

---

## 7.14 Import JSON

La GUI lista archivos encontrados en la carpeta de profiles.

Flujo:

1. seleccionar JSON;
2. parsear;
3. validar schema/version;
4. mostrar warnings;
5. cargar al editor;
6. NO aplicar automáticamente;
7. usuario revisa y pulsa Apply/Create Card.

### IDs de mods ausentes

Un profile puede mencionar bloques de un mod no instalado.

No crashear.

Recomendación:

- mantener el ID no resuelto en el profile;
- mostrar warning en editor;
- ignorarlo en runtime mientras el registry no lo contenga;
- no borrarlo automáticamente al reexportar.

Esto permite mover cards entre modpacks sin destruir información.

---

## 7.15 Climbing Rules Card como item

Usar preferentemente un Data Component tipado en vez de meter el profile en strings/NBT arbitrario.

Tooltip sugerido:

```text
Climbing Rules Card
Castle Parkour

Stable: 18
Unstable: 4
Explicitly blocked: 2
Unlisted: Unclimbable
Durability: 100 %
Player mining: Disabled
```

La tarjeta no debería aplicar reglas por clic derecho casual.

Edición/aplicación ocurre mediante la Rules Jukebox.

---

## 7.16 Network 1.2.0

Bump recomendado:

- protocolo 14 -> 15.

Payloads probables:

- `WorldRulesSyncPayload`;
- requests de apply/reset;
- editor/station sync si Menu estándar no cubre todo;
- import/create requests validados por servidor.

Todo payload que altere world rules debe volver a comprobar permisos en servidor.

Nunca confiar en que “la GUI sólo aparece en creative”.

---

# 8. Precedencia de reglas propuesta

## Default sin profile

```text
unclimbable tag
> unstable tag
> stable tag
> fallback
```

Exactamente como 1.0.3.

## Con profile activo

```text
explicit profile unclimbable
> explicit profile unstable
> explicit profile stable
> unlisted policy
```

Si `unlisted policy = DEFAULT`, delegar al clasificador 1.0.3.

Si `unlisted policy = UNCLIMBABLE`, devolver Unclimbable.

Un import con el mismo ID en varias categorías debe rechazarse o normalizarse de forma determinística. Preferencia: marcar error y obligar a corregir; la GUI normal nunca generará duplicados.

---

# 9. Invariantes que NO deben romper 1.1.0 ni 1.2.0

- servidor conserva autoridad de posición y movimiento;
- range físico no se amplía por configs visuales;
- collision validation no se salta;
- Strong Grip sigue requerido para techo;
- Sturdy Latch mantiene sus reglas;
- Unbreaking sigue actuando mediante vanilla durability;
- identidad UUID sigue siendo por ItemStack, no por slot/mano;
- off-hand priority actual se conserva;
- Shift continúa siendo override de interacción normal;
- bloque interactivo sin Shift no muestra indicador;
- Jade y otros overlays nunca heredan tintes de Pick Climber;
- cambiar HUD no cambia gameplay;
- apagar HUD no apaga climbing;
- hot-disable limpia el estado antes de quedar inactivo;
- world profile no puede ser modificado por cliente sin autorización;
- restaurar defaults vuelve realmente al comportamiento 1.0.3.

---

# 10. QA y testing: nueva estrategia

El checklist monolítico actual debe dividirse.

## `TESTING-BASELINE-1.0.3.md`

Sólo comportamiento histórico que debe preservarse.

## `TESTING-1.1.0.md`

HUD/options/hot-disable/multiplayer prefs.

## `TESTING-1.2.0.md`

Jukebox/card/JSON/world rules/mining/persistence/multiplayer.

## Tests automatizables

Agregar tests para:

- precedencia de superficies;
- mapping `AnchorEvaluation -> indicator presentation`;
- hide/show contextual;
- durability multiplier;
- profile duplicate validation;
- JSON encode/decode round trip;
- migration `format_version`;
- filename sanitization;
- unresolved ResourceLocations;
- default/reset profile;
- cooldown remaining sync.

La física, poses y colisiones complejas siguen necesitando QA in-game.

---

# 11. Performance y compatibilidad a considerar

## HUD

- no reflection;
- no ejecutar use hooks falsos;
- no allocation excesiva por frame;
- evaluación compartida cuando sea posible;
- renderer sólo presenta un resultado ya calculado.

## Rules Jukebox block browser

En modpacks grandes puede haber miles de blocks.

- cachear catálogo;
- no reconstruir registry list cada tick;
- filtrar por búsqueda;
- virtualizar/renderizar sólo elementos visibles;
- no generar ItemStack innecesarios permanentemente;
- refresh sólo cuando cambien registry/resource data relevantes.

## Multiplayer

- server authority;
- permisos en cada write;
- payload sizes limitados;
- profile sync al login y change;
- restore defaults sync inmediato;
- jugador con HUD Off sigue obedeciendo world rules.

## Dedicated server

No referenciar `Minecraft`, `Screen`, render classes o filesystem cliente desde common/server code.

Import/export físico del JSON debe permanecer en capa cliente; aplicación del profile al mundo se valida y persiste en servidor.

---

# 12. Scope que debe quedar FUERA de 1.2.0

Para evitar convertir 1.2.0 en un proyecto infinito:

- reglas por región/radio;
- múltiples jukebox con prioridades espaciales;
- reglas distintas por dimensión;
- cooldown custom;
- física custom por tarjeta;
- velocidad custom;
- jump boost custom;
- colocar/break protection completa del mapa;
- explosiones/contraptions protection;
- editor de tags avanzado;
- scripting/rutinas tipo comportamiento de golems;
- triggers por redstone;
- cambio automático de card por zona.

Todas son expansiones válidas para 1.3.x o posteriores, pero no deben retrasar la primera herramienta útil de mapmaking.

---

# 13. Orden recomendado de desarrollo

## Branch: `1.1.0`

1. Snapshot 1.0.3 baseline.
2. Phase 0.0 docs/freeze.
3. Phase 0.1 format + CI + tests.
4. Phase 0.2 AnchorEvaluation.
5. Phase 0.3 stores.
6. Phase 0.4 wear/sync/lifecycle.
7. Phase 0.5 physics extraction si la regresión sigue limpia.
8. 1.1 HUD config model.
9. 1.1 options screen.
10. contextual policy.
11. icon renderer abstraction.
12. pickaxe outline.
13. hot-disable + network 14.
14. complete QA.
15. 1.1.0 release.

## Branch: `1.2.0`

1. registries preparados para blocks/menus/components.
2. `ClimbingRulesProfile` + Codec + validation.
3. `AnchorSurfaceResolver` world-rule aware.
4. `ToolWearService` durability multiplier.
5. `WorldRulesSavedData`.
6. sync rules to clients / network 15.
7. mining restriction.
8. Rules Jukebox block + BlockEntity + Menu.
9. base GUI.
10. paper -> card flow.
11. block browser/tabs/search/multi-select.
12. apply/reset world.
13. JSON export/import + filename.
14. tooltip/card presentation.
15. persistence/reload/multiplayer tests.
16. full 1.0.3 + 1.1 regression.
17. 1.2.0 release.

---

# 14. Release identity propuesta

## 1.1.0

**Player Customization & Runtime Control**

La actualización que transforma Pick Climber de una experiencia rígida a una experiencia personalizable sin alterar la mecánica base.

## 1.2.0

**Mapmaker Tools / Climbing Rules**

La actualización que permite distribuir mundos de parkour/puzzle con reglas de escalada propias, restricciones de mining y perfiles portables.

---

# 15. Conclusión de auditoría

Pick Climber 1.0.3 no necesita una reescritura.

La base está funcionalmente madura, los resources están ordenados y el formato general está bien. El problema es que el crecimiento beta dejó demasiado conocimiento concentrado en `ClimbManager` y en estado `static` global.

La estrategia correcta no es comenzar la Jukebox ni meter veinte booleans dentro de `ClimbManager`. Es crear primero cuatro puntos de extensión claros:

1. **AnchorEvaluation** — una sola verdad para saber si/por qué se puede anclar.
2. **AnchorSurfaceResolver** — una sola verdad para clasificar superficie, primero por defaults y después por world profile.
3. **ToolWearService / Rules View** — una sola ruta para desgaste configurable.
4. **Runtime/config/world-state separados** — no mezclar preferencias visuales, opt-out personal y reglas autoritativas del mapa.

Con esas fronteras, 1.1.0 y 1.2.0 dejan de ser modificaciones peligrosas al corazón del mod y pasan a ser capas encima de una mecánica estable.
