# Checklist 0.1.14-beta — bloques interactivos y balance del encantamiento

## Bloques con menú vanilla

- [ ] Clic derecho normal con una picota sobre una mesa de crafteo abre su interfaz y no engancha.
- [ ] Clic derecho normal sobre horno, ahumador y alto horno abre su interfaz y no engancha.
- [ ] Clic derecho normal sobre cofre, cofre atrapado, barril y cofre de Ender abre su interfaz y no engancha.
- [ ] `Shift + clic derecho` sobre una cara vertical válida de esos bloques crea el anclaje sin abrir el menú.
- [ ] `Shift + clic derecho` sobre una cara inválida no gasta durabilidad ni inicia cooldown.
- [ ] Sin Shift, el indicador de alcance no aparece sobre un bloque con menú.
- [ ] Con Shift, el indicador aparece cuando la cara, distancia, cooldown y colisión permiten el anclaje.
- [ ] Probar con el pico únicamente en la mano principal.
- [ ] Probar con el pico únicamente en la mano secundaria.
- [ ] Probar con dos picos disponibles: la secundaria conserva prioridad para el anclaje.
- [ ] Probar con un bloque de construcción en la principal y el pico anclado en la secundaria: `Shift + clic derecho` coloca sobre el bloque interactivo y no secuestra el clic.

## Detach intencional por clic izquierdo

- [ ] Ancla secundaria + minar con la principal: el jugador sigue sujeto.
- [ ] Ancla secundaria + atacar con la principal: el jugador sigue sujeto.
- [ ] Ancla principal + clic izquierdo: termina el anclaje sin impulso.
- [ ] Transferir con `F` a la secundaria permite volver a minar con la principal sin soltarse.
- [ ] El detach limpia pose, gravedad y grietas correctamente.

## Balance de Pick Climber

- [ ] Pick Climber sigue teniendo niveles I, II y III.
- [ ] Aparece con mayor frecuencia relativa por `weight: 6`.
- [ ] La mesa respeta coste mínimo base 5 y aumento 8 por nivel.
- [ ] El JSON carga sin errores de datapack.
- [ ] Combinar libros en yunque refleja `anvil_cost: 1`.
- [ ] Aplicar Pick Climber a una picota previamente encantada resulta más viable que en 0.1.13.
- [ ] Las alturas de impulso y wall jump no cambiaron con el rebalance.

## Regresión 0.1.13

- [ ] La secundaria sigue teniendo prioridad entre picos disponibles.
- [ ] `F` transfiere el mismo pico sin durabilidad, sonido, cooldown ni movimiento adicional.
- [ ] El cooldown individual sigue bajando desde el instante del uso.
- [ ] Enganche desde reposo, caída y ápice sigue funcionando.
- [ ] Impulso solo después de un salto real.
- [ ] No reaparece el wall jump automático.
- [ ] Las grietas se limpian al cambiar de punto, dimensión o desconectarse.
- [ ] La pose clavada sigue correcta en ambas manos.
