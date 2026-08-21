## 1.0.1 - 2026-08-20

### Interaction priority hotfix

- Pick Climber now waits until NeoForge's post-block interaction phase before anchoring.
- Blocks that consume right click for their own GUI or interaction keep priority, including Easy Villagers Farmers and Easy Farmer's Delight Compat Farmers.
- Off-hand climbing-tool priority is preserved when the targeted block does not handle the click.
- Shift-right-click can still reach Pick Climber when Minecraft skips the block interaction while sneaking.

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

## 1.0.0

- Primera release pública estable de Pick Climber para Minecraft 1.21.1 y NeoForge 21.1.235.
- Consolida escalada de paredes, frenado progresivo, superficies inestables, maniobras con dos picotas y movimiento de techo.
- Incluye Pick Climber I–III, Sturdy Latch I y Strong Grip I con sus compatibilidades y especializaciones vigentes.
- Publica indicadores de anclaje, mensajes localizados, poses elevadas y selección data-driven de herramientas y superficies.
- Declara `celerbi` como autor y localiza la descripción de la lista de mods en inglés, español general y español de Chile.
- Los scripts de Windows y Linux leen la versión desde `gradle.properties` y verifican el JAR exacto antes de declarar éxito.
- No modifica el protocolo 13 ni el balance físico validado en 0.1.27-beta.

## 0.1.27-beta

- Reemplaza el indicador genérico de cuerda por una banda compacta y localizada bajo la mira.
- Distingue anclaje firme, superficie inestable, superficie no escalable, obstrucción, cooldown y destino fuera del alcance real.
- Los techos informan por separado si requieren Strong Grip o Strong Grip + Sturdy Latch.
- La evaluación visual reutiliza las mismas comprobaciones de herramienta, cara, hitbox, distancia y prioridad de manos del enganche sin ejecutar física ni consumir recursos.
- Los bloques con menú conservan el indicador oculto durante uso normal y lo habilitan con Shift.
- El String vuelve a ser el único indicador permanente bajo la mira, teñido y enmarcado por estado; se retira el texto diagnóstico inferior tras validar visualmente los colores.
- Suprime completamente el HUD sobre `MISS`, entidades y bloques situados a más de 3 bloques del punto de vista.
- Evalúa el radio de 1.5 bloques antes de buscar una hitbox corregida, evitando confundir destinos lejanos con `ANCHOR OBSTRUCTED`.
- Un agarre inestable sin Sturdy Latch usa cian; con Sturdy Latch pasa a verde porque el resultado será firme.
- Los requisitos de un nuevo punto de techo se calculan sobre la picota libre; el pico Strong Grip ya ocupado no convierte erróneamente el estado en cooldown.
- Un clic derecho rechazado muestra en la barra de acción el motivo localizado: Strong Grip, cooldown, alcance, bloque obstructivo, falta de espacio o entidad.
- Los avisos de Sturdy Latch no generan mensaje de rechazo porque el descenso inestable permitido conserva su comportamiento diferenciado.
- Limita el HUD a 3 bloques: muestra `OUT OF RANGE` entre 1.5 y 3, se oculta más lejos y solo evalúa `ANCHOR OBSTRUCTED` dentro del alcance físico.
- Ajusta el descenso inestable sin Sturdy Latch de `0.128` a `0.136` bloques/tick, 70 % más rápido que la base original.
- No modifica protocolo 13, alcance físico de anclaje, durabilidad, cooldown ni selección autoritativa del servidor.

## 0.1.26-beta

- Añade los tags de objeto `pickclimber:climbing_tools` y `pickclimber:excluded_climbing_tools` para compatibilidad configurable con herramientas de otros mods.
- Toda la elegibilidad durante escalada, cooldown y render pasa por un clasificador central; una exclusión siempre prevalece sobre una inclusión.
- `climbing_tools` hereda `#minecraft:pickaxes` para conservar compatibilidad general e incluye de forma opcional la maza-picota `eternal_starlight:thermal_springstone_hammer`.
- La picota de madera se incluye en `excluded_climbing_tools` y deja de poder iniciar o mantener anclajes.
- La integración con Eternal Starlight es opcional mediante tag y no añade dependencias de carga.
- Valida manualmente las picotas de Eternal Starlight y Twilight Forest, la exclusión de madera, el cambio de mano y los encantamientos.
- Sincroniza la pose elevada de Strong Grip con los clientes que observan al jugador mediante un payload visual mínimo e independiente de la física.
- La pose remota se actualiza al enganchar, cambiar con `F`, pasar entre techo y pared o soltarse; una renovación periódica cubre observadores tardíos y un timeout evita estados huérfanos.
- Protocolo interno actualizado a versión 13.
- Documenta como validadas las maniobras Strong Grip con dos picotas, cambio con `F`, salto con momento, cruce de huecos y transición alrededor de bordes.
- No modifica física, distancias, durabilidad, cooldown ni balance de encantamientos.

## 0.1.25-beta

- Añade una pose elevada propia para el brazo y la picota activos al usar Strong Grip en primera persona.
- Corrige la vista local en tercera persona: el brazo activo se eleva y la picota sigue la transformación del modelo en lugar de permanecer abajo.
- Corrige puntos válidos de pared rechazados bajo un techo: si la altura ideal solapa la hitbox con el bloque superior, se elige la altura libre más cercana dentro del mismo alcance.
- El icono y el servidor comparten el mismo destino corregido; una posición que realmente colisione continúa siendo inválida y no consume recursos.
- Corrige la liberación de techo con Espacio: el impulso voluntario usa `W/A/S/D` orientado por cámara en vez de empujar siempre hacia donde se mira.
- Iguala ese impulso voluntario a los `0.65` del salto desde pared y amplía el límite combinado para que la inercia alineada lo refuerce sin quedar recortada prematuramente.
- La solicitud de liberación incluye atómicamente `W/A/S/D` y cámara del instante de Espacio; el servidor ya no depende del último paquete periódico para calcular el salto. Protocolo interno actualizado a versión 12.
- Corrige la aplicación final del salto de techo: el servidor envía explícitamente al cliente el vector calculado tras desenganchar, evitando que la física local descarte tanto el impulso direccional como la inercia del balanceo.
- Separa la velocidad limitada de la hitbox del momento de liberación: el balanceo acumula hasta `0.38` bloques/tick en cualquier dirección y el límite del radio ya no elimina esa energía antes del salto.
- Suaviza el balanceo aplicando cada destino servidor-validado desde el payload de techo y reserva el teletransporte absoluto para confirmaciones periódicas, evitando dos correcciones duras por tick.
- La pose entra suavemente durante 4 ticks, queda fija sin acumular swing vanilla y se refleja para ambas manos y jugadores zurdos.
- Transferir el mismo pico con `F` mueve la pose al otro brazo sin reiniciar su transición ni recrear el anclaje.
- La pose de pared y el render de la mano libre permanecen sin cambios; el hotfix de liberación no modifica durabilidad ni cooldown.

## 0.1.24-beta

- Reduce 30 % el radio del balanceo Strong Grip, de `0.95` a `0.665` bloques.
- El balanceo de techo confirma movimientos pequeños cada tick para eliminar tirones durante la desaceleración y el retorno.

## 0.1.23-beta

- Añade balanceo restringido bajo techos Strong Grip, integrado y validado por el servidor.
- Espacio libera hacia la cámara conservando inercia y aprovechando la amplitud acumulada.
- Doble Shift y clic izquierdo con el pico activo liberan sin velocidad horizontal.
- Protocolo interno actualizado a versión 11 para identificar visualmente anclajes de techo.

## 0.1.22-beta

- Añade Strong Grip I, exclusivo con Pick Climber y compatible con Sturdy Latch.
- Permite anclaje estático en techos firmes; un techo inestable exige Strong Grip + Sturdy Latch.
- Cada anclaje de techo cuesta 20 de durabilidad y permanecer suspendido cobra 1 cada 20 ticks.

## 0.1.21-beta

- Corrige el desgaste adicional con dos picotas al separar UUIDs duplicados antes del frenado dual.
- Restaura el cambio de punto con la otra mano hasta 1.5 bloques mientras una picota sigue anclada.

## 0.1.20-beta

- Sturdy Latch I reduce el cooldown inicial de una superficie inestable de 40 a 20 ticks, igual que una superficie firme.

## 0.1.19-beta

- Añade desgaste proporcional al rescate: 15 de base y 10 adicionales por cada bloque vertical completo de `BRAKING`.
- Dos picotas equipadas participan en el frenado, reciben los mismos costes y absorben la caída aproximadamente al doble de velocidad.
- Aumenta 60 % la velocidad del descenso controlado en superficies inestables sin Sturdy Latch.

## 0.1.18-beta

- Añade `Sturdy Latch I` como encantamiento data-driven para `#minecraft:pickaxes`.
- En superficies inestables, fija el anclaje tras frenado; en caída leve lo fija de inmediato.
- Sin Sturdy Latch, arena, grava, concreto en polvo y nieve mantienen el descenso controlado.
- Conserva el cooldown de 40 ticks de las superficies inestables y no altera Strong Grip, techos ni desgaste proporcional.

## 0.1.17-beta

- Corrige la cámara bloqueada durante frenado o descenso: las correcciones de posición conservan la rotación local.
- Añade payload servidor-autoritativo de intención de cámara y `W/A/S/D` para que el movimiento lateral funcione tanto durante `BRAKING` como `UNSTABLE_SLIDING`.
- Sincroniza el estado móvil en cada tick, eliminando el retraso de actualización de ancla y grietas durante el deslizamiento.
- Corrige la orientación de A/D durante el deslizamiento.
- Llegar al suelo durante un descenso termina el anclaje pasivamente en lugar de congelarlo.
- Doble pulsación de Shift dentro de 7 ticks suelta el anclaje sin impulso.
- El ancla conserva el punto exacto del clic para resolver correctamente el bloque y la grieta durante el deslizamiento.
- En frenado por caída libre, la primera dirección lateral queda fijada como trayectoria diagonal hasta que el anclaje termine o se estabilice.
- El umbral de frenado pasa de `-0.25` a `-0.40` bloques/tick para que caídas leves se enganchen directamente.
- El frenado absorbe la caída aproximadamente durante el doble de tiempo; valores de balance actuales fijados en arrastre `0.75` y recuperación `0.035`.
- `BRAKING` ahora exige más de 5 bloques de caída acumulada además de velocidad suficiente, evitando activarlo tras saltos o caídas cortas.
- Protocolo interno actualizado a versión 10.

## 0.1.16-beta

- Permite desplazamiento lateral con `W/A/S/D` durante frenado y descenso a la mitad de la velocidad vertical actual, limitado al plano de la pared.
- La cámara conserva orientación libre durante el movimiento; el servidor mantiene autoridad sobre la posición final y las colisiones.
- El ancla y las grietas avanzan al bloque que sostiene actualmente el pico al deslizarse entre bloques.
- Encontrar una pared firme durante un descenso inestable lo convierte en un anclaje fijo; acabar la superficie termina el anclaje de forma segura.
- Los anclajes iniciados en superficies inestables aplican 40 ticks de cooldown individual en vez de 20.
- El cooldown persistente conserva su duración para que el indicador cliente represente correctamente ambos tiempos.
- Protocolo interno actualizado a versión 9 para sincronizar el cooldown inicial variable de un anclaje.

## 0.1.15-beta

- Añade clasificación de superficies mediante los tags data-driven `stable_anchor_blocks`, `unstable_anchor_blocks` y `unclimbable_blocks`.
- La prioridad es no escalable, inestable, estable y fallback compatible vigente.
- Hielo, hielo compacto, hielo azul, hielo escarchado y yunques se declaran firmes.
- Arena, grava, concreto en polvo, nieve, capas de nieve y nieve en polvo se declaran inestables.
- Los bloques administrativos/protegidos incluidos en `unclimbable_blocks` rechazan el anclaje.
- Una caída rápida se frena progresivamente desde el servidor; no usa velocidad cliente ni corrección hacia el ancla.
- En superficies firmes el frenado termina fijo; en inestables continúa con descenso controlado y comprobación de colisiones por tick.
- No añade desgaste proporcional, Sturdy Latch, Strong Grip ni techos.

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
