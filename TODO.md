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

### Completado en 0.1.15

- Tags data-driven `stable_anchor_blocks`, `unstable_anchor_blocks` y `unclimbable_blocks`.
- Prioridad de clasificación: no escalable, inestable, estable y fallback firme vigente.
- Hielo y yunques clasificados como firmes; arena, grava, concreto en polvo y toda la nieve como inestables.
- Bloques inestables permiten anclaje incluso cuando la geometría de capas de nieve no declara una cara lateral sturdy.
- Frenado servidor-autoritativo para caídas rápidas y descenso controlado en superficies inestables.
- Cada desplazamiento del ancla durante el frenado o descenso comprueba colisiones antes de confirmar la nueva posición.

### Completado en 0.1.16

- El jugador puede desplazarse lateralmente en el plano de la pared a la mitad de su velocidad de descenso durante frenado o deslizamiento.
- El ancla, superficie activa y grietas avanzan de bloque al deslizarse; una superficie firme encontrada detiene el descenso.
- Los anclajes iniciados en superficies inestables usan un cooldown individual de 40 ticks.

### Completado en 0.1.17

- Input de cámara y movimiento de deslizamiento enviado cada tick al servidor, que sigue validando plano, velocidad y colisión.
- Las correcciones de posición usan rotación relativa y no restauran una orientación vieja de la cámara.
- El estado en movimiento se sincroniza cada tick para que el bloque ancla y la grieta cambien sin el retraso del intervalo fijo.
- A/D sigue la dirección esperada durante el deslizamiento; llegar al suelo termina el anclaje pasivamente.
- Doble Shift dentro de 7 ticks libera el anclaje sin impulso.

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

- Completado base en 0.1.15–0.1.16: se conserva la velocidad vertical inicial y una caída rápida se frena de forma visible y servidor-autoritativa.
- Completado base en 0.1.16: el ancla sigue las superficies durante el descenso y se detiene al encontrar una superficie firme o una colisión segura.
- Pendiente: calibrar valores en juego y conservar la velocidad restante si el pico se rompe durante el frenado proporcional futuro.
- El servidor conserva autoridad total sobre posición, velocidad y daño de caída.
- Balance cerrado actual: frenado solo tras más de 5 bloques de caída descendida y velocidad menor que `-0.40`; arrastre `0.75`, recuperación `0.035` y recorrido físico máximo `0.60` bloques/tick.

### Completado en 0.1.19 — frenado con dos picotas

- Si ambas manos sostienen picotas válidas al engancharse durante una caída que activa `BRAKING`, ambas reciben el coste base de 15 y participan temporalmente en el frenado.
- El agarre dual aplica dos pasos de frenado por tick y reduce aproximadamente a la mitad la distancia necesaria para absorber la misma caída; representa el doble de puntos de apoyo, no vuelo ni inmunidad.
- El cooldown de esfuerzo se comparte entre las dos picotas equipadas para impedir alternar clics y anular el frenado.
- Cada bloque vertical completo recorrido en `BRAKING` añade 10 de desgaste a cada pico participante.
- Si el segundo pico se rompe o deja de estar equipado, el ancla principal continúa con frenado simple y no se cobra a una herramienta de reemplazo.
- Dos picotas con UUID duplicado se desambigüan al enganchar para que ambas conserven coste, cooldown y desgaste propios.

## Durabilidad proporcional al rescate

- Completado en 0.1.19: 15 puntos como coste base del enganche de frenado y 10 adicionales por cada bloque vertical completo realmente deslizado.
- El desgaste adicional se aplica progresivamente, por lo que la rotura del pico activo termina el anclaje de forma segura.
- `Unbreaking` afecta tanto al coste base como al desgaste adicional mediante `hurtAndBreak`.
- Evaluar desgaste periódico durante el descenso sostenido sobre bloques inestables.

## Sturdy Latch

### Completado en 0.1.18

- Encantamiento data-driven `Sturdy Latch I`, compatible con todas las picotas actuales.
- Sin él, arena, grava, concreto en polvo y nieve mantienen el descenso controlado.
- Con él, una caída leve queda fija al engancharse; una caída fuerte completa primero `BRAKING` y luego queda fija.
- No elimina el frenado ni el futuro desgaste proporcional; reduce el cooldown inicial inestable de 40 a 20 ticks.
- Valores actuales: `weight 3`, `anvil_cost 1`, coste mínimo 12 y máximo 42.

Pendiente validar junto con Strong Grip el presupuesto final de yunque y la exclusividad entre especializaciones.

## Diseño obligatorio posterior: Strong Grip y movimiento de techo

> Especificación completa: [`docs/README-STRONG-GRIP.md`](docs/README-STRONG-GRIP.md)

### Completado en 0.1.22

- `Strong Grip I` data-driven y exclusivo con `Pick Climber` mediante tag de exclusividad.
- Enganche estático en cara inferior de techos firmes; los techos inestables exigen también `Sturdy Latch I`.
- Coste de 20 por anclaje de techo y 1 de durabilidad por cada 20 ticks completos, respetando Unbreaking y `F`.

### Completado en 0.1.23

- Balanceo restringido de techo con radio inicial de `0.95`, reducido 30 % a `0.665` en 0.1.24; aceleración, amortiguación y velocidad máxima autoritativas del servidor.
- El retorno lento confirma desplazamientos pequeños cada tick y evita acumularlos en saltos de `0.05` bloques.
- Colisiones comprobadas antes de confirmar cada posición del arco pendular.
- Espacio conserva hasta `0.38` de momento pendular acumulado y suma `0.65` solo en la dirección de `W/A/S/D` orientada por cámara.
- Doble Shift y clic izquierdo con el pico activo liberan verticalmente con velocidad horizontal cero.

### Completado en 0.1.25

- Pose elevada de primera persona para el brazo y la picota activos en anclajes de techo.
- Entrada suave de 4 ticks, orientación reflejada por brazo y supresión del swing vanilla superpuesto.
- La transferencia con `F` mueve la pose sin reiniciar la transición del mismo pico.
- La vista local en tercera persona eleva el brazo activo y alinea la picota sostenida mediante la pose del modelo.
- Los cambios de techo a pared resuelven una altura libre cercana cuando el destino ideal solaparía la hitbox con el techo, manteniendo paridad entre indicador y servidor.
- Espacio usa la direccional pulsada para el impulso de techo y la combina vectorialmente con el momento pendular acumulado; el límite del radio no elimina esa energía de liberación.
- El destino de balanceo validado se aplica suavemente mediante el sync de techo, con teletransporte absoluto periódico como confirmación autoritativa.

Pendiente en fases posteriores:

- Validar que Shift mantenido conserve agachado e interacciones vanilla también sobre máquinas modded.

Validado manualmente en 0.1.25:

- Cambio con `F`, transferencia visual del brazo y nuevo enganche con una segunda picota durante el balanceo.
- Liberación pasiva y salto con momento pendular acumulado.
- Extensión efectiva del alcance desde una posición balanceada válida, cruce de huecos y transición alrededor de bordes hacia una pared superior.
- Un nuevo enganche termina intencionalmente el momento previo porque confirma una nueva posición autoritativa.

Implementado en 0.1.26, pendiente de validación con dos clientes:

- La pose elevada de techo se sincroniza con observadores mediante un payload visual separado de la física.
- El cambio con `F`, el cambio techo/pared y el desenganche actualizan o limpian la mano elevada remota.
- Una renovación periódica permite que un cliente que empieza a observar tarde reciba la pose; un timeout evita estados visuales huérfanos.

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

### Completado en 0.1.26

```text
pickclimber:climbing_tools
pickclimber:excluded_climbing_tools
```

- La elegibilidad se resuelve en un clasificador central compartido por lógica, render y cooldown.
- `climbing_tools` incluye `#minecraft:pickaxes` y una entrada opcional explícita para `eternal_starlight:thermal_springstone_hammer`.
- `excluded_climbing_tools` excluye la picota de madera y siempre tiene prioridad sobre inclusión.
- Ambos tags admiten ampliación desde datapacks y modpacks.

Validado manualmente:

- Picotas de Eternal Starlight y Twilight Forest.
- Exclusión funcional de la picota de madera.
- Cambio de mano y encantamientos con herramientas compatibles.

Objetivos:

- Probar herramientas híbridas añadidas únicamente por `pickclimber:climbing_tools`, sin pertenecer a `#minecraft:pickaxes`.
- Probar extensión y exclusión simultáneas mediante datapack.
- Validar bloques con menú y máquinas de mods sin listas hardcodeadas.

Las pruebas de interacción específica con bloques y máquinas modded se realizarán después de cerrar el comportamiento vanilla.

## Visuales posteriores

- **Release 1.0.0:** baseline público cerrado sobre las funciones validadas de 0.1.27-beta.
- Evaluar una pose equivalente en tercera persona para paredes.
- Validar la pose elevada de Strong Grip con dos clientes en multijugador, incluyendo `F`, detach, dimensión y desconexión.
- Probar `IItemDecorator` con otras escalas de GUI y mods de interfaz.
- **Implementado en 0.1.27:** String coloreado para superficies firmes, inestables, no escalables, obstrucción, cooldown y alcance; texto diagnóstico inferior retirado tras la validación visual.
- **Implementado en 0.1.27:** requisitos separados para Strong Grip y Strong Grip + Sturdy Latch en techos.
- **Ajustado en 0.1.27:** HUD visible hasta 3 bloques y descenso inestable a `0.136` bloques/tick sin Sturdy Latch.
- **Ajustado en 0.1.27:** los cambios de punto de techo evalúan Strong Grip en la herramienta libre y los clics rechazados explican el motivo en la barra de acción; Sturdy Latch conserva aviso visual sin mensaje de rechazo.
- Evaluar sonidos suaves de tensión y partículas diferenciadas para nieve, hielo y frenado.

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
10. **Compatibilidad base completada en 0.1.26:** tags de herramientas y pruebas con Eternal Starlight/Twilight Forest.
11. **Completado en 0.1.27:** indicadores de superficie, requisitos de techo, cooldown, obstrucción y alcance.
12. **Siguiente:** validar con dos clientes la pose remota ya implementada y pulir sonidos, partículas y poses corporales adicionales.
