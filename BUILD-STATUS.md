# Estado de compilación

Versión preparada: `0.1.14-beta`

Objetivo: Minecraft 1.21.1, NeoForge 21.1.235 y Java 21.

Cambios funcionales:

- Los bloques que declaran un menú conservan el clic derecho normal.
- `Shift + clic derecho` habilita el intento de anclaje sobre esos bloques.
- El selector de mano y el indicador visual comparten exactamente la misma regla.
- Se mantiene el detach intencional al usar el mismo pico que sostiene el ancla.
- Rebalance data-driven de Pick Climber: `weight: 6`, `anvil_cost: 1`, costes 5/8 y 25/8.
- No hay cambios de protocolo de red ni de estados físicos.

Validación realizada en este entorno:

- `ClimbingHandSelector.java` compiló con Java 21 contra las clases ya compiladas del proyecto y los artefactos exactos de Minecraft/NeoForge 21.1.235 incluidos en la fuente recibida.
- La llamada a `BlockState#getMenuProvider` fue verificada contra los mappings/artefactos de 1.21.1.
- El JSON del encantamiento y el resto de JSON del proyecto fueron validados sintácticamente.
- El manifiesto de archivos fue regenerado y sus hashes fueron comprobados.
- La lógica de red, física, cooldown, transferencia y render no fue modificada.

No se ejecutó una build completa de Gradle en este entorno. Compila localmente mediante:

```text
build-beta.bat
```

Resultado esperado:

```text
build/libs/pickclimber-1.21.1-0.1.14-beta.jar
```
