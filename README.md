# Pick Climber — Beta experimental 0.1.12

Mod para **Minecraft 1.21.1**, **NeoForge 21.1.235** y **Java 21**.

Convierte cualquier herramienta incluida en `#minecraft:pickaxes` en una herramienta de escalada, impulso y rescate.

## Base funcional estable

- Quieto, cayendo o cerca del ápice: clic derecho intenta engancharse.
- Tras un salto real y mientras todavía asciendes: clic derecho ejecuta el impulso.
- La velocidad positiva por sí sola no autoriza el impulso.
- El wall jump exige soltar Espacio después de engancharse y volver a pulsarlo.
- Movimiento horizontal y diagonal entre puntos de apoyo hasta 1,5 bloques.
- 15 puntos de durabilidad por enganche o impulso exitoso, respetando `Unbreaking`.
- Pick Climber I–III mejora el impulso y el wall jump.

## Prioridad de manos en 0.1.12

La mano secundaria tiene prioridad **entre picos disponibles**. Minecraft conserva el orden vanilla completo:

1. Se prueba la interacción de la mano principal.
2. Si colocar, usar, abrir o consumir un objeto tiene éxito, esa acción termina el clic.
3. Si la principal devuelve `PASS`, se prueba la secundaria y Pick Climber puede enganchar con ella.

Esto permite comenzar un enganche con el pico izquierdo cuando ambas manos tienen picos, sin impedir que un bloque, comida, cubo u otro objeto de la principal conserve su comportamiento normal.

Mientras la mano secundaria sostiene al jugador:

- La principal puede minar bloques.
- La principal puede atacar entidades.
- Un bloque en la principal puede colocarse sin soltar el ancla.
- Objetos y bloques interactivos pueden usarse normalmente cuando la principal no está actuando como otro pico disponible.
- Otro pico disponible en la principal puede crear el siguiente punto de apoyo y sustituir el ancla izquierda.

La liberación por clic izquierdo se conserva temporalmente solo cuando el propio pico de la mano principal es el ancla. Su eliminación total permanece como tarea posterior.

## Estado visual, cooldown y pose

Cada pico mantiene un cooldown individual de 20 ticks identificado por el UUID persistente del `ItemStack`.

El cooldown comienza inmediatamente cuando el servidor confirma un enganche o un impulso. Su overlay aparece lleno al inicio y baja de forma continua hasta cero, incluso si el pico continúa clavado en la pared. Soltar el pico o cambiar al otro no inicia, reinicia ni prolonga ese temporizador.

Mientras un pico mantiene el anclaje, su modelo de primera persona entra suavemente a una pose adelantada durante 4 ticks y permanece congelado. La mano contraria continúa renderizándose normalmente. La pose desaparece de inmediato al soltarse y funciona de forma reflejada para mano izquierda o derecha.

## Impulso base y encantamiento Pick Climber

| Nivel | Altura adicional aproximada |
|---|---:|
| Sin encantamiento | 1 bloque |
| Pick Climber I | 1,5 bloques |
| Pick Climber II | 2 bloques |
| Pick Climber III | 2,5 bloques |

El encantamiento también mejora el wall jump en aproximadamente 0,5 bloques por nivel.

## Controles vigentes

- Clic derecho sobre una cara vertical válida: impulso o enganche según el estado del salto.
- Con dos picos disponibles, se prefiere la mano secundaria.
- Espacio después de soltarlo y volver a pulsarlo: wall jump.
- Clic izquierdo con ancla secundaria: minar o atacar normalmente.
- Clic izquierdo con ancla principal: todavía libera sin impulso durante esta fase.

## Compilar en Windows

1. Instala Java 21.
2. Ejecuta `build-beta.bat`.
3. El JAR aparecerá en:

```text
build/libs/pickclimber-1.21.1-0.1.12-beta.jar
```

Retira versiones anteriores antes de instalar esta beta.

## Identidad visual

El icono oficial se encuentra en `src/main/resources/pickclimber_logo.png`.

Autor: **NotACelery**.
