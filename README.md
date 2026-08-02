# Pick Climber — Beta experimental 0.1.10

Mod para **Minecraft 1.21.1**, **NeoForge 21.1.235** y **Java 21**.

Convierte cualquier herramienta incluida en `#minecraft:pickaxes` en una herramienta de escalada, impulso y rescate.

## Base funcional heredada de 0.1.6

- Quieto, cayendo o cerca del ápice: clic derecho intenta engancharse.
- Tras un salto real y mientras todavía asciendes: clic derecho ejecuta el impulso.
- La velocidad positiva por sí sola no autoriza el impulso.
- El wall jump exige soltar Espacio después de engancharse y volver a pulsarlo.
- Movimiento horizontal y diagonal entre puntos de apoyo hasta 1,5 bloques.
- 15 puntos de durabilidad por enganche o impulso exitoso, respetando `Unbreaking`.
- Pick Climber I–III mejora el impulso y el wall jump.

## Estado visual y cooldown del pico en 0.1.10

Cada pico mantiene un cooldown individual de 20 ticks identificado por el UUID persistente del `ItemStack`.

El cooldown comienza inmediatamente cuando el servidor confirma un enganche o un impulso. Su overlay aparece lleno al inicio y baja de forma continua hasta cero, incluso si el pico continúa clavado en la pared. Soltar el pico o cambiar al otro no inicia, reinicia ni prolonga ese temporizador.

El estado de ancla activa sigue siendo independiente: un pico clavado no puede reutilizarse aunque su cooldown ya haya terminado. Su indicador visual dedicado se añadirá por separado junto con la pose congelada de la herramienta.

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
- Espacio tras soltarlo y volver a pulsarlo: wall jump.
- Clic izquierdo todavía libera sin impulso durante esta fase.

## Compilar en Windows

1. Instala Java 21.
2. Ejecuta `build-beta.bat`.
3. El JAR aparecerá en:

```text
build/libs/pickclimber-1.21.1-0.1.10-beta.jar
```

Retira versiones anteriores antes de instalar esta beta.

## Portada temporal

El logo placeholder es el gato guardián compilador: `src/main/resources/pickclimber_logo.png`.

Autor: **NotACelery**.
