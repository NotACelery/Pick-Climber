# Changelog

## 1.2.0 — unreleased

### Mapmaker rules

- Added portable Climbing Rule Books with Stable, Unstable and Unclimbable block overrides.
- Added `Use Pick Climber Defaults` and strict `Unclimbable` handling for unlisted blocks.
- Added durability multiplier, Player Mining and Unmineable Terminals rule fields.
- Preserved missing-mod ResourceLocations in portable data/JSON.

### Rules authoring and application

- Split authoring and gameplay responsibilities into Climbing Rules Table and Climbing Rules Terminal.
- Added transactional Rule Book creation/edit/import with Book + Dye material validation on Save.
- Added Permanent WORLD, Temporary WORLD and Temporary PLAYER activation modes.
- Added WORLD no-op/refresh/conflict behavior and per-player Temporary PLAYER isolation.
- Added server-authoritative PLAYER -> WORLD -> defaults effective-rule precedence.
- Added logout/death cleanup and WORLD-transition cancellation for Temporary PLAYER overlays.

### JSON portability

- Added full Rule Book schema v2 under `config/pickclimber/rules/`.
- Export uses `.rules.json`; legacy `.json` profile files remain importable for migration.
- Added filename sanitization, traversal protection, size limits and overwrite confirmation.

### Rule distribution

- Added the Creative-only Climbing Rule Dispenser with a persisted Temporary Rule Book master.
- Added per-player owner/token issuance, 1..60 second transport lifetime and owner-only Temporary Rule Books.
- Added inventory/ground/death/destruction expiry and immediate issuance release on successful Terminal use.
- Added one-pending-copy protection per player without blocking other players.

### HUD and synchronization

- Added world and per-player rules synchronization on protocol 15.
- Added compact Rules and Temporary Rule Book countdowns above the hotbar.
- Added simultaneous localized `Rules` / `Book` timer labels across all eight locales.
- WORLD and PLAYER transitions immediately revalidate the affected anchors.

### Architecture and quality

- Kept surface policy centralized through `AnchorSurfaceResolver` / `EffectiveClimbingRulesService`.
- Added Structural Geometry Safety using the concrete BlockState collision shape before Rule Book overrides.
- Kept Pick Climber wear centralized through `ToolWearService`.
- Kept `climb` independent from rules persistence and NeoForge transport.
- Added JUnit coverage for profile/Rule Book codecs, validation, runtime views, PLAYER client precedence and timer formatting.
- Added localization parity and rules-integrity Gradle verification for all eight supported locales.
- Fixed `verifyRulesIntegrity` for Gradle 9.2.1 Configuration Cache by resolving protected file paths at configuration time.
- Fixed Windows rules/network path normalization in `verifyRulesIntegrity` using the platform separator.

## 1.1.0 — 2026-09-01

- Added the responsive Pick Climber Options screen and configurable `K` keybind.
- Added Contextual / Always / Off HUD modes and String / Pickaxe styles.
- Added HUD size, transparency, colors, box and failure-message controls.
- Added per-player runtime hot-disable and protocol 14 preference synchronization.
- Completed the Phase 0 modular architecture while preserving the 1.0.3 gameplay baseline.
- Added complete localization for en_us, en_gb, es_cl, es_es, es_ar, es_mx, pt_br and pt_pt.

## 1.0.3 — 2026-09-01

- Fixed Pick Climber HUD tint leaking into Jade, event text and other overlays.
- Kept the anchor indicator hidden over interactive blocks during normal use.
- Holding Shift reveals the force-anchor preview when the targeted face is valid.

## 1.0.2 — 2026-08-30

- Isolated String indicator tinting to the Pick Climber icon draw.

## 1.0.1 — 2026-08-20

- Added the official visual identity and creative tab.
- Fixed right-click interaction priority and added Shift force-anchor.

## 1.0.0

- First stable public release for Minecraft 1.21.1 / NeoForge.
