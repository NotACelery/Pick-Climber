# Estado de compilación — Pick Climber 1.1.0

Versión preparada: **1.1.0**

Objetivo:

- Minecraft `1.21.1`
- NeoForge `21.1.235`
- Java `21`
- Protocolo de red `14`

## Estado de implementación

La implementación planificada para 1.1.0 está completa.

Phase 0 dejó el runtime modularizado y 1.1.0 consume esos boundaries sin volver a centralizar física, networking,
presentación ni persistencia.

Resultado estructural vigente:

- `ClimbManager` permanece como fachada de compatibilidad.
- evaluación mecánica centralizada en `AnchorEvaluator`;
- política de superficies centralizada en `AnchorSurfaceResolver`;
- lifecycle centralizado en `AnchorLifecycle`;
- desgaste centralizado en `ToolWearService`;
- cooldown centralizado en `AnchorCooldownService`;
- transporte NeoForge fuera del paquete `climb`;
- opciones cliente en `PickClimberClientOptionsStore`;
- menú dedicado mediante keybind configurable;
- hot-disable por jugador mediante runtime preferences transitorias;
- renderer String/Pickaxe aislado de overlays externos.

## QA funcional aceptado

Durante la preparación de 1.1.0 se validó in-game:

- mecánicas de pared, frenado, superficies inestables y techo;
- Pick Climber, Strong Grip y Sturdy Latch;
- interacción con picotas y cambio de mano;
- prioridad frente a bloques interactivos;
- String y Pickaxe;
- tamaños/transparencias/colores del indicador;
- caja del indicador y sus opciones;
- `Contextual / Always / Off`;
- failure messages;
- Show Unclimbable;
- hot-disable normal;
- hot-disable mientras existe un anclaje activo, incluyendo cleanup;
- preview del menú en sus tamaños extremos;
- aislamiento de color respecto de Jade y otros overlays;
- idiomas `en_us`, `en_gb`, `es_cl`, `es_es`, `es_ar`, `es_mx`, `pt_br`, `pt_pt`;
- GUI responsiva de dos columnas/una columna;
- scroll vertical en ventanas de poca altura.

No queda una feature de 1.1.0 pendiente de implementación.

## Migración de configuración

El formato final sigue siendo `configVersion: 3`.

La release:

- lee configuraciones sin versión;
- convierte `iconOpacity` / `boxOpacity`;
- migra `pickaxe_outline` a `pickaxe`;
- migra el antiguo `colorIntensity` compartido;
- aplica defaults a campos ausentes;
- normaliza valores mediante `PickClimberClientOptions`;
- reescribe configuraciones v3-o-anteriores a la representación canónica;
- no degrada automáticamente configs cuya versión declarada sea futura.

Los nombres legacy permanecen exclusivamente como aliases de migración dentro del store.

## Calidad de source

El proyecto mantiene `verifySourceQuality` y `verifyArchitectureBoundaries` como dependencias de `check`.

La pasada de release no introduce:

- tabs;
- trailing whitespace;
- líneas Java mayores a 120 caracteres;
- debug output directo;
- marcadores temporales;
- bypasses nuevos de arquitectura.

La limpieza de release retira documentación temporal de snapshots y el antiguo nombre
`PickaxeOutlineIndicatorIconRenderer`.

## Gate final de build

Después de aplicar el patch de release, ejecutar:

```text
build.bat
```

El BAT lee:

```text
mod_version=1.1.0
minecraft_version=1.21.1
```

y sólo declara éxito si existe exactamente:

```text
build/libs/pickclimber-1.21.1-1.1.0.jar
```

El build ejecuta `clean build --stacktrace`, incluyendo los verificadores de calidad/arquitectura.

Este documento no afirma un build verde de la fuente de release hasta ejecutar ese BAT después de la limpieza final.
Una vez verde, el source queda listo para etiquetar/publicar como `1.1.0`.
