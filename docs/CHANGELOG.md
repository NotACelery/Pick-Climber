# Changelog

## 1.1.0-dev.8 — 2026-09-01

### Integration repair — options screen and deprecation cleanup

- Fixed the 1.21.1 Mojmap package for the vanilla Options screen used by the in-world Pick Climber options entry (`net.minecraft.client.gui.screens.options.OptionsScreen`).
- Removed the deprecated explicit event-bus selector from the client mod-event subscriber; NeoForge routes `IModBusEvent` handlers to the mod bus automatically.
- Switched anchor sound lookup to the contextual `BlockState#getSoundType(level, pos, player)` path, preserving vanilla behavior while allowing modded blocks to provide context-sensitive sound types.
- No climbing physics, balance, anchor classification, runtime preferences or HUD defaults are intentionally changed.
- `dev.7` reached `compileJava` and exposed only the OptionsScreen mapping error plus deprecation warnings; this patch addresses that reported layer.

## 1.1.0-dev.7 — 2026-09-01

### 1.1.0 — options UX finalization

- Finished the planned 1.1.0 options surface source-side; the next gate is compile/API repair and gameplay QA rather than additional feature expansion.
- Added dependency-aware controls: visual widgets are disabled while Indicator Mode is Off, box opacity is disabled while the box is hidden, and Show Unclimbable is only editable in Contextual mode.
- Added `Reset All` alongside `Reset HUD`; the former restores interaction/runtime defaults and immediately requests a preference resync, while the latter preserves the interaction-enable choice.
- Added `configVersion: 1` to `pickclimber-client.json` while keeping old files without a version readable and tolerating newer files by loading known fields defensively.
- Avoided redundant option-file writes when the effective record does not change and clean up temporary files after atomic-move fallback.
- Improved footer responsiveness for narrow GUI layouts.
- Build/API repair remains intentionally deferred to the consolidated integration pass; this entry does not claim a successful JAR build.

## 1.1.0-dev.6 — 2026-09-01

### 1.1.0 — indicator-style completion

- Added the `String / Pickaxe Outline` HUD style selector requested for the 1.1.0 customization menu.
- Implemented Pickaxe Outline as a dedicated line-art renderer; it does not reuse a vanilla pickaxe item or copy a Minecraft texture.
- Split indicator icon drawing behind `IndicatorIconRenderer` and a style dispatcher so future styles remain presentation-only.
- Kept String rendering in its own renderer with the 1.0.2 flush/tint/reset isolation invariant intact.
- Pickaxe Outline uses local ARGB GUI draws and therefore never changes global shader color.
- Persisted `indicatorStyle` in `config/pickclimber-client.json` with a defensive fallback to String for old or malformed configs.
- Added localized style labels for English, Spanish and Chilean Spanish.
- Build/API repair remains intentionally deferred to the final integration pass; this snapshot is source-side implementation, not a claimed successful JAR build.

## 1.1.0-dev.5 — 2026-09-01

### 1.1.0 — options core and runtime control

- Added an in-world-only `Pick Climber Options` entry to the vanilla Options screen; it is not injected when no world/player is loaded.
- Added persistent client HUD preferences in `config/pickclimber-client.json` with defensive default merging for missing/invalid fields.
- Added `Contextual / Always / Off` indicator modes. Contextual hides `UNCLIMBABLE` by default while preserving actionable states.
- Added HUD controls for unclimbable visibility, icon scale, icon opacity, indicator-box visibility, box opacity and failure-message visibility.
- Preserved the 1.0.2 shader-isolation invariant while adding icon opacity and scaling.
- Added HUD reset that restores visual defaults without silently re-enabling disabled Pick Climber interactions.
- Added the first real hot-disable path: a per-player runtime preference is mirrored to the server, client input/feedback stops immediately, and an active attachment is detached through the existing lifecycle path.
- Bumped the development networking protocol from 13 to 14 and added `RuntimePreferencePayload` for transient per-player interaction/failure-text preferences.
- Added server-side transient runtime/presentation preference stores and cleanup on logout.
- Kept icon style on the String renderer for this pass; the planned Pickaxe Outline renderer remains pending.
- Build/API repair is intentionally deferred until the feature implementation pass is complete, per the current development strategy.
- Removed legacy documentation copies from the snapshot root; only `README.md` remains as root documentation. `MIGRATE-DOCS-DEV5.bat` removes stale copies when applying the patch over earlier dev trees.

## 1.1.0-dev.4 — 2026-09-01

### Phase 0.9 — compile repair and repository cleanup

- Fixed the first real Java compile failure exposed by `dev.3`: client movement input is now read from `LocalPlayer` instead of a base `Player` reference.
- Preserved the exact 1.0.3 input values (`forwardImpulse`, `leftImpulse`, yaw and pitch) and existing protocol-13 payloads.
- Moved release/development documentation out of the repository root: build status, changelog, roadmap and manifest now live in `docs/`, while regression checklists live in `docs/testing/`.
- Added `MIGRATE-DOCS-DEV4.bat` to safely remove only the stale root copies left behind when the ZIP is extracted over a `dev.3` tree.
- Updated all active documentation references and Phase 0.9 targets to the new paths and `dev.4`.
- No gameplay, physics, balance, anchor classification, interaction priority or network protocol behavior is intentionally changed in this patch.

## 1.1.0-dev.3 — 2026-09-01

### Phase 0.3–0.8 — runtime decomposition

- Extracted authoritative attach/detach/cleanup orchestration into `AnchorLifecycle` and attachment coherence into `AnchorStateValidator`.
- Reworked the large positional `ServerClimbState` construction around coherent restore/input/braking/ceiling substates while preserving compatibility accessors.
- Completed `ToolWearService` and `AnchorCooldownService`; all Pick Climber durability and cooldown persistence now goes through focused boundaries.
- Added typed wear reasons so future world rules can scale durability without coupling map profiles to physics internals.
- Fixed the audited remaining-cooldown reporting mismatch that could visually truncate a 40-tick unstable cooldown to 20 ticks.
- Broke the old `climb <-> network` package cycle with a domain `AnchorSyncSink` and NeoForge transport adapter; protocol 13 is unchanged.
- Extracted wall motion, ceiling motion, impulses, positioning, visuals, runtime ticking, action commit and validated input state from `ClimbManager`.
- Reduced `ClimbManager` from the original 1,723 lines to a 154-line compatibility façade.
- Extracted HUD drawing and client input from `ClientEvents`; it is now a 49-line NeoForge event adapter.
- Added default pass-through runtime and presentation policy gates as explicit seams for the future 1.1.0 options/hot-disable implementation. No customization behavior is enabled yet.
- Centralized forced-anchor and post-block-use interaction decisions in `AnchorInteractionService`.
- Split creative-tab registration into `ModCreativeTabs` while preserving existing resources and behavior.
- Preserved 1.0.3 physics/tuning as the intended regression baseline; no new 1.1.0 player-facing feature is part of this structural snapshot.

### Phase 0.9 preparation

- Added Java 21 GitHub Actions CI for `main`, `preparacionUpdate` and pull requests.
- Expanded architecture verification to protect evaluation, surface, wear, networking and thin-adapter boundaries.
- Reworked `verifyArchitectureBoundaries` so its source directories/file trees are resolved during Gradle configuration, addressing the configuration-cache failure exposed by the `1.1.0-dev.2` build attempt.
- Full NeoForge compilation and gameplay regression are intentionally deferred to the final Phase 0.9 integration pass.

## 1.1.0-dev.2 — 2026-09-01

### Phase 0.2 — unified anchor evaluation

- Extracted side-effect-free anchor evaluation from `ClimbManager` for both hands.
- Hand selection, real anchor permission, HUD state and failure messages now consume the same evaluated facts.
- Moved anchor face/target/collision calculations into `AnchorGeometry` and made attach/boost reuse the resolved target instead of deciding again.
- Added `AnchorSurfaceResolver` as the world-aware policy seam that future map rules will extend while preserving current tags as the default.
- Added `ClimbSessionView` so the evaluator reads active-session state without depending on the orchestration manager.
- Added an architecture verification gate that rejects side effects inside evaluation and direct bypasses of the surface resolver.
- `canAttemptAnchor` no longer mutates duplicated tool UUIDs or clears cooldown data during a validation query; identity separation remains in the attach commit path.
- No physics, balance, controls, HUD presentation defaults, interaction priority, tags or network protocol were intentionally changed.

## 1.1.0-dev.1 — Phase 0 structural preparation

### Internal architecture

- Started the 1.1.0 modularization pass on top of the validated 1.0.3 gameplay baseline.
- Centralized climbing tuning values in `ClimbTuning` without changing any values.
- Extracted runtime state storage, jump authorization, tool lookup/wear and anchor cooldown coordination from `ClimbManager`.
- Moved the remote anchor pose record out of the manager into its own immutable type.
- Added a Gradle source-quality gate for Java formatting invariants and JSON/MCMeta syntax.
- No climbing physics, balance, interaction priority, enchantment behavior, tags or network protocol behavior changed in this pass.

## 1.0.3 — 2026-09-01

### HUD fixes

- Fixed the String anchor indicator affecting the colors of Jade tooltips, event text and other HUD overlays.
- The anchor indicator now keeps normal right-click interactions clear: it stays hidden while aiming at interactive blocks unless the player is sneaking.
- Holding Shift reveals the indicator again when the targeted face can be used as a valid anchor.
- Interactive-block detection covers menu-backed blocks, loaded BlockEntity-backed machines and the extensible `pickclimber:interactive_blocks` tag.
- Direct-use machines such as noise switches are covered without third-party hardcodes, reflection or fake interaction calls.

## 1.0.2 — 2026-08-30

### HUD compatibility fix

- Isolated the anchor-status tint to the String indicator drawn under the crosshair.
- Pending GUI draws are flushed before the tint is applied, the icon is flushed while its tint is active, and the shader color is restored to white immediately afterward.
- Jade tooltips, game-event text and other HUD overlays no longer inherit Pick Climber's green, cyan, red, violet, gray or yellow status colors.
- The indicator border keeps using a local ARGB fill color and does not modify shared shader state.

### Source cleanup

- Extracted the tinted icon render and border render into focused helpers.
- Replaced HUD layout/color magic numbers with named constants and centralized RGB channel conversion.
- No climbing physics, networking, balance, interaction priority or anchor validation behavior changed in this cleanup.

## 1.0.1 — 2026-08-20

### Branding / icon integration

- Replaced the packaged mod logo with the new pixel-art climbing-pick icon.
- Added a Pick Climber creative tab for the mod enchantments, using the same new icon in-game.
- The same icon is now used for NeoForge/FML mod details and launcher mod menus through `logoFile`.

### Interaction priority hotfix

- Pick Climber now waits until NeoForge's post-block interaction phase before anchoring.
- Blocks that consume right click for their own GUI or interaction keep priority, including Easy Villagers Farmers and Easy Farmer's Delight Compat Farmers.
- Off-hand climbing-tool priority is preserved when the targeted block does not handle the click.
- Shift-right-click is an explicit force-anchor override: when a valid climbing tool and anchor face are available, Pick Climber takes priority even if the target block normally consumes Shift interaction.
- Invalid anchor attempts do not steal the block's normal Shift interaction.

## 1.0.0

- Primera release pública estable de Pick Climber para Minecraft 1.21.1 y NeoForge 21.1.235.
- Consolida escalada de paredes, frenado progresivo, superficies inestables, maniobras con dos picotas y movimiento de techo.
- Incluye Pick Climber I–III, Sturdy Latch I y Strong Grip I con sus compatibilidades y especializaciones vigentes.
- Publica indicadores de anclaje, mensajes localizados, poses elevadas y selección data-driven de herramientas y superficies.
- Declara `celerbi` como autor y localiza la descripción de la lista de mods en inglés, español general y español de Chile.
- Los scripts de Windows y Linux leen la versión desde `gradle.properties` y verifican el JAR exacto antes de declarar éxito.
- No modifica el protocolo 13 ni el balance físico validado en 0.1.27-beta.

## 0.1.27-beta

- Reemplaza el indicador genérico de cuerda por una banda compacta y localizada bajo la mira.
- Distingue anclaje firme, superficie inestable, superficie no escalable, obstrucción, cooldown y destino fuera del alcance real.
- Los techos informan por separado si requieren Strong Grip o Strong Grip + Sturdy Latch.
- La evaluación visual reutiliza las mismas comprobaciones de herramienta, cara, hitbox, distancia y prioridad de manos del enganche sin ejecutar física ni consumir recursos.
- Los bloques con menú conservan el indicador oculto durante uso normal y lo habilitan con Shift.
- El String vuelve a ser el único indicador permanente bajo la mira, teñido y enmarcado por estado; se retira el texto diagnóstico inferior tras validar visualmente los colores.
- Suprime completamente el HUD sobre `MISS`, entidades y bloques situados a más de 3 bloques del punto de vista.
- Evalúa el radio de 1.5 bloques antes de buscar una hitbox corregida, evitando confundir destinos lejanos con `ANCHOR OBSTRUCTED`.
- Un agarre inestable sin Sturdy Latch usa cian; con Sturdy Latch pasa a verde porque el resultado será firme.
- Los requisitos de un nuevo punto de techo se calculan sobre la picota libre; el pico Strong Grip ya ocupado no convierte erróneamente el estado en cooldown.
- Un clic derecho rechazado muestra en la barra de acción el motivo localizado: Strong Grip, cooldown, alcance, bloque obstructivo, falta de espacio o entidad.
- Los avisos de Sturdy Latch no generan mensaje de rechazo porque el descenso inestable permitido conserva su comportamiento diferenciado.
- Limita el HUD a 3 bloques: muestra `OUT OF RANGE` entre 1.5 y 3, se oculta más lejos y solo evalúa `ANCHOR OBSTRUCTED` dentro del alcance físico.
- Ajusta el descenso inestable sin Sturdy Latch de `0.128` a `0.136` bloques/tick, 70 % más rápido que la base original.
- No modifica protocolo 13, alcance físico de anclaje, durabilidad, cooldown ni selección autoritativa del servidor.

## 0.1.26-beta

- Añade los tags de objeto `pickclimber:climbing_tools` y `pickclimber:excluded_climbing_tools` para compatibilidad configurable con herramientas de otros mods.
- Toda la elegibilidad durante escalada, cooldown y render pasa por un clasificador central; una exclusión siempre prevalece sobre una inclusión.
- `climbing_tools` hereda `#minecraft:pickaxes` para conservar compatibilidad general e incluye de forma opcional la maza-picota `eternal_starlight:thermal_springstone_hammer`.
- La picota de madera se incluye en `excluded_climbing_tools` y deja de poder iniciar o mantener anclajes.
- La integración con Eternal Starlight es opcional mediante tag y no añade dependencias de carga.
- Valida manualmente las picotas de Eternal Starlight y Twilight Forest, la exclusión de madera, el cambio de mano y los encantamientos.
- Sincroniza la pose elevada de Strong Grip con los clientes que observan al jugador mediante un payload visual mínimo e independiente de la física.
- La pose remota se actualiza al enganchar, cambiar con `F`, pasar entre techo y pared o soltarse; una renovación periódica cubre observadores tardíos y un timeout evita estados huérfanos.
- Protocolo interno actualizado a versión 13.
- Documenta como validadas las maniobras Strong Grip con dos picotas, cambio con `F`, salto con momento, cruce de huecos y transición alrededor de bordes.
- No modifica física, distancias, durabilidad, cooldown ni balance de encantamientos.

## 0.1.25-beta

- Añade una pose elevada propia para el brazo y la picota activos al usar Strong Grip en primera persona.
- Corrige la vista local en tercera persona: el brazo activo se eleva y la picota sigue la transformación del modelo en lugar de permanecer abajo.
- Corrige puntos válidos de pared rechazados bajo un techo: si la altura ideal solapa la hitbox con el bloque superior, se elige la altura libre más cercana dentro del mismo alcance.
- El icono y el servidor comparten el mismo destino corregido; una posición que realmente colisione continúa siendo inválida y no consume recursos.
- Corrige la liberación de techo con Espacio: el impulso voluntario usa `W/A/S/D` orientado por cámara en vez de empujar siempre hacia donde se mira.
- Iguala ese impulso voluntario a los `0.65` del salto desde pared y amplía el límite combinado para que la inercia alineada lo refuerce sin quedar recortada prematuramente.
- La solicitud de liberación incluye atómicamente `W/A/S/D` y cámara del instante de Espacio; el servidor ya no depende del último paquete periódico para calcular el salto. Protocolo interno actualizado a versión 12.
- Corrige la aplicación final del salto de techo: el servidor envía explícitamente al cliente el vector calculado tras desenganchar, evitando que la física local descarte tanto el impulso direccional como la inercia del balanceo.
- Separa la velocidad limitada de la hitbox del momento de liberación: el balanceo acumula hasta `0.38` bloques/tick en cualquier dirección y el límite del radio ya no elimina esa energía antes del salto.
- Suaviza el balanceo aplicando cada destino servidor-validado desde el payload de techo y reserva el teletransporte absoluto para confirmaciones periódicas, evitando dos correcciones duras por tick.
- La pose entra suavemente durante 4 ticks, queda fija sin acumular swing vanilla y se refleja para ambas manos y jugadores zurdos.
- Transferir el mismo pico con `F` mueve la pose al otro brazo sin reiniciar su transición ni recrear el anclaje.
- La pose de pared y el render de la mano libre permanecen sin cambios; el hotfix de liberación no modifica durabilidad ni cooldown.

## 0.1.24-beta

- Reduce 30 % el radio del balanceo Strong Grip, de `0.95` a `0.665` bloques.
- El balanceo de techo confirma movimientos pequeños cada tick para eliminar tirones durante la desaceleración y el retorno.

## 0.1.23-beta

- Añade balanceo restringido bajo techos Strong Grip, integrado y validado por el servidor.
- Espacio libera hacia la cámara conservando inercia y aprovechando la amplitud acumulada.
- Doble Shift y clic izquierdo con el pico activo liberan sin velocidad horizontal.
- Protocolo interno actualizado a versión 11 para identificar visualmente anclajes de techo.

## 0.1.22-beta

- Añade Strong Grip I, exclusivo con Pick Climber y compatible con Sturdy Latch.
- Permite anclaje estático en techos firmes; un techo inestable exige Strong Grip + Sturdy Latch.
- Cada anclaje de techo cuesta 20 de durabilidad y permanecer suspendido cobra 1 cada 20 ticks.

## 0.1.21-beta

- Corrige el desgaste adicional con dos picotas al separar UUIDs duplicados antes del frenado dual.
- Restaura el cambio de punto con la otra mano hasta 1.5 bloques mientras una picota sigue anclada.

## 0.1.20-beta

- Sturdy Latch I reduce el cooldown inicial de una superficie inestable de 40 a 20 ticks, igual que una superficie firme.

## 0.1.19-beta

- Añade desgaste proporcional al rescate: 15 de base y 10 adicionales por cada bloque vertical completo de `BRAKING`.
- Dos picotas equipadas participan en el frenado, reciben los mismos costes y absorben la caída aproximadamente al doble de velocidad.
- Aumenta 60 % la velocidad del descenso controlado en superficies inestables sin Sturdy Latch.

## 0.1.18-beta

- Añade `Sturdy Latch I` como encantamiento data-driven para `#minecraft:pickaxes`.
- En superficies inestables, fija el anclaje tras frenado; en caída leve lo fija de inmediato.
- Sin Sturdy Latch, arena, grava, concreto en polvo y nieve mantienen el descenso controlado.
- Conserva el cooldown de 40 ticks de las superficies inestables y no altera Strong Grip, techos ni desgaste proporcional.

## 0.1.17-beta

- Corrige la cámara bloqueada durante frenado o descenso: las correcciones de posición conservan la rotación local.
- Añade payload servidor-autoritativo de intención de cámara y `W/A/S/D` para que el movimiento lateral funcione tanto durante `BRAKING` como `UNSTABLE_SLIDING`.
- Sincroniza el estado móvil en cada tick, eliminando el retraso de actualización de ancla y grietas durante el deslizamiento.
- Corrige la orientación de A/D durante el deslizamiento.
- Llegar al suelo durante un descenso termina el anclaje pasivamente en lugar de congelarlo.
- Doble pulsación de Shift dentro de 7 ticks suelta el anclaje sin impulso.
- El ancla conserva el punto exacto del clic para resolver correctamente el bloque y la grieta durante el deslizamiento.
- En frenado por caída libre, la primera dirección lateral queda fijada como trayectoria diagonal hasta que el anclaje termine o se estabilice.
- El umbral de frenado pasa de `-0.25` a `-0.40` bloques/tick para que caídas leves se enganchen directamente.
- El frenado absorbe la caída aproximadamente durante el doble de tiempo; valores de balance actuales fijados en arrastre `0.75` y recuperación `0.035`.
- `BRAKING` ahora exige más de 5 bloques de caída acumulada además de velocidad suficiente, evitando activarlo tras saltos o caídas cortas.
- Protocolo interno actualizado a versión 10.

## 0.1.16-beta

- Permite desplazamiento lateral con `W/A/S/D` durante frenado y descenso a la mitad de la velocidad vertical actual, limitado al plano de la pared.
- La cámara conserva orientación libre durante el movimiento; el servidor mantiene autoridad sobre la posición final y las colisiones.
- El ancla y las grietas avanzan al bloque que sostiene actualmente el pico al deslizarse entre bloques.
- Encontrar una pared firme durante un descenso inestable lo convierte en un anclaje fijo; acabar la superficie termina el anclaje de forma segura.
- Los anclajes iniciados en superficies inestables aplican 40 ticks de cooldown individual en vez de 20.
- El cooldown persistente conserva su duración para que el indicador cliente represente correctamente ambos tiempos.
- Protocolo interno actualizado a versión 9 para sincronizar el cooldown inicial variable de un anclaje.

## 0.1.15-beta

- Añade clasificación de superficies mediante los tags data-driven `stable_anchor_blocks`, `unstable_anchor_blocks` y `unclimbable_blocks`.
- La prioridad es no escalable, inestable, estable y fallback compatible vigente.
- Hielo, hielo compacto, hielo azul, hielo escarchado y yunques se declaran firmes.
- Arena, grava, concreto en polvo, nieve, capas de nieve y nieve en polvo se declaran inestables.
- Los bloques administrativos/protegidos incluidos en `unclimbable_blocks` rechazan el anclaje.
- Una caída rápida se frena progresivamente desde el servidor; no usa velocidad cliente ni corrección hacia el ancla.
- En superficies firmes el frenado termina fijo; en inestables continúa con descenso controlado y comprobación de colisiones por tick.
- No añade desgaste proporcional, Sturdy Latch, Strong Grip ni techos.

## 0.1.14-beta

- Los bloques que declaran un `MenuProvider` conservan el clic derecho vanilla mientras el jugador no mantenga Shift.
- `Shift + clic derecho` permite intentar el enganche sobre hornos, mesas de crafteo, cofres y otros bloques con menú sin abrir su interfaz.
- La regla se aplica antes de seleccionar la mano y también al indicador de alcance, evitando mostrar un anclaje que el clic normal no ejecutará.
- No se utilizan listas hardcodeadas de bloques interactivos.
- Un intento inválido continúa sin consumir durabilidad ni iniciar cooldown.
- Se conserva como comportamiento intencional el detach pasivo al intentar minar o atacar con el mismo pico que sostiene el ancla.
- Rebalance de Pick Climber: `weight` 4 → 6, `anvil_cost` 4 → 1, coste mínimo base 10 → 5 y progresión 12 → 8.
- El coste máximo pasa a base 25 y progresión 8 por nivel adicional.
- No modifica impulso, wall jump, física, red, transferencia con `F`, pose ni limpieza de grietas.

## 0.1.13-beta

- Completa la transferencia del anclaje al intercambiar manos con `F`.
- Detecta el mismo pico por UUID en la mano contraria y actualiza únicamente la mano activa.
- Transferir no consume durabilidad, no reinicia cooldown, no repite sonido, no recrea grietas y no mueve el ancla.
- La pose clavada pasa a la nueva mano y la anterior vuelve al render vanilla.
- Cambiar de slot o retirar el pico de ambas manos termina el anclaje de forma pasiva; ya no se repara el UUID sobre otra herramienta distinta.
- El estado servidor guarda la dimensión exacta donde se creó el anclaje.
- Cambiar de dimensión o desconectarse limpia la grieta en el nivel original.
- El payload de anclaje sincroniza la posición del bloque y el `crackId` para permitir limpieza local.
- `ClientPlayerNetworkEvent.LoggingOut` elimina el overlay antes de destruir el `ClientLevel`.
- Los timeouts cliente y los cambios de punto también limpian overlays huérfanos.
- Protocolo interno actualizado a versión 8.
- Mantiene la prioridad secundaria y la mano principal libre de 0.1.12 sin alterar impulso, wall jump, cooldown, desgaste ni pose.

## 0.1.12-beta

- Añade un selector central de mano para todas las maniobras de Pick Climber.
- Cuando ambos picos están disponibles, la mano secundaria tiene prioridad para enganchar o impulsar.
- La interacción de la mano principal no se cancela al preferir la secundaria: colocar, usar, abrir o consumir objetos conserva el pipeline vanilla y solo un resultado `PASS` permite continuar con la izquierda.
- Con un pico secundario sosteniendo el ancla, el clic izquierdo vuelve a minar y atacar normalmente con la mano principal.
- Colocar bloques y usar objetos con la principal no desengancha el pico secundario.
- Un segundo pico disponible en la principal puede reemplazar el anclaje secundario.
- Se conserva temporalmente el detach por clic izquierdo únicamente cuando el ancla está en la propia mano principal.
- No modifica la física de impulso, wall jump, cooldown, durabilidad ni la pose clavada de 0.1.11.

## 0.1.11-beta

- Añade un render dedicado de primera persona para el pico que mantiene el anclaje.
- La herramienta entra durante 4 ticks a una pose adelantada equivalente al golpe vanilla y queda congelada allí.
- Solo se cancela y redibuja la mano que sostiene el pico activo; la otra mano conserva su render vanilla.
- La pose se refleja correctamente para mano izquierda y derecha usando el brazo real del jugador.
- Cambiar de pico reinicia la entrada visual únicamente para el nuevo pico activo.
- Soltarse restaura inmediatamente la pose idle normal.
- El cooldown sigue bajando desde el instante del enganche y permanece independiente de la pose.
- Reemplaza el gato placeholder por el nuevo icono oficial de Pick Climber.
- No modifica impulso, enganche, durabilidad, cooldown ni física estable de la 0.1.6.

## 0.1.10-beta

- Corrige la interpretación visual del cooldown durante un enganche.
- El cooldown comienza al confirmar el enganche y el overlay baja inmediatamente de 100 % a 0 %.
- El overlay ya no queda congelado al 100 % mientras el pico sigue clavado.
- Soltar el pico, saltar o cambiar al segundo pico no inicia ni reinicia el cooldown.
- El estado de pico activo queda separado del temporizador; su indicador dedicado se implementará junto con la pose clavada.
- No modifica la física, el impulso, la durabilidad ni la corrección crítica de salto de la 0.1.6.

## 0.1.9-beta

- El cooldown individual vuelve a comenzar inmediatamente al confirmar un enganche.
- El pico activo sigue mostrando el overlay al 100 % mientras permanece clavado.
- Al liberarse, el cooldown no se reinicia: solo continúa con los ticks que queden.
- Si el jugador permanece enganchado durante 20 ticks o más, el pico queda disponible al soltarse.
- Cambiar al segundo pico no reinicia ni prolonga el cooldown del primero.
- Evita que un wall jump corto deje al pico sin una ventana de bloqueo y provoque un impulso accidental al intentar volver a engancharse arriba.
- No modifica la física estable de impulso y enganche de la 0.1.6.

## 0.1.8-beta

- Corrige el overlay de cooldown completo en picos nuevos o sin cooldown.
- Evita el desbordamiento de `long` causado por `Long.MIN_VALUE - gameTime`.
- Un `ItemStack` sin `cooldown_until` ahora representa correctamente 0 ticks restantes.
- El cálculo visual comprueba que el cooldown siga activo antes de restar tiempos.
- No modifica la física, el enganche ni el impulso de la 0.1.7.

## 0.1.7-beta

- Reemplaza el overlay manual de hotbar por un `IItemDecorator` registrado en el render real del `ItemStack`.
- El pico activo muestra el mismo blanco translúcido del cooldown vanilla, congelado al 100 %.
- El cooldown de 20 ticks ya no comienza al engancharse: empieza únicamente al liberar el pico.
- Al cambiar al segundo pico, el anterior comienza su cooldown y el nuevo queda marcado como activo.
- Los estados visuales se vinculan al UUID propio de cada pico, incluso cuando ambos son del mismo material.
- El indicador funciona en hotbar, mano secundaria e inventarios que rendericen las decoraciones del objeto.
- Un pico activo queda ocupado y no puede reutilizarse para crear otro anclaje.
- El payload de anclaje ahora sincroniza el UUID exacto y los ticks de cooldown al liberar.
- Protocolo de red actualizado a versión 7.

## 0.1.6-beta

- Corregido el desenganche automático causado por `KeyMapping.consumeClick()`.
- El wall jump ahora exige una pulsación nueva de salto después de soltar la tecla.
- Mantener Espacio durante un salto, impulso o enganche ya no provoca un salto fantasma.
- Agregado diagnóstico `DETACH_JUMP`/`DETACH_PASSIVE` con edad del anclaje en `latest.log`.

## 0.1.5-beta

- Reescribe la sincronización física del anclaje desde la raíz.
- Sustituye `ServerPlayer#setPos` por `ServerGamePacketListenerImpl#teleport` para confirmar la posición con el cliente.
- Elimina todas las correcciones de posición mediante velocidad hacia el ancla.
- El cliente deja de cambiar posición, gravedad, vuelo o estado físico durante el enganche.
- El cliente ya no invalida anclajes de forma autónoma sin avisar al servidor.
- Un paquete atrasado de impulso ya no puede desmontar un anclaje confirmado.
- El impulso requiere un salto real reciente, estar en el aire y seguir ascendiendo.
- La velocidad positiva por sí sola siempre cae en la ruta segura de enganche.
- Cada autorización de salto solo puede consumirse una vez.
- Tocar el suelo o engancharse limpia la autorización anterior.
- Un wall jump autoriza un posible impulso posterior con el otro pico.
- Añade una línea diagnóstica por uso válido con la acción elegida y el estado del jugador.
- Protocolo de red actualizado a versión 6.

## 0.1.4-beta

- Convierte el antiguo impulso accidental en una mecánica explícita y controlada.
- Clic derecho mientras el jugador asciende por encima de `0.08 Y` ejecuta un impulso.
- Clic derecho al caer, quedar quieto o alcanzar el ápice ejecuta el enganche normal.
- El vuelo creativo activo siempre utiliza la ruta de enganche.
- El impulso y el enganche son rutas mutuamente excluyentes.
- El impulso conserva la inercia horizontal y no crea estado de anclaje ni grietas.
- Añade sincronización específica cliente-servidor para el impulso y su cooldown visual.
- Añade el encantamiento data-driven `Pick Climber I–III`.
- El pico base añade aproximadamente 1 bloque de ascenso restante.
- Cada nivel de Pick Climber añade 0,5 bloques al impulso.
- Cada nivel añade también 0,5 bloques al wall jump desde un anclaje.
- Mantiene el coste de 15 de durabilidad y el cooldown individual por pico.
- Protocolo de red actualizado a versión 5.

## 0.1.3-beta

- Refuerza la limpieza de estados cliente atrasados.
- Retira temporalmente la pose experimental de primera persona.
- Mejora el indicador del pico activo y la sincronización visual del cooldown.

## 0.1.2-beta

- Cooldown individual por pico.
- Coste aumentado a 15 de durabilidad.
- Indicador de alcance y overlays de hotbar.
- Movimiento horizontal y diagonal estabilizado.

## 0.1.1-beta

- Enganche durante caídas.
- Soporte creativo.
- Identidad persistente del pico.
- Grietas y sonido del bloque objetivo.

- Agregada portada/logo temporal del mod: el gato guardián compilador.
