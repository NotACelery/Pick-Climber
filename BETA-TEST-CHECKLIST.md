# Checklist 0.1.11-beta — pose del pico clavado

## Pose principal

- [ ] Enganchar con el pico de la mano principal inicia una entrada visual corta.
- [ ] Tras aproximadamente 4 ticks, el pico queda congelado hacia adelante.
- [ ] El pico no termina la animación ni vuelve a idle mientras siga enganchado.
- [ ] Saltar o desengancharse restaura inmediatamente la pose normal.

## Dos manos

- [ ] Enganchar con el segundo pico mueve la pose fija exclusivamente al nuevo pico activo.
- [ ] La mano anterior vuelve a idle sin deformaciones ni transformaciones acumuladas.
- [ ] La pose de la mano izquierda aparece reflejada correctamente.
- [ ] La mano no activa conserva sus animaciones vanilla normales.

## Cooldown y física

- [ ] El overlay de cooldown comienza al engancharse y baja mientras el pico sigue clavado.
- [ ] La pose fija continúa aunque el cooldown visual llegue a cero.
- [ ] Impulso, enganche, wall jump y rescate durante caídas se comportan igual que en 0.1.10.
- [ ] No reaparece el salto automático corregido en 0.1.6.

## Compatibilidad visual

- [ ] Probar pico vanilla sin encantamiento.
- [ ] Probar pico encantado con glint.
- [ ] Probar mano principal configurada como izquierda en las opciones de skin.
- [ ] Probar con FOV normal y FOV alto.
