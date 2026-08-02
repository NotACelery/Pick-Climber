# Pick Climber — Beta experimental 0.1.13

Mod para **Minecraft 1.21.1**, **NeoForge 21.1.235** y **Java 21**.

Convierte cualquier herramienta incluida en `#minecraft:pickaxes` en una herramienta de escalada, impulso y rescate.

## Mecánica principal

- Quieto, cayendo o cerca del ápice: clic derecho intenta engancharse.
- Tras un salto real y mientras todavía asciendes: clic derecho ejecuta el impulso.
- La velocidad positiva por sí sola no autoriza el impulso.
- El wall jump exige soltar Espacio después de engancharse y volver a pulsarlo.
- Movimiento horizontal y diagonal entre puntos de apoyo hasta 1,5 bloques.
- 15 puntos de durabilidad por enganche o impulso exitoso, respetando `Unbreaking`.
- Pick Climber I–III mejora el impulso y el wall jump.

## Prioridad de manos y mano principal libre

La mano secundaria tiene prioridad **entre picos disponibles**, pero no reemplaza una interacción vanilla exitosa de la mano principal:

1. Minecraft prueba primero la interacción de la mano principal.
2. Si colocar, usar, abrir o consumir algo tiene éxito, el clic termina ahí.
3. Si la principal devuelve `PASS`, se prueba la secundaria y Pick Climber puede utilizar su pico.

Mientras la mano secundaria sostiene al jugador, la principal puede:

- minar bloques;
- atacar entidades;
- colocar bloques;
- usar comida, cubos y herramientas;
- interactuar con bloques;
- usar otro pico disponible para crear un nuevo punto de apoyo.

El clic izquierdo todavía libera sin impulso cuando el propio pico de la mano principal es el ancla. Su eliminación definitiva está pendiente.

## Transferencia del ancla con `F`

El intercambio vanilla de manos conserva el anclaje cuando el mismo pico pasa a la mano contraria:

- no se consume durabilidad adicional;
- no se reinicia el cooldown;
- no se repiten sonido ni grietas;
- no se recrea ni se mueve el punto de apoyo;
- la pose clavada cambia a la nueva mano;
- la mano anterior recupera su render normal.

La transferencia se detecta por el UUID persistente del pico, no por el material ni por el slot. Si el pico activo deja de estar equipado en ambas manos, el enganche termina de forma pasiva y sin impulso.

## Cooldown y pose

Cada pico mantiene un cooldown individual de 20 ticks.

El cooldown comienza inmediatamente cuando el servidor confirma un enganche o impulso. Su overlay baja de forma continua hasta cero aunque el pico continúe clavado. Soltarlo, cambiar al otro pico o transferirlo con `F` no reinicia ni prolonga el temporizador.

Mientras un pico mantiene el anclaje, su modelo de primera persona entra suavemente a una pose adelantada durante 4 ticks y permanece congelado. La otra mano sigue usando su render normal.

## Limpieza de grietas

La beta 0.1.13 refuerza la limpieza del overlay visual:

- el servidor recuerda la dimensión original del bloque ancla;
- un cambio de dimensión limpia la grieta en el nivel antiguo;
- al desconectarse, el cliente elimina directamente el overlay antes de destruir su `ClientLevel`;
- un timeout de sincronización también limpia la grieta local;
- cambiar de punto elimina inmediatamente la grieta anterior.

## Impulso y encantamiento Pick Climber

| Nivel | Altura adicional aproximada |
|---|---:|
| Sin encantamiento | 1 bloque |
| Pick Climber I | 1,5 bloques |
| Pick Climber II | 2 bloques |
| Pick Climber III | 2,5 bloques |

El encantamiento también mejora el wall jump en aproximadamente 0,5 bloques por nivel.

## Controles vigentes

- **Clic derecho** sobre una cara vertical válida: impulso o enganche.
- **Espacio**, después de soltarlo y volver a pulsarlo: wall jump.
- **F**: intercambia manos sin perder el anclaje.
- **Clic izquierdo con ancla secundaria**: minería o ataque vanilla.
- **Clic izquierdo con ancla principal**: todavía libera sin impulso durante esta fase.

## Compilar en Windows

1. Instala Java 21.
2. Ejecuta `build-beta.bat`.
3. El JAR aparecerá en:

```text
build/libs/pickclimber-1.21.1-0.1.13-beta.jar
```

Retira versiones anteriores antes de instalar esta beta.

## Identidad visual

El icono oficial se encuentra en `src/main/resources/pickclimber_logo.png`.

Autor: **NotACelery**.
