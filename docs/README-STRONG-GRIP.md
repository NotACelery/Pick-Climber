# Strong Grip, Sturdy Latch y movimiento de techo

> **Estado:** anclaje estático, balanceo base y poses elevadas locales implementados; la sincronización visual multijugador y maniobras avanzadas siguen pendientes.
>
> Este documento conserva el comportamiento obligatorio y distingue lo implementado de las fases pendientes.
>
> **Dependencias:** controles base estabilizados, clasificación de superficies, frenado progresivo, desgaste proporcional y `Sturdy Latch`.

## 1. Objetivo del sistema

Esta expansión divide las picotas de escalada en dos especializaciones incompatibles:

- `Pick Climber`: movilidad vertical, impulso y wall jump.
- `Strong Grip`: agarre en techos, permanencia suspendida y desplazamiento mediante balanceo.

`Sturdy Latch` será un encantamiento auxiliar compatible con cualquiera de las dos especializaciones. Su función será convertir superficies blandas o inestables en anclajes firmes una vez terminado el frenado inicial.

El objetivo no es fabricar una única picota que haga todo. El sistema debe incentivar herramientas dedicadas, decisiones de equipo y, para las maniobras avanzadas de techo, el uso de dos picotas con `Strong Grip`.

## 2. Encantamientos

### 2.1 Pick Climber

Especialización de movilidad vertical.

- Conserva el enganche normal en paredes.
- Mejora el impulso realizado durante un salto real.
- Mejora el wall jump.
- Mantiene sus niveles I–III.
- No habilita el enganche a la cara inferior de los bloques.
- Será incompatible con `Strong Grip`.

### 2.2 Strong Grip

Especialización de agarre y movimiento de techo.

- Tendrá un único nivel.
- Habilita el enganche a la cara inferior de un bloque válido.
- Conserva el uso base del pico en paredes.
- No recibe las mejoras verticales de `Pick Climber`.
- Será incompatible con `Pick Climber` mediante el sistema de exclusividad de encantamientos.
- Debe poder coexistir con `Sturdy Latch`, Mending y Unbreaking.

### 2.3 Sturdy Latch

Encantamiento auxiliar de anclaje reforzado.

- Nombre de trabajo fijado: **Sturdy Latch**.
- Tendrá un único nivel, porque su efecto principal es binario.
- Será compatible con `Pick Climber`.
- Será compatible con `Strong Grip`.
- Permitirá terminar completamente inmóvil sobre superficies inestables después del frenado inicial.
- No elimina el frenado causado por una caída rápida.
- No elimina el desgaste proporcional a la velocidad absorbida.
- Debe funcionar con arena, grava, concreto en polvo y todo tipo de nieve incluido mediante tags.

### 2.4 Exclusividad

La matriz obligatoria será:

| Combinación | Permitida | Resultado |
|---|---:|---|
| Pick Climber + Strong Grip | No | Especializaciones mutuamente excluyentes |
| Pick Climber + Sturdy Latch | Sí | Movilidad vertical y agarre firme en superficies inestables |
| Strong Grip + Sturdy Latch | Sí | Techos y paredes firmes, incluidos materiales inestables |
| Strong Grip + Unbreaking/Mending | Sí | Herramienta dedicada de exploración de techos |

La exclusividad debe respetarse en mesa, libros y yunque. No debe depender de una comprobación únicamente durante el uso.

### 2.5 Niveles y presupuesto de yunque

- `Pick Climber` será el único encantamiento del mod con niveles: I–III.
- `Strong Grip` existirá solamente como nivel I.
- `Sturdy Latch` existirá solamente como nivel I.
- Debe ser posible combinar la especialización elegida y `Sturdy Latch` en un solo libro.
- Ese libro combinado debe poder aplicarse como paso final a una picota con Unbreaking III, Mending, Efficiency V y Fortune III o Silk Touch sin provocar “Too Expensive”.
- Un coste final cercano a 30 niveles es aceptable: la herramienta especializada debe ser cara, pero alcanzable con un orden optimizado.
- Los valores de `anvil_cost` de Strong Grip y Sturdy Latch se decidirán conjuntamente para cumplir esta condición, no de forma aislada.

## 3. Clasificación de superficies

La clasificación general seguirá siendo data-driven:

```text
pickclimber:stable_anchor_blocks
pickclimber:unstable_anchor_blocks
pickclimber:unclimbable_blocks
```

La prioridad será:

1. `unclimbable`
2. `unstable`
3. `stable`
4. regla de respaldo para herramientas/bloques compatibles

### 3.1 Hielo

Las variantes de hielo son escalables normalmente con la picota base:

- hielo;
- hielo compacto;
- hielo azul;
- hielo escarchado cuando su estado permita una cara válida;
- variantes de hielo de otros mods añadidas mediante tags.

Mecánicamente se consideran superficies firmes. El hecho de que sean resbalosas para caminar no impide que una picota penetre el material.

### 3.2 Nieve

Toda la nieve se considera superficie inestable, incluyendo:

- bloque de nieve;
- capas de nieve;
- nieve en polvo;
- variantes de nieve añadidas por otros mods.

Sin `Sturdy Latch`:

- el pico puede engancharse;
- el jugador no queda completamente inmóvil;
- después del frenado inicial continúa descendiendo a velocidad controlada;
- el anclaje evita una caída libre, pero no solidifica el material.

Con `Sturdy Latch`:

- el frenado inicial sigue siendo obligatorio;
- el desgaste por caída rápida sigue aplicándose;
- al terminar el frenado, el jugador puede quedar completamente fijo.

Las capas de nieve y la nieve en polvo no deben depender solamente de `isFaceSturdy`, porque algunas geometrías vanilla no exponen una cara sólida convencional. La compatibilidad deberá resolverse mediante tags y comprobaciones de geometría específicas.

### 3.3 Arena, grava y concreto en polvo

Se comportan como nieve inestable:

- sin `Sturdy Latch`: descenso controlado continuo;
- con `Sturdy Latch`: anclaje firme tras completar el frenado;
- una caída rápida siempre genera frenado y desgaste adicional.

Los yunques quedan excluidos del comportamiento inestable automático de los bloques con gravedad.

### 3.4 Techos inestables

Para colgarse de la cara inferior de una superficie inestable se necesitan ambos encantamientos:

```text
Strong Grip + Sturdy Latch
```

`Strong Grip` habilita la orientación de techo. `Sturdy Latch` permite que el material blando sostenga finalmente al jugador.

Sin `Sturdy Latch`, un techo de nieve, arena, grava o material equivalente no puede mantener un anclaje estático. El resultado exacto deberá ser seguro: rechazo del anclaje o desprendimiento/deslizamiento inmediato controlado, nunca una suspensión infinita.

## 4. Creación de un anclaje de techo

### 4.1 Condiciones

Un intento de enganche al techo será válido solamente cuando:

- el clic impacte la cara inferior del bloque;
- el pico utilizado tenga `Strong Grip`;
- el bloque no esté en `unclimbable`;
- la superficie pueda sostener el tipo de anclaje según `Sturdy Latch`;
- el objetivo esté dentro del alcance permitido;
- la trayectoria y posición final no tengan colisiones inválidas;
- el pico no esté en cooldown;
- el pico no sea ya la herramienta activa del mismo anclaje;
- el jugador esté vivo, no sea espectador y no esté en un estado incompatible.

Un intento rechazado no debe:

- gastar durabilidad;
- iniciar cooldown;
- reproducir sonido;
- crear grietas;
- alterar velocidad o gravedad.

### 4.2 Coste inicial

Cada anclaje nuevo confirmado en el techo cuesta:

```text
20 puntos de durabilidad
```

Reglas:

- se aplica mediante `hurtAndBreak`;
- Unbreaking debe poder reducir el desgaste efectivo;
- cambiar a otro punto del techo vuelve a cobrar 20;
- transferir el mismo pico con `F` no vuelve a cobrar;
- una sincronización de red o corrección de mano no vuelve a cobrar;
- si el pico se rompe durante el coste inicial, el nuevo anclaje no se confirma.

### 4.3 Desgaste sostenido

Mientras el jugador permanezca suspendido por un anclaje de techo:

```text
1 punto de durabilidad cada 20 ticks completos
```

Reglas:

- se cobra solamente a la herramienta activa;
- se aplica mediante `hurtAndBreak`;
- Unbreaking debe funcionar;
- el contador se detiene al desengancharse, cambiar de ancla o perder el estado de techo;
- `F` conserva el contador del mismo pico, no lo reinicia;
- cambiar a otra picota crea un nuevo anclaje y su propio contador;
- no se cobra por ticks incompletos acumulados después de terminar el anclaje;
- si el pico se rompe, el jugador se suelta pasivamente y recupera gravedad sin impulso artificial.

## 5. Estados físicos previstos

El sistema debe distinguir explícitamente al menos estos estados:

```text
DETACHED
WALL_BRAKING
WALL_FIXED
WALL_UNSTABLE_SLIDE
CEILING_BRAKING
CEILING_FIXED
CEILING_SWINGING
CEILING_DESCENDING
```

No deben inferirse únicamente a partir de la velocidad actual.

### 5.1 Ceiling braking

Si el jugador alcanza el techo con velocidad significativa:

- no se detiene de manera instantánea;
- se aplica el futuro sistema de frenado progresivo;
- el desgaste adicional depende de la velocidad absorbida;
- el cuerpo debe mantenerse fuera de la colisión del bloque;
- una vez absorbida la velocidad, pasa a `CEILING_FIXED` o a la conducta inestable correspondiente.

`Strong Grip` no anula las consecuencias físicas de engancharse a gran velocidad.

### 5.2 Ceiling fixed

Estado central bajo el punto de anclaje:

- gravedad controlada por el servidor;
- caída anulada mientras el estado sea válido;
- coste sostenido activo;
- brazo y pico elevados;
- permite iniciar balanceo, descenso, salto o transferencia.

### 5.3 Ceiling swinging

Movimiento pendular restringido alrededor del ancla. No será vuelo libre ni una cuerda completamente simulada.

Implementado como base en `0.1.23-beta`: el servidor integra el movimiento en un plano horizontal seguro para la hitbox vertical de Minecraft. En `0.1.24` el radio se redujo 30 % a `0.665`; la aceleración es `0.025`, la amortiguación `0.96` y la velocidad máxima `0.18` bloques/tick. Los movimientos pequeños de techo se confirman cada tick para evitar tirones durante el retorno.

Principios:

- el ancla actúa como pivote;
- el jugador mantiene una distancia máxima respecto del pivote;
- la gravedad produce retorno hacia la posición central;
- `W`, `A`, `S` y `D` agregan impulso lateral limitado;
- la dirección de cámara orienta la intención de movimiento;
- se comprueban colisiones durante toda la trayectoria;
- no se permite atravesar techo, paredes ni esquinas;
- la velocidad máxima queda limitada para evitar catapultas y desincronización;
- el servidor conserva autoridad sobre posición y velocidad.

Valores iniciales para pruebas, no definitivos:

- desplazamiento horizontal normal: aproximadamente 0,75–1,0 bloque desde el centro;
- alcance mayor solamente gracias a la posición real conseguida por el balanceo;
- amortiguación suficiente para que soltar las teclas reduzca gradualmente el movimiento.

## 6. Maniobras con dos picotas Strong Grip

La movilidad avanzada de techo está pensada para dos picotas dedicadas.

### 6.1 Cambio de punto

Secuencia esperada:

```text
Pico A sostiene al jugador
→ el jugador se balancea
→ apunta con Pico B
→ Pico B crea un nuevo anclaje
→ Pico A deja de ser el ancla activa
```

El nuevo punto:

- cuesta 20 de durabilidad al Pico B;
- inicia el cooldown individual del Pico B;
- no reinicia el cooldown del Pico A;
- mueve las grietas al nuevo bloque;
- transfiere la pose elevada al nuevo brazo;
- conserva una transición física controlada.

Implementado en `0.1.25-beta`: al pasar de techo a una cara lateral, el destino conserva primero la altura derivada del punto exacto de impacto. Si esa posición introduce la hitbox en el techo o suelo adyacente, se busca por pasos la altura libre más cercana hacia la posición actual. La corrección máxima es media altura del jugador, el resultado sigue sujeto al radio real de 1.5 bloques y cliente/servidor ejecutan la misma comprobación.

### 6.2 Cruce de huecos

El balanceo debe permitir intentar alcanzar un techo separado por aproximadamente dos bloques de vacío, siempre que:

- la posición balanceada real coloque el objetivo dentro del alcance;
- la segunda picota tenga `Strong Grip`;
- la segunda picota esté lista;
- no existan colisiones entre la posición actual y la nueva;
- el servidor valide el punto final.

No se aumentará arbitrariamente el alcance base para todos los enganches. La ventaja proviene de desplazar físicamente al jugador antes del segundo clic.

### 6.3 Rodear un borde

Desde la cara inferior de un bloque, el balanceo debe permitir:

- asomarse más allá del borde;
- apuntar a la cara lateral del mismo bloque o de uno vecino;
- transferir el ancla a una pared;
- continuar la escalada sin una liberación ciega.

La segunda picota sigue siendo necesaria para mantener un punto mientras se crea el siguiente. Una picota única permite colgarse y soltarse, pero no debe replicar la seguridad ni el alcance de dos herramientas.

## 7. Controles

### 7.1 Movimiento

Mientras se está enganchado al techo:

- `W/A/S/D`: agregar impulso al balanceo.
- Movimiento de cámara: orientar la dirección deseada.
- Clic derecho con la otra picota: intentar un nuevo anclaje.
- `F`: intercambiar manos manteniendo el mismo pico y el mismo anclaje.
- Clic izquierdo con la mano libre: conservar ataque/minería sin desenganchar.
- Clic izquierdo usando el mismo pico activo: desenganche pasivo, porque la herramienta deja de sostener el ancla al usarse.

### 7.2 Espacio

Espacio será la liberación activa con impulso.

Desde el techo:

- no debe empujar al jugador verticalmente contra el bloque;
- primero debe separarlo de la cara inferior;
- puede aplicar impulso horizontal según `W/A/S/D`, orientado por la mirada;
- puede incluir una pequeña componente descendente inicial para evitar colisión con la cabeza;
- `Pick Climber` no participa porque es incompatible con `Strong Grip`.

Los valores exactos se ajustarán en pruebas.

Implementado en `0.1.23-beta` y corregido en `0.1.25-beta`: el impulso horizontal base es `0.65`, la misma magnitud que el salto desde pared, pero solo se aplica en la dirección de `W/A/S/D` que siga pulsada, orientada por la cámara. La integración mantiene por separado la velocidad restringida de la hitbox y un momento pendular de liberación de hasta `0.38`; llegar al radio máximo puede detener el desplazamiento físico sin destruir la energía acumulada. Una inercia contraria reduce el salto y una coincidente lo refuerza, con límite total `1.03` bloques/tick. Sin input ni balanceo acumulado, Espacio libera sin velocidad horizontal. El destino servidor-validado se sincroniza cada tick mediante posición directa interpolable y se confirma con teletransporte absoluto cada cinco ticks.

### 7.3 Shift mantenido

Mantener Shift conserva el comportamiento vanilla de agacharse. No inicia descenso por sí solo.

Esto permite:

- colocar bloques sobre mesas de crafteo, hornos, cofres y otros bloques interactivos sin abrir su interfaz;
- conservar el control vanilla de sneak durante la construcción;
- usar `Shift + clic derecho` como gesto explícito para intentar engancharse a una cara válida de un bloque interactivo.

Regla de prioridad para bloques interactivos:

1. Clic derecho normal conserva la interacción vanilla del bloque.
2. `Shift + clic derecho` con una herramienta de escalada intenta el anclaje.
3. Si el anclaje es inválido, no se consume durabilidad ni cooldown.
4. Las pruebas específicas con máquinas modded se realizarán después de cerrar el comportamiento vanilla.

### 7.4 Doble pulsación de Shift

Mientras el jugador esté anclado al techo, una doble pulsación de Shift provoca una liberación pasiva vertical.

Comportamiento obligatorio:

- no realiza wall jump;
- no añade impulso horizontal;
- ignora W/A/S/D al calcular la velocidad inicial de liberación;
- comienza con velocidad horizontal cero y una separación vertical segura respecto del techo;
- permite caer de forma precisa sobre un bloque situado directamente debajo;
- una interacción de clic derecho entre ambas pulsaciones, o en el mismo gesto, cancela la detección de doble toque;
- mantener la segunda pulsación continúa siendo agachado vanilla y no cambia la trayectoria inicial ya resuelta.

La ventana máxima entre pulsaciones y la duración de una posible estabilización horizontal de pocos ticks se ajustarán mediante pruebas.

### 7.5 Descenso vertical pasivo

Al reconocer la doble pulsación:

- se elimina el anclaje sin aplicar salto;
- se limpia pose y grietas;
- se restaura gravedad;
- se neutraliza el impulso horizontal inicial;
- `fallDistance` queda en un estado coherente;
- el comportamiento se distingue de una caída por rotura del pico únicamente por la intención y sincronización, no mediante una ventaja de movimiento adicional.

## 8. Construcción e interacción mientras cuelga

La ventaja de estar suspendido no debe inutilizar la mano libre.

Mientras una mano sostiene el ancla:

- la otra puede colocar bloques;
- la otra puede usar objetos;
- la otra puede abrir o activar bloques;
- la otra puede minar o atacar cuando corresponda;
- el clic derecho normal abre o usa el bloque interactivo;
- `Shift + clic derecho` con una picota de escalada intenta engancharse al bloque en vez de abrir su interfaz;
- la doble pulsación de Shift se cancela cuando existe una interacción de uso asociada al gesto.

Casos obligatorios de prueba:

- colocar sobre mesa de crafteo con Shift;
- colocar sobre cofre con Shift;
- interactuar con puertas y trampillas;
- usar bloques con inventario;
- usar máquinas de Create y otros mods;
- usar objetos mantenidos en la mano principal mientras la secundaria sostiene.

## 9. Animación y modelo del jugador

La referencia conceptual es el comportamiento visual de la sombrilla del mod Artifacts: el brazo que sostiene la herramienta permanece elevado. Pick Climber no debe depender de Artifacts.

### 9.1 Primera persona

En un anclaje de techo:

- la mano activa se eleva por encima de la cámara;
- el pico apunta hacia el bloque ancla;
- la pose queda fija respecto del punto de contacto;
- el swing vanilla del pico no debe acumularse encima;
- la otra mano mantiene sus animaciones normales;
- `F` mueve la pose a la mano opuesta sin recrear el anclaje;
- al cambiar de punto, la transición debe ser breve y sin brazos duplicados.

La pose de pared existente no debe reutilizarse sin adaptación: el techo requiere una transformación específica.

Implementado en `0.1.25-beta`: el render cancela únicamente la mano activa, eleva brazo y picota mediante una transformación específica de techo y conserva el render vanilla de la mano libre. La entrada usa la misma curva suave de 4 ticks de la pose clavada; una transferencia con `F` conserva el UUID y el instante inicial, por lo que no reinicia la animación. No requiere dependencia de Artifacts ni modifica la física servidor-autoritativa.

### 9.2 Tercera persona

El modelo del jugador debe mostrar:

- brazo activo levantado hacia el techo;
- pico alineado con la cara inferior;
- brazo libre operando normalmente;
- torso ligeramente inclinado según el balanceo;
- piernas acompañando el movimiento sin simular vuelo;
- reflejo correcto entre mano derecha e izquierda;
- pose visible para otros jugadores en multijugador.

La implementación debe usar un estado sincronizado de pose, no deducirla solamente del item en la mano.

Implementado localmente en `0.1.25-beta`: la vista en tercera persona consulta el tipo de anclaje y la mano activa sincronizados, aplica una pose elevada al brazo correcto y deja que la capa vanilla del objeto alinee la picota con la mano. La pose anterior del modelo se restaura al terminar cada render para no afectar al brazo libre ni a jugadores sin anclaje.

Implementado para observadores en `0.1.26-beta`: un payload visual independiente transporta únicamente UUID del jugador, presencia del anclaje de techo y mano activa. Se actualiza al enganchar, transferir con `F`, cambiar entre techo y pared o soltar; además se renueva cada 20 ticks para observadores tardíos y expira tras 40 ticks sin sincronización. El payload no contiene ni modifica posición, velocidad, durabilidad, cooldown o física. La validación visual definitiva queda pendiente de una sesión con dos clientes.

### 9.3 Balanceo visual

Durante el balanceo:

- el brazo activo permanece conectado visualmente al ancla;
- el torso puede inclinarse en la dirección del desplazamiento;
- las piernas pueden retrasarse ligeramente respecto de la velocidad;
- la amplitud visual nunca debe superar la física real;
- las animaciones no deben afectar la hitbox ni reemplazar la validación servidor.

## 10. Grietas, sonido y efectos

- Las grietas se muestran en el bloque del ancla de techo.
- Cambiar de punto limpia el bloque anterior antes de mostrar el nuevo.
- `F` no limpia ni recrea grietas.
- Desconexión, muerte, dimensión, rotura del pico y pérdida del bloque limpian inmediatamente el overlay.
- El sonido inicial usa el material del bloque.
- El coste sostenido no reproduce el sonido de enganche cada segundo.
- Puede evaluarse un sonido suave de tensión o crujido, pero no es requisito inicial.
- Nieve e hielo podrán recibir partículas/sonidos diferenciados en una fase de pulido.

## 11. Autoridad y sincronización

El servidor será la única autoridad de:

- tipo de anclaje;
- bloque, cara y punto de contacto;
- herramienta activa y UUID;
- estado físico;
- velocidad y posición;
- coste de durabilidad;
- rotura del pico;
- transición entre anclas;
- cooldown;
- liberación y caída.

El cliente manejará:

- entrada del jugador;
- predicción visual limitada;
- pose de primera persona;
- pose de tercera persona recibida;
- indicadores de alcance y superficie.

Datos mínimos a sincronizar para un anclaje de techo:

```text
anchor block
anchor face
anchor point/pivot
active hand
tool UUID
surface type
physical state
swing offset/velocity or authoritative snapshots
pose state
attached time / durability cadence
crack id
```

La física del balanceo no debe ejecutarse de manera independiente en cliente y servidor durante largos periodos sin correcciones.

## 12. Indicadores de interfaz

El indicador de alcance deberá poder distinguir:

- pared válida;
- superficie inestable;
- techo que requiere `Strong Grip`;
- techo inestable que requiere `Strong Grip + Sturdy Latch`;
- superficie no escalable;
- segundo punto alcanzable durante el balanceo;
- herramienta en cooldown.

No es obligatorio que todos usen iconos nuevos en la primera implementación, pero el estado no debe inducir a gastar durabilidad en un intento imposible.

## 13. Roturas y recuperación segura

### 13.1 Rotura del pico

Si el pico activo se rompe:

- se elimina el estado de anclaje;
- se limpian grietas y pose;
- se detiene el coste periódico;
- se restaura gravedad;
- no se añade un impulso artificial;
- se conserva únicamente la velocidad física coherente del momento;
- el cliente recibe una sincronización explícita.

### 13.2 Pérdida del bloque

Si el bloque:

- se rompe;
- cae por gravedad;
- se mueve mediante pistón;
- cambia a un estado inválido;
- descarga su chunk de forma incompatible;

el anclaje debe terminar de forma pasiva y segura.

### 13.3 Cambio de manos o inventario

- `F` con el mismo UUID transfiere la pose y conserva el anclaje.
- Mover el pico fuera de ambas manos termina el anclaje.
- Otro pico nunca hereda el UUID activo.
- Buscar automáticamente la herramienta dentro del inventario no está permitido.

## 14. Balance

Valores fijados:

| Acción | Coste |
|---|---:|
| Anclaje normal de pared | 15 durabilidad base |
| Anclaje nuevo de techo | 20 durabilidad base |
| Permanencia en techo | 1 durabilidad por cada 20 ticks completos |
| Cambio a otro punto de techo | 20 durabilidad en la nueva picota |
| Transferencia con F del mismo pico | 0 durabilidad adicional |

Costes futuros adicionales:

- frenado de una caída rápida;
- desgaste proporcional a velocidad absorbida;
- posible desgaste durante descenso sostenido en superficies inestables.

`Unbreaking` debe aplicar a todos los daños realizados mediante la herramienta. Mending permite mantener una picota dedicada, pero no elimina la necesidad de experiencia y reparación.

## 15. Configuraciones de equipo esperadas

### Picota vertical

```text
Pick Climber III
Sturdy Latch
Unbreaking III
Mending
```

### Picota de techo

```text
Strong Grip
Sturdy Latch
Unbreaking III
Mending
```

### Equipo de travesía avanzada

Dos picotas de techo con `Strong Grip`. `Sturdy Latch` será necesario para incluir nieve y otros materiales inestables en la ruta.

## 16. Orden obligatorio de implementación

Este sistema no debe comenzar antes de completar sus dependencias.

1. **Completado en 0.1.14:** conservar el detach intencional y cerrar `Shift + clic derecho` sobre bloques interactivos vanilla.
2. **Balance base completado en 0.1.14:** costes actuales de `Pick Climber`; la obtención final seguirá afinándose mediante pruebas.
3. **Completado:** tags de superficies.
4. **Completado:** frenado progresivo.
5. **Completado:** desgaste proporcional a la caída.
6. **Completado:** comportamiento inestable de arena, grava, concreto en polvo y nieve.
7. **Completado:** `Sturdy Latch`.
8. **Completado:** exclusividad `Pick Climber` / `Strong Grip`.
9. **Completado:** enganche estático básico de techo.
10. **Completado:** coste inicial y coste sostenido.
11. **Completado:** pose elevada de primera persona.
12. **Implementado, pendiente de prueba multijugador:** pose elevada de tercera persona local y remota.
13. **Completado:** doble pulsación de Shift para caída vertical; queda validación modded de Shift mantenido.
14. **Completado:** balanceo restringido.
15. **Completado y validado:** transferencias de techo, rodeo de bordes y cruce de huecos.
16. **Siguiente fase:** pulir indicadores, sonidos y partículas; completar pruebas modded y multijugador.

## 17. Criterios de aceptación

La función no se considera terminada hasta que pase, como mínimo, estos casos.

### Encantamientos

- Pick Climber y Strong Grip no pueden coexistir.
- Sturdy Latch funciona con cualquiera de los dos.
- Un libro incompatible no puede aplicarse mediante yunque.
- Los intentos rechazados no gastan durabilidad.

### Superficies

- Hielo permite anclaje base.
- Nieve hace resbalar sin Sturdy Latch.
- Nieve queda firme con Sturdy Latch después del frenado.
- Techos firmes requieren Strong Grip.
- Techos de nieve requieren Strong Grip + Sturdy Latch.
- Bloques no escalables siempre rechazan.

### Costes

- Cada techo nuevo cobra 20.
- Permanecer 20 ticks cobra 1.
- F no cobra.
- Cambiar a otro punto sí cobra.
- Unbreaking funciona.
- Rotura del pico suelta al jugador correctamente.

### Controles

- W/A/S/D permiten balanceo controlado.
- Espacio libera sin golpear la cabeza contra el techo.
- Shift mantenido permite construir sobre bloques interactivos sin abrir su interfaz.
- `Shift + clic derecho` permite intentar anclaje sobre bloques interactivos.
- Doble pulsación de Shift libera verticalmente sin salto ni impulso horizontal inicial.
- La mano libre puede minar, atacar, colocar y usar.
- Usar el mismo pico activo con clic izquierdo provoca desenganche pasivo.

### Movimiento avanzado

- Dos picotas permiten cambiar de punto.
- El balanceo permite rodear un borde.
- Puede alcanzarse un techo separado por aproximadamente dos bloques de vacío solamente desde una posición balanceada válida.
- No se atraviesan bloques.
- No se obtiene vuelo ni aceleración infinita.

### Visuales y red

- El brazo correcto queda levantado.
- F transfiere la pose.
- Otros jugadores ven la mano correcta.
- Las grietas se limpian al desconectarse.
- Alta latencia no duplica costes ni anclas.
- Un paquete atrasado no restaura un estado viejo.

## 18. Valores abiertos para pruebas

Estos puntos siguen siendo ajustables y no deben confundirse con decisiones cerradas:

- rareza, peso y coste de yunque de Strong Grip;
- rareza, peso y coste de Sturdy Latch;
- ventana exacta de la doble pulsación de Shift;
- duración de una posible estabilización horizontal inicial tras la liberación vertical;
- radio máximo del balanceo, inicialmente 0,75–1,0 bloque;
- aceleración, amortiguación y velocidad máxima del balanceo;
- trayectoria exacta del salto desde el techo;
- tratamiento final de un techo inestable sin Sturdy Latch;
- partículas y sonidos específicos de hielo y nieve;
- alcance visual del indicador durante el balanceo.

## 19. Decisiones cerradas

No deben cambiarse sin una revisión explícita del diseño:

- Pick Climber y Strong Grip son incompatibles.
- Pick Climber es el único encantamiento con niveles I–III.
- Strong Grip y Sturdy Latch existen únicamente como nivel I.
- Sturdy Latch es compatible con ambos.
- La especialización elegida y Sturdy Latch deben poder aplicarse como un único libro final a una picota perfecta sin provocar “Too Expensive”.
- El hielo es escalable normalmente.
- Toda nieve es inestable sin Sturdy Latch.
- Strong Grip cuesta 20 al crear un anclaje de techo.
- Strong Grip cuesta 1 por segundo colgado.
- El desgaste respeta Unbreaking.
- El balanceo es limitado y servidor-autoritativo.
- Las maniobras avanzadas están pensadas para dos picotas Strong Grip.
- Shift mantenido conserva el agachado y las interacciones vanilla.
- Doble pulsación de Shift produce una liberación vertical pasiva sin salto ni impulso horizontal inicial.
- `Shift + clic derecho` permite intentar anclaje sobre bloques interactivos sin abrir su interfaz.
- La mano activa aparece elevada en primera y tercera persona.
- El sistema es obligatorio para una fase futura, pero no tiene prioridad inmediata.
