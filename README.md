# Pick Climber — Beta 0.1.5

Mod experimental para **Minecraft 1.21.1**, **NeoForge 21.1.235** y **Java 21**.

Convierte cualquier herramienta incluida en `#minecraft:pickaxes` en una herramienta de escalada, impulso y rescate.

## Corrección crítica de esta versión

La 0.1.5 rehace la sincronización del anclaje desde la raíz:

- El servidor posiciona al jugador mediante `ServerGamePacketListenerImpl#teleport`, no mediante `setPos` ni velocidad correctiva.
- El cliente deja de modificar por su cuenta posición, gravedad, vuelo o estado físico del anclaje.
- El cliente conserva únicamente el estado visual para la hotbar y el HUD.
- Un paquete atrasado de impulso no puede borrar un anclaje ya confirmado.
- Las correcciones de posición usan teletransporte sincronizado; nunca empujan al jugador hacia el ancla.
- Todos los desenganches pasivos eliminan movimiento artificial antes de devolver la gravedad.

El objetivo es eliminar el fallo histórico en que un intento de enganche podía transformarse en un lanzamiento vertical y dejar al jugador sin posibilidad de sujetarse durante una caída.

## Selección de la acción

La velocidad vertical ya no es suficiente para autorizar un impulso.

### Impulso

Se ejecuta únicamente cuando se cumplen todas estas condiciones:

- El servidor registró un salto real mediante `LivingJumpEvent` o un wall jump autorizado.
- Ese ascenso todavía no fue consumido por otro impulso.
- El jugador sigue en el aire.
- La velocidad vertical es superior a `0.08`.
- El jugador no está volando activamente ni enganchado.
- La pared, la cara y el pico son válidos.

### Enganche

Cualquier otro uso válido ejecuta el enganche, incluso cuando existe una velocidad positiva espuria causada por red, escalones o correcciones de posición.

Por lo tanto:

- Quieto frente a una pared: engancha.
- Cayendo: engancha y reinicia el daño de caída.
- Cerca del ápice: engancha.
- Volando en creativo: engancha.
- Saltando realmente y todavía ascendiendo: impulsa.

## Impulso base y Pick Climber

Cualquier pico compatible añade aproximadamente **1 bloque** a la trayectoria vertical restante.

| Nivel | Altura adicional aproximada |
|---|---:|
| Sin encantamiento | 1 bloque |
| Pick Climber I | 1,5 bloques |
| Pick Climber II | 2 bloques |
| Pick Climber III | 2,5 bloques |

El encantamiento también mejora el wall jump en aproximadamente **0,5 bloques por nivel**.

## Reglas vigentes

- Movimiento horizontal y diagonal entre puntos de apoyo, hasta 1,5 bloques.
- Enganche de emergencia durante caídas.
- 15 puntos de durabilidad por enganche o impulso exitoso, respetando `Unbreaking`.
- Cooldown individual de 20 ticks por pico.
- Dos picos pueden alternarse sin compartir cooldown.
- Grietas y sonido del bloque durante el enganche.
- Clic izquierdo todavía libera sin impulso en esta fase.
- Salto libera mediante wall jump.
- Autor: **NotACelery**.

## Registro diagnóstico

Cada clic válido registra en `latest.log` la decisión del servidor:

```text
[PickClimber] action=ATTACH ...
[PickClimber] action=BOOST ...
```

Si volviera a aparecer un impulso desde reposo, esa línea permitirá distinguir inmediatamente entre una clasificación errónea y una desincronización posterior al enganche.

## Compilar en Windows

1. Instala Java 21.
2. Ejecuta `build-beta.bat`.
3. El JAR aparecerá en:

```text
build/libs/pickclimber-1.21.1-0.1.6-beta.jar
```

Retira las versiones anteriores del mod antes de instalar esta beta.

## Portada temporal

El mod ahora usa como logo placeholder al gato guardián compilador (`src/main/resources/pickclimber_logo.png`).

## Seguridad del wall jump

El salto para desengancharse usa detección de flanco real. Tras engancharse, hay que soltar la tecla de salto y volver a presionarla; pulsaciones antiguas almacenadas por Minecraft no pueden activar el wall jump.
