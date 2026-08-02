# Checklist 0.1.6-beta — corrección crítica

## Prueba principal desde reposo

- [ ] Permanecer completamente quieto frente a una pared.
- [ ] Usar un pico 20 veces, esperando su cooldown entre intentos.
- [ ] Cada intento debe terminar enganchado.
- [ ] Ningún intento puede producir impulso vertical.
- [ ] `latest.log` debe mostrar `action=ATTACH` en todos esos intentos.

## Sincronización del anclaje

- [ ] Engancharse apuntando arriba, abajo y a los lados.
- [ ] El jugador llega al punto y permanece inmóvil.
- [ ] No existe rubber-banding entre la posición anterior y el ancla.
- [ ] Mantener teclas de movimiento no genera velocidad acumulada.
- [ ] Al soltar una tecla, el jugador no sale disparado.
- [ ] La grieta y el indicador del pico permanecen estables.

## Impulso autorizado

- [ ] Saltar normalmente y usar el pico durante el ascenso produce impulso.
- [ ] El log muestra `action=BOOST`.
- [ ] Usar el pico por segunda vez durante el mismo salto no vuelve a impulsar con ese ascenso ya consumido.
- [ ] Al tocar el suelo, un nuevo salto vuelve a autorizar el impulso.
- [ ] Wall jump y uso del segundo pico durante el ascenso permiten encadenar un impulso.

## Salvamento durante caídas

- [ ] Caer desde suficiente altura y engancharse detiene la caída.
- [ ] El log muestra `action=ATTACH`, nunca `BOOST`.
- [ ] El daño acumulado de caída queda en cero.
- [ ] Tras un impulso, comenzar a caer y usar el otro pico permite engancharse.
- [ ] Un intento fallido o un cooldown nunca deja al jugador bloqueado para futuros enganches.

## Creativo

- [ ] Quieto en creativo: engancha.
- [ ] Cayendo en creativo: engancha.
- [ ] Ascendiendo mediante vuelo creativo: engancha, no impulsa.
- [ ] Activar y desactivar vuelo después de soltarse funciona normalmente.

## Regresión

- [ ] Movimiento horizontal y diagonal sigue funcionando.
- [ ] Enganche e impulso consumen 15 de durabilidad.
- [ ] Unbreaking funciona.
- [ ] Los cooldowns siguen siendo individuales.
- [ ] Pick Climber I–III modifica impulso y wall jump.
- [ ] Romper el bloque ancla libera sin impulso residual.

## Prueba crítica de salto fantasma

- [ ] Saltar varias veces lejos de una pared, quedarse quieto y engancharse: no debe saltar solo.
- [ ] Engancharse manteniendo Espacio: debe permanecer sujeto.
- [ ] Soltar Espacio y volver a pulsarlo: debe ejecutar exactamente un wall jump.
- [ ] Impulsarse con Espacio mantenido y luego engancharse con el otro pico: no debe desengancharse automáticamente.
- [ ] Verificar en `latest.log` que solo aparezca `DETACH_JUMP` después de una pulsación nueva.
