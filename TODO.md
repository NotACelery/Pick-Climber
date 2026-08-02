# Backlog maestro de Pick Climber

## Interacción y controles

### Completado en 0.1.12–0.1.14

- Prioridad de la mano secundaria entre picos disponibles.
- Mano principal libre para minar, atacar, colocar y usar objetos mientras la secundaria sostiene.
- Cambio de punto con otro pico disponible en la mano principal.
- Transferencia del mismo pico activo mediante `F`, conservando ancla, UUID, cooldown, pose, sonido y grietas.
- Cancelación pasiva al retirar el pico activo de ambas manos.
- Minería normal con la mano libre mientras la otra sostiene el ancla.
- Clic izquierdo con el mismo pico activo provoca desenganche pasivo. Este comportamiento es intencional: el pico deja de sostener al jugador cuando se intenta usar para minar o atacar.

### Completado en 0.1.14

- Los bloques que declaran un menú conservan el clic derecho normal para abrir su interfaz.
- `Shift + clic derecho` con una herramienta de escalada intenta engancharse a una cara vertical válida del bloque interactivo.
- Un intento rechazado no gasta durabilidad ni inicia cooldown.
- La detección usa `BlockState#getMenuProvider`; no mantiene listas hardcodeadas de bloques.
- El indicador de alcance respeta la misma regla y solo aparece con Shift sobre bloques con menú.
- Las pruebas específicas con máquinas y contenido modded continúan aplazadas a la fase de compatibilidad.

### Control futuro de techo

- Mantener Shift conserva el agachado y las interacciones vanilla; no inicia descenso.
- Una doble pulsación de Shift durante un anclaje de techo provoca liberación pasiva vertical, sin salto ni impulso horizontal inicial.
- El doble toque debe ignorar dirección de movimiento al comenzar la caída para permitir aterrizajes precisos sobre bloques pequeños.
- Una interacción de uso entre ambas pulsaciones, o en el mismo gesto, cancela la detección de doble toque.
- La ventana exacta entre pulsaciones y la breve estabilización vertical inicial quedan abiertas a pruebas.

## Encantamientos y coste de yunque

### Pick Climber

- Será el único encantamiento del mod con niveles: I–III.
- Aplicado en 0.1.14: `anvil_cost` reducido de 4 a 1.
- Aplicado en 0.1.14: `weight` aumentado de 4 a 6.
- Aplicado en 0.1.14: costes de mesa ajustados a base 5 y aumento 8 por nivel; coste máximo base 25 y aumento 8.
- Medir en juego la altura real del impulso base y de Pick Climber I–III.
- Ajustar la ventana posterior al salto real —actualmente 8 ticks— sin volver a depender solo de la velocidad.
- Evaluar si impulso, enganche y wall jump deben tener costes de durabilidad distintos.

### Strong Grip y Sturdy Latch

- `Strong Grip` tendrá solamente nivel I.
- `Sturdy Latch` tendrá solamente nivel I.
- `Pick Climber` y `Strong Grip` serán incompatibles.
- `Sturdy Latch` será compatible con cualquiera de las dos especializaciones.
- Debe ser posible combinar `Pick Climber I–III + Sturdy Latch I` o `Strong Grip I + Sturdy Latch I` en un único libro.
- Ese libro combinado debe poder aplicarse como paso final a una picota perfecta con Unbreaking III, Mending, Efficiency V y Fortune III o Silk Touch sin provocar “Too Expensive”.
- Se acepta que el paso final ronde aproximadamente 30 niveles. El objetivo no es que sea barato, sino que siga siendo realizable con un orden optimizado.
- Los valores exactos de `anvil_cost` de Strong Grip y Sturdy Latch deberán elegirse en conjunto para cumplir esta restricción.

## Superficies de anclaje

Crear tags data-driven:

```text
pickclimber:stable_anchor_blocks
pickclimber:unstable_anchor_blocks
pickclimber:unclimbable_blocks
```

Reglas previstas:

- `stable`: permite detenerse completamente después del frenado inicial.
- `unstable`: produce descenso controlado y no fija al jugador por defecto.
- `unclimbable`: rechaza completamente el enganche y tiene prioridad sobre los otros tags.
- Arena, grava, concreto en polvo y otros bloques con gravedad deben ser inestables por defecto.
- Toda la nieve debe tratarse como superficie inestable.
- El hielo debe seguir siendo escalable como superficie firme.
- Los yunques quedan exceptuados del comportamiento inestable automático.

## Frenado progresivo durante caídas

- Guardar la velocidad inicial al comenzar el enganche.
- Una caída lenta debe detenerse casi de inmediato.
- Una caída rápida debe producir un deslizamiento visible antes de detenerse.
- La distancia y el tiempo de frenado deben depender de la velocidad absorbida.
- En bloques sólidos, el frenado termina en reposo completo.
- En bloques inestables, el frenado termina en una velocidad de descenso constante y controlada.
- Si el pico se rompe durante el frenado, el jugador continúa con la velocidad restante.
- El servidor debe conservar autoridad total sobre posición, velocidad y daño de caída.

## Durabilidad proporcional al rescate

- Mantener 15 puntos como coste base del enganche.
- Añadir desgaste según la velocidad vertical absorbida.
- Considerar también tiempo o distancia de deslizamiento.
- Aplicar el desgaste adicional progresivamente para permitir que el pico se rompa durante el frenado.
- `Unbreaking` debe afectar tanto al coste base como al desgaste adicional.
- Evaluar desgaste periódico durante el descenso sostenido sobre bloques inestables.

## Sturdy Latch

- Encantamiento de nivel único.
- Sin él, arena, grava, concreto en polvo y nieve mantienen el descenso controlado.
- Con él, el jugador puede quedar completamente fijo después de terminar el frenado inicial.
- No elimina el deslizamiento causado por engancharse a gran velocidad.
- No elimina el desgaste proporcional a la caída.
- Será compatible con `Pick Climber` y con `Strong Grip`.

Pendiente definir rareza, costes concretos y fuentes de obtención respetando la restricción de yunque anterior.

## Diseño obligatorio posterior: Strong Grip y movimiento de techo

> Especificación completa: [`docs/README-STRONG-GRIP.md`](docs/README-STRONG-GRIP.md)

Implementar en una fase posterior, después de superficies, frenado y `Sturdy Latch`:

- `Strong Grip` de nivel único.
- Incompatibilidad entre `Pick Climber` y `Strong Grip`.
- Enganche a la cara inferior de bloques firmes.
- Coste de 20 de durabilidad por cada anclaje nuevo de techo.
- Coste de 1 de durabilidad por cada segundo completo colgado.
- Compatibilidad de `Sturdy Latch` con ambas especializaciones.
- Techos de nieve y otros materiales inestables requieren `Strong Grip + Sturdy Latch`.
- Pose elevada del brazo y pico en primera y tercera persona.
- Balanceo restringido mediante movimiento, con autoridad del servidor.
- Maniobras avanzadas con dos picotas `Strong Grip`.
- Posibilidad de rodear bordes y alcanzar techos separados por aproximadamente dos bloques de vacío desde una posición balanceada válida.
- Espacio para liberación activa con impulso.
- Doble pulsación de Shift para liberación vertical pasiva, sin salto ni impulso horizontal inicial.
- Shift mantenido conserva agachado e interacciones vanilla.

Este bloque es **mandatorio**, pero no tiene prioridad inmediata sobre la estabilización y las físicas base.

## Seguridad y limpieza

- Probar desconexión y reconexión sin grietas persistentes.
- Probar cambio de dimensión y confirmar limpieza en el nivel anterior.
- Probar muerte, cambio de gamemode, vuelo creativo, Elytra y teletransportes externos.
- Probar descarga de chunk, pistones y movimiento/desaparición del bloque ancla.
- Probar bloques con gravedad que comiencen a caer mientras están anclados.
- Probar alta latencia y paquetes atrasados.
- Probar rotura del pico durante un futuro frenado progresivo.
- Probar rotura del pico por desgaste sostenido de Strong Grip.

## Compatibilidad de herramientas

Crear:

```text
pickclimber:climbing_tools
pickclimber:excluded_climbing_tools
```

Objetivos:

- Twilight Forest.
- Eternal Starlight.
- Picos de otros mods.
- Herramientas híbridas.
- Mazas o herramientas que funcionen como pico sin pertenecer a `#minecraft:pickaxes`.
- Lista configurable para modpacks.

Las pruebas de interacción específica con bloques y máquinas modded se realizarán después de cerrar el comportamiento vanilla.

## Visuales posteriores

- Evaluar una pose equivalente en tercera persona para paredes.
- Implementar la pose elevada de Strong Grip en primera y tercera persona.
- Probar `IItemDecorator` con otras escalas de GUI y mods de interfaz.
- Evaluar indicadores distintos para superficies firmes, inestables y no escalables.
- Considerar variantes del icono de alcance según el tipo de superficie.
- Añadir indicadores para techos que requieren Strong Grip o Strong Grip + Sturdy Latch.

## Orden de trabajo previsto

1. **Completado en 0.1.14:** `Shift + clic derecho` para bloques interactivos vanilla.
2. **Completado en 0.1.14:** balance y costes actuales de `Pick Climber`.
3. Tags de superficies estables, inestables y prohibidas.
4. Frenado progresivo y durabilidad proporcional.
5. Comportamiento inestable de arena, grava, concreto en polvo y nieve.
6. Implementar `Sturdy Latch` y validar el coste combinado de yunque.
7. Implementar `Strong Grip`, exclusividad y anclajes básicos de techo.
8. Implementar pose elevada y doble pulsación de Shift.
9. Implementar balanceo y maniobras con dos picotas.
10. Compatibilidad general de herramientas, pruebas modded y pulido visual.
