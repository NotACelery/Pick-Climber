# Pick Climber — Release 1.0.1

Mod para **Minecraft 1.21.1**, **NeoForge 21.1.235** y **Java 21**.

Convierte las herramientas incluidas en `#pickclimber:climbing_tools` en herramientas de escalada, impulso y rescate. `#pickclimber:excluded_climbing_tools` tiene prioridad y permite vetar herramientas concretas.

## Mecánica principal

- Quieto, cayendo o cerca del ápice: clic derecho intenta engancharse.
- Tras un salto real y mientras todavía asciendes: clic derecho ejecuta el impulso.
- La velocidad positiva por sí sola no autoriza el impulso.
- El wall jump exige soltar Espacio después de engancharse y volver a pulsarlo.
- Movimiento horizontal y diagonal entre puntos de apoyo hasta 1,5 bloques.
- 15 puntos de durabilidad por enganche o impulso exitoso, respetando `Unbreaking`.
- Pick Climber I–III mejora el impulso y el wall jump.

## Bloques interactivos y prioridad del clic derecho

Desde 1.0.1, Pick Climber espera hasta que el bloque tenga su oportunidad normal de manejar el clic antes de intentar engancharse:

- **Clic derecho normal** sobre un bloque que abre una interfaz o consume la interacción: el bloque conserva el clic y Pick Climber no se engancha.
- Esto funciona también con bloques modded que abren su GUI directamente desde la interacción, como los Farmers de Easy Villagers y Easy Farmer's Delight Compat.
- Si el bloque no utiliza el clic, Pick Climber puede convertir la cara válida en un punto de apoyo.
- **Shift + clic derecho** sigue permitiendo engancharse cuando Minecraft omite la interacción normal del bloque al usar la acción secundaria.
- Un intento inválido no consume durabilidad ni inicia cooldown.

La prioridad se resuelve mediante el propio pipeline de interacción de NeoForge, sin listas hardcodeadas de bloques o mods. El indicador conserva además la detección preventiva de los bloques que exponen un `MenuProvider` directamente.

## Prioridad de manos y mano principal libre

La mano secundaria tiene prioridad **entre picos disponibles**, pero no reemplaza una interacción vanilla exitosa de la mano principal:

1. Minecraft prueba primero la interacción de la mano principal.
2. Si colocar, usar, abrir o consumir algo tiene éxito, el clic termina ahí.
3. Si la principal devuelve `PASS`, se prueba la secundaria y Pick Climber puede utilizar su pico.

Mientras la mano secundaria sostiene al jugador, la principal puede:

- minar bloques;
- atacar entidades;
- colocar bloques;
- usar comida, cubos y herramientas;
- interactuar con bloques;
- usar otro pico disponible para crear un nuevo punto de apoyo.

El clic izquierdo con el mismo pico que mantiene el ancla provoca un desenganche pasivo. Es intencional: al intentar minar o atacar con esa herramienta, el pico deja de sostener al jugador. Minar con la mano libre no cancela el anclaje.

## Transferencia del ancla con `F`

El intercambio vanilla de manos conserva el anclaje cuando el mismo pico pasa a la mano contraria:

- no se consume durabilidad adicional;
- no se reinicia el cooldown;
- no se repiten sonido ni grietas;
- no se recrea ni se mueve el punto de apoyo;
- la pose clavada cambia a la nueva mano;
- la mano anterior recupera su render normal.

La transferencia se detecta por el UUID persistente del pico, no por el material ni por el slot. Si el pico activo deja de estar equipado en ambas manos, el enganche termina de forma pasiva y sin impulso.

## Balance de Pick Climber

Pick Climber continúa siendo el único encantamiento del mod con niveles I–III.

Valores de la beta 0.1.14:

```text
weight: 6
anvil_cost: 1
min_cost: 5 + 8 por nivel adicional
max_cost: 25 + 8 por nivel adicional
```

| Nivel | Altura adicional aproximada |
|---|---:|
| Sin encantamiento | 1 bloque |
| Pick Climber I | 1,5 bloques |
| Pick Climber II | 2 bloques |
| Pick Climber III | 2,5 bloques |

El encantamiento también mejora el wall jump en aproximadamente 0,5 bloques por nivel.

## Cooldown, pose y limpieza

Cada pico mantiene un cooldown individual de 20 ticks. Comienza inmediatamente cuando el servidor confirma un enganche o impulso y continúa bajando aunque el pico siga clavado.

Mientras un pico mantiene el anclaje, su modelo de primera persona entra suavemente a una pose adelantada durante 4 ticks y permanece congelado. La otra mano sigue usando su render normal.

Las grietas sintéticas se limpian al cambiar de punto, soltarse, cambiar de dimensión, perder el pico, romper el bloque o desconectarse.

## Superficies y frenado de caída

La clasificación de superficies es data-driven y puede ampliarse desde datapacks o modpacks:

- `pickclimber:unclimbable_blocks`: rechaza el anclaje.
- `pickclimber:unstable_anchor_blocks`: permite engancharse, pero continúa con un descenso controlado.
- `pickclimber:stable_anchor_blocks`: declara superficies firmes; los bloques no clasificados conservan el comportamiento firme anterior.

Las exclusiones tienen prioridad sobre cualquier otro tag. Hielo, hielo compacto, hielo azul, hielo escarchado y yunques son firmes. Arena, grava, concreto en polvo y toda la nieve —incluidas capas y nieve en polvo— son inestables.

El frenado solo se activa si el jugador ya descendió más de 5 bloques (`fallDistance`) y su velocidad vertical es menor que `-0.40` bloques/tick. Los saltos y caídas cortas se enganchan directamente. Durante el frenado el servidor absorbe la velocidad gradualmente y desplaza el ancla hacia abajo sin atravesar colisiones. En una superficie firme termina inmóvil; en una inestable continúa a velocidad descendente controlada.

Durante el frenado o descenso, `W/A/S/D` permite desplazarse por el plano de la pared a la mitad de la velocidad vertical actual. En `BRAKING`, la primera dirección lateral queda fijada como trayectoria diagonal. La intención de cámara y movimiento llega al servidor cada tick, mientras las correcciones de posición conservan la rotación local. El punto de anclaje y sus grietas siguen el contacto exacto del clic; al llegar a una superficie firme o al suelo, el descenso termina. Un enganche iniciado en una superficie inestable usa 40 ticks de cooldown. Una caída que activa `BRAKING` también aplica 20 ticks de esfuerzo al otro pico equipado para evitar anular el frenado alternando manos.

`Sturdy Latch I` convierte una superficie inestable en un anclaje firme: en caída leve queda fijo al engancharse; en caída fuerte conserva primero todo el frenado y queda fijo al terminar. También convierte su cooldown inicial de 40 a 20 ticks, igual que una superficie firme.

El frenado cobra 15 de durabilidad inicial. Si hay dos picotas equipadas, ambas participan, pagan ese coste y el frenado aplica dos pasos por tick, reduciendo aproximadamente a la mitad el recorrido. Por cada bloque vertical completo realmente deslizado durante `BRAKING`, cada pico participante recibe 10 de desgaste adicional; Unbreaking sigue interviniendo en cada cobro. Sin Sturdy Latch, el descenso controlado por bloques inestables es 70 % más rápido que la base original (`0.136` bloques/tick).

Mientras una picota sostiene el ancla, la otra puede crear un nuevo punto dentro de 1.5 bloques del punto actual. Las dos herramientas mantienen UUID y cooldown propios; si dos copias llegan con el mismo UUID, el servidor separa la identidad de la segunda antes de cobrar o anclar.

Al cambiar entre techo y pared, el destino mantiene el punto ideal siempre que la hitbox sea libre. Si ese cálculo introduciría parcialmente al jugador en un suelo o techo cercano, cliente y servidor buscan la altura libre más próxima sin superar el alcance de 1.5 bloques; un destino que siga colisionando continúa rechazándose sin coste.

`Strong Grip I` habilita anclajes en la cara inferior de techos firmes. Cada anclaje nuevo cuesta 20 de durabilidad y permanecer suspendido cobra 1 cada 20 ticks; ambos costes respetan Unbreaking. Los techos inestables exigen además `Sturdy Latch I`. Strong Grip y Pick Climber son especializaciones excluyentes.

Mientras cuelga, `W/A/S/D` impulsa un balanceo servidor-autoritativo de hasta 0.665 bloques, con aceleración, amortiguación, velocidad máxima y colisiones limitadas. El cliente recibe cada tick el destino ya validado y el servidor conserva confirmaciones absolutas periódicas, reduciendo tirones sin delegar la física. Espacio suma hasta `0.38` bloques/tick de momento pendular acumulado al impulso de salto de `0.65` en la dirección de `W/A/S/D`: ambos vectores pueden reforzarse o contrarrestarse hasta el límite combinado de `1.03`. Sin direccional ni balanceo acumulado, la liberación no recibe velocidad horizontal. Doble Shift o clic izquierdo con el pico activo producen siempre una liberación pasiva con velocidad horizontal cero.

En primera persona, el brazo y la picota activos adoptan una pose elevada propia de los anclajes de techo. La entrada dura 4 ticks, no acumula el swing vanilla y se refleja al brazo correcto; transferir el mismo pico con `F` mueve la pose sin reiniciarla. En tercera persona, el servidor sincroniza un estado visual mínimo con los clientes que observan al jugador para elevar el brazo activo y alinear la picota también en multijugador. La otra mano conserva su render normal.

## Controles vigentes

- **Clic derecho** sobre una cara vertical válida: impulso o enganche.
- **Clic derecho normal en bloque con menú**: interacción vanilla.
- **Shift + clic derecho en bloque con menú**: intento de anclaje.
- **Espacio**, después de soltarlo y volver a pulsarlo: wall jump.
- **F**: intercambia manos sin perder el anclaje.
- **Clic izquierdo con ancla secundaria**: minería o ataque vanilla con la principal.
- **Clic izquierdo con el mismo pico activo**: desenganche pasivo sin impulso.

## Compatibilidad de herramientas

La selección de herramientas es data-driven:

- `pickclimber:climbing_tools` incluye por defecto `#minecraft:pickaxes` y puede ampliarse desde datapacks o mods.
- `pickclimber:excluded_climbing_tools` siempre gana si un objeto aparece en ambos tags.
- La picota de madera está excluida por defecto.
- `eternal_starlight:thermal_springstone_hammer` se declara como integración opcional y no obliga a instalar Eternal Starlight.

Todos los controles, cooldowns, UUID, desgaste y render consultan el mismo clasificador central durante la escalada.

Compatibilidad validada manualmente con las picotas de Eternal Starlight y Twilight Forest. La picota de madera queda excluida de la escalada, mientras cambio de mano y encantamientos continúan funcionando con herramientas compatibles.

## Indicador de anclaje

Al apuntar con una herramienta de escalada, una banda compacta bajo la mira informa el resultado antes del clic:

- verde: anclaje firme disponible, incluidas superficies inestables reforzadas con Sturdy Latch;
- cian: agarre inestable sin Sturdy Latch o techo inestable que lo requiere;
- rojo: superficie no escalable u obstruida;
- violeta: requiere Strong Grip;
- gris: herramienta todavía en cooldown;
- amarillo: destino fuera del alcance real.

El String coloreado solo aparece sobre un bloque real situado a 3 bloques o menos. Entre 1.5 y 3 bloques usa el color de `OUT OF RANGE`; por encima de 3 se oculta para no interferir con minería o exploración. `ANCHOR OBSTRUCTED` queda reservado para caras situadas dentro del radio real de 1.5 bloques pero sin apoyo físico o sin espacio final.

El indicador reutiliza las comprobaciones de cara, colisión, encantamientos y distancia del enganche. Los bloques con menú continúan ocultándolo durante el clic normal y lo muestran al mantener Shift. Si el clic derecho intenta una acción rechazada, la barra de acción explica el motivo: encantamiento ausente, cooldown, alcance, bloque sin apoyo, entidad o falta de espacio para la hitbox. Las superficies inestables sin Sturdy Latch no muestran rechazo porque el agarre controlado sigue siendo válido.

Al cambiar de punto desde un techo, los requisitos se evalúan sobre la picota libre: una picota con Strong Grip que ya sostiene el ancla no puede ocultar que la segunda herramienta carece del encantamiento.

## Compilar en Windows

1. Instala Java 21.
2. Ejecuta `build-beta.bat`; el script lee automáticamente la versión y Minecraft desde `gradle.properties`.
3. El JAR aparecerá en:

```text
build/libs/pickclimber-1.21.1-1.0.1.jar
```

Retira versiones anteriores antes de instalar la release.

## Diseño futuro obligatorio

La especificación de `Strong Grip`, `Sturdy Latch`, nieve, hielo, anclajes de techo, doble Shift y balanceo está documentada en:

```text
docs/README-STRONG-GRIP.md
```

El documento se conserva como especificación y registro de las fases implementadas.

## Identidad visual

El icono oficial se encuentra en `src/main/resources/pickclimber_logo.png`.

Autor: **NotACelery**.
