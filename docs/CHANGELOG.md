# Changelog

## 1.1.0 — 2026-09-01

### Player customization

- Added a dedicated in-world Pick Climber Options screen, opened by a configurable keybind (`K` by default).
- Added `Contextual / Always / Off` indicator modes.
- Added `String / Pickaxe` indicator styles.
- Added 50%–200% icon scaling.
- Added independent icon and box transparency.
- Added independent Muted / Normal / Neon color intensity for icon and box.
- Added Indicator Box, Show Unclimbable and Failure Messages controls.
- Added one Reset to Defaults action for the complete client option set.

### Runtime control

- Added per-player Enable Pick Climber Interactions.
- Disabling interactions while attached uses the normal authoritative lifecycle to detach and clean state safely.
- Added protocol 14 transient runtime preference synchronization.
- Failure-text visibility is mirrored to the server; purely visual HUD preferences remain client-local.

### Options GUI

- Added a dedicated preview surface.
- Fixed preview icon overflow at large configured icon sizes without changing the real HUD scale.
- Added fully responsive two-column / one-column layout.
- Added row scrolling for low-height GUI layouts.
- Kept preview/footer fixed while option rows scroll.
- Added stacked footer controls for very narrow GUI widths.

### Indicator rendering

- Added the texture-backed Pickaxe indicator based on a monochrome 16x16 pickaxe silhouette.
- Preserved isolated shader tinting for String and Pickaxe so Jade and unrelated overlays keep their own colors.
- Renamed the old player-facing Pickaxe Outline style to Pickaxe.

### Client config

- Final schema remains `configVersion: 3`.
- Added automatic migration for legacy opacity fields and the old `pickaxe_outline` style.
- Migrated the former shared `colorIntensity` field to independent icon/box values.
- Configs up to v3 are normalized to the canonical current schema after loading.
- Future-version configs are read defensively without automatic downgrade.
- Redundant writes are skipped and temporary save files are cleaned after fallback writes.

### Localization

Added complete locale coverage for:

- English (US)
- English (UK)
- Spanish (Chile)
- Spanish (Spain)
- Spanish (Argentina)
- Spanish (Mexico)
- Portuguese (Brazil)
- Portuguese (Portugal)

### Architecture and compatibility

- Completed the Phase 0 runtime modularization started after 1.0.3.
- Kept anchor evaluation, lifecycle, wear, cooldown, physics, networking and presentation behind focused boundaries.
- Preserved data-driven climbing-tool, excluded-tool, surface and interactive-block tags.
- Preserved 1.0.3 gameplay/balance as the regression baseline.
- Preserved normal block-interaction priority and Shift force-anchor behavior.

## 1.0.3 — 2026-09-01

- Fixed Pick Climber HUD tint leaking into Jade, event text and other overlays.
- Kept the anchor indicator hidden over interactive blocks during normal use.
- Holding Shift reveals the force-anchor preview when the targeted face is valid.
- Expanded conservative interactive-block detection to menus, loaded BlockEntities and the extensible tag.

## 1.0.2 — 2026-08-30

- Isolated String indicator tinting to the icon draw.
- Restored shader color immediately after the indicator render.
- Kept the indicator border on local ARGB drawing.

## 1.0.1 — 2026-08-20

- Added the official Pick Climber visual identity and creative tab.
- Fixed right-click interaction priority with vanilla/modded blocks.
- Added Shift + right click as the explicit force-anchor override.

## 1.0.0

- First stable public release for Minecraft 1.21.1 / NeoForge.
- Included wall climbing, progressive fall braking, unstable surfaces, dual-pickaxe movement and ceiling traversal.
- Included Pick Climber I–III, Sturdy Latch I and Strong Grip I.
