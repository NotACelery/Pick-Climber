# Estado de compilación

Versión preparada: `1.0.1`

Objetivo: Minecraft 1.21.1, NeoForge 21.1.235 y Java 21.

Baseline funcional vigente:

- 1.0.1 conserva la prioridad de interacción de bloques y usa Shift + clic derecho como override explícito de anclaje.
- 1.0.1 incorpora la identidad visual actual y la pestaña creativa del mod.
- Clasificación data-driven de superficies estables, inestables y no escalables.
- Frenado progresivo servidor-autoritativo para caídas rápidas.
- Descenso controlado en superficies inestables y comprobación de colisión antes de cada desplazamiento.
- Desplazamiento lateral a media velocidad de descenso con input cliente validado por servidor y transición inmediata de grietas/superficie.
- Frenado definitivo: `fallDistance > 5`, velocidad `< -0.40`, arrastre `0.75`, recuperación `0.035` y movimiento máximo `0.60` bloques/tick.
- La caída de frenado fija la primera dirección diagonal y aplica 20 ticks de esfuerzo al otro pico equipado.
- Sturdy Latch I fija superficies inestables tras el frenado y reduce su cooldown inicial de 40 a 20 ticks.
- BRAKING cobra 15 de base y 10 por bloque vertical completo; dos picotas duplican la deceleración y comparten desgaste.
- El descenso sin Sturdy Latch en superficie inestable usa `-0.136` bloques/tick, 70 % más rápido que la base original.
- UUIDs duplicados entre dos picotas equipadas se separan antes del agarre dual o cambio de punto, conservando el desgaste y cooldown por herramienta.
- Strong Grip I añade anclaje estático de techo con coste inicial de 20 y desgaste de 1 por segundo; techo inestable requiere Sturdy Latch.
- Balanceo de techo limitado y servidor-autoritativo; Espacio conserva hasta `0.38` de momento acumulado y las liberaciones pasivas anulan velocidad horizontal.
- Radio de balanceo reducido a `0.665`; corrección de techo con umbral mínimo para evitar acumulación visible de desplazamientos lentos.
- Pose elevada de primera persona para brazo y picota activos bajo techo, con transición de 4 ticks y transferencia estable mediante `F`.
- Pose elevada local en tercera persona para el brazo activo; la capa vanilla alinea la picota y el brazo libre conserva su postura.
- Resolución de altura libre para cambios techo/pared: evita falsos rechazos por solape con bloques adyacentes sin aceptar hitboxes en colisión ni ampliar el alcance.
- Liberación de techo basada en input: `W/A/S/D` aporta el impulso voluntario y se combina vectorialmente con un momento pendular independiente de la velocidad restringida de la hitbox.
- El impulso voluntario de techo comparte ahora la magnitud horizontal `0.65` del wall jump; el límite combinado `1.03` solo protege contra estados fuera del presupuesto físico máximo.
- La velocidad final de liberación del techo se sincroniza explícitamente después del detach, pues no puede predecirse desde el estado cliente sin delegar la inercia autoritativa del balanceo.
- Los destinos de techo validados se aplican suavemente en cliente cada tick; el servidor usa `setPos` para pasos intermedios y conserva `teleport` cada cinco ticks como confirmación dura.
- Protocolo 13: conserva input/cámara y liberación autoritativa, y añade un payload mínimo para la pose elevada de observadores multijugador.
- Clasificador central de herramientas basado en `pickclimber:climbing_tools` con exclusión prioritaria mediante `pickclimber:excluded_climbing_tools`.
- Compatibilidad opcional explícita con `eternal_starlight:thermal_springstone_hammer`; la picota de madera queda excluida.
- Compatibilidad manual confirmada con Eternal Starlight y Twilight Forest, incluida transferencia de mano y encantamientos.
- La pose remota se refresca cada 20 ticks, se limpia explícitamente al terminar y expira en cliente tras 40 ticks sin sincronización.
- Indicador localizado bajo la mira con estados para superficie firme, inestable, prohibida, obstruida, fuera de alcance, cooldown y requisitos de encantamiento de techo.
- La evaluación del HUD reutiliza la validación real y no altera autoridad, payloads o física.
- El HUD se limita a 3 bloques, muestra `OUT OF RANGE` entre 1.5 y 3 y solo evalúa obstrucción dentro del alcance físico.
- El String teñido queda como único indicador permanente bajo la mira; los intentos rechazados explican en la barra de acción el encantamiento, cooldown, alcance, bloque, entidad o espacio que impide el anclaje.
- Un Strong Grip ya ocupado en el techo no cuenta como herramienta candidata para el siguiente punto; la picota libre determina el requisito mostrado.
- La metadata declara `celerbi` como autor y traduce la descripción de la lista de mods mediante los recursos `en_us`, `es_es` y `es_cl`.

Validación realizada en este entorno:

- Una compilación previa de la misma línea 1.0.x terminó con `BUILD SUCCESSFUL` usando Java 21; la limpieza actual debe recompilarse antes de publicar.
- La última compilación documentada en este archivo corresponde a la línea 1.0.x; tras la limpieza de source 1.0.1 debe repetirse el build y QA in-game antes de publicación.
- `bash -n build.sh` valida el script Linux y `.gitattributes` fija sus finales de línea en LF.
- La metadata de 1.0.1 debe declarar versión 1.0.1, autor `celerbi`, licencia All Rights Reserved, Minecraft 1.21.1, NeoForge 21.1.235 mínimo y ejecución en ambos lados; el JAR contiene la descripción localizada en `en_us`, `es_es` y `es_cl`.
- La lógica de tags, frenado, descenso y render elevado compila contra NeoForge 21.1.235.
- La regresión final específica del JAR 1.0.1 dentro de Minecraft sigue pendiente antes de publicarlo.

```text
build.bat
```

Resultado esperado:

```text
build/libs/pickclimber-1.21.1-1.0.1.jar
```

## Estado del source 1.0.1

- El árbol Java fue normalizado manteniendo la misma lógica ejecutable.
- La documentación técnica se centraliza en `docs/DEVELOPMENT.md`.
- Los comentarios explicativos fueron retirados del runtime Java y trasladados a documentación.
- `SOURCE-MANIFEST.json` debe corresponder a esta base 1.0.1.
- El build y la regresión in-game siguen siendo obligatorios antes de publicar cualquier artefacto generado desde esta limpieza.
