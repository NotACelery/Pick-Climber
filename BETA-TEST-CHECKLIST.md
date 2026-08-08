# Checklist de release 1.0.0 — indicadores y regresiones

## Indicadores de anclaje

- [ ] Piedra y hielo muestran `ANCHOR READY` en verde cuando el destino es válido.
- [ ] Arena, grava, concreto en polvo y nieve sin Sturdy Latch muestran `UNSTABLE GRIP` en cian.
- [ ] Las mismas superficies con Sturdy Latch muestran `ANCHOR READY` en verde.
- [ ] Bedrock y demás bloques prohibidos muestran `UNCLIMBABLE` en rojo sin permitir el clic.
- [ ] Un destino cuya hitbox colisiona muestra `ANCHOR OBSTRUCTED` en rojo.
- [ ] Un destino situado entre 1.5 y 3 bloques del jugador/ancla muestra `OUT OF RANGE` en amarillo.
- [ ] Una herramienta en cooldown muestra `TOOL RECOVERING` en gris y cambia a disponible al terminar.
- [ ] Un techo firme sin Strong Grip muestra `STRONG GRIP REQUIRED` en violeta.
- [ ] Hacer clic en ese techo muestra el mensaje localizado de Strong Grip en la barra de acción.
- [ ] Colgado con una picota Strong Grip y otra sin el encantamiento, apuntar a un nuevo techo muestra `STRONG GRIP REQUIRED`, no `TOOL RECOVERING`.
- [ ] Un techo inestable con Strong Grip pero sin Sturdy Latch muestra `STURDY LATCH REQUIRED` en cian.
- [ ] Los estados inestables relacionados con Sturdy Latch no generan mensaje de rechazo al hacer clic.
- [ ] Con los encantamientos requeridos, esos techos cambian a firme o inestable según su superficie.
- [ ] Un bloque con menú no muestra la banda durante clic normal y sí la evalúa al mantener Shift.
- [ ] Mano secundaria, principal y dos herramientas muestran el estado correspondiente a la herramienta que finalmente selecciona el servidor.
- [ ] Ocultar la interfaz con F1 oculta también el indicador.
- [ ] Apuntar al vacío, una entidad o un impacto situado a más de 3 bloques no muestra icono ni texto.
- [ ] Un bloque real entre 1.5 y 3 bloques muestra `OUT OF RANGE`, no `ANCHOR OBSTRUCTED`.
- [ ] Cuerda, vegetación seleccionable y caras sin soporte muestran `ANCHOR OBSTRUCTED` únicamente dentro de 1.5 bloques.
- [ ] Clic derecho rechazado informa en la barra de acción si falta alcance, cooldown, apoyo de bloque o espacio para la hitbox.
- [ ] Clic derecho sobre una entidad con una herramienta de escalada equipada informa que no se puede anclar a entidades sin cancelar su interacción vanilla.
- [ ] El mensaje de obstrucción incluye el nombre localizado del bloque cuando una planta, cuerda u otro bloque sin apoyo intercepta el clic.

## Tags de herramientas

- [x] La picota de madera no muestra indicador ni permite engancharse.
- [ ] Picotas vanilla de piedra, hierro, oro, diamante y netherita conservan todas las maniobras existentes.
- [x] Las picotas de Eternal Starlight y Twilight Forest permiten escalar normalmente.
- [x] Cambio de mano y encantamientos funcionan con las herramientas modded validadas.
- [ ] Una herramienta añadida por datapack a `pickclimber:climbing_tools` recibe UUID, cooldown, desgaste y render de Pick Climber.
- [ ] Una herramienta presente simultáneamente en ambos tags queda excluida.
- [ ] Retirar Eternal Starlight no provoca errores de carga del tag opcional.

## Pose elevada remota en multijugador

- [ ] Con dos clientes, el observador ve elevado el brazo que sostiene el anclaje de techo.
- [ ] `F` cambia inmediatamente el brazo elevado también para el observador.
- [ ] Cambiar de techo a pared restaura la pose remota sin esperar el timeout.
- [ ] Espacio, doble Shift, clic izquierdo, rotura del pico y pérdida del bloque limpian la pose remota.
- [ ] Cambio de dimensión y desconexión no dejan el brazo elevado en el cliente observador.
- [ ] Entrar en distancia de seguimiento mientras el otro jugador ya cuelga recibe la pose en un máximo de 20 ticks.
- [ ] Un paquete de pose atrasado no altera posición, velocidad, cooldown, durabilidad ni grietas.

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
- [ ] Clic en la parte alta, central y baja de un bloque: la grieta empieza y sigue el bloque de contacto correcto.
- [ ] Durante una caída libre, la primera dirección lateral queda fija y no puede reorientarse hasta terminar el frenado.
- [ ] Saltar desde el suelo y engancharse tras una caída corta no activa `BRAKING`.
- [ ] Más de 5 bloques de descenso y velocidad menor que `-0.40` activa `BRAKING`; medir la distancia con una caída de 30 bloques.
- [ ] Con dos picos equipados, una caída que frena aplica 20 ticks de cooldown al pico no activo y no permite anular el desliz con un segundo clic.

## Sturdy Latch I

- [ ] Aparece como encantamiento de nivel único para picotas y puede aplicarse mediante libro/yunque.
- [ ] En arena, grava, concreto en polvo, bloque/capa de nieve y nieve en polvo, una caída leve queda fija con Sturdy Latch.
- [ ] En esos mismos materiales, una caída fuerte conserva el frenado y queda fija solo al terminar.
- [ ] Sin Sturdy Latch, los mismos casos continúan en descenso controlado.
- [ ] Sturdy Latch reduce el cooldown inicial de una superficie inestable a 20 ticks, igual que una superficie firme.
- [ ] Sin Sturdy Latch, el mismo anclaje inestable conserva 40 ticks; ambos casos rechazan intentos inválidos sin coste.

## Desgaste de frenado y doble picota

- [ ] Una caída que activa `BRAKING` con una picota cobra 15 iniciales y 10 más por cada bloque vertical completo recorrido.
- [ ] La misma caída con dos picotas equipadas cobra ambos importes a las dos herramientas y termina aproximadamente en la mitad del recorrido.
- [ ] Unbreaking puede evitar tanto el coste inicial como cada tramo adicional de 10.
- [ ] Si se rompe el pico activo durante el frenado, el anclaje termina sin conservar gravedad o grietas.
- [ ] Si el pico de apoyo se rompe o se retira, el pico activo continúa sin cobrar a una herramienta distinta.
- [ ] Sin Sturdy Latch, arena, grava, concreto en polvo y nieve descienden a `0.136` bloques/tick.
- [ ] Con dos picotas con UUID distintos o copiados, cada bloque de BRAKING cobra el tramo adicional a ambas herramientas.
- [ ] Mientras una picota está anclada, la otra crea un nuevo punto a hasta 1.5 bloques sin soltar la actual; ambas conservan cooldown individual.

## Strong Grip I — anclaje estático de techo

- [ ] Strong Grip I no puede combinarse con Pick Climber en mesa, libro ni yunque; ambos sí admiten Sturdy Latch.
- [ ] Sin Strong Grip, un clic en la cara inferior no crea ancla ni cobra coste.
- [ ] Con Strong Grip, un techo firme crea un ancla estática bajo el bloque y cobra 20 de durabilidad una sola vez.
- [ ] Tras 20 ticks completos suspendido cobra 1 adicional; `F` conserva el contador y no cobra de nuevo.
- [ ] Un techo inestable requiere Strong Grip + Sturdy Latch; sin Sturdy Latch se rechaza sin coste.
- [ ] Romper el pico activo o el bloque del techo limpia grietas, restaura gravedad y suelta sin impulso artificial.

## Balanceo y liberación de techo

- [ ] W/A/S/D acelera según cámara sin superar 0.665 bloques respecto del centro ni atravesar bloques.
- [ ] Al soltar W/A/S/D cerca de la amplitud máxima, el retorno avanza suavemente cada tick sin pausas ni saltos visibles de 0.05 bloques.
- [ ] Soltar movimiento amortigua el balanceo y el retorno al centro no genera aceleración infinita.
- [ ] Espacio libera hacia la dirección de `W/A/S/D` orientada por la cámara y conserva la velocidad del balanceo.
- [ ] Liberar cerca de la amplitud máxima cubre más distancia que liberar desde reposo, sin superar el límite de velocidad.
- [ ] Espacio sin `W/A/S/D` ni balanceo acumulado libera con velocidad horizontal cero.
- [ ] Con W, A, S o D pulsada, Espacio impulsa en esa dirección relativa a la cámara, no simplemente hacia la mirada.
- [ ] Liberar con el balanceo a favor aumenta el impulso resultante; hacerlo con la inercia contraria lo reduce o desvía mediante suma vectorial.
- [ ] Liberar con acumulación baja, media y máxima produce tres alcances distinguibles; el momento pendular nunca supera `0.38` ni el total `1.03` bloques/tick.
- [ ] Alcanzar el límite de 0.665 bloques no borra el momento acumulado ni convierte el salto siguiente en el impulso fijo de reposo.
- [ ] Con una direccional pulsada, Espacio aporta `0.65` bloques/tick horizontales —igual que el wall jump— antes de sumar vectorialmente el balanceo.
- [ ] Pulsar o cambiar de direccional en el mismo tick que Espacio usa esa dirección exacta; no cae en peso muerto por conservar un input periódico anterior.
- [ ] El impulso calculado se observa inmediatamente en el cliente tras soltar el techo, sin un tick de caída en peso muerto ni pérdida de la velocidad pendular servidor-autoritativa.
- [ ] Soltar la direccional justo antes de Espacio conserva solamente la inercia real del balanceo, sin añadir el impulso base de `0.65`.
- [ ] Doble Shift libera con velocidad horizontal exactamente cero.
- [ ] Clic izquierdo con el pico activo en la principal libera con velocidad horizontal cero; la mano principal libre no rompe un ancla secundaria.
- [ ] F conserva ancla, velocidad, contador de desgaste y mano correcta durante el balanceo.
- [ ] El balanceo y retorno se ven continuos entre confirmaciones absolutas de cinco ticks, sin tirones periódicos ni desincronización acumulada.
- [ ] Desde un anclaje de techo, apuntar cerca, al centro y al borde de una pared contigua muestra el icono siempre que exista una altura final libre dentro de 1.5 bloques.
- [ ] El clic confirma exactamente los mismos puntos que anuncia el icono; no aparece icono en posiciones donde la hitbox siga atravesando techo, suelo o pared.
- [ ] Cambiar de techo a pared bajo un techo bajo no eleva al jugador dentro del bloque ni exige apuntar artificialmente al borde exterior.
- [ ] Apuntar más de media altura de jugador por encima o debajo del destino seguro no usa la corrección para saltarse el límite vertical.

## Pose elevada de techo en primera persona

- [ ] Al confirmar un anclaje de techo, brazo y picota activos suben suavemente durante 4 ticks y quedan fijos sobre la cámara.
- [ ] La picota apunta visualmente hacia el techo sin acumular encima el swing vanilla.
- [ ] Mano principal, secundaria y jugador zurdo reflejan correctamente la pose.
- [ ] La mano libre conserva colocar, usar, minar, atacar y sus animaciones vanilla.
- [ ] `F` transfiere la pose al otro brazo sin reiniciar la entrada, duplicar brazos, cobrar desgaste ni recrear grietas.
- [ ] Cambiar a un punto de techo nuevo reinicia brevemente la entrada solo para el nuevo anclaje.
- [ ] Soltarse con Espacio, doble Shift o clic izquierdo restaura inmediatamente el render normal.
- [ ] La pose clavada de pared permanece visualmente idéntica a 0.1.24.

## Pose elevada en tercera persona

- [ ] En `F5`, solamente el brazo que sostiene el anclaje de techo apunta hacia arriba y la picota acompaña su rotación.
- [ ] Con dos picotas, la herramienta libre permanece en su pose normal.
- [ ] Mano principal, secundaria, `F` y jugador zurdo elevan el brazo correcto.
- [ ] Soltarse por cualquiera de las rutas restaura ambos brazos sin conservar la pose de techo.
- [ ] Un anclaje de pared no activa la pose elevada de tercera persona.
- [ ] Confirmar la pose desde un segundo cliente multijugador mediante el estado remoto de protocolo 13.

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
