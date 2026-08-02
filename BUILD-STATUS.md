# Estado de compilación

Versión preparada: `0.1.12-beta`

Objetivo: Minecraft 1.21.1, NeoForge 21.1.235 y Java 21.

Cambios funcionales de esta beta:

- Selector central de mano con prioridad para la secundaria.
- El pipeline principal permanece sin cancelar cuando la secundaria es la elegida, de modo que una acción vanilla exitosa conserva prioridad.
- Minería y ataque dejan de ser interceptados cuando el ancla está en la mano secundaria.
- El comportamiento legado de detach por clic izquierdo se conserva únicamente para un ancla principal.

`ClimbingHandSelector`, `CommonEvents` y `ClientEvents` pasaron compilación aislada con Java 21 contra los artefactos de Minecraft/NeoForge 21.1.235 de la build estable conocida. La comprobación usó stubs mínimos únicamente para las anotaciones y contratos externos del event bus que no forman parte del JAR merged de desarrollo.

La ejecución completa de Gradle no puede repetirse en este entorno porque el plugin `foojay-resolver-convention` no está disponible en la caché offline. Compila localmente mediante:

```text
build-beta.bat
```

Resultado esperado:

```text
build/libs/pickclimber-1.21.1-0.1.12-beta.jar
```
