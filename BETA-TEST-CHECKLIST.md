# Checklist de pruebas — 0.1.10-beta

## Cooldown en tiempo real

- [ ] Al engancharse, el overlay aparece lleno y comienza a bajar inmediatamente.
- [ ] El overlay llega a cero después de 20 ticks aunque el pico siga clavado.
- [ ] Soltarse antes de 20 ticks conserva solo la fracción restante, sin reiniciarla.
- [ ] Soltarse después de 20 ticks no crea un cooldown nuevo.
- [ ] Cambiar del pico A al B no reinicia el cooldown de A y B inicia el suyo al engancharse.
- [ ] Alternar dos picos no permite spam de enganches: cada uso exitoso tiene sus propios 20 ticks.


## Cooldown al enganchar

- [ ] Sacar dos picos nuevos: ninguno presenta overlay.
- [ ] Engancharse con el pico A: A queda visualmente cubierto al 100 %.
- [ ] Soltarse antes de 20 ticks: A muestra solo la fracción restante y termina normalmente.
- [ ] Permanecer enganchado más de 20 ticks: al soltarse, A queda disponible inmediatamente.
- [ ] Cambiar de A a B antes de 20 ticks: B queda cubierto al 100 % y A continúa con el tiempo restante, sin reiniciarse.
- [ ] Cambiar de A a B después de 20 ticks: A queda disponible al instante y B pasa a ser el activo.
- [ ] Hacer un wall jump rápido y pulsar clic derecho durante el ascenso: el mismo pico sigue bloqueado durante el tiempo restante y no produce un impulso accidental.
- [ ] Cuando termine el cooldown y el jugador esté cayendo o cerca del ápice, el pico puede volver a engancharse.
- [ ] Dos picos del mismo material conservan UUID, overlays y cooldowns independientes.

## Regresión funcional

- [ ] Desde reposo, clic derecho engancha y no impulsa.
- [ ] Durante un salto real, clic derecho produce el impulso esperado.
- [ ] Durante una caída, clic derecho sirve como rescate.
- [ ] No reaparece el desenganche automático corregido en 0.1.6.
- [ ] Grietas, sonido y desgaste de 15 funcionan.
- [ ] Unbreaking sigue afectando el desgaste.
