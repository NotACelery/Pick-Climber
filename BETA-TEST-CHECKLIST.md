# Checklist 0.1.13-beta — manos, transferencia y limpieza de grietas

## Prioridad secundaria y mano principal libre

- [ ] Con dos picos disponibles, el primer enganche utiliza la mano secundaria.
- [ ] Si la secundaria está en cooldown, la principal puede enganchar.
- [ ] Con ancla secundaria, la principal mina bloques sin soltarse.
- [ ] Con ancla secundaria, atacar entidades no cancela el anclaje.
- [ ] Colocar bloques, usar comida, cubos y objetos conserva el resultado vanilla.
- [ ] Una interacción principal exitosa impide que la secundaria consuma el mismo clic.
- [ ] Si la principal devuelve `PASS`, la secundaria puede intentar el enganche.
- [ ] Otro pico disponible en la principal puede reemplazar el ancla secundaria.

## Transferencia con F

- [ ] Ancla principal + `F`: el mismo pico pasa a secundaria sin soltar al jugador.
- [ ] Ancla secundaria + `F`: el mismo pico pasa a principal sin soltar al jugador.
- [ ] Probar la transferencia con la otra mano vacía.
- [ ] Probar la transferencia con dos picos distintos.
- [ ] No se consumen otros 15 puntos de durabilidad.
- [ ] El cooldown no se reinicia ni se prolonga.
- [ ] No se repiten sonido ni grietas.
- [ ] El punto de apoyo y la posición del jugador no cambian.
- [ ] La pose clavada pasa a la nueva mano.
- [ ] La mano anterior recupera inmediatamente el render normal.
- [ ] Repetir `F` varias veces no duplica UUID ni estado.

## Cambio de slot y pérdida del pico

- [ ] Cambiar el slot de un pico activo en la principal termina el anclaje sin impulso.
- [ ] Soltar o destruir el pico activo termina el anclaje sin impulso.
- [ ] Un pico distinto no hereda el UUID ni el estado activo.
- [ ] Volver a equipar el pico anterior no restaura automáticamente un anclaje terminado.

## Hotfix de grietas

- [ ] Engancharse, salir del mundo y volver: la grieta no permanece.
- [ ] En multijugador, los demás jugadores ven desaparecer la grieta al desconectarse el escalador.
- [ ] Cambiar de dimensión elimina la grieta en la dimensión anterior.
- [ ] Un timeout cliente no deja grietas huérfanas.
- [ ] Cambiar de punto limpia el bloque anterior inmediatamente.
- [ ] Wall jump, rotura del bloque y pérdida del pico siguen limpiando la grieta.

## Regresión física y visual

- [ ] Impulso solo después de un salto real.
- [ ] Enganche desde reposo, caída y ápice.
- [ ] No reaparece el wall jump automático.
- [ ] Cooldowns independientes y descendentes desde el instante del uso.
- [ ] Desgaste de 15 y `Unbreaking` correctos.
- [ ] Pose fija correcta en ambas manos.
- [ ] Probar en supervivencia, creativo y con alta latencia si es posible.
