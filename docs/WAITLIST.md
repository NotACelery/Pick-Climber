# Pick Climber — Lista de espera / Work Queue

> **Documento vivo.**  
> Actualizar después de cada pasada de implementación.  
> Fecha inicial: **2026-09-02**.  
> Estado materializado actual: **1.2.0-dev.21**.  
> dev.8 fue el code floor recuperado; dev.9 reconstruyó Rule Book/Table/Terminal, dev.10 materializó authoring final +
> Temporary WORLD, dev.11 completó Temporary PLAYER, dev.12 materializó Rule Dispenser + Temporary Rule Book
> transport/HUD dual, dev.13 añadió Structural Geometry Safety, dev.14-dev.16 cerraron la campaña de build, dev.17
> materializó Viewer + Duplicate y **fue confirmado con build SUCCESS en Windows**. dev.18 cerró portable identity,
> JSON/filesystem cross-platform y migración estricta Table/Terminal/Dispenser y **fue confirmado con build SUCCESS en Windows**.
> dev.19 cerró el código core pendiente de Table/networking y fue confirmado build-clean. Por decisión de cierre visual,
> dev.20 materializó los assets finales y fue confirmado build-clean; dev.21 materializa JEI/EMI opcionales como último bloque de código antes del feature freeze.


> **Pass dev.21 (2026-09-02):** integración opcional JEI/EMI aislada en `integration/jei` y `integration/emi`,
> relaciones visuales de autoría/aplicación sin recipes survival, Rule Book blanco actual como icono/output, Table/Terminal
> como catalysts/workstations y gate estático que impide hard-links o dependencias runtime obligatorias desde el core.

> **Pass dev.20 (2026-09-02):** assets finales materializados desde las referencias aprobadas: Rule Book 32x32 en
> dos layers perfectamente alineados, tint dinámico por los 16 `DyeColor`, páginas/hardware/icono HUD fijos, Table
> orientable horizontalmente y modelos/texturas finales 64x64 para Table/Terminal/Dispenser. Terminal y Dispenser
> usan modelos horizontal/vertical separados para facilitar una futura cara vertical específica del Dispenser.

> **Pass dev.19 (2026-09-02):** cierre de código core antes de integraciones/assets: confirmación real de Restore World
> Defaults mediante `ConfirmScreen`, ghost de Dye estilo Loom sin asset propio, `MAX_EXPLICIT_BLOCKS=8192`, Rule Book
> serializado limitado a 512 KiB, NBT codecs con `NbtAccounter` acotado y preflight/validación client+server.

## Orden de cierre 1.2.0

1. **P0 — Código core:** Table UX final + networking hardening. **Materializado en dev.19.**
2. **P1 — Assets finales:** Rule Book tintable, Table, Terminal y Dispenser. **Materializado en dev.20; queda sólo el refinamiento vertical del Dispenser.**
3. **P2 — Código de integración:** JEI + EMI opcionales, sin hard dependency. **Materializado en dev.21.**
4. **Después:** feature freeze de código, refinamiento vertical del Dispenser, QA, documentación de release y RC.

> **Pass dev.18 (2026-09-02):** JSON export usa exclusivamente `bookName` validado como basename, política de nombre
> portable Windows/Linux/macOS centralizada, `cover_color` ausente migra a White, feedback específico de version incompatible,
> filesystem roundtrip/overwrite tests, canonical portable identity tests, `book_name == profile.name` como invariant y
> política legacy estricta: Table repara; Terminal/Dispenser sólo aceptan schema v2 actual.

> **Pass dev.17 (2026-09-02):** Viewer read-only desde right-click del Rule Book, tabs Overview/Stable/Unstable/
> Unclimbable con Search compartido y cierre por Escape/keybind real de Inventory; Duplicate sub-GUI con source inmutable,
> Books xN, Dye override opcional, preview/count y validación/consumo server-side. Output va al inventario y el overflow
> seguro cae al suelo. Search compartido ahora reconoce también el ResourceLocation completo `namespace:path`.

> **Baseline dev.20 (2026-09-02):** `clean build` confirmado SUCCESS por el usuario en Windows. Assets finales y
> tint dinámico del Rule Book quedan incorporados a la línea base estable/build-clean; dev.21 requiere aceptación de build.

## Leyenda

- `[x]` Existe en el code floor recuperado o fue completado y verificado en source persistido.
- `[~]` Diseño cerrado / implementación parcial previa, pero debe verificarse o reconstruirse en el source actual.
- `[ ]` Pendiente.
- `[>]` Diferido deliberadamente a otra versión.
- `[!]` Riesgo / requiere decisión o QA especial.

---




> **Pass dev.13 (2026-09-02):** Structural Geometry Safety central por `BlockState` concreto +
> `isCollisionShapeFullBlock`, precedencia sobre Stable/Unstable/Unlisted cuando Rules está activo, soporte correcto de
> estados del mismo ID (slab simple bloqueado, slab DOUBLE permitido), tests representativos vanilla y fix portable del
> path `rules/network` en `verifyRulesIntegrity` para Windows/Linux.

> **Pass dev.12 (2026-09-02):** Climbing Rule Dispenser Creative-only, master TEMPORARY persistente, lifetime de
> transporte 1..60 s (default 30), issuance por UUID/token, Temporary Rule Book owner-only con expiry en inventario,
> mano/suelo, cleanup por death/destrucción/Terminal, reconnect sync y HUD Rules/Book dual. También corrige
> `verifyRulesIntegrity` para Gradle 9.2.1 Configuration Cache sin desactivar el cache.

> **Pass dev.11 (2026-09-02):** Temporary PLAYER server-side por UUID, precedencia PLAYER -> WORLD -> defaults,
> sync sólo al jugador afectado, expiry por game time, cancelación en death/logout, WORLD mutation cancela overlays,
> revalidación de anchors y countdown HUD para reglas temporales. En 1.2 el cambio de dimensión conserva la sesión
> PLAYER porque las reglas WORLD siguen siendo globales.

> **Pass dev.10 (2026-09-02):** editor completo de Rule Book, create/import transaccional al Save, Import Current
> Rules, JSON full Rule Book con `.rules.json` + migración `.json` legacy, Effective Rules seam, Permanent WORLD +
> Temporary WORLD persistente, no-op/refresh, sync/revalidación inmediata y consumo correcto desde Terminal.

> **Pass dev.9 (2026-09-02):** migración física Card -> Rule Book, creación del schema portable v2,
> `unmineableTerminals`, stale sessions sin revision portable, separación Table/Terminal y slots Book/Rule Book/Dye.
> El build no pudo ejecutarse porque el entorno no pudo resolver `services.gradle.org`; los tests nuevos están persistidos
> pero quedan pendientes de ejecución real.

# 0. Baseline y recuperación

- [x] Release pública 1.1.0 como baseline estable.
- [x] Baseline fuente completa recuperada desde `Pick-Climber(20260902-005522).zip`.
- [x] Batch acumulativo 1.2.0-dev.8 recuperado.
- [x] dev.8 superpuesto sobre baseline para crear source completo recuperable.
- [x] Minecraft 1.21.1.
- [x] NeoForge 21.1.235.
- [x] Java 21.
- [x] Documentos post-dev.8 preservados en `reference/` del handoff snapshot.
- [x] Scope separado en 1.2.0 / 1.3.0.
- [x] README actualizado al scope general.
- [x] ROADMAP actualizado al scope 1.2/1.3.
- [x] WAITLIST creada.
- [x] Tras reconstruir post-dev.8, cambiar `mod_version` al siguiente dev real (`1.2.0-dev.9`).
- [ ] Crear commit/snapshot Git local después de reconstrucción.

---

# 1. 1.2.0 — Naming y migración Card -> Rule Book

- [x] Reemplazar concepto `Climbing Rules Card` por `Climbing Rule Book` en dominio Java activo.
- [x] Renombrar registry ID final a `climbing_rule_book`.
- [x] Renombrar clases Java Card -> RuleBook en el flujo activo.
- [x] Renombrar lang keys.
- [x] Renombrar modelos/texturas activos; el rediseño visual final sigue en sección 18.
- [x] Renombrar JSON terminology activa a Rule Book/schema v2.
- [x] Eliminar residuos `card` user-facing no necesarios.
- [ ] Definir/mantener aliases sólo si algún build público lo requiere; builds dev no necesitan compat alias.
- [x] `Temporary Rule Card` -> `Temporary Rule Book`.
- [x] Verificar que no aparezca Temporary Rule Book en Creative tab.
- [x] Actualizar mensajes `Rules/Card` -> `Rules/Book` en HUD/locales.
- [ ] Actualizar docs de dev.8 que todavía hablan de Terminal editor/Card.

---

# 2. 1.2.0 — Domain schema final

## 2.1. Profile mecánico

- [x] dev.8: Stable/Unstable/Unclimbable domain.
- [x] dev.8: Unlisted Policy.
- [x] dev.8: Durability Multiplier.
- [x] dev.8: Player Mining.
- [x] Añadir `unmineableTerminals` al profile/codec con default legacy `false`.
- [x] Validar defaults del campo nuevo (`false`).
- [x] Unit/runtime tests cubren `unmineableTerminals` y su participación en igualdad mecánica.

## 2.2. Rule Book definition

- [x] Crear `ClimbingRuleBookDefinition` como contenedor portable separado del profile mecánico.
- [x] `bookName`.
- [x] `coverColor` / DyeColor.
- [x] Activation `PERMANENT | TEMPORARY`.
- [x] Scope `WORLD | PLAYER`.
- [x] `durationSeconds`.
- [x] Normalizar `PERMANENT => WORLD` en validator.
- [x] Forzar `TEMPORARY => duration > 0` en validator.
- [x] Duración serializada por `Codec.INT`; arithmetic runtime usa `long` para ticks sin overflow de `int * 20`.
- [x] Definir Rule Book schema portable v2 para la línea 1.2.
- [x] Codec portable completo v2 implementado para NBT/JSON.
- [x] Tests roundtrip del Rule Book schema ejecutados dentro de la baseline build-clean; dev.18 amplía cobertura.
- [x] Tests JSON manipulated invalid combinations (duration, version, missing cover).

## 2.3. Stack identity

- [x] Normal Rule Book `maxStackSize = 64`.
- [x] Stack identity usa la igualdad de componentes/NBT portable del ItemStack; Rule Books idénticos comparten stack.
- [x] Nombre distinto -> canonical portable data distinto -> no stack.
- [x] Dye distinto -> canonical portable data distinto -> no stack.
- [x] Rules distinto -> canonical portable data distinto -> no stack.
- [x] Duration distinto -> canonical portable data distinto -> no stack.
- [x] Scope distinto -> canonical portable data distinto -> no stack.
- [x] Temporary/Permanent distinto -> canonical portable data distinto -> no stack.
- [x] Missing IDs participan correctamente en canonical portable identity.
- [x] Eliminar `revision` portable dev.8; stale protection usa session token server-side + snapshot.

---

# 3. 1.2.0 — Structural Geometry Safety

- [x] Crear servicio/clasificador central `STRUCTURALLY_NON_ANCHORABLE` o equivalente.
- [x] Definir criterio geométrico robusto: full collision del `BlockState` concreto.
- [x] Revisar slabs; single parcial bloqueado, `DOUBLE` full-collision permitido.
- [x] Revisar stairs.
- [x] Revisar fences.
- [x] Revisar walls.
- [x] Revisar panes.
- [x] Revisar doors.
- [x] Revisar trapdoors.
- [x] Revisar otros partial collision shapes mediante criterio geométrico común.
- [x] Definir interacción con blocks sin collision: structurally non-anchorable.
- [x] Definir interacción con shapes dinámicos: se evalúa estado/world actual y se revalida durante anchor.
- [x] Precedencia sobre Rule Book explícito cuando existe Rules profile activo.
- [x] Integrar sólo por seam central (`ClimbingRulesIntegration` -> `StructuralAnchorSafety`).
- [x] Tests representative vanilla shapes.
- [ ] QA con bloques modded parciales.
- [>] Clasificación automática avanzada de todo registry -> 1.3.0.

---

# 4. 1.2.0 — Effective rules per player

- [x] Seam central `EffectiveClimbingRulesService` materializado; PLAYER overlay todavía pendiente.
- [x] Implementar/recuperar `EffectiveClimbingRulesService` central.
- [x] Surface resolution recibe player/contexto efectivo.
- [x] ToolWearService recibe player/contexto efectivo.
- [x] Player Mining consulta effective rules.
- [x] Unmineable Terminals consulta effective rules.
- [x] Client HUD recibe vista efectiva correcta.
- [x] `climb` conserva el seam bridge/policy y no consulta SavedData directamente.
- [ ] Tests de dos jugadores con reglas efectivas distintas.

---

# 5. 1.2.0 — Climbing Rules Table

## 5.1. Block / BE / Menu

- [x] Separar físicamente el editor dev.8 como `ClimbingRulesTable*`; Terminal vuelve a ser bloque consumer independiente.
- [x] Crear/recuperar `ClimbingRulesTableBlock`.
- [x] Crear/recuperar `ClimbingRulesTableBlockEntity`.
- [x] Crear/recuperar `ClimbingRulesTableMenu`.
- [x] Registrar Table block/item/BE/menu y Terminal block/item separado.
- [x] Creative-only acquisition.
- [x] Survival break protection.
- [x] Creative break allowed.

## 5.2. Slots

- [x] Rule Book slot max 1.
- [x] Material Book slot x64.
- [x] Dye slot x64.
- [x] Cambiar material de Paper -> `minecraft:book`.
- [x] Reject non-Book material en slot/server action.
- [x] Reject stack >1 in Rule Book slot.
- [x] Dye placeholder visual estilo Loom; dev.19 usa White Dye vanilla tenue, sin asset final adicional.
- [x] Implementar routing inicial de shift-click para Rule Book / Book / Dye.

## 5.3. Permission

- [x] dev.8 tenía mapmaker permission base.
- [x] Table autoría exige Creative + permission >=2.
- [x] Server valida cada acción, no sólo open.
- [x] Distancia/dimensión/Table position server-side.

## 5.4. Create

- [x] Reconstruir flujo base Book -> Rule Book sobre Rules Table.
- [x] Book -> Rule Book base con defaults 1.2.
- [x] Defaults 1.2 cargados.
- [x] Dye opcional; define cover al crear y permite recolor en Edit.
- [x] Nombre obligatorio/trimmed/limitado y revalidado server-side.
- [x] Material Book se consume sólo al Save exitoso; abrir/cancelar draft no consume.
- [x] Fail/no-op de authoring no consume Book ni Dye.

## 5.5. Edit

- [x] Stale-session deja de depender de revision portable: token server-side + snapshot exacto de Rule Book.
- [x] Session = player + dimension + table pos + token + exact source snapshot.
- [x] Save verifica source exacto.
- [x] Reemplazo de item durante edición -> reject/stale.
- [x] Logout invalidates.
- [x] Death/respawn invalidates.
- [x] Dimension change invalidates.
- [x] Table removal invalidates.

## 5.6. Import Current Rules

- [x] Diseño y edge cases materializados para WORLD actual.
- [x] Botón disabled si WORLD defaults.
- [x] Botón disabled sin Book material.
- [x] Snapshot mecánico correcto.
- [x] Temporary WORLD conserva configured duration original.
- [x] No copia remaining time.
- [x] No copia runtime snapshot/session IDs.
- [x] Import Current conserva el nombre validado del WORLD Rule Book efectivo.
- [x] Import Current usa Dye slot si existe; si no, White; non-White consume un Dye al Save.

## 5.7. Restore Defaults

- [x] dev.8 Restore World Defaults existía.
- [x] Restore Defaults vive en Table.
- [x] Confirmación real con `ConfirmScreen` antes de Restore World Defaults.
- [x] Restore Defaults cancela Temporary WORLD.
- [x] Restore Defaults cancela PLAYER overlays mediante la transición WORLD central.
- [x] Sync WORLD después de transición.
- [x] Revalidación inmediata de anchors después de transición WORLD.

---

# 6. 1.2.0 — Editor UI

- [x] dev.8 editor visual base.
- [x] dev.8 catálogo cacheado.
- [x] dev.8 search por name/namespace/RL.
- [x] dev.8 Stable/Unstable/Unclimbable tabs.
- [x] dev.8 multi-select/Select Visible/Restore Selected.
- [x] dev.8 responsive narrow scrolling.
- [x] Reutilizar Search en Create/Edit/Viewer.
- [x] Editor adaptado a definición completa Rule Book.
- [x] Name field edita `bookName` y nombre interno del profile.
- [x] Editor muestra cover derivado del Dye permitido por la sesión.
- [x] Añadir Unmineable Terminals.
- [x] Añadir Permanent/Temporary selector.
- [x] Añadir duration control.
- [x] Añadir WORLD/PLAYER selector sólo para Temporary.
- [x] Permanent fuerza WORLD visualmente y server-side.
- [x] Missing mod IDs no forman parte del catálogo/grid/search.
- [x] Missing mod IDs permanecen en los sets del draft y se serializan al Save.
- [ ] Revisar responsive con nuevos controles.
- [ ] Revisar GUI scale alto.
- [ ] Revisar ventanas pequeñas.

---

# 7. 1.2.0 — Rule Book Viewer

- [x] Diseño cerrado / implementación previa no persistida.
- [x] Right-click item use abre viewer cuando no hay interacción prioritaria.
- [x] Bloques interactivos conservan prioridad.
- [x] Overview.
- [x] Stable.
- [x] Unstable.
- [x] Unclimbable.
- [x] Search obligatorio en tabs de bloques.
- [x] Misma semántica Search del editor.
- [x] Missing IDs ocultos.
- [x] Read-only real.
- [x] Esc closes.
- [x] Inventory keybind closes.
- [x] No hardcode `E`.

---

# 8. 1.2.0 — Duplicate Rule Book GUI

- [x] Diseño cerrado tipo anvil.
- [x] Crear Screen/submenu de Duplicate.
- [x] Mostrar source Rule Book.
- [x] Mostrar Book material count.
- [x] Mostrar Dye override.
- [x] Mostrar output preview/count.
- [x] Confirm enabled sólo con recursos válidos.
- [x] Source no se consume.
- [x] Books consumidos = output count.
- [x] Dye consumido sólo si recolor.
- [x] Dye count >= copies si recolor.
- [x] Clone name.
- [x] Clone cover si no override.
- [x] Clone profile.
- [x] Clone activation.
- [x] Clone scope.
- [x] Clone configured duration.
- [x] Server calcula output, no confiar en client preview.
- [x] Manejar inventario/output sin espacio.
- [x] Output va al inventario; overflow seguro se dropea al jugador sin pérdida.

---

# 9. 1.2.0 — Climbing Rules Terminal

- [~] Diseño input-only cerrado; implementación previa no persistida.
- [x] TerminalBlockEntity dev.8 eliminado del source activo.
- [x] TerminalMenu dev.8 eliminado del source activo.
- [x] TerminalScreen admin dev.8 eliminado del source activo.
- [x] Payloads activos de autoría pertenecen a Table; no quedan payloads Terminal admin legacy.
- [x] Terminal es simple interaction block sin BE/menu.
- [x] FACING 6 directions.
- [x] Placement orientation wall/floor/ceiling en blockstate lógico.
- [ ] Model/blockstate six directions.
- [x] Use normal Rule Book.
- [x] Normal Rule Book TEMPORARY WORLD y Temporary Rule Book runtime del Dispenser se aplican por Terminal.
- [x] Server validate.
- [x] Consume exactly 1 on apply/refresh success.
- [x] No consume on reject.
- [x] Legacy reject message localizado: legacy debe pasar primero por Rules Table.
- [x] Survival/Adventure usage allowed.
- [x] Creative usage allowed.
- [x] Unmineable Terminals survival policy.
- [x] Creative break exemption.

---

# 10. 1.2.0 — WORLD rules persistence/runtime

- [x] dev.8 world-global active profile SavedData.
- [x] dev.8 client WORLD sync.
- [x] SavedData expandido para Permanent WORLD + Temporary WORLD overlay.
- [x] Permanent WORLD state.
- [x] Temporary WORLD active definition/profile.
- [x] Permanent baseline se conserva como snapshot implícito bajo el Temporary WORLD overlay.
- [x] `expiresAtGameTime` persistido en game time.
- [x] Configured duration vive en la definición Temporary.
- [x] `policyRevision` cambia sólo en mutaciones/transiciones.
- [x] No nested WORLD temporary diferente; conflicto reject.
- [x] Restart preserva expiry en game time; offline no consume duración.
- [x] Offline server no avanza game time.
- [x] Expiry elimina overlay y revela Permanent WORLD/defaults actual.
- [x] Permanent durante Temporary reemplaza baseline y cancela overlay.
- [x] Restore Defaults cancela Temporary WORLD.
- [x] Invalid/metadata-incompatible persisted WORLD data se descarta de forma segura.
- [x] `setDirty` sólo en mutaciones; tick normal no escribe.
- [ ] Unit tests serialization/migration.

---

# 11. 1.2.0 — PLAYER temporary runtime

- [x] Diseño materializado y centralizado.
- [x] One temporary PLAYER session per player.
- [x] Capture world policy revision marker; nunca restaura snapshots WORLD antiguos.
- [x] Apply effective overlay.
- [x] Sync only affected player.
- [x] Timer basado en server/world game time.
- [x] Expiry -> remove overlay -> resolve current WORLD.
- [x] Logout -> cancel.
- [x] Death -> cancel.
- [x] Respawn cleanup defensivo.
- [x] Dimension change en 1.2 conserva PLAYER temporal; WORLD sigue siendo global. 1.3 redefine interacción dimensional.
- [x] WORLD mutation -> cancel personal overlay immediately.
- [x] Old snapshot never overwrites new WORLD.
- [ ] Multiplayer isolation tests.

---

# 12. 1.2.0 — No-op / refresh service

- [x] Semantics WORLD centralizadas en `ClimbingRulesService`.
- [x] Comparación mecánica central ignora nombre/cover y considera todas las reglas.
- [x] Ignore bookName for no-op.
- [x] Ignore coverColor for no-op.
- [x] Permanent identical -> reject/no consume.
- [x] Temporary identical to Permanent/default -> reject/no consume.
- [x] Temporary WORLD idéntico al Temporary WORLD activo -> refresh/consume.
- [x] Temporary WORLD diferente mientras hay Temporary WORLD activo -> reject.
- [x] PLAYER/WORLD scope comparison correct.
- [x] Mensajes localizados para apply/refresh/no-op/conflict/reject.

---

# 13. 1.2.0 — Anchor revalidation

- [x] Diseño e implementación central materializados.
- [x] Transiciones WORLD centralizan broadcast + revalidación.
- [x] Permanent apply revalida anchors.
- [x] Temporary WORLD apply revalida anchors.
- [x] Temporary WORLD expiry revalida anchors.
- [x] Restore defaults revalida anchors.
- [x] Temporary PLAYER apply.
- [x] Temporary PLAYER expiry.
- [x] PLAYER cancel on WORLD change.
- [x] Logout/death cleanup.
- [x] Revalidación usa `AnchorStateValidator` y detach normal.
- [x] Anchors válidos se preservan.
- [x] Invalid anchors se detach sólo por `AnchorLifecycle.detachServer`.

---

# 14. 1.2.0 — Climbing Rule Dispenser

## 14.1. Block / Menu

- [x] Implementación reconstruida y persistida en dev.12.
- [x] Register block/item.
- [x] BlockEntity.
- [x] Menu.
- [x] Config screen.
- [x] Creative-only acquisition.
- [x] Survival break protection.
- [x] Creative break allowed.
- [x] FACING orientation.
- [~] White/gray model funcional con recursos vanilla; asset original final sigue pendiente.

## 14.2. Master

- [x] Single slot max 1.
- [x] Accept only final-schema Temporary Rule Book.
- [x] Reject Permanent.
- [x] Never consume master.
- [x] Preserve master on unload/restart.

## 14.3. Lifetime config

- [x] Required 1..60 seconds.
- [x] Default = 30 seconds, selected/documented.
- [x] Server-side authoritative.
- [x] Synced to menu.

## 14.4. Issuance

- [x] Per-player issuance registry/service.
- [x] One pending copy per player.
- [x] Other players independent.
- [x] Reject if rules already effective for scope/player.
- [x] Spawn Temporary Rule Book toward facing direction.
- [x] Owner UUID.
- [x] Issuance token.
- [x] Source dimension + block position identity.
- [x] Cover color inherited in copied Rule Book definition; final tint asset remains visual work.

## 14.5. Temporary Rule Book item

- [x] Separate item class.
- [x] maxStackSize 1.
- [x] Not Creative tab.
- [x] Fixed localized name `Temporary Rule Book`.
- [x] Cannot edit in Table.
- [x] Cannot duplicate.
- [x] Cannot export.
- [x] Owner-only pickup.
- [x] Owner-only Terminal use.
- [x] Invalid foreign possession cleanup.
- [x] Inventory expiry.
- [x] Hand expiry via inventory sanitation.
- [x] Ground ItemEntity expiry.
- [x] Death = vanish.
- [x] ItemEntity destruction hook releases issuance.
- [x] Successful Terminal use releases issuance.
- [x] Expiry releases issuance.
- [x] Issuance registry is runtime-only; stale transport books self-clean after restart.

---

# 15. 1.2.0 — Timers HUD

- [x] Rules + Book timer HUD materializado en dev.12.
- [x] WORLD/PLAYER rules timer source.
- [x] Temporary Rule Book transport timer source.
- [x] Rules-only timer -> countdown without label.
- [x] Simultaneous -> Rules + Book labels.
- [x] en_us/en_gb labels.
- [x] es_cl/es_es/es_ar/es_mx labels.
- [x] pt_br/pt_pt labels.
- [x] No chat spam.
- [x] Rules y transport Book countdowns limpian/caen correctamente por expiry/sync.
- [x] Player joining during WORLD temporary receives absolute expiry via WORLD sync.

---

# 16. 1.2.0 — JSON / filesystem final

- [x] dev.8 local folder `config/pickclimber/rules/`.
- [x] dev.8 file list/import/export infrastructure.
- [x] dev.8 4 MiB defensive limit.
- [x] dev.8 path/reserved-name hardening partial/advanced.
- [x] JSON convierte/importa/exporta definición completa Rule Book v2.
- [x] Export final usa `.rules.json`; import también acepta `.json` legacy.
- [x] Filename = validated `bookName`; no existe filename editable separado.
- [x] Import bookName = filename basename normalizado.
- [x] `cover_color` via Rule Book codec.
- [x] activation mode via Rule Book codec.
- [x] scope via Rule Book codec.
- [x] configured duration via Rule Book codec.
- [x] unmineable_terminals via profile codec.
- [x] Missing `cover_color` -> White.
- [x] Missing mod IDs preserved by ResourceLocation sets.
- [x] Windows reserved names including `CON.rules.json` cubiertos por policy/tests.
- [x] Política de nombres usa el subset portable Windows/Linux/macOS en todas las plataformas.
- [x] Corrupt/oversized file returns localized error; no crash.
- [x] Invalid/future Rule Book version devuelve feedback localizado específico.
- [x] JSON parser only; no code/command execution path.
- [x] JSON filesystem roundtrip + overwrite + migration tests.

---

# 17. 1.2.0 — Legacy migration

- [x] Policy materializada: Terminal/Dispenser strict current-schema; Table repair/migration.
- [x] Detect legacy dev.8 profile payloads dentro del item actual; no alias para el ID dev-only no publicado.
- [x] Table migration defaults White/Permanent/WORLD/unmineable=false mediante `ClimbingRuleBookData`.
- [x] Migración conserva todos los ResourceLocations del profile.
- [x] Missing IDs permanecen ocultos en UI pero serializables después de migración.
- [x] Terminal legacy rejection localizado; requiere reparación en Table.
- [x] Import legacy profile JSON -> White/Permanent/WORLD migrated draft.
- [x] Save migrated draft materializes current Rule Book v2.
- [x] Imported filename basename becomes validated Book/profile name.
- [x] Unit tests de migración JSON legacy a White/Permanent/WORLD/unmineable=false.

---

# 18. 1.2.0 — Visual assets

## 18.1. Rule Book

- [x] Replace Card sprite with original Rule Book sprite.
- [x] Pixel-art style compatible with Minecraft inventory (32x32).
- [x] Cover tint mask/strategy: `layer0` tintable + `layer1` fixed details.
- [x] Pages remain light/white.
- [x] White base cover via neutral grayscale mask + `DyeColor.WHITE`.
- [ ] Test all 16 dye colors.
- [ ] Test enchanted glint/readability if relevant.
- [x] Item model.

## 18.2. Table

- [x] Original white/gray texture.
- [x] Top authoring/readability motif.
- [x] Block model.
- [x] Item model.
- [x] Break particles/texture references correct (`particle` -> side texture).

## 18.3. Terminal

- [x] dev.8 had provisional white/gray reader texture.
- [x] Redesign/adapt to six-direction facing.
- [x] Make identity distinct from Jukebox.
- [x] Front slot readable.
- [x] Blockstates six faces via horizontal + vertical model split.
- [x] Item model facing-up presentation via vertical model.

## 18.4. Dispenser

- [x] Original white/gray dispenser-inspired texture.
- [x] Front aperture identifiable.
- [x] FACING model/blockstate with horizontal + vertical model split.
- [~] Dedicated UP/DOWN front texture path/model seam exists; dev.20 copies the approved normal front until centered-aperture art is finalized.
- [x] Item model.

---

# 19. 1.2.0 — JEI / EMI

- [x] Optional integrations isolated under `integration/jei`, `integration/emi` and shared `integration/recipeviewer`.
- [x] JEI compile target: `19.32.0.359` for Minecraft 1.21.1.
- [x] EMI compile target: `1.1.24+1.21.1` NeoForge API.
- [x] No hard dependency: external APIs are `compileOnly` and no loader dependency is declared.
- [x] No survival recipes; both viewers receive synthetic documentation relations only.
- [x] JEI visual documentation:
  - [x] `[Book] + [Rules Table] => [Rule Book]`.
  - [x] `[Rule Book] => [Rules Terminal]` with Terminal as catalyst/workstation, not crafted output.
- [x] EMI visual documentation:
  - [x] same first relation;
  - [x] same second relation;
  - [x] slot/icon presentation only; no long text page required.
- [x] Terminal item model already presents the facing-up/vertical model in the relation.
- [x] Base/current-schema White Rule Book used as category icon and authoring output.
- [x] Static classloading/isolation gate rejects viewer API references outside their integration packages.
- [ ] Runtime mod-absent classloading test. **QA.**
- [ ] JEI-only runtime test. **QA.**
- [ ] EMI-only runtime test. **QA.**
- [ ] Both-present runtime test. **QA.**

---

# 20. 1.2.0 — Localization

- [x] dev.8 parity had 8 locales.
- [x] Active localization usa Rule Book/Table/Terminal; no quedan keys Card activas.
- [x] Table/editor/import keys completas en 8 locales.
- [x] Terminal WORLD apply/no-op/refresh/conflict keys completas en 8 locales.
- [x] Dispenser keys presentes y con paridad en 8 locales.
- [x] Temporary Rule Book keys presentes y con paridad en 8 locales.
- [x] Duplicate GUI.
- [x] Viewer.
- [x] Search/editor labels presentes en 8 locales.
- [x] Name/file validation feedback localizado.
- [x] JSON import/export/migration feedback localizado.
- [x] Legacy rejection localized.
- [x] No-op rejection localizado.
- [x] Temporary WORLD refresh outcome localizado; HUD timer labels siguen pendientes.
- [x] Rules/Book HUD labels presentes en 8 locales.
- [x] Unmineable Terminals editor/tooltip strings presentes.
- [x] JEI/EMI category string present with parity in all 8 locales.
- [x] Ocho locales con paridad exacta y sin translation refs faltantes en rules UI.

---

# 21. 1.2.0 — Creative tab / acquisition

- [x] Rules Table aparece en creative tab.
- [x] Rules Terminal aparece en creative tab.
- [x] Rule Dispenser appears.
- [x] Normal default Rule Book aparece en creative tab.
- [x] Temporary Rule Book runtime está registrado pero no aparece en Creative tab.
- [x] No hay recetas survival para Rule Book/Table/Terminal/Dispenser/Temporary Rule Book.
- [x] No loot tables exponen infraestructura protegida como contenido survival-obtainable.

---

# 22. 1.2.0 — Networking cleanup

- [x] dev.8 protocol 15.
- [x] dev.8 client request boundary removed direct PacketDistributor from Screens.
- [x] Old Terminal editor payload identity removed; authoring payloads belong to Table.
- [x] Table create/import/import-current/edit/save/eject/restore actions registered.
- [x] Rule Book full-definition editor payloads + server session token.
- [x] PLAYER effective sync separado y dirigido sólo al jugador afectado.
- [x] WORLD temporary state/expiry/policyRevision synced to clients.
- [x] Dispenser lifetime config payload + menu data sync + Temporary Rule Book HUD state payload.
- [~] Core Table/Terminal/Dispenser action results localized; audit remaining edge-case result keys before freeze.
- [x] Large payload hardening: 8192 explicit block overrides + 512 KiB serialized Rule Book cap.
- [x] Client NBT es untrusted: bounded NBT codec + preflight + decode + validator + server session/material checks.
- [x] Architecture invariants reescaneados; dev.20 es baseline build-clean confirmado.

---

# 23. 1.2.0 — Tests

- [x] dev.8 ToolWearMath tests.
- [x] dev.8 profile validator/runtime/codec tests.
- [x] dev.8 JSON filename tests.
- [x] Tests final Rule Book schema/validator/codec ampliados.
- [x] Canonical portable identity tests cubren name/dye/rules/duration/scope/activation/missing IDs.
- [x] Activation validator tests persisted; ejecución real pendiente porque dev.11 falló antes en verifyRulesIntegrity.
- [x] Unmineable terminal behavior tests persisted; ejecución real pendiente tras fix de verifyRulesIntegrity.
- [~] Effective rules PLAYER/WORLD client precedence test persistido; multiplayer server-side sigue pendiente.
- [~] WORLD temporary state implemented; dedicated SavedData/service unit tests still pending.
- [~] No-op/refresh implementation complete; dedicated pure-service tests pending.
- [x] Migration tests para legacy profile JSON y defaults v2.
- [x] Rule Book codec + filesystem roundtrip/overwrite + filename tests.
- [x] Structural safety tests.
- [~] Temporary transport client-state tests añadidos; issuance service requiere integration/runtime QA.
- [x] Filename cross-platform/reserved-name tests.

---

# 24. 1.2.0 — Source quality / architecture gates

- [x] dev.8 source-quality gates existed.
- [x] dev.8 rules-integrity gate existed.
- [x] `verifyRulesIntegrity` actualizado a Rule Book/Table/Terminal/Dispenser y prohíbe identidad Card legacy.
- [x] Gate comprueba ausencia de clases Terminal admin retiradas.
- [x] Terminal activo no tiene authoring menu/BE.
- [x] Table posee el authoring UI/payload flow activo.
- [x] Gate prohíbe `TEMPORARY_RULE_BOOK` en creative tab.
- [x] Gate prohíbe recetas/loot para IDs protegidos de Rules.
- [x] Scan dev.12 confirma ToolWearService como único `hurtAndBreak` owner.
- [x] Surface classification continúa centralizada por classifier/resolver.
- [x] Screens sin PacketDistributor directo.
- [x] 8 locales mantienen paridad exacta; dev.19 = 192 keys por locale.
- [x] Static scan dev.19 sin debug/TODO/FIXME/HACK.
- [x] Static scan dev.19 sin lines >120/tabs/trailing whitespace.
- [ ] Assert optional JEI/EMI integrations do not hard-load absent mods.

---

# 25. 1.2.0 — Documentation before build

- [x] New master ROADMAP.
- [x] New WAITLIST.
- [x] README general scope.
- [ ] Update DEVELOPMENT with final architecture.
- [ ] Update CHANGELOG 1.2 draft.
- [ ] Update BUILD-STATUS after first compile.
- [ ] Rewrite TESTING-1.2.0 for Rule Books/Table/Dispenser.
- [ ] Rewrite RELEASE-1.2.0.
- [ ] Rewrite CurseForge changelog.
- [ ] Document JSON schema example.
- [ ] Document permissions.
- [ ] Document JEI/EMI behavior.

---

# 26. 1.2.0 — Build campaign (sólo después de feature-complete)

- [ ] Feature freeze.
- [ ] Static audit.
- [ ] `gradle check`.
- [ ] First NeoForge compile.
- [ ] Batch fix compile errors.
- [ ] Repeat until clean.
- [ ] runClient.
- [ ] Dedicated server startup.
- [ ] Singleplayer QA.
- [ ] Multiplayer QA.
- [ ] High GUI scale QA.
- [ ] Optional mods QA.
- [ ] JSON filesystem QA Windows.
- [ ] JSON filesystem QA Linux/WSL if available.
- [ ] RC snapshot.
- [ ] Final release build.
- [ ] CurseForge upload.

---

# 27. 1.3.0 — Dimensional Rules foundation

- [>] No implementar antes de cerrar 1.2.0 salvo seams que eviten rework.
- [ ] Definir final data model v3.
- [ ] Definir full-profile vs diff dimension override.
- [ ] Definir missing dimension ID preservation.
- [ ] Definir runtime behavior de temporales WORLD con múltiples dimensiones.
- [ ] Definir runtime behavior de temporary PLAYER al cambiar dimensión.
- [ ] Definir selector dimensional UX.
- [ ] Definir naming/display de dimensiones modded.

---

# 28. 1.3.0 — Dimension discovery

- [ ] Enumerar Overworld.
- [ ] Enumerar Nether.
- [ ] Enumerar End.
- [ ] Enumerar dimensiones externas registradas.
- [ ] No hardcode Twilight Forest.
- [ ] No hardcode Eternal Starlight.
- [ ] ResourceLocation fallback.
- [ ] Cache/update lifecycle when datapacks/dimensions change.
- [ ] Dedicated server authoritative list.
- [ ] Client receives list for UI.

---

# 29. 1.3.0 — Rule Book schema v3

- [ ] `globalProfile`.
- [ ] `dimensionOverrides`.
- [ ] Preserve 1.2 metadata.
- [ ] v2 -> v3 migration.
- [ ] Unknown dimension IDs preserved.
- [ ] JSON codec.
- [ ] Network codec/state.
- [ ] SavedData update.
- [ ] Unit tests.

---

# 30. 1.3.0 — Dimensional editor/viewer

- [ ] Global tab/context.
- [ ] Overworld.
- [ ] Nether.
- [ ] End.
- [ ] Dynamic modded entries.
- [ ] Same Search component.
- [ ] Same block catalog.
- [ ] Inherited/global indicators.
- [ ] Override indicators.
- [ ] Viewer selector.
- [ ] Responsive layout with long dimension lists.
- [ ] Missing dimension representation policy.

---

# 31. 1.3.0 — Advanced default classifier

- [ ] Central registry scan/cache.
- [ ] Pick Climber tags authority.
- [ ] Full solid -> Stable candidate.
- [ ] Falling/gravity block -> Unstable candidate.
- [ ] Structural non-anchorable -> locked.
- [ ] Evaluate leaves/non-solid special cases.
- [ ] Evaluate fluids/waterlogged states.
- [ ] Evaluate modded blocks with dynamic shapes.
- [ ] Performance benchmark.
- [ ] No per-frame registry scan.
- [ ] Clear/rebuild cache lifecycle.

---

# 32. 1.3.0 — Clear / Vaciar

- [ ] Button in authoring UI.
- [ ] Clear current context only.
- [ ] Stable editable set -> empty.
- [ ] Unstable editable set -> empty.
- [ ] Unclimbable editable set -> empty.
- [ ] Structural exclusions remain.
- [ ] Other dimensions remain.
- [ ] Confirmation/undo policy.
- [ ] Search/catalog remains available after clear.

---

# 33. 1.3.0 — Dimensional runtime

- [ ] Resolve current dimension.
- [ ] WORLD override -> global -> defaults.
- [ ] PLAYER overlay interaction.
- [ ] Cross-dimension travel during WORLD temporary.
- [ ] Cross-dimension travel during PLAYER temporary.
- [ ] WORLD mutation invalidates PLAYER overlays consistently.
- [ ] Anchor revalidation on dimension transition.
- [ ] Mining/durability/surface all use same dimension-aware effective view.
- [ ] Multiplayer different dimensions.

---

# 34. 1.3.0 — Dimensional QA

- [ ] Overworld default.
- [ ] Nether override.
- [ ] End override.
- [ ] External dimension discovered.
- [ ] Mod removed but override preserved.
- [ ] Mod reinstalled restores visible override.
- [ ] v2 migration.
- [ ] Global inheritance.
- [ ] Clear.
- [ ] Automatic unstable modded sand-like block.
- [ ] Automatic stable full modded block.
- [ ] Structural slab/stair stays blocked.
- [ ] Player moves between dimensions during timer.
- [ ] Two players in different dimensions.
- [ ] JSON v3 roundtrip.

---

# 35. Ideas explícitamente fuera de espera actual

No agregar a 1.2/1.3 salvo decisión futura explícita:

- checkpoint system propio;
- parkour run manager;
- goals/objectives;
- lives;
- teleport routing;
- command scripting;
- rule redstone networks;
- region editor;
- scoreboard framework.

Pick Climber puede integrarse con herramientas externas para esos casos.

---

# 36. Rutina obligatoria de mantenimiento de esta lista

Después de cada pasada:

1. marcar `[x]` sólo si existe en source persistido y fue revisado;
2. usar `[~]` para trabajo conceptual/parcial que todavía requiera verificación;
3. registrar bugs descubiertos como subtareas;
4. no borrar un pendiente: marcarlo completado o diferido;
5. actualizar ROADMAP sólo si cambia diseño/scope;
6. crear snapshot/handoff en hitos grandes;
7. antes de cambiar de chat, incluir source + ROADMAP + WAITLIST + status manifest.


# 35. 1.3.0 — Rule Dispenser Persist on Pickup

- [>] Toggle `Persist on Pickup` para Temporary Rule Books PLAYER.
- [>] Timer comienza al salir del dispenser.
- [>] Persist OFF: primer pickup consume entidad del suelo.
- [>] Persist ON: pickup compartido no consume entidad del suelo.
- [>] Todos los pickups heredan el mismo `expiresAt` absoluto.
- [>] Hotbar muestra tiempo restante heredado.
- [>] Emisión persistente desaparece al expirar.
- [>] Dispenser bloquea nueva emisión mientras la anterior siga vigente.
- [>] Persistir `emissionId` / `emittedAt` / `expiresAt` en estado del dispenser.
- [>] Bind PLAYER por UUID + `sourceEmissionId`.
- [>] Evitar transferencia por drop a otro jugador.
- [>] Evitar que el mismo jugador reinicie timer recogiendo de nuevo la misma emisión.
- [>] QA multijugador concurrido (30+ jugadores sobre un pickup spot).
