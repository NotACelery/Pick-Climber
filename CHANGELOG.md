
## 0.1.6-beta

- Corregido el desenganche automático causado por `KeyMapping.consumeClick()`.
- El wall jump ahora exige una pulsación nueva de salto después de soltar la tecla.
- Mantener Espacio durante un salto, impulso o enganche ya no provoca un salto fantasma.
- Agregado diagnóstico `DETACH_JUMP`/`DETACH_PASSIVE` con edad del anclaje en `latest.log`.

# Changelog

## 0.1.5-beta

- Reescribe la sincronización física del anclaje desde la raíz.
- Sustituye `ServerPlayer#setPos` por `ServerGamePacketListenerImpl#teleport` para confirmar la posición con el cliente.
- Elimina todas las correcciones de posición mediante velocidad hacia el ancla.
- El cliente deja de cambiar posición, gravedad, vuelo o estado físico durante el enganche.
- El cliente ya no invalida anclajes de forma autónoma sin avisar al servidor.
- Un paquete atrasado de impulso ya no puede desmontar un anclaje confirmado.
- El impulso requiere un salto real reciente, estar en el aire y seguir ascendiendo.
- La velocidad positiva por sí sola siempre cae en la ruta segura de enganche.
- Cada autorización de salto solo puede consumirse una vez.
- Tocar el suelo o engancharse limpia la autorización anterior.
- Un wall jump autoriza un posible impulso posterior con el otro pico.
- Añade una línea diagnóstica por uso válido con la acción elegida y el estado del jugador.
- Protocolo de red actualizado a versión 6.

## 0.1.4-beta

- Convierte el antiguo impulso accidental en una mecánica explícita y controlada.
- Clic derecho mientras el jugador asciende por encima de `0.08 Y` ejecuta un impulso.
- Clic derecho al caer, quedar quieto o alcanzar el ápice ejecuta el enganche normal.
- El vuelo creativo activo siempre utiliza la ruta de enganche.
- El impulso y el enganche son rutas mutuamente excluyentes.
- El impulso conserva la inercia horizontal y no crea estado de anclaje ni grietas.
- Añade sincronización específica cliente-servidor para el impulso y su cooldown visual.
- Añade el encantamiento data-driven `Pick Climber I–III`.
- El pico base añade aproximadamente 1 bloque de ascenso restante.
- Cada nivel de Pick Climber añade 0,5 bloques al impulso.
- Cada nivel añade también 0,5 bloques al wall jump desde un anclaje.
- Mantiene el coste de 15 de durabilidad y el cooldown individual por pico.
- Protocolo de red actualizado a versión 5.

## 0.1.3-beta

- Refuerza la limpieza de estados cliente atrasados.
- Retira temporalmente la pose experimental de primera persona.
- Mejora el indicador del pico activo y la sincronización visual del cooldown.

## 0.1.2-beta

- Cooldown individual por pico.
- Coste aumentado a 15 de durabilidad.
- Indicador de alcance y overlays de hotbar.
- Movimiento horizontal y diagonal estabilizado.

## 0.1.1-beta

- Enganche durante caídas.
- Soporte creativo.
- Identidad persistente del pico.
- Grietas y sonido del bloque objetivo.

- Agregada portada/logo temporal del mod: el gato guardián compilador.
