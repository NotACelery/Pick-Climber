# Pick Climber

Pick Climber convierte picotas compatibles en herramientas de movilidad vertical para **Minecraft 1.21.1**.
El proyecto utiliza **NeoForge 21.1.235** y **Java 21**.

La release pública estable actual es **1.1.0**. El desarrollo siguiente se divide deliberadamente en dos releases minor
para mantener un alcance publicable y evitar convertir una sola actualización en un salto artificial a 2.0.0:

- **1.2.0 — Mapmaker Climbing Rules**
- **1.3.0 — Dimensional Rules**

La línea 1.x continúa siendo compatible conceptualmente con el gameplay base de Pick Climber. Las nuevas capas de reglas
no deben alterar el comportamiento normal cuando no existe una configuración de mapa activa.

> **Estado de este snapshot:** el source materializado está en `1.2.0-dev.41`. Sobre la línea completa de Rule Book v2,
> Rules Table, Terminal, Dispenser, reglas WORLD/PLAYER, Viewer y JEI/EMI opcionales. dev.33 completa la migración
> reference-first de Rule Books: las definiciones pesadas viven una sola vez en SavedData del mundo/server, los libros
> normales y temporales transportan IDs + metadata liviana, y render/name/tooltips no decodifican perfiles completos.
> Las reglas WORLD siguen sincronizándose y revalidándose en caliente; el editor custom es fail-closed para bloques no
> clasificados. `1.2.0-dev.20` sigue siendo el último baseline confirmado build-clean; dev.33 requiere Windows clean build,
> benchmark FPS y QA multiplayer antes de promoción.

---

## 1. Gameplay estable

El runtime base conserva las mecánicas introducidas y estabilizadas antes de la línea Mapmaker Rules:

- anclaje a paredes;
- movimiento entre anchors;
- wall jump;
- climbing boost;
- frenado progresivo de caída;
- superficies inestables;
- dos picotas y transferencia de mano;
- Strong Grip para techos;
- Sturdy Latch para superficies inestables;
- Pick Climber I–III;
- cooldown, alcance, colisiones y tool eligibility;
- detach normal y por movimiento;
- interacción con bloques interactivos;
- HUD y opciones cliente.

Las reglas de mapa nunca deben duplicar estas físicas. Deben entrar mediante seams centrales como clasificación de
superficie, desgaste y políticas de interacción.

---

# 2. Pick Climber 1.2.0 — Mapmaker Climbing Rules

La 1.2.0 será la gran actualización de **autoría y aplicación de reglas de escalada**.

Su objetivo es permitir que creadores de mapas y pack makers preparen conjuntos portables de reglas de Pick Climber,
los inspeccionen, los dupliquen, los importen/exporten y los apliquen durante gameplay sin convertir Pick Climber en un
gestor de checkpoints, regiones u objetivos de parkour.

## 2.1. Componentes principales

La arquitectura final de 1.2.0 se divide en cuatro responsabilidades.

### Climbing Rule Book

Objeto portable de reglas.

Reemplaza el concepto experimental anterior de **Climbing Rules Card**.

Características previstas:

- item Creative-only;
- sin receta survival;
- `maxStackSize = 64`;
- sólo stackea con Rule Books que tengan **exactamente la misma información interna**;
- nombre personalizado;
- tapa teñible usando los 16 pigmentos vanilla;
- hojas visualmente claras/blancas;
- Permanent o Temporary;
- Temporary WORLD o Temporary PLAYER;
- duración definida en la propia Rules Table;
- reglas de superficie;
- multiplicador de durability;
- Player Mining;
- Unmineable Terminals;
- import/export JSON;
- IDs de bloques de mods ausentes preservados;
- visor gráfico read-only al usar el libro fuera de una interacción prioritaria.

El nombre, pigmento, reglas, duración, activation mode y scope forman parte de su identidad de stacking.

### Climbing Rules Table

Estación Creative-only de autoría.

Responsabilidades:

- crear Rule Books desde `minecraft:book`;
- editar un Rule Book existente;
- importar las reglas activas del mundo a un Book;
- importar JSON;
- exportar JSON;
- duplicar Rule Books;
- cambiar pigmento;
- restaurar defaults mundiales;
- abrir editor con búsqueda;
- realizar toda mutación server-side con permisos y stale-session protection.

Diseño visual objetivo: estación blanca/gris inspirada estructuralmente en una Cartography Table, pero con recursos
propios de Pick Climber.

Slots conceptuales:

```text
[Rule Book x1] [Book xN] [Dye xN]
```

El slot de Rule Book de la Table acepta **una sola unidad**, aunque el item pueda stackear 64 fuera de la Table.

### Climbing Rules Terminal

Bloque de **aplicación durante gameplay**.

No es editor, no aloja inventario administrativo y no debe convertirse otra vez en la estación de autoría de dev.8.

Características previstas:

- Creative-only como item/bloque obtenido;
- utilizable por Survival, Adventure o Creative;
- orientación en seis direcciones;
- la cara/ranura frontal mira hacia quien lo colocó;
- Rule Book en mano + interacción válida -> server valida -> aplica -> consume exactamente uno;
- rechazo sin consumo cuando la aplicación no es válida;
- Permanent WORLD;
- Temporary WORLD;
- Temporary PLAYER;
- refresh del timer cuando corresponde;
- rechazo de no-ops según las reglas definidas;
- soporte de `Unmineable Terminals`.

### Climbing Rule Dispenser

Infraestructura Creative-only para distribuir **copias temporales** de un Rule Book maestro.

Características previstas:

- estética blanca/gris inspirada estructuralmente en un Dispenser;
- master Rule Book x1;
- sólo acepta master configurada como Temporary;
- la master nunca se consume;
- emite un **Temporary Rule Book** especial;
- copia owner-bound;
- una issuance pendiente por jugador;
- distintos jugadores pueden utilizar el mismo Dispenser;
- lifetime de transporte obligatorio configurado entre 1 y 60 segundos;
- desaparece al expirar;
- comportamiento equivalente a Curse of Vanishing al morir;
- no puede ser robado/utilizado por otro jugador;
- timer HUD propio;
- no entrega una copia si sus reglas ya son efectivas para el scope correspondiente.

---

## 2.2. Autoridad de reglas

El servidor es la autoridad.

El cliente puede:

- mostrar GUI;
- editar drafts autorizados;
- leer/escribir JSON local;
- mostrar timers;
- mostrar el perfil sincronizado;
- solicitar acciones.

El cliente no decide:

- si un Rule Book es válido;
- si debe consumirse;
- si una temporal puede comenzar;
- si un timer puede refrescarse;
- qué reglas son efectivas;
- si un anchor sigue siendo válido;
- si una Terminal es rompible para un jugador.

---

## 2.3. Resolución efectiva 1.2.0

La 1.2.0 seguirá siendo world-global como baseline, pero podrá superponer una temporal personal:

```text
Temporary PLAYER
      ↓
WORLD rules
      ↓
Pick Climber defaults
```

No existirán reglas permanentes PLAYER.

Una regla `PERMANENT` siempre implica:

```text
scope = WORLD
```

Una regla `TEMPORARY` podrá usar:

```text
scope = WORLD
scope = PLAYER
```

---

## 2.4. Temporales WORLD

Una Temporary WORLD:

1. captura snapshot del estado WORLD anterior;
2. instala su perfil;
3. persiste expiration usando game time del servidor;
4. muestra countdown;
5. al expirar restaura el snapshot;
6. revalida anchors;
7. desengancha sólo a quienes ya no tengan un anchor válido.

No se permiten temporales WORLD anidadas.

Una Permanent aplicada durante una Temporary WORLD:

- cancela la temporal;
- descarta el snapshot anterior;
- cancela el timer;
- se convierte en el nuevo baseline permanente.

---

## 2.5. Temporales PLAYER

Una Temporary PLAYER modifica sólo la vista efectiva de quien la activa.

Reglas de lifecycle:

- logout -> cancelar temporal personal;
- muerte -> cancelar temporal personal;
- un cambio WORLD posterior invalida la temporal personal y hace prevalecer inmediatamente el nuevo WORLD;
- nunca debe restaurar un snapshot personal antiguo encima de reglas WORLD más nuevas;
- al desaparecer el overlay personal, el jugador vuelve a resolver las reglas WORLD actuales;
- los anchors deben revalidarse al cambiar la vista efectiva.

---

## 2.6. Timers

Si sólo existe un timer relevante, puede mostrarse de forma limpia:

```text
0:42
```

Si coinciden una sesión temporal de reglas y un Temporary Rule Book de transporte:

```text
Rules 0:42
Book  0:18
```

Las etiquetas deben localizarse en los ocho idiomas soportados.

---

## 2.7. Viewer read-only

Usar un Rule Book sin que una interacción de bloque de mayor prioridad lo consuma abre un visor read-only.

Debe permitir:

- Overview;
- Stable;
- Unstable;
- Unclimbable;
- búsqueda por nombre localizado;
- búsqueda por namespace;
- búsqueda por ResourceLocation.

Debe cerrarse mediante:

- Escape;
- el keybind real de Inventory del jugador, no una `E` hardcodeada.

No permite modificar el libro.

---

## 2.8. Editor y búsqueda

Create, Edit y Viewer comparten el mismo criterio de catálogo/búsqueda.

El editor debe soportar:

- Stable;
- Unstable;
- Unclimbable;
- selección múltiple;
- scroll;
- Select Visible;
- Clear Selection;
- Restore Selected;
- Unlisted Policy;
- Durability Multiplier;
- Player Mining;
- Unmineable Terminals;
- Permanent/Temporary;
- duración;
- WORLD/PLAYER cuando Temporary;
- Search.

Los IDs pertenecientes a mods no instalados:

- permanecen en la definición;
- permanecen en JSON;
- no se muestran en catálogo/editor/viewer mientras no existan en el registry;
- no participan de la clasificación efectiva mientras estén ausentes.

---

## 2.9. Stacking y edición

Dos Rule Books sólo stackean si coinciden completamente en:

- nombre;
- pigmento;
- versión de formato compatible;
- listas Stable/Unstable/Unclimbable;
- Unlisted Policy;
- Durability Multiplier;
- Player Mining;
- Unmineable Terminals;
- Permanent/Temporary;
- duración;
- scope.

La antigua `revision` portable de Cards de dev.7/dev.8 no forma parte del diseño final.

La protección stale pertenece a una sesión server-side de la Rules Table:

```text
player
+ dimension
+ table position
+ session token
+ exact source ItemStack snapshot
```

---

## 2.10. Pigmentos y duplicación

La tapa de un Rule Book normal usa un pigmento vanilla.

Default:

```text
WHITE
```

Al crear:

- Book + sin dye -> Rule Book blanco;
- Book + dye -> Rule Book de ese color.

Al duplicar:

```text
[Source Rule Book x1] + [Book xN] + [Dye opcional]
```

Sin dye override:

- hereda nombre;
- hereda pigmento;
- hereda reglas;
- hereda activation/scope/duration.

Con dye override:

- conserva todo salvo el pigmento;
- requiere un dye por copia recoloreada;
- consume exactamente tantos Books como copias producidas.

La fuente nunca se consume.

---

## 2.11. Import Current Rules

La Rules Table puede convertir un Book vanilla en un Rule Book que representa el estado WORLD activo.

Sólo se habilita cuando:

- existe Book en el slot de material;
- las reglas WORLD no son Pick Climber defaults.

Debe copiar:

- reglas mecánicas;
- duración original configurada si el WORLD activo es temporal;
- activation metadata portable relevante.

No debe copiar:

- remaining ticks;
- runtime expiration timestamp;
- snapshot runtime;
- session IDs.

---

## 2.12. JSON

Directorio predeterminado:

```text
.minecraft/config/pickclimber/rules/
```

No se usará Desktop como default.

El archivo se nombra desde el Rule Book:

```text
<My Rule Book>.rules.json
```

El nombre debe ser seguro en Windows/Linux/macOS.

Se deben rechazar:

- path traversal;
- separadores de ruta;
- control characters;
- `.` / `..`;
- nombres reservados Windows (`CON`, `PRN`, `AUX`, `NUL`, `COM1..9`, `LPT1..9`, incluso con extensión);
- nombres incompatibles con la política portable definida.

El JSON jamás se ejecuta.

No debe existir:

- shell execution;
- command evaluation;
- scripting;
- reflection basada en contenido externo.

La importación se decodifica exclusivamente mediante el schema/codec de Pick Climber.

Al importar:

- el nombre del Rule Book se toma del filename;
- el pigmento se toma del JSON;
- pigmento ausente -> WHITE;
- legacy compatible -> Table intenta regularizar/migrar;
- Terminal no regulariza legacy.

---

## 2.13. Legacy

Política final:

```text
Terminal = strict consumer
Table    = migration/repair boundary
```

Un Rule Book/Card de schema antiguo utilizado directamente en Terminal:

- se rechaza;
- muestra mensaje de formato antiguo;
- no se consume.

Insertado en Rules Table:

- se intenta migrar;
- se agregan defaults seguros para campos nuevos;
- se preservan ResourceLocations desconocidas;
- tras guardar/exportar queda regularizado al schema vigente.

---

## 2.14. No-op y refresh

La aplicación compara **gameplay efectivo**, no metadata cosmética.

Nombre y pigmento influyen en stacking, no en determinar si aplicar el Book cambiaría gameplay.

Reglas previstas:

- Permanent idéntica a las reglas efectivas -> rechazo sin consumo;
- Temporary igual a una Permanent/default ya efectiva -> rechazo sin consumo;
- Temporary idéntica a una Temporary activa del mismo scope -> refresh del timer y operación válida;
- una Temporary distinta sobre otra Temporary incompatible -> rechazo; no se anidan snapshots.

---

## 2.15. Unmineable Terminals

Regla opcional del perfil.

Cuando está activa para un jugador Survival:

- Climbing Rules Terminal no puede romperse;
- el servidor cancela el break;
- el cliente no debe presentar progreso engañoso cuando sea viable evitarlo.

Creative queda exento.

Rules Table y Rule Dispenser son infraestructura de mapmaker y deben estar protegidos de Survival independientemente de
esta regla, para impedir robar masters o romper la lógica del mapa.

---

## 2.16. Geometry safety

La 1.2.0 introduce un invariant estructural independiente de las listas editables:

```text
Structurally Non-Anchorable
```

Objetivo: impedir que el editor fuerce como anchorable geometrías que Pick Climber no soporta de forma segura.

Implementación dev.13:

- `StructuralAnchorSafety` evalúa el `BlockState` concreto con `isCollisionShapeFullBlock`;
- collision vacía o parcial => `STRUCTURALLY_NON_ANCHORABLE`;
- slabs simples, stairs, panes, fences, walls, doors, trapdoors y otros partial shapes quedan bloqueados;
- estados full-collision del mismo ID siguen siendo válidos (por ejemplo, un slab `DOUBLE`);
- shapes dinámicos se evalúan con su estado/world actuales y la validación activa vuelve a ejecutarse durante el anchor;
- sin reglas activas no se altera el baseline 1.1.0; con Rules activas la seguridad estructural precede Stable/Unstable.

Esta capa:

- no es editable por Rule Book;
- no desaparece con operaciones Clear futuras;
- tiene precedencia sobre overrides configurables;
- está centralizada y no reparte `instanceof` por múltiples clases.

La clasificación automática avanzada de todo el registry se reserva para 1.3.0.

---

## 2.17. JEI / EMI

Los objetos del sistema siguen siendo Creative-only y **no reciben recetas survival reales**.

Sin embargo deben documentarse en JEI/EMI.

Representación visual mínima prevista:

```text
[Book] + [Climbing Rules Table] => [Rule Book]
```

Y una segunda relación de uso:

```text
[Rule Book] => [Climbing Rules Terminal]
```

EMI debe priorizar una representación visual mediante slots/iconos, sin depender de una página larga de texto.

JEI puede complementar con información breve si su API y estilo lo permiten sin introducir dependencias duras
innecesarias.

La compatibilidad con JEI/EMI es opcional: las APIs sólo se usan desde paquetes de integración `compileOnly`, el core
no importa clases de ningún recipe viewer y no se declara dependencia obligatoria en NeoForge. Las dos relaciones son
sintéticas y documentales, por lo que no crean recipes survival ni handlers de recipe transfer.

---

# 3. Pick Climber 1.3.0 — Dimensional Rules

La 1.3.0 amplía el Rules System sin inflar la release 1.2.0.

Su identidad es:

> **reglas de Pick Climber por dimensión, con soporte dinámico para dimensiones vanilla y modded.**

## 3.1. Dimensiones base

La UI debe ofrecer siempre:

- Overworld;
- Nether;
- End.

## 3.2. Dimensiones externas

No se deben hardcodear Twilight Forest, Eternal Starlight u otros mods.

Las dimensiones externas deben descubrirse dinámicamente a partir del servidor/registry y conservarse como
`ResourceKey<Level>`/ResourceLocation.

Ejemplos de uso esperados:

- Twilight Forest;
- Eternal Starlight;
- dimensiones de otros mods instalados;
- dimensiones custom de modpacks.

## 3.3. Global + Dimension Overrides

Modelo previsto:

```text
Global Rules
    ↓
Dimension Overrides
```

Una dimensión sin override usa Global.

Una dimensión con override puede modificar su comportamiento sin duplicar innecesariamente todo el Rule Book.

Resolución conceptual futura:

```text
Temporary PLAYER for current dimension
        ↓
WORLD dimension override
        ↓
WORLD global rules
        ↓
Pick Climber defaults
```

La implementación exacta se cerrará antes de comenzar 1.3.0 para mantener compatibilidad con temporales 1.2.0.

## 3.4. Rule Book schema v3

La 1.2.0 debe poder publicar un schema portable estable.

La 1.3.0 puede introducir un nuevo format version con migración determinista:

```text
1.2 Rule Book
    ↓ migrate
Global Rules = antiguo perfil
Dimension Overrides = {}
```

No se debe perder información al actualizar.

## 3.5. Editor dimensional

La Rules Table de 1.3.0 añadirá selección de contexto:

- Global;
- Overworld;
- Nether;
- End;
- dimensiones modded detectadas.

Search/Create/Edit/View deben seguir compartiendo catálogo y semántica de IDs ausentes.

## 3.6. Defaults y clasificación automática avanzada

La 1.3.0 añadirá generación de defaults más inteligente a partir de:

- tags de Pick Climber;
- registry actual;
- comportamiento del bloque;
- geometría;
- gravedad;
- full-solid classification;
- structural exclusions.

Objetivo:

- bloques sólidos completos -> candidatos Stable;
- bloques con comportamiento de caída/gravedad -> candidatos Unstable;
- geometrías no compatibles -> Structurally Non-Anchorable.

Debe beneficiar automáticamente a mods externos sin una lista hardcodeada por mod.

## 3.7. Clear / Empty

La UI dimensional avanzada podrá ofrecer **Clear / Vaciar** para comenzar una clasificación desde cero.

Debe limpiar únicamente categorías editables.

Nunca puede eliminar:

```text
Structurally Non-Anchorable
```

Los invariants de seguridad siguen vigentes.

---

# 4. Versionado y estrategia de releases

La progresión objetivo queda:

```text
1.0.x  Core climbing gameplay
1.1.0  Player customization / HUD / options
1.2.0  Mapmaker Rules / Rule Books / Table / Terminal / Dispenser
1.3.0  Dimensional Rules / dynamic dimensions / advanced defaults
```

No se utilizará un salto a 2.0.0 sólo porque una minor release sea grande.

Cada `1.X.0` debe representar una feature principal coherente y publicable.

---

# 5. Documentación de desarrollo

Los documentos autoritativos para esta línea son:

- `docs/ROADMAP.md` — alcance y arquitectura por versión;
- `docs/WAITLIST.md` — lista viva de trabajo pendiente/estado;
- `docs/BUILD-STATUS.md` — estado del build cuando comience la campaña de compilación;
- `docs/testing/` — QA ejecutable por corte;
- `../reference/POST-dev8-RULE-SYSTEM-SPEC-v2.md` en el snapshot — especificación detallada de la expansión posterior a dev.8.

Cuando se implemente o descarte una feature, **WAITLIST debe actualizarse en la misma pasada** para que el handoff nunca
dependa de recordar una conversación.

---

# 6. Build

Baseline técnica actual:

- Minecraft 1.21.1;
- NeoForge 21.1.235;
- Java 21.

Windows:

```text
build.bat
```

Linux/WSL:

```text
./build.sh
```

El source materializado de este snapshot usa `mod_version=1.2.0-dev.41`. `docs/WAITLIST.md` es la fuente operativa
para distinguir lo completado de lo pendiente; no se debe cambiar a `1.2.0` hasta cerrar features, build y QA.
