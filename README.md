# Pick Climber — 1.1.0

Pick Climber convierte picotas compatibles en herramientas de movilidad vertical para **Minecraft 1.21.1** con **NeoForge 21.1.235** y **Java 21**.

La release 1.1.0 mantiene como base el comportamiento de gameplay validado en 1.0.3 y añade personalización completa del HUD, control runtime por jugador, un menú responsivo y una arquitectura interna preparada para futuras reglas de mapa.

## Mecánicas principales

- Clic derecho sobre una cara válida para engancharse o ejecutar el impulso contextual.
- `Shift + clic derecho` fuerza el intento de anclaje cuando un bloque normalmente consume la interacción.
- Movimiento horizontal y diagonal entre puntos de apoyo dentro del alcance permitido.
- Frenado progresivo de caídas peligrosas y descenso controlado sobre superficies inestables.
- Wall jump al soltar y volver a pulsar Espacio después de engancharse.
- Transferencia del mismo pico entre manos con `F` sin recrear el ancla ni cobrar desgaste adicional.
- Strong Grip permite anclajes de techo y balanceo.
- Sturdy Latch vuelve firmes las superficies inestables compatibles.
- Pick Climber I–III aumenta el impulso y el wall jump.

El servidor sigue siendo autoritativo para posición, física, durabilidad, cooldown y lifecycle del ancla.

## Herramientas y compatibilidad

La selección de herramientas es data-driven:

- `#pickclimber:climbing_tools` incluye por defecto `#minecraft:pickaxes`.
- `#pickclimber:excluded_climbing_tools` tiene prioridad y excluye herramientas concretas.
- La picota de madera está excluida por defecto.
- Integraciones externas pueden añadirse mediante tags sin hardcodes de clases.

La clasificación de superficies también es extensible:

- `pickclimber:stable_anchor_blocks`
- `pickclimber:unstable_anchor_blocks`
- `pickclimber:unclimbable_blocks`
- `pickclimber:interactive_blocks`

Los bloques interactivos conservan el clic derecho normal. El indicador se oculta preventivamente sobre menús,
BlockEntities y bloques marcados como interactivos; mantener Shift vuelve a mostrar el preview de force-anchor cuando corresponde.

## Encantamientos

### Pick Climber I–III

Especialización de movilidad para paredes. Mejora el impulso y el wall jump.

### Strong Grip I

Permite anclajes en la cara inferior de techos firmes y habilita el balanceo bajo techo. Los techos inestables requieren además Sturdy Latch.

### Sturdy Latch I

Convierte superficies inestables en anclajes firmes y reduce su cooldown inicial al valor estable.

Strong Grip y Pick Climber son especializaciones excluyentes.

## HUD de anclaje

El indicador reutiliza la misma evaluación mecánica del intento real de anclaje y comunica estados como:

- READY
- UNSTABLE
- UNCLIMBABLE
- REQUIRES_STRONG_GRIP
- REQUIRES_STURDY_LATCH
- COOLDOWN
- OUT_OF_RANGE
- OBSTRUCTED

El tinte de los iconos está aislado al draw de Pick Climber. Jade y otros overlays no heredan sus colores.

## Pick Climber Options

El menú se abre dentro de una partida mediante un keybind configurable en **Controles**. La tecla por defecto es `K`.

Opciones disponibles:

- Enable Pick Climber Interactions.
- Indicator Mode: `Contextual / Always / Off`.
- Failure Messages.
- Show Unclimbable.
- Indicator Style: `String / Pickaxe`.
- Icon Size: `50%–200%`.
- Icon Transparency.
- Icon Colors: `Muted / Normal / Neon`.
- Indicator Box.
- Box Transparency.
- Box Colors: `Muted / Normal / Neon`.
- Reset to Defaults.

Los cambios visuales se aplican inmediatamente.

### Hot-disable

`Enable Pick Climber Interactions` es una preferencia por jugador. Al apagarla mientras existe un anclaje activo:

- se usa el lifecycle normal de detach;
- se restaura el estado físico correspondiente;
- se limpian cracks y pose;
- se detienen nuevos inputs de Pick Climber;
- las interacciones vanilla ajenas al mod siguen funcionando.

La preferencia se sincroniza de forma transitoria con el servidor mediante el protocolo 14.

### Diseño responsivo

El menú usa dos columnas cuando existe ancho suficiente y cambia automáticamente a una columna en ventanas estrechas.
Si la altura disponible no alcanza, sólo la lista de opciones se vuelve scrollable: preview y footer permanecen accesibles.

El preview del indicador limita únicamente su tamaño visual dentro del panel del menú. El tamaño real del HUD conserva el valor configurado hasta 200%.

## Configuración persistente

Las opciones cliente viven en:

```text
config/pickclimber-client.json
```

Formato actual: `configVersion: 3`.

La carga migra y normaliza automáticamente configuraciones anteriores compatibles:

- configs antiguas sin `configVersion`;
- `iconOpacity` / `boxOpacity` antiguos;
- `colorIntensity` compartido;
- `indicatorStyle: "pickaxe_outline"`.

Los archivos de versión futura se leen defensivamente usando sólo campos conocidos y no se degradan automáticamente.

## Idiomas incluidos

- English (US) — `en_us`
- English (UK) — `en_gb`
- Español (Chile) — `es_cl`
- Español (España) — `es_es`
- Español (Argentina) — `es_ar`
- Español (México) — `es_mx`
- Português (Brasil) — `pt_br`
- Português (Portugal) — `pt_pt`

## Build

Requisitos:

- Java 21
- Minecraft 1.21.1
- NeoForge 21.1.235

Windows:

```text
build.bat
```

Linux/WSL:

```text
./build.sh
```

La versión se obtiene desde `gradle.properties`. Para 1.1.0 el artefacto esperado es:

```text
build/libs/pickclimber-1.21.1-1.1.0.jar
```

`gradle clean build` ejecuta además `verifySourceQuality` y `verifyArchitectureBoundaries`.

## Documentación técnica

- `docs/DEVELOPMENT.md` — arquitectura actual e invariantes runtime.
- `docs/BUILD-STATUS.md` — estado de aceptación/build de la release.
- `docs/CHANGELOG.md` — historial público de versiones.
- `docs/ROADMAP.md` — trabajo planificado después de 1.1.0.
- `docs/testing/TESTING-1.1.0.md` — registro de QA de la release.
- `docs/STRONG-GRIP-DESIGN.md` — especificación histórica de Strong Grip/Sturdy Latch.
- `docs/ARCHITECTURE-AUDIT-1.1.0.md` — auditoría histórica que originó la modularización.

## Identidad visual

El logo oficial está en `src/main/resources/pickclimber_logo.png`.

Autor: **celerbi**.
