# Estado de compilación

Versión preparada: `0.1.11-beta`

Objetivo: Minecraft 1.21.1, NeoForge 21.1.235 y Java 21.

La modificación está limitada al ciclo del cooldown en `ClimbManager`: comienza al crear el anclaje, no se reinicia al soltarlo y se sincronizan únicamente los ticks restantes. La clase modificada fue validada con `javac` contra las clases y artefactos de NeoForge 21.1.235 de la build estable conocida.

La ejecución completa de Gradle no pudo repetirse en este entorno porque el plugin `foojay-resolver-convention` no está disponible en la caché offline. Compila localmente mediante:

```text
build-beta.bat
```

Resultado esperado:

```text
build/libs/pickclimber-1.21.1-0.1.11-beta.jar
```


Validación adicional 0.1.11: `PinnedPickaxeRenderer`, `ClimbManager` y `ClientClimbState` compilaron con Java 21 contra las clases de NeoForge 21.1.235 mediante verificación aislada.
