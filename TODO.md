# Pendientes posteriores a 0.1.9

- Congelar la pose del pico hacia adelante mientras está enganchado mediante render dedicado por mano.
- Mantener la transformación separada para mano derecha e izquierda sin acumular animaciones vanilla.
- Prioridad de mano secundaria y mano principal libre para minar/colocar.
- Transferencia segura del pico activo mediante F.
- Físicas futuras de frenado progresivo y superficies inestables.

# Pendientes de Pick Climber

## Interacción y visuales

- Priorizar la mano secundaria para enganchar.
- Permitir minería y colocación con la principal mientras la secundaria sostiene.
- Transferir el ancla al intercambiar manos con `F`.
- Dejar el salto como única liberación voluntaria.
- Cancelar sin impulso al cambiar el pico activo fuera de las manos.
- Recuperar la pose del pico clavado mediante renderizado dedicado por mano.
- Probar el nuevo `IItemDecorator` con escalas de GUI y mods de interfaz distintos.

## Física avanzada posterior

- Frenar progresivamente al enganchar durante una caída rápida.
- Deslizar una distancia proporcional a la velocidad que llevaba el jugador.
- Aumentar el daño del pico según la velocidad absorbida.
- Crear superficies firmes, inestables y no enganchables mediante tags.
- Arena, grava y bloques con gravedad —excepto yunques— deberán producir descenso controlado en vez de anclaje estático.
- Anular el daño de caída al terminar un descenso todavía controlado por el pico.

## Balance del impulso y encantamiento

- Medir en juego la altura real del impulso base y de Pick Climber I–III.
- Ajustar la ventana posterior al salto real —actualmente 8 ticks— sin volver a depender solo de la velocidad.
- Mantener `0.08` únicamente como comprobación secundaria de ascenso.
- Revisar peso, costes y disponibilidad del encantamiento en mesa, libros y aldeanos.
- Evaluar si impulso y enganche deben tener costes de durabilidad diferentes.

## Compatibilidad

- Twilight Forest.
- Eternal Starlight.
- Herramientas híbridas y mazas que funcionen como pico.
- Tag `pickclimber:climbing_tools` y lista configurable de exclusiones.

## Ajustes visuales posteriores

- Ajustar el valor exacto de la pose clavada después de pruebas in-game.
- Evaluar una pose equivalente en tercera persona.
