# Pick Climber — Testing 1.1.0-dev.3 / Phase 0 final integration

Baseline de comparación: **1.0.3 estable**.

Objetivo: demostrar que la modularización de Phase 0 conserva el comportamiento validado de 1.0.3, salvo el fix aislado del reporte de cooldown inestable de 40 ticks.

No iniciar las features visibles de 1.1.0 hasta completar este checklist con el JAR exacto de `1.1.0-dev.3` (o el snapshot de reparación que lo sustituya dentro de Phase 0.9).

## A. Build y quality gates

- [ ] Compilar con Java 21 / NeoForge 21.1.235 mediante `clean build --stacktrace`.
- [ ] `verifySourceQuality` pasa dentro de Gradle `check`.
- [ ] `verifyArchitectureBoundaries` pasa con configuration cache habilitado.
- [ ] Confirmar que el verifier ya no produce el error de `file(...)` observado en `dev.2`.
- [ ] Confirmar JAR exacto y metadata/version `1.1.0-dev.3`.
- [ ] No hay warnings/errores nuevos que indiquen payloads mal registrados o código client-only cargado en dedicated server.

## B. Anchors básicos y evaluación unificada

- [ ] Pared estable: clic derecho crea anchor exactamente como 1.0.3.
- [ ] Cara superior (`UP`) continúa sin aceptarse como wall anchor normal.
- [ ] Bloque fuera del alcance real no consume durabilidad ni cooldown.
- [ ] Target con colisión real no se acepta.
- [ ] Target corregible por altura/espacio conserva el comportamiento validado de 1.0.3.
- [ ] Bloque `UNCLIMBABLE`: no crea anchor.
- [ ] Bloque `UNSTABLE`: conserva su comportamiento actual.
- [ ] El HUD y el intento real coinciden para READY/UNSTABLE/UNCLIMBABLE/requisitos/cooldown/rango/obstrucción.
- [ ] Una consulta visual/`canAttemptAnchor` no cambia UUID, cooldown, durabilidad, posición, cracks ni red.

## C. Interacciones y prioridad de manos

- [ ] Clic normal sobre crafting table/horno/cofre abre el bloque y no engancha.
- [ ] VNS/Cutter u otra máquina BlockEntity modded conserva el clic normal y no muestra String sin Shift.
- [ ] `Shift + clic derecho` sobre una cara válida de bloque interactivo fuerza el anchor.
- [ ] `Shift + clic derecho` inválido no roba una interacción que deba continuar ni gasta recursos.
- [ ] Con dos herramientas válidas, off-hand conserva prioridad.
- [ ] Intercambiar el pico activo con `F` conserva el anchor sin coste/cooldown extra.
- [ ] Mano principal libre puede seguir minando/colocando/usando mientras off-hand mantiene el anchor.
- [ ] Clic izquierdo con la herramienta activa produce detach pasivo; clic izquierdo con la mano/herramienta libre no lo cancela.

## D. Identidad, cooldown y durabilidad

- [ ] Dos herramientas con UUID duplicado se separan sólo al comprometer la operación que requiere identidades distintas.
- [ ] Cada herramienta conserva cooldown individual después de moverla de slot/mano.
- [ ] Wall attach consume exactamente el coste 1.0.3.
- [ ] Ceiling attach consume exactamente el coste 1.0.3.
- [ ] Boost consume exactamente el coste 1.0.3.
- [ ] Braking base/per-block y soporte de segunda picota conservan costes 1.0.3.
- [ ] Sustained ceiling wear conserva la cadencia 1.0.3.
- [ ] Unbreaking sigue aplicando mediante vanilla `hurtAndBreak`.
- [ ] Si el coste rompe la herramienta durante un boost, el vector del boost sigue calculándose con los encantamientos que la herramienta tenía antes del daño, como en 1.0.3.
- [ ] Cooldown normal sigue siendo 20 ticks donde corresponde.
- [ ] Cooldown inestable de 40 ticks se mantiene autoritativamente y el cliente muestra/reportea la duración restante real, sin truncarla a 20.

## E. Boost, wall jump y jump latch

- [ ] Velocidad positiva sin salto real no autoriza boost.
- [ ] Salto real + ascenso dentro de la ventana autoriza boost.
- [ ] Boost no reevalúa una superficie diferente después de la evaluación inicial.
- [ ] Wall jump exige soltar y volver a pulsar Espacio según el latch validado.
- [ ] Dirección/magnitud del wall jump coincide con 1.0.3.

## F. Braking y superficies inestables

- [ ] Caída peligrosa entra a BRAKING en los mismos umbrales de 1.0.3.
- [ ] Arrastre, recuperación y velocidad máxima de frenado coinciden con 1.0.3.
- [ ] La dirección diagonal de frenado se fija como antes.
- [ ] Segunda picota válida duplica la deceleración/participa como antes.
- [ ] Cambio entre bloques mientras frena conserva surface/cracks/desgaste correctos.
- [ ] Descenso inestable sin Sturdy Latch mantiene la velocidad validada.
- [ ] Sturdy Latch fija la superficie inestable en los mismos casos y conserva su cooldown reducido.

## G. Strong Grip / techo

- [ ] Techo estable requiere Strong Grip y engancha con coste inicial correcto.
- [ ] Techo inestable requiere Strong Grip + Sturdy Latch.
- [ ] Techo sin encantamiento informa el requisito correcto y no consume recursos.
- [ ] Balanceo conserva radio, damping, aceleración, colisiones y autoridad de servidor.
- [ ] Input W/A/S/D durante balanceo produce la misma respuesta que 1.0.3.
- [ ] Release voluntario con Espacio conserva impulso + momento acumulado.
- [ ] Release pasivo anula el impulso horizontal cuando corresponde.
- [ ] Desgaste sostenido de techo continúa cada segundo según baseline.
- [ ] Transición pared/techo y transferencia de mano con `F` conservan pose y física.

## H. Lifecycle y cleanup

- [ ] Detach manual restaura gravedad/flying exactamente una vez.
- [ ] Pérdida/rotura de la herramienta activa produce cleanup correcto.
- [ ] Pérdida/destrucción/invalidez del bloque anchor produce cleanup correcto.
- [ ] Stale attachment se recupera sin dejar `noGravity`, flying, cracks o pose huérfanos.
- [ ] Logout limpia estado servidor, cliente y pose remota.
- [ ] Cambio de dimensión no deja estado del anchor anterior.
- [ ] Detach por hot-paths distintos termina usando el mismo resultado autoritativo.
- [ ] No quedan cracks de bloque después de detach/cleanup.

## I. HUD y presentación 1.0.3

- [ ] String READY conserva su color y caja actual.
- [ ] El shader tint no contamina Jade, textos de eventos u otros overlays.
- [ ] El icono se oculta sobre bloque interactivo sin Shift y reaparece con Shift si procede.
- [ ] OUT_OF_RANGE entre 1.5 y 3 bloques conserva comportamiento.
- [ ] Más de 3 bloques el indicador sigue oculto.
- [ ] OBSTRUCTED continúa reservado a un anchor potencial dentro del alcance pero físicamente bloqueado.
- [ ] Los nuevos `ClimbRuntimeGate` / `ClimbPresentationGate` con defaults Phase 0 no cambian nada visible.

## J. Networking / multiplayer

- [ ] Dedicated server inicia sin cargar clases client-only.
- [ ] Attach/detach local sincroniza correctamente con protocolo 13.
- [ ] Boost final sincroniza vector correcto al cliente.
- [ ] Slide input W/A/S/D/yaw/pitch llega validado al servidor.
- [ ] Dos clientes: observador ve pose elevada Strong Grip correcta.
- [ ] Cambio `F`, techo/pared y detach actualizan al observador.
- [ ] Refresco remoto cada 20 ticks conserva la pose para observers tardíos.
- [ ] Timeout remoto limpia pose huérfana tras pérdida de sync.
- [ ] Disconnect/dimension change no deja pose remota pegada.

## K. Compatibilidad data-driven

- [ ] `#pickclimber:climbing_tools` sigue aceptando herramientas externas compatibles.
- [ ] `#pickclimber:excluded_climbing_tools` continúa ganando sobre inclusión.
- [ ] Tags de `stable`, `unstable` y `unclimbable` mantienen precedencia 1.0.3.
- [ ] Datapack que modifica esos tags altera la clasificación sin tocar código.
- [ ] `pickclimber:interactive_blocks` sigue pudiendo marcar direct-use blocks externos.
- [ ] VNS/Cutter y al menos otra máquina modded representativa conservan interacción normal/Shift.

## L. Aceptación final de Phase 0

- [ ] Repetir smoke test con el JAR 1.0.3 y el JAR Phase 0 en la misma instancia/perfil de mods.
- [ ] Ninguna diferencia de física/balance no documentada.
- [ ] Ninguna diferencia de prioridad de interacción no documentada.
- [ ] Ninguna regresión de HUD/render/shader.
- [ ] Ninguna regresión de dedicated server/multiplayer.
- [ ] `ROADMAP.md`, `BUILD-STATUS.md`, `docs/DEVELOPMENT.md` y `SOURCE-MANIFEST.json` corresponden al source aceptado.
- [ ] Phase 0 se marca COMPLETE sólo después de este checklist.
