# Backlog maestro de Pick Climber

## Interacción y controles

### Completado en 0.1.12–0.1.13

- Prioridad de la mano secundaria entre picos disponibles.
- Mano principal libre para minar, atacar, colocar y usar objetos mientras la secundaria sostiene.
- Cambio de punto con otro pico disponible en la mano principal.
- Transferencia del mismo pico activo mediante `F`, conservando ancla, UUID, cooldown, pose, sonido y grietas.
- Cancelación pasiva al retirar el pico activo de ambas manos.

### Pendiente

- Dejar Espacio como única liberación voluntaria y eliminar completamente el detach por clic izquierdo.
- Definir un bypass explícito para bloques interactivos cuando la mano principal también contiene otro pico listo; evaluar `Shift + clic derecho` sin listas específicas por mod.
- Validar el pipeline con bloques, herramientas y máquinas de otros mods.

## Encantamiento Pick Climber

- Reducir `anvil_cost` de 4 a 1.
- Aumentar `weight` de 4 a 6.
- Ajustar costes de mesa a base 5 y aumento 8 por nivel.
- Medir en juego la altura real del impulso base y de Pick Climber I–III.
- Ajustar la ventana posterior al salto real —actualmente 8 ticks— sin volver a depender solo de la velocidad.
- Evaluar si impulso, enganche y wall jump deben tener costes de durabilidad distintos.

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

## Encantamiento para superficies inestables

Añadir un encantamiento separado de Pick Climber:

- Sin él, arena, grava y concreto en polvo mantienen el descenso controlado.
- Con él, el jugador puede quedar completamente fijo después de terminar el frenado inicial.
- No elimina el deslizamiento causado por engancharse a gran velocidad.
- No elimina el desgaste proporcional a la caída.
- Probablemente tendrá un solo nivel porque el efecto es binario.

Pendiente definir nombre, rareza, costes, compatibilidad con Pick Climber y fuentes de obtención.

## Seguridad y limpieza

- Probar desconexión y reconexión sin grietas persistentes.
- Probar cambio de dimensión y confirmar limpieza en el nivel anterior.
- Probar muerte, cambio de gamemode, vuelo creativo, Elytra y teletransportes externos.
- Probar descarga de chunk, pistones y movimiento/desaparición del bloque ancla.
- Probar bloques con gravedad que comiencen a caer mientras están anclados.
- Probar alta latencia y paquetes atrasados.
- Probar rotura del pico durante un futuro frenado progresivo.

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

## Visuales posteriores

- Evaluar una pose equivalente en tercera persona.
- Probar `IItemDecorator` con otras escalas de GUI y mods de interfaz.
- Evaluar indicadores distintos para superficies firmes, inestables y no escalables.
- Considerar variantes del icono de alcance según el tipo de superficie.
