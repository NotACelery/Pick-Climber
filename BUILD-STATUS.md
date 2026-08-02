# Estado de compilación

Versión preparada: `0.1.13-beta`

Objetivo: Minecraft 1.21.1, NeoForge 21.1.235 y Java 21.

Cambios funcionales:

- Prioridad secundaria y mano principal libre conservadas desde 0.1.12.
- Transferencia del pico activo mediante el intercambio vanilla de manos (`F`).
- Validación estricta por UUID para diferenciar transferencia de pérdida/cambio de slot.
- Limpieza cliente de grietas durante logout, detach y timeout.
- Limpieza servidor en la dimensión original del anclaje.
- Payload de anclaje ampliado con `anchorBlock` y `crackId`; protocolo 8.

Validación realizada en este entorno:

- `ServerClimbState` y `ClientClimbState` compilaron con Java 21 contra el JAR merged de Minecraft/NeoForge 21.1.235.
- `AnchorSyncPayload` compiló contra las mismas clases usando un stub mínimo exclusivamente para `ByteBuf`.
- `ClimbManager` compiló contra los artefactos exactos de 21.1.235 y las clases conocidas del proyecto; los stubs externos se limitaron a logging/JOML ausentes del artefacto merged.
- `ClientEvents`, `CommonEvents` y `ClimbingHandSelector` compilaron con los contratos oficiales de eventos y stubs mínimos del event bus externo.
- Los JSON y el manifiesto de archivos fueron validados.

La ejecución completa de Gradle no puede repetirse aquí porque las dependencias externas del plugin no están disponibles offline. Compila localmente mediante:

```text
build-beta.bat
```

Resultado esperado:

```text
build/libs/pickclimber-1.21.1-0.1.13-beta.jar
```
