# Estado de compilación

Versión preparada: `0.1.24-beta`

Objetivo: Minecraft 1.21.1, NeoForge 21.1.235 y Java 21.

Cambios funcionales:

- Clasificación data-driven de superficies estables, inestables y no escalables.
- Frenado progresivo servidor-autoritativo para caídas rápidas.
- Descenso controlado en superficies inestables y comprobación de colisión antes de cada desplazamiento.
- Desplazamiento lateral a media velocidad de descenso con input cliente validado por servidor y transición inmediata de grietas/superficie.
- Frenado definitivo: `fallDistance > 5`, velocidad `< -0.40`, arrastre `0.75`, recuperación `0.035` y movimiento máximo `0.60` bloques/tick.
- La caída de frenado fija la primera dirección diagonal y aplica 20 ticks de esfuerzo al otro pico equipado.
- Sturdy Latch I fija superficies inestables tras el frenado y reduce su cooldown inicial de 40 a 20 ticks.
- BRAKING cobra 15 de base y 10 por bloque vertical completo; dos picotas duplican la deceleración y comparten desgaste.
- El descenso sin Sturdy Latch en superficie inestable usa `-0.128` bloques/tick, 60 % más rápido que antes.
- UUIDs duplicados entre dos picotas equipadas se separan antes del agarre dual o cambio de punto, conservando el desgaste y cooldown por herramienta.
- Strong Grip I añade anclaje estático de techo con coste inicial de 20 y desgaste de 1 por segundo; techo inestable requiere Sturdy Latch.
- Balanceo de techo limitado y servidor-autoritativo; Espacio conserva inercia/amplitud y las liberaciones pasivas anulan velocidad horizontal.
- Radio de balanceo reducido a `0.665`; corrección de techo con umbral mínimo para evitar acumulación visible de desplazamientos lentos.
- Protocolo 11: identifica anclajes de techo y sincroniza input/cámara sin delegar física al cliente.

Validación realizada en este entorno:

- `cmd /c build-beta.bat` terminó con `BUILD SUCCESSFUL` usando Java 21.
- El JAR generado corresponde a la versión 0.1.24-beta.
- La lógica de tags, frenado y descenso compila contra NeoForge 21.1.235.
- Las pruebas físicas dentro de Minecraft siguen pendientes.

```text
build-beta.bat
```

Resultado esperado:

```text
build/libs/pickclimber-1.21.1-0.1.24-beta.jar
```
