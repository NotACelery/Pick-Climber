# Estado de compilación

Versión preparada: `0.1.17-beta`

Objetivo: Minecraft 1.21.1, NeoForge 21.1.235 y Java 21.

Cambios funcionales:

- Clasificación data-driven de superficies estables, inestables y no escalables.
- Frenado progresivo servidor-autoritativo para caídas rápidas.
- Descenso controlado en superficies inestables y comprobación de colisión antes de cada desplazamiento.
- Desplazamiento lateral a media velocidad de descenso con input cliente validado por servidor y transición inmediata de grietas/superficie.
- Frenado definitivo: `fallDistance > 5`, velocidad `< -0.40`, arrastre `0.75`, recuperación `0.035` y movimiento máximo `0.60` bloques/tick.
- La caída de frenado fija la primera dirección diagonal y aplica 20 ticks de esfuerzo al otro pico equipado.
- Protocolo 10: sincroniza el cooldown inicial variable y la intención de movimiento/cámara sin delegar física al cliente.

Validación realizada en este entorno:

- `cmd /c build-beta.bat` terminó con `BUILD SUCCESSFUL` usando Java 21.
- El JAR generado corresponde a la versión 0.1.17-beta.
- La lógica de tags, frenado y descenso compila contra NeoForge 21.1.235.
- Las pruebas físicas dentro de Minecraft siguen pendientes.

```text
build-beta.bat
```

Resultado esperado:

```text
build/libs/pickclimber-1.21.1-0.1.17-beta.jar
```
