# Checklist 0.1.17-beta — superficies, deslizamiento y regresiones

## Tags de superficies

- [ ] Hielo, hielo compacto, hielo azul e hielo escarchado permiten anclaje firme.
- [ ] Yunques permiten anclaje firme pese a verse afectados por gravedad.
- [ ] Arena, arena roja, grava y concreto en polvo permiten engancharse pero descienden controladamente.
- [ ] Bloque de nieve, capa de nieve y nieve en polvo permiten engancharse y se clasifican como inestables.
- [ ] Bedrock, barrera, bloques de comando, structure block y jigsaw rechazan sin coste, cooldown, sonido ni grietas.
- [ ] Un datapack puede añadir un bloque a cada tag y la prioridad `unclimbable > unstable > stable > fallback` se respeta.

## Frenado y descenso

- [ ] Enganche desde reposo o caída lenta en piedra: queda fijo inmediatamente.
- [ ] Enganche tras caída alta en piedra: desliza visiblemente y termina fijo sin atravesar bloques.
- [ ] Enganche tras caída alta en arena o nieve: frena primero y luego continúa descendiendo a velocidad controlada.
- [ ] Una pared con obstáculo durante el deslizamiento no permite atravesarlo ni teletransporta al jugador dentro de bloques.
- [ ] Al aterrizar aún sujeto durante el descenso controlado no se cobra daño de la caída ya absorbida.
- [ ] Soltarse tras frenar inicia una caída nueva; el daño posterior no incluye la caída previamente absorbida.
- [ ] Durante frenado rápido, W/A/S/D desplaza lateralmente a la mitad de la velocidad de caída sin permitir separarse ni atravesar la pared.
- [ ] Durante descenso en grava/nieve, mirar y girar 90° o más no bloquea la cámara ni cambia la autoridad del servidor.
- [ ] La grieta abandona el bloque anterior y aparece en cada bloque actual de la ruta de deslizamiento.
- [ ] Al llegar desde grava a una pared de piedra/cobblestone, el descenso se detiene en esa superficie firme.
- [ ] Al terminar la pared inestable sin soporte, el anclaje termina de forma segura.
- [ ] Un anclaje iniciado en arena, grava, concreto en polvo o nieve aplica exactamente 40 ticks de cooldown; piedra e hielo siguen usando 20.
- [ ] La cámara no vuelve sola a una orientación previa durante frenado o deslizamiento; verificar giro a izquierda, derecha, arriba y abajo.
- [ ] W/A/S/D funciona durante `BRAKING` por caída rápida y durante `UNSTABLE_SLIDING` desde reposo.
- [ ] El bloque ancla, la grieta y la pose sincronizada cambian sin esperar el intervalo anterior de cinco ticks.
- [ ] A y D desplazan al lado visualmente correspondiente en cada orientación de pared.
- [ ] Llegar al suelo durante descenso suelta el anclaje sin impulso ni estado congelado.
- [ ] Doble Shift dentro de 7 ticks libera pasivamente; Shift mantenido no lo hace.

## Regresión 0.1.14 — bloques con menú vanilla

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
