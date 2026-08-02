# Estado de compilación

Versión preparada: `0.1.6-beta`

Objetivo: Minecraft 1.21.1, NeoForge 21.1.235 y Java 21.

La estructura del proyecto y sus recursos fueron validados. La compilación completa no pudo ejecutarse en este entorno porque `services.gradle.org` no resuelve desde la red disponible.

Compila mediante:

```text
build-beta.bat
```

Resultado esperado:

```text
build/libs/pickclimber-1.21.1-0.1.6-beta.jar
```

Cambio de API principal por validar al compilar: `ServerPlayer.connection.teleport(double, double, double, float, float)`.
