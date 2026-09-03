# Pick Climber — Master Roadmap 1.2.0 / 1.3.0

> **Fecha de congelación:** 2026-09-02  
> **Release pública estable:** 1.1.0  
> **Code floor recuperable de este snapshot:** 1.2.0-dev.8  
> **Minecraft:** 1.21.1  
> **NeoForge:** 21.1.235  
> **Java:** 21

Este documento reemplaza el roadmap previo centrado en un único `Climbing Rules Terminal` editor. La arquitectura
posterior a dev.8 divide responsabilidades, cambia Cards por Rule Books y separa deliberadamente el alcance en dos
releases minor. A 2026-09-02, dev.20 fue confirmado build-clean en Windows y queda como baseline estable. dev.19 cerró
UX/networking core; dev.20 materializó el Rule Book tintable y los assets aprobados de Table/Terminal/Dispenser.
JEI/EMI queda materializado en dev.21 como el último bloque de integración opcional antes del freeze de código.

Las releases son:

- **1.2.0 — Mapmaker Climbing Rules**
- **1.3.0 — Dimensional Rules**

El objetivo es poder publicar por partes, mantener una línea 1.x coherente y evitar que una única update acumule todo el
futuro del mod.

---

# A. Principios invariantes de la línea 1.x

## A.1. Pick Climber sigue siendo un mod de escalada

Queda fuera de alcance:

- checkpoints;
- gestión de vidas;
- objetivos de parkour;
- regiones genéricas;
- scripting de mapas;
- teletransportes automáticos;
- puntuación;
- redes de lógica de redstone propias;
- reemplazo de command blocks u otros mods de mapmaking.

El Rules System sólo define, transporta, aplica y restaura **reglas propias de Pick Climber**.

## A.2. No profile = gameplay normal

Sin reglas activas, el runtime debe equivaler al comportamiento normal de Pick Climber 1.1.0 salvo fixes explícitos.

## A.3. Servidor autoritativo

Toda decisión de gameplay se valida server-side.

## A.4. Boundaries obligatorios

- Surface policy entra por `AnchorSurfaceResolver` o un seam equivalente único.
- Durability entra por `ToolWearService`.
- Networking no debe filtrarse a la física.
- GUI nunca es autoridad.
- JSON nunca es autoridad runtime hasta validarse/aplicarse.
- Un objeto portable nunca es la persistencia WORLD activa.
- Revalidación de anchors usa el lifecycle normal de detach.

## A.5. Compatibilidad con mods ausentes

ResourceLocations desconocidas deben preservarse en objetos/JSON pero permanecer inactivas e invisibles en UI mientras
el registry no las contenga.

---

# B. 1.2.0 — Mapmaker Climbing Rules

## B.0. Definición de éxito de la release

1.2.0 está lista cuando un mapmaker puede:

1. obtener la infraestructura en Creative;
2. crear un Climbing Rule Book desde un Book vanilla;
3. nombrarlo y teñir su tapa;
4. configurar reglas con Search;
5. inspeccionarlo sin editar;
6. duplicarlo;
7. importarlo/exportarlo en JSON seguro;
8. aplicar reglas WORLD permanentes o temporales;
9. aplicar reglas PLAYER temporales;
10. distribuir copias efímeras mediante Rule Dispenser;
11. restaurar defaults;
12. reiniciar servidor sin corromper WORLD temporales;
13. jugar multiplayer sin mezclar sesiones personales;
14. mantener HUD/anchor prediction coherentes con servidor;
15. usar el sistema sin instalar JEI/EMI;
16. consultar su uso visualmente cuando JEI/EMI sí están instalados.

No incluye reglas distintas por dimensión.

---

## B.1. Naming definitivo

### B.1.1. Normal portable item

Nombre de producto:

```text
Climbing Rule Book
```

Nombre técnico recomendado:

```text
ClimbingRuleBookItem
ClimbingRuleBookData / ClimbingRuleBookDefinition
pickclimber:climbing_rule_book
```

El antiguo naming `Climbing Rules Card` queda como identidad de desarrollo previa y debe migrarse/eliminarse antes de
release.

### B.1.2. Temporary transport item

```text
Temporary Rule Book
```

Debe ser un item runtime distinto del Rule Book normal.

### B.1.3. Blocks

```text
Climbing Rules Table
Climbing Rules Terminal
Climbing Rule Dispenser
```

---

## B.2. Climbing Rule Book — modelo portable

### B.2.1. Separación perfil / metadata

Modelo conceptual:

```text
ClimbingRuleBookDefinition
├── formatVersion
├── bookName
├── coverColor
├── profile
│   ├── stableBlocks
│   ├── unstableBlocks
│   ├── unclimbableBlocks
│   ├── unlistedPolicy
│   ├── durabilityMultiplierPercent
│   ├── playerMiningEnabled
│   └── unmineableTerminals
└── activation
    ├── PERMANENT | TEMPORARY
    ├── WORLD | PLAYER
    └── durationSeconds
```

### B.2.2. Inmutabilidad

El contenido portable debe tratarse como value object.

No almacenar identidad mutable por unidad.

### B.2.3. Stack

```text
maxStackSize = 64
```

Dos Rule Books sólo stackean cuando coinciden todos los campos portables.

Diferencias de nombre o tapa son suficientes para impedir stack aunque las reglas mecánicas sean iguales.

### B.2.4. Nombre

El nombre:

- es obligatorio tras guardar una configuración;
- se muestra en item/UI;
- forma parte del stack identity;
- se usa como basename para JSON;
- se valida cross-platform;
- nunca contiene comandos/componentes ejecutables.

### B.2.5. Tapa teñible

Usar los 16 `DyeColor` vanilla.

Default `WHITE`.

El sprite final representa un manual técnico propio de Pick Climber en pixel art de inventario. La tapa vive en una
capa tintable alineada con una segunda capa fija; páginas, herrajes y el emblema permanecen sin tint. El emblema usa
exactamente la silueta lineal del indicador de picota ya incluido en el mod, no una picota renderizada.

No copiar sprites de terceros.

### B.2.6. Activation mode

`PERMANENT`:

- siempre WORLD;
- duración no aplica.

`TEMPORARY`:

- WORLD o PLAYER;
- `durationSeconds > 0`;
- duración definida durante authoring.

No existe Permanent PLAYER.

---

## B.3. Reglas mecánicas de un perfil

### B.3.1. Stable

Bloques explícitamente anchorables/estables.

### B.3.2. Unstable

Bloques anchorables que requieren la semántica de superficie inestable de Pick Climber.

### B.3.3. Unclimbable

Bloques explícitamente no anchorables.

### B.3.4. Unlisted Policy

Opciones:

```text
USE_PICK_CLIMBER_DEFAULTS
UNCLIMBABLE
```

### B.3.5. Durability Multiplier

Rango actual objetivo:

```text
0% .. 500%
```

Default `100%`.

La acumulación fraccional determinista debe seguir preservando costes como 50%, 150%, etc.

### B.3.6. Player Mining

Permite deshabilitar rotura manual de jugadores sin afectar máquinas/explosiones/Create/commands.

### B.3.7. Unmineable Terminals

Cuando es efectivo para un jugador Survival, la Climbing Rules Terminal no puede romperse.

Creative siempre puede romperla.

Table y Rule Dispenser se protegen de Survival por ser infraestructura de mapmaker, independientemente de este flag.

---

## B.4. Structural Geometry Safety

### B.4.1. Objetivo

Impedir que un Rule Book convierta en anchor válido una geometría incompatible con la física de Pick Climber.

### B.4.2. Categoría interna

Nombre conceptual:

```text
STRUCTURALLY_NON_ANCHORABLE
```

No es una cuarta lista editable del Book.

Es un invariant del motor.

### B.4.3. Precedencia

```text
STRUCTURALLY_NON_ANCHORABLE
    > explicit Unclimbable
    > explicit Unstable
    > explicit Stable
    > Unlisted Policy / defaults
```

### B.4.4. Candidatos iniciales

Evaluar centralmente:

- slabs;
- stairs;
- fences;
- walls;
- panes;
- doors;
- trapdoors;
- shapes parciales;
- otros bloques cuya collision/shape no pueda satisfacer de forma segura AnchorGeometry.

No basarse únicamente en nombres de clase si una comprobación geométrica fiable es viable.

### B.4.5. Compatibilidad futura

La 1.3.0 reutiliza esta capa para clasificación automática avanzada.

---

## B.5. Climbing Rules Table

### B.5.1. Rol

Único lugar de autoría y modificación de Rule Books.

### B.5.2. Obtención

Creative-only.

Sin crafting recipe survival.

### B.5.3. Permiso de autoría

```text
Creative
AND
permission level >= 2
```

### B.5.4. Slots

```text
Rule Book slot: max 1
Material Book slot: 1..64
Dye slot: 1..64
```

Mostrar sprite/placeholder de dye equivalente visualmente al patrón usado por Loom cuando el slot está vacío, usando
recursos/API permitidos por Minecraft/NeoForge.

### B.5.5. Create

Entrada:

```text
minecraft:book
```

No Paper.

Resultado:

```text
Climbing Rule Book
```

El Book material se consume únicamente tras save server-side exitoso.

### B.5.6. Create defaults

Para 1.2.0, un nuevo Rule Book comienza desde el perfil default de Pick Climber vigente en la arquitectura 1.2.

La generación automática avanzada de listas por registry/gravedad/geometría se difiere a 1.3.0, salvo structural
safety obligatoria.

### B.5.7. Edit

Requiere Rule Book x1.

No puede editar un stack >1 porque el slot lo impide.

### B.5.8. Import Current Rules

Habilitar sólo cuando:

- existe Book material;
- WORLD actual != defaults.

Convierte un Book en un Rule Book con snapshot portable del estado WORLD.

Si WORLD es temporal, conservar la duración **original configurada**, no el tiempo restante.

### B.5.9. Import JSON

- Lee desde carpeta local del cliente.
- Decodifica/valida.
- Abre draft.
- No auto-aplica.
- Server revalida antes de materializar Rule Book.

### B.5.10. Export JSON

Sólo Rule Book válido.

### B.5.11. Duplicate

Abrir sub-GUI estilo anvil.

Vista conceptual:

```text
[Source Rule Book x1] + [Book xN] + [Dye opcional] -> [Rule Book xN]
```

Mostrar preview antes de Confirm.

Fuente no consumida.

### B.5.12. Dye semantics

Sin dye:

- hereda coverColor.

Con dye distinto:

- override de coverColor;
- requiere `Dye x número de copias`;
- consume exactamente esos dyes al confirmar.

Si dye es igual al color fuente, no debe exigir/consumir pigmento innecesariamente.

### B.5.13. Restore World Defaults

Acción administrativa con confirmación.

Debe:

- limpiar Permanent WORLD;
- cancelar Temporary WORLD;
- cancelar overlays PLAYER afectados por la transición global;
- limpiar snapshots/timers correspondientes;
- sincronizar clientes;
- revalidar anchors.

---

## B.6. Editor visual

### B.6.1. Search compartido

Create, Edit y Viewer utilizan la misma fuente de catálogo y criterio de filtro.

Search matching:

- localized block name;
- namespace;
- full ResourceLocation.

### B.6.2. Tabs

```text
Stable
Unstable
Unclimbable
```

### B.6.3. Operaciones

- multiselect;
- Set Stable;
- Set Unstable;
- Set Unclimbable;
- Restore Selected;
- Select Visible;
- Clear Selection;
- scroll del grid;
- layout responsive.

### B.6.4. Missing IDs

No mostrar bloques ausentes en grid/search.

No eliminar sus IDs del draft al guardar.

### B.6.5. Settings del perfil

- Unlisted Policy;
- Durability Multiplier;
- Player Mining;
- Unmineable Terminals;
- Permanent/Temporary;
- duration;
- WORLD/PLAYER si Temporary.

### B.6.6. Nombre y cover

El nombre se edita en Table/editor.

El coverColor debe reflejar el dye/estado actual.

---

## B.7. Viewer read-only

### B.7.1. Apertura

Usar Rule Book cuando no se está priorizando una interacción de bloque compatible abre viewer.

### B.7.2. Tabs

- Overview;
- Stable;
- Unstable;
- Unclimbable.

### B.7.3. Search

Sí, obligatorio en las vistas de bloques.

### B.7.4. Read-only

No hay botones de Set/Restore/Save.

### B.7.5. Cierre

- Escape;
- keybind de Inventory real.

---

## B.8. Climbing Rules Terminal

### B.8.1. Rol

Consumer de gameplay.

No editor.

### B.8.2. Inventario

No necesita inventario persistente ni BlockEntity para almacenar Rule Books.

### B.8.3. Interacción

Rule Book válido en mano:

1. server valida;
2. calcula outcome;
3. aplica/refresh si corresponde;
4. consume exactamente 1 en éxito;
5. no consume en rechazo.

### B.8.4. Orientación

Property:

```text
FACING = 6 directions
```

Colocación en pared -> cara frontal/ranura hacia jugador.

Suelo -> arriba.

Techo -> abajo.

Model/blockstate deben representar las seis orientaciones.

### B.8.5. Legacy

Schema antiguo -> rechazo con mensaje de regularización mediante Rules Table.

### B.8.6. No-op

Comparar reglas mecánicas efectivas, no nombre/color.

Permanent igual -> reject.

Temporary igual a Permanent/default -> reject.

Temporary igual a temporal del mismo scope -> refresh.

Temporary distinta sobre temporal incompatible -> reject.

---

## B.9. WORLD runtime

### B.9.1. Permanent WORLD

Persistido en world SavedData.

### B.9.2. Temporary WORLD state

Debe persistir al menos:

- active temporary definition/profile;
- previous WORLD snapshot o defaults marker;
- expiresAtGameTime;
- configuredDurationSeconds;
- policy/session generation necesaria para invalidar carry/runtime obsoleto.

### B.9.3. Timer source

Usar server/world game time, no wall clock.

Resultado:

- servidor offline -> timer no avanza;
- chunk del Terminal descargado -> timer sigue existiendo;
- timer pertenece al mundo, no al bloque que lo activó.

### B.9.4. Expiry

- restore snapshot;
- clear temporary session;
- persist;
- sync;
- revalidate anchors;
- clear/replace HUD countdown.

### B.9.5. Permanent durante temporal

Permanent reemplaza la sesión:

- no esperar expiry;
- descartar snapshot;
- cancelar timer;
- nueva Permanent pasa a baseline.

---

## B.10. PLAYER runtime

### B.10.1. Resolución

```text
Temporary PLAYER
      ↓
WORLD
      ↓
defaults
```

### B.10.2. Estado personal

Mantener una única temporal PLAYER por jugador.

### B.10.3. Logout

Cancelar temporal personal.

No persistir para reactivarla al relog.

### B.10.4. Death

Cancelar temporal personal.

Temporary Rule Book de transporte desaparece como Curse of Vanishing.

### B.10.5. WORLD transition mientras PLAYER temporal

Cancelar PLAYER temporal inmediatamente.

Descartar baseSnapshot personal antiguo.

Aplicar/resolver el WORLD nuevo.

### B.10.6. Anchors

Toda transición de vista efectiva debe revalidar al jugador.

---

## B.11. Effective rules service

Introducir/solidificar un seam único capaz de responder por jugador:

```text
EffectiveClimbingRulesService.resolve(player)
```

Debe alimentar:

- surface resolution;
- durability multiplier;
- Player Mining;
- Unmineable Terminals;
- client sync/HUD view.

No introducir checks PLAYER aislados en múltiples servicios de física.

---

## B.12. Anchor revalidation

Ejecutar tras:

- apply Permanent;
- apply Temporary WORLD;
- refresh cuando cambie revision relevante;
- expiry WORLD;
- restore defaults;
- apply Temporary PLAYER;
- expiry PLAYER;
- death/logout cleanup;
- WORLD transition que cancela PLAYER.

Sólo detach si el anchor actual deja de satisfacer el evaluador autoritativo.

No detach masivo por comodidad.

---

## B.13. Climbing Rule Dispenser

### B.13.1. Obtención y protección

Creative-only.

Survival no puede romperlo.

### B.13.2. Master slot

```text
max stack = 1
accepted = valid Temporary Climbing Rule Book only
```

### B.13.3. Master no consumida

Nunca shrink al emitir.

### B.13.4. Emisión

Interacción por jugador, no redstone en 1.2.0.

UX de dev.12:

- click derecho normal: intentar emitir una copia para ese jugador;
- Shift + click derecho: abrir configuración sólo si el jugador cumple permiso mapmaker.

Razón: owner, issuance y timer requieren identidad de jugador explícita.

### B.13.5. Temporary Rule Book especial

Metadata runtime:

- owner UUID;
- issuance UUID/token;
- source dispenser identity/position cuando sea necesario;
- expiresAtGameTime/runtime time source;
- copia del perfil/activation relevante;
- cover color visual heredado.

Nombre visible fijo localizado:

```text
Temporary Rule Book
```

No usar el nombre custom de la master como nombre del objeto temporal.

### B.13.6. Stack

```text
maxStackSize = 1
```

### B.13.7. Owner-only

Otro jugador no puede:

- recogerlo;
- utilizarlo;
- robarlo para consumir la issuance de su owner.

Si termina en inventario ajeno por una ruta externa, destruir/invalidar de forma segura.

### B.13.8. One issuance per player

Un jugador sólo puede tener una copia pendiente emitida por Rule Dispenser a la vez.

No limitar a otros jugadores.

### B.13.9. Lifetime de transporte

Config obligatorio:

```text
1 .. 60 seconds
```

Este lifetime no cambia la duración de reglas definida en la master.

Default seleccionado para 1.2.0:

```text
30 seconds
```

### B.13.10. Expiry

Debe funcionar:

- en inventario;
- en mano;
- como ItemEntity en suelo.

Al expirar:

- eliminar item;
- liberar issuance;
- permitir pedir otra copia.

### B.13.11. Death

Comportamiento equivalente a Curse of Vanishing:

- desaparece;
- no queda como drop recuperable;
- libera issuance.

### B.13.12. Destrucción externa

Fuego/lava/daño de ItemEntity u otra eliminación debe liberar issuance.

### B.13.13. Uso exitoso

Al consumirse en Terminal:

- liberar issuance inmediatamente;
- aplicar/refresh según reglas normales.

### B.13.14. No-op antes de emitir

Si la master produciría reglas ya efectivas para ese player/scope, no emitir copia.

---

## B.14. HUD de timers

### B.14.1. Sólo Rules

```text
0:42
```

### B.14.2. Sólo Book

```text
0:18
```

### B.14.3. Simultáneos

```text
Rules 0:42
Book  0:18
```

Localizar labels.

No spam de chat.

Usar overlay/actionbar/área HUD consistente con UI existente.

---

## B.15. JSON y filesystem

### B.15.1. Ruta

```text
.minecraft/config/pickclimber/rules/
```

### B.15.2. Extensión

```text
.rules.json
```

### B.15.3. Filename

Derivado de bookName.

### B.15.4. Seguridad

Validar:

- reserved Windows device names;
- separators;
- traversal;
- invalid path;
- control chars;
- reserved endings/segments definidos;
- tamaño máximo defensivo (dev.8 proponía 4 MiB).

### B.15.5. Import name

El Rule Book resultante usa el basename del archivo importado como bookName normalizado/validado.

### B.15.6. Color

Leer `cover_color`/equivalente del archivo.

Ausente -> WHITE.

### B.15.7. Unknown mod IDs

Preservar textual ResourceLocations.

### B.15.8. No arbitrary code

El parser sólo acepta data schema.

---

## B.16. Legacy migration

### B.16.1. dev.8 Card/Profile -> final Rule Book

Rules Table es migration boundary.

Defaults de migración previstos:

- coverColor = WHITE;
- `unmineableTerminals = false`;
- activation = PERMANENT;
- scope = WORLD;
- conservar listas y ResourceLocations;
- nombre derivado de metadata existente/filename o solicitar uno si no existe uno seguro.

### B.16.2. Terminal

No auto-migra.

### B.16.3. JSON

Import puede detectar formato legacy compatible y abrir un draft migrado.

---

## B.17. JEI / EMI

### B.17.1. Principio

Sin recipes survival reales.

Documentación opcional mediante integrations.

### B.17.2. Visual authoring relation

```text
[Book] + [Climbing Rules Table icon] => [Base-color Rule Book]
```

### B.17.3. Visual application relation

```text
[Rule Book] => [Climbing Rules Terminal facing up]
```

### B.17.4. EMI

Priorizar slots/iconos.

No requerir una página explicativa de texto largo.

### B.17.5. JEI

Puede mostrar relación visual y explicación breve.

### B.17.6. Optional loading

Materializado en dev.21 mediante plugins aislados por viewer y APIs `compileOnly`. El core no referencia paquetes
JEI/EMI y un gate estático protege ese boundary.

No introducir dependencia obligatoria.

---

## B.18. Visual assets 1.2.0

### B.18.1. Rule Book

Materializado en dev.20 como sprite 32x32 de dos capas perfectamente alineadas.

Objetivos cumplidos:

- lectura inmediata como libro/manual;
- `layer0` neutral pigmentable mediante los 16 `DyeColor`;
- `layer1` fijo con páginas, herrajes, rombo y outline real del indicador de picota;
- páginas blancas/claras;
- buen contraste a escala de inventario;
- mismo modelo visual para Rule Book y Temporary Rule Book;
- no copiar assets de terceros.

### B.18.2. Table

Materializada en dev.20 con set 64x64 blanco/gris, top de autoría y front/side/bottom propios. La Table ahora conserva
orientación horizontal para que el frontal siga la colocación del mapmaker.

### B.18.3. Terminal

Materializado en dev.20 como lector blanco/gris de seis direcciones, con slot frontal y modelos horizontal/vertical
separados. Mantiene identidad visual distinta de vanilla Jukebox.

### B.18.4. Dispenser

Materializado en dev.20 como Dispenser blanco/gris con apertura frontal propia y seam horizontal/vertical. El modelo
vertical ya referencia una textura dedicada; por ahora esa textura replica el front aprobado hasta dibujar la apertura
centrada específica para UP/DOWN.

---

## B.19. Localization

Locales requeridos:

- `en_us`;
- `en_gb`;
- `es_cl`;
- `es_es`;
- `es_ar`;
- `es_mx`;
- `pt_br`;
- `pt_pt`.

Toda nueva key debe existir en los ocho archivos.

Incluir:

- Rule Book naming;
- Temporary Rule Book;
- Table/Terminal/Dispenser;
- editor;
- viewer;
- duplicate;
- JSON;
- migration;
- rejection messages;
- refresh messages;
- Rules/Book timer labels;
- Unmineable Terminals;
- structural safety cuando sea user-facing;
- JEI/EMI strings si corresponde.

---

## B.20. Networking

### B.20.1. Protocol

1.2.0 todavía no es release estable durante estos dev builds. El protocolo puede permanecer en la generación 15 si se
redefine consistentemente antes de release y no se necesita compatibilidad con builds dev anteriores.

### B.20.2. Payload categories

Separar:

- WORLD state sync;
- PLAYER effective state sync;
- Table actions;
- editor session;
- dispenser config/state;
- action results;
- timers sólo si no pueden derivarse del estado sincronizado.

### B.20.3. Screens

Screens no deben enviar `PacketDistributor` directamente si existe un client request boundary.

---

## B.21. Persistence

### B.21.1. WORLD SavedData

Debe soportar:

- no profile/defaults;
- Permanent WORLD;
- Temporary WORLD;
- snapshot;
- expiration;
- configured duration;
- format migration/fallback defensivo.

### B.21.2. PLAYER sessions

No persistir entre logout/restart como sesiones reanudables.

### B.21.3. Rule Dispenser issuance

Definir storage server-side suficiente para:

- impedir segunda copia del mismo jugador;
- limpiar uso/expiry/death/destruction;
- no bloquear permanentemente al jugador tras crash/restart.

Debe existir estrategia de reconstrucción/limpieza tras restart.

---

## B.22. QA mínimo de 1.2.0

### B.22.1. Baseline

- mundo sin rules = 1.1.0 behavior;
- HUD/options normales;
- Strong Grip;
- Sturdy Latch;
- unstable;
- two-pickaxe;
- durability/Unbreaking.

### B.22.2. Rule Book

- create blanco;
- create con dye;
- stack identical x64;
- no stack diferente nombre;
- no stack diferente dye;
- no stack diferente duration;
- no stack diferente rules;
- viewer search;
- inventory key closes;
- interactive block priority.

### B.22.3. Table

- permissions;
- Card/Book slot max 1;
- materials consume server-side;
- stale session rejected;
- duplicate counts;
- dye counts;
- import current enabled/disabled;
- restore defaults confirmation.

### B.22.4. Terminal

- six orientations;
- Permanent success consumes 1;
- reject no-op no consume;
- temporary refresh consumes valid input;
- legacy reject;
- Adventure usage;
- Unmineable survival;
- Creative break.

### B.22.5. WORLD temporary

- start;
- timer;
- restart;
- expiry;
- restore snapshot;
- permanent override;
- anchor revalidation.

### B.22.6. PLAYER temporary

- independent players;
- logout cancel;
- death cancel;
- WORLD transition cancels;
- effective durability;
- effective mining;
- effective surfaces;
- anchor revalidation.

### B.22.7. Dispenser

- master only Temporary;
- no consume master;
- owner pickup;
- thief rejected;
- same player second issuance rejected;
- other player allowed;
- 1s;
- 60s;
- inventory expiry;
- ground expiry;
- death vanishing;
- use releases issuance;
- destroyed item releases issuance;
- no-op issuance reject.

### B.22.8. JSON

- roundtrip;
- filename;
- reserved names;
- traversal;
- corrupt JSON;
- oversized file;
- missing color -> white;
- missing mod IDs survive;
- legacy migration.

### B.22.9. JEI/EMI

- mod absent -> no crash/classloading;
- JEI present -> docs visible;
- EMI present -> visual relations visible;
- no actual survival recipe registered.

### B.22.10. Structural safety

- representative slabs/stairs/etc cannot become anchorable via Book;
- normal full blocks unaffected;
- structural rule central and deterministic.

---

# C. 1.3.0 — Dimensional Rules

## C.0. Definición de éxito

1.3.0 está lista cuando un Rule Book puede definir un baseline global y overrides por dimensión, la Rules Table descubre
las dimensiones existentes del servidor —incluidas modded— y el runtime resuelve correctamente la dimensión actual sin
romper temporales/JSON/migración de 1.2.0.

---

## C.1. Dimension discovery

### C.1.1. Vanilla

Siempre representar:

- `minecraft:overworld`;
- `minecraft:the_nether`;
- `minecraft:the_end`.

### C.1.2. Modded

Descubrir dinámicamente las dimensiones cargadas/registradas.

No hardcodear mods.

### C.1.3. Missing dimension IDs

Como con bloques de mods ausentes, considerar preservar overrides cuyos dimension IDs no estén actualmente presentes,
para permitir Rule Books portables entre instalaciones.

Definir UX antes de implementación.

---

## C.2. Data model v3

Modelo conceptual:

```text
RuleBook v3
├── metadata / activation
└── rules
    ├── globalProfile
    └── dimensionOverrides
        ├── minecraft:overworld -> override/profile
        ├── minecraft:the_nether -> override/profile
        ├── minecraft:the_end -> override/profile
        └── modid:dimension -> override/profile
```

Determinar si override es:

- perfil completo;
- diff parcial;
- sistema híbrido.

Preferencia inicial: diff/override para evitar duplicación masiva, pero debe evaluarse por complejidad y UX.

---

## C.3. Migration v2 -> v3

Migración obligatoria:

```text
v2 profile -> v3 globalProfile
v3 dimensionOverrides = {}
```

Mantener:

- name;
- cover;
- activation;
- duration;
- scope;
- missing block IDs.

---

## C.4. Dimensional editor

Añadir selector de contexto:

```text
Global
Overworld
Nether
End
<modded dimensions>
```

La selección no debe multiplicar implementaciones del editor.

El mismo editor opera sobre un context adapter.

---

## C.5. Runtime resolution

Propuesta inicial:

```text
PLAYER temporary effective rules for current dimension
            ↓
WORLD current-dimension override
            ↓
WORLD global rules
            ↓
Pick Climber defaults
```

Debe definirse cómo snapshots WORLD temporary interactúan con múltiples dimensiones:

- snapshot del Rule Book WORLD completo, no sólo dimensión actual;
- transición atómica;
- cambio de dimensión de jugador durante temporal.

---

## C.6. Advanced automatic defaults

### C.6.1. Full solid

Bloques full-solid compatibles -> Stable candidate.

### C.6.2. Falling/gravity

Bloques con comportamiento equivalente a FallingBlock -> Unstable candidate.

No asumir sólo `minecraft:sand`/`gravel`.

### C.6.3. Structural blocked

Siempre Non-Anchorable.

### C.6.4. Tags

Respetar tags explícitos Pick Climber como autoridad de compatibilidad.

### C.6.5. Modded registry

Catalogar dinámicamente bloques instalados.

### C.6.6. Performance

La clasificación inicial debe cachearse; no recorrer registry/render shapes por frame.

---

## C.7. Clear / Vaciar

Botón por contexto.

Debe:

- vaciar categorías editables del contexto;
- no borrar metadata;
- no borrar structural exclusions;
- no borrar overrides de otras dimensiones;
- pedir confirmación si hay cambios no guardados según UX final.

---

## C.8. Search dimensional

Search conserva semántica 1.2.0.

El catálogo puede indicar:

- clasificación Global;
- override en dimensión actual;
- inherited/default state.

Definir iconografía antes de implementar.

---

## C.9. Modded dimension UX

Cada dimensión debe mostrar:

- localized/display name cuando exista de forma segura;
- ResourceLocation como fallback/identificador técnico;
- icono opcional sólo si puede obtenerse sin integración específica.

Nunca depender de una API del mod de dimensión para existir.

---

## C.10. WORLD/PLAYER temporaries + dimensions

Escenarios obligatorios:

1. Temporary WORLD aplicada en Overworld y player viaja Nether.
2. Temporary PLAYER aplicada y player cambia de dimensión.
3. WORLD rules cambian mientras PLAYER temporal está activa en otra dimensión.
4. Temporary expiry mientras player está en dimensión distinta de donde comenzó.
5. Missing mod dimension al cargar world/rulebook.

Resolver en servicio central, no en eventos ad-hoc.

---

## C.11. JSON v3

Debe representar:

- global;
- dimension overrides;
- missing dimension IDs;
- todos los campos portable 1.2.

Mantener seguridad filesystem de 1.2.

---

## C.12. Viewer 1.3

Read-only viewer añade selector dimensional.

Search sigue disponible.

No edición.

---

## C.13. JEI/EMI 1.3

No requiere cambio conceptual de pseudo-usos, salvo actualizar descripciones para indicar soporte dimensional si se desea.

No convertir Dimensional Rules en recipes.

---

## C.14. QA mínimo 1.3

- vanilla dimensions discovered;
- Twilight Forest-like external dimension discovered without hardcoding;
- Eternal Starlight-like external dimension discovered without hardcoding;
- unknown dimension ID preserved;
- v2 -> v3 migration;
- global inheritance;
- dimension override;
- clear current context only;
- automatic FallingBlock-like unstable;
- structural geometry cannot be overridden;
- player dimension change during WORLD temporary;
- player dimension change during PLAYER temporary;
- multiplayer players in different dimensions;
- JSON roundtrip v3;
- no regression 1.2 Rule Book workflows.

---

## C.15. Rule Dispenser — Persist on Pickup

Modo opcional para `Temporary Rule Book` PLAYER emitidos por un Climbing Rule Dispenser.

Principios:

- el timer comienza cuando el Rule Dispenser emite el Temporary Rule Book, no cuando cada jugador lo recoge;
- con `Persist on Pickup = OFF`, el primer pickup consume la entidad del suelo y una nueva entrega requiere otra
  activación del dispenser;
- con `Persist on Pickup = ON`, recoger no elimina la entidad del suelo y múltiples jugadores pueden obtener una
  asignación PLAYER desde la misma emisión;
- cada jugador hereda el `expiresAt` absoluto de la emisión original. Ejemplo: duración 30 s, pickup a T+11 s ->
  quedan 19 s y ese tiempo restante se muestra sobre la hotbar;
- la entidad persistente desaparece al expirar el timer original;
- mientras la emisión siga vigente, el dispenser bloquea nuevas emisiones. El cooldown corresponde a la vigencia de
  esa emisión, no a cada pickup individual;
- el dispenser debe persistir un identificador/estado de emisión (`emissionId`, `emittedAt`, `expiresAt`) para que la
  exclusión no dependa únicamente de que la ItemEntity siga cargada;
- cada asignación PLAYER queda bindeada al UUID del jugador y al `sourceEmissionId`;
- dropear/retransferir un Temporary Rule Book no puede transferir la asignación a otro jugador;
- un mismo jugador no puede volver a recoger la misma emisión para reiniciar o ampliar su timer;
- treinta jugadores pueden atravesar el mismo pickup spot y recibir la misma expiración absoluta sin requerir treinta
  emisiones.

QA obligatorio:

- pickup único con Persist OFF;
- pickup concurrente de muchos jugadores con Persist ON;
- herencia correcta de tiempo restante;
- hotbar muestra remaining real;
- no re-emisión mientras `expiresAt` esté vigente;
- desaparición del pickup al expirar;
- mismo jugador no puede renovar desde la misma `emissionId`;
- UUID binding impide transferencia entre jugadores;
- logout/login no extiende la expiración absoluta de una asignación ya emitida.

---

# D. Release gates

## D.1. Feature freeze

No compilar como gate final hasta terminar el scope de la versión en desarrollo, salvo smoke checks útiles durante
implementación.

## D.2. Build campaign

Después de feature-complete:

1. source-quality audit;
2. architecture gates;
3. localization parity;
4. unit tests;
5. compile;
6. corregir errores por lotes;
7. runClient/dev testing;
8. dedicated server;
9. multiplayer QA;
10. JEI/EMI optional compat;
11. release candidate.

## D.3. No weakening gates

Nunca resolver un build fallido eliminando verificaciones arquitectónicas legítimas.

---

# E. Document discipline

`docs/WAITLIST.md` es el tablero vivo.

Cada implementación debe, en la misma pasada:

- marcar items completados;
- agregar subtareas descubiertas;
- registrar decisiones nuevas;
- mover explícitamente features diferidas a su versión destino;
- no dejar el estado únicamente en la conversación.

`ROADMAP.md` cambia cuando cambia **el diseño/alcance**.

`WAITLIST.md` cambia cuando cambia **el estado de trabajo**.
