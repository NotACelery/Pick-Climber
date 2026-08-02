# Pendientes posteriores a 0.1.12

## Interacción y controles

- Validar en juego la prioridad de mano secundaria y la mano principal libre con mods que añadan interacciones propias.
- Transferir el ancla al intercambiar manos con `F`, conservando UUID, cooldown, grietas y pose.
- Dejar el salto como única liberación voluntaria y eliminar completamente el detach por clic izquierdo.
- Cancelar sin impulso cuando el pico activo deja de estar equipado en ambas manos.
- Definir un bypass explícito para bloques interactivos cuando la mano principal también contiene un pico disponible.

## Encantamiento Pick Climber

- Reducir `anvil_cost` de 4 a 1.
- Aumentar `weight` de 4 a 6.
- Ajustar costes de mesa a base 5 y aumento 8 por nivel.
- Medir en juego la altura real del impulso base y de Pick Climber I–III.
- Ajustar la ventana posterior al salto real —actualmente 8 ticks— sin volver a depender solo de la velocidad.
- Evaluar si impulso y enganche deben tener costes de durabilidad diferentes.

## Física avanzada posterior

- Frenar progresivamente al enganchar durante una caída rápida.
- Deslizar una distancia proporcional a la velocidad que llevaba el jugador.
- Aumentar el daño del pico según la velocidad absorbida.
- Crear superficies firmes, inestables y no enganchables mediante tags.
- Arena, grava y bloques con gravedad —excepto yunques— deberán producir descenso controlado en vez de anclaje estático.
- Anular el daño de caída al terminar un descenso todavía controlado por el pico.

## Compatibilidad

- Twilight Forest.
- Eternal Starlight.
- Herramientas híbridas y mazas que funcionen como pico.
- Tag `pickclimber:climbing_tools` y lista configurable de exclusiones.

## Ajustes visuales posteriores

- Probar `IItemDecorator` con escalas de GUI y mods de interfaz distintos.
- Evaluar una pose equivalente en tercera persona.
