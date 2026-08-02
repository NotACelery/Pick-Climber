# Checklist 0.1.12-beta — prioridad secundaria y mano principal libre

## Selección de mano

- [ ] Con dos picos disponibles, el primer enganche usa la mano secundaria.
- [ ] La pose congelada aparece únicamente en el pico secundario seleccionado.
- [ ] Si el pico secundario está en cooldown, el principal puede enganchar.
- [ ] Si el pico secundario es el ancla activa, un pico principal disponible puede sustituirlo.
- [ ] Cambiar de pico limpia correctamente la pose anterior, las grietas anteriores y mantiene cooldowns independientes.

## Mano principal libre con ancla secundaria

- [ ] Mantener clic izquierdo mina normalmente con la mano principal.
- [ ] Atacar una entidad no suelta el ancla secundaria.
- [ ] Colocar un bloque desde la principal no suelta el ancla.
- [ ] Usar comida, cubos u otros objetos de la principal conserva su resultado vanilla.
- [ ] Abrir un cofre, puerta u otro bloque interactivo con la principal vacía o sosteniendo un objeto no escalador no dispara el pico secundario.
- [ ] Romper deliberadamente el bloque ancla termina el enganche de forma pasiva y sin wall jump.

## Pipeline de clic derecho

- [ ] Si la acción principal consume el clic, la secundaria no se activa.
- [ ] Si la acción principal devuelve `PASS`, el pico secundario intenta enganchar.
- [ ] Un intento rechazado no consume durabilidad ni inicia cooldown.
- [ ] El icono de alcance solo aparece cuando al menos una mano puede intentar la maniobra.

## Compatibilidad temporal del clic izquierdo

- [ ] Con ancla secundaria, el clic izquierdo nunca envía `DetachRequestPayload(false)`.
- [ ] Con ancla principal, el comportamiento legado de soltarse con clic izquierdo sigue funcionando.
- [ ] Espacio mantiene la detección segura por flanco y no reaparece el salto automático.

## Regresión física y visual

- [ ] Impulso, enganche, wall jump y rescate durante caídas se comportan igual que en 0.1.11.
- [ ] El cooldown comienza al engancharse y continúa bajando aunque el pico siga clavado.
- [ ] La pose fija permanece únicamente en la herramienta activa.
- [ ] Grietas, sonido, 15 puntos de desgaste y `Unbreaking` siguen funcionando.
- [ ] Probar en supervivencia y creativo.
