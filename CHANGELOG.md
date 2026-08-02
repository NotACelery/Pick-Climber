## 0.1.14-beta

- Los bloques que declaran un `MenuProvider` conservan el clic derecho vanilla mientras el jugador no mantenga Shift.
- `Shift + clic derecho` permite intentar el enganche sobre hornos, mesas de crafteo, cofres y otros bloques con menú sin abrir su interfaz.
- La regla se aplica antes de seleccionar la mano y también al indicador de alcance, evitando mostrar un anclaje que el clic normal no ejecutará.
- No se utilizan listas hardcodeadas de bloques interactivos.
- Un intento inválido continúa sin consumir durabilidad ni iniciar cooldown.
- Se conserva como comportamiento intencional el detach pasivo al intentar minar o atacar con el mismo pico que sostiene el ancla.
- Rebalance de Pick Climber: `weight` 4 → 6, `anvil_cost` 4 → 1, coste mínimo base 10 → 5 y progresión 12 → 8.
- El coste máximo pasa a base 25 y progresión 8 por nivel adicional.
- No modifica impulso, wall jump, física, red, transferencia con `F`, pose ni limpieza de grietas.

## 0.1.13-beta

- Completa la transferencia del anclaje al intercambiar manos con `F`.
- Detecta el mismo pico por UUID en la mano contraria y actualiza únicamente la mano activa.
- Transferir no consume durabilidad, no reinicia cooldown, no repite sonido, no recrea grietas y no mueve el ancla.
- La pose clavada pasa a la nueva mano y la anterior vuelve al render vanilla.
- Cambiar de slot o retirar el pico de ambas manos termina el anclaje de forma pasiva; ya no se repara el UUID sobre otra herramienta distinta.
- El estado servidor guarda la dimensión exacta donde se creó el anclaje.
- Cambiar de dimensión o desconectarse limpia la grieta en el nivel original.
- El payload de anclaje sincroniza la posición del bloque y el `crackId` para permitir limpieza local.
- `ClientPlayerNetworkEvent.LoggingOut` elimina el overlay antes de destruir el `ClientLevel`.
- Los timeouts cliente y los cambios de punto también limpian overlays huérfanos.
- Protocolo interno actualizado a versión 8.
- Mantiene la prioridad secundaria y la mano principal libre de 0.1.12 sin alterar impulso, wall jump, cooldown, desgaste ni pose.

## 0.1.12-beta

- Añade un selector central de mano para todas las maniobras de Pick Climber.
- Cuando ambos picos están disponibles, la mano secundaria tiene prioridad para enganchar o impulsar.
- La interacción de la mano principal no se cancela al preferir la secundaria: colocar, usar, abrir o consumir objetos conserva el pipeline vanilla y solo un resultado `PASS` permite continuar con la izquierda.
- Con un pico secundario sosteniendo el ancla, el clic izquierdo vuelve a minar y atacar normalmente con la mano principal.
- Colocar bloques y usar objetos con la principal no desengancha el pico secundario.
- Un segundo pico disponible en la principal puede reemplazar el anclaje secundario.
- Se conserva temporalmente el detach por clic izquierdo únicamente cuando el ancla está en la propia mano principal.
- No modifica la física de impulso, wall jump, cooldown, durabilidad ni la pose clavada de 0.1.11.

## 0.1.11-beta

- Añade un render dedicado de primera persona para el pico que mantiene el anclaje.
- La herramienta entra durante 4 ticks a una pose adelantada equivalente al golpe vanilla y queda congelada allí.
- Solo se cancela y redibuja la mano que sostiene el pico activo; la otra mano conserva su render vanilla.
- La pose se refleja correctamente para mano izquierda y derecha usando el brazo real del jugador.
- Cambiar de pico reinicia la entrada visual únicamente para el nuevo pico activo.
- Soltarse restaura inmediatamente la pose idle normal.
- El cooldown sigue bajando desde el instante del enganche y permanece independiente de la pose.
- Reemplaza el gato placeholder por el nuevo icono oficial de Pick Climber.
- No modifica impulso, enganche, durabilidad, cooldown ni física estable de la 0.1.6.

## 0.1.10-beta

- Corrige la interpretación visual del cooldown durante un enganche.
- El cooldown comienza al confirmar el enganche y el overlay baja inmediatamente de 100 % a 0 %.
- El overlay ya no queda congelado al 100 % mientras el pico sigue clavado.
- Soltar el pico, saltar o cambiar al segundo pico no inicia ni reinicia el cooldown.
- El estado de pico activo queda separado del temporizador; su indicador dedicado se implementará junto con la pose clavada.
- No modifica la física, el impulso, la durabilidad ni la corrección crítica de salto de la 0.1.6.

## 0.1.9-beta

- El cooldown individual vuelve a comenzar inmediatamente al confirmar un enganche.
- El pico activo sigue mostrando el overlay al 100 % mientras permanece clavado.
- Al liberarse, el cooldown no se reinicia: solo continúa con los ticks que queden.
- Si el jugador permanece enganchado durante 20 ticks o más, el pico queda disponible al soltarse.
- Cambiar al segundo pico no reinicia ni prolonga el cooldown del primero.
- Evita que un wall jump corto deje al pico sin una ventana de bloqueo y provoque un impulso accidental al intentar volver a engancharse arriba.
- No modifica la física estable de impulso y enganche de la 0.1.6.

## 0.1.8-beta

- Corrige el overlay de cooldown completo en picos nuevos o sin cooldown.
- Evita el desbordamiento de `long` causado por `Long.MIN_VALUE - gameTime`.
- Un `ItemStack` sin `cooldown_until` ahora representa correctamente 0 ticks restantes.
- El cálculo visual comprueba que el cooldown siga activo antes de restar tiempos.
- No modifica la física, el enganche ni el impulso de la 0.1.7.

## 0.1.7-beta

- Reemplaza el overlay manual de hotbar por un `IItemDecorator` registrado en el render real del `ItemStack`.
- El pico activo muestra el mismo blanco translúcido del cooldown vanilla, congelado al 100 %.
- El cooldown de 20 ticks ya no comienza al engancharse: empieza únicamente al liberar el pico.
- Al cambiar al segundo pico, el anterior comienza su cooldown y el nuevo queda marcado como activo.
- Los estados visuales se vinculan al UUID propio de cada pico, incluso cuando ambos son del mismo material.
- El indicador funciona en hotbar, mano secundaria e inventarios que rendericen las decoraciones del objeto.
- Un pico activo queda ocupado y no puede reutilizarse para crear otro anclaje.
- El payload de anclaje ahora sincroniza el UUID exacto y los ticks de cooldown al liberar.
- Protocolo de red actualizado a versión 7.

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
