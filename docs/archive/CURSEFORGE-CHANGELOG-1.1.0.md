# Pick Climber 1.1.0 — Player Customization & Runtime Control

Pick Climber 1.1.0 is focused on customization, runtime control and usability while preserving the climbing mechanics and balance validated in 1.0.3.

## New Pick Climber Options menu

Press the configurable Pick Climber Options key (`K` by default) while playing to customize:

- Indicator Mode: Contextual / Always / Off
- Indicator Style: String / Pickaxe
- Icon Size: 50%–200%
- Icon Transparency
- Icon Colors: Muted / Normal / Neon
- Indicator Box
- Box Transparency
- Box Colors: Muted / Normal / Neon
- Show Unclimbable
- Failure Messages
- Reset to Defaults

The menu is fully responsive: it uses two columns when there is room, switches to one column on narrow windows, and becomes scrollable when the GUI is too short.

## Per-player hot-disable

You can now disable Pick Climber interactions without restarting the game.

If interactions are disabled while you are already anchored, Pick Climber safely detaches through the normal lifecycle and cleans the active climbing state.

## New Pickaxe indicator

The HUD now supports a dedicated Pickaxe indicator in addition to String.

The Pickaxe style uses a small monochrome Minecraft-style pickaxe silhouette and follows the same anchor-state colors as the normal indicator.

## HUD improvements

- Preview icon is kept inside its options-menu panel even at large configured sizes.
- Icon and box transparency are independent.
- Icon and box color intensity can be configured independently.
- Jade and other HUD overlays remain isolated from Pick Climber's shader tint.

## Client config migration

`config/pickclimber-client.json` now uses the finalized v3 schema.

Older configs are migrated automatically, including legacy opacity fields and the old `pickaxe_outline` style name.
Existing player preferences are preserved whenever they can be mapped safely.

## Localization

Added complete translations for:

- English (US)
- English (UK)
- Spanish (Chile)
- Spanish (Spain)
- Spanish (Argentina)
- Spanish (Mexico)
- Portuguese (Brazil)
- Portuguese (Portugal)

## Compatibility / internals

1.1.0 also completes a major internal modularization of Pick Climber's anchor evaluation, lifecycle, physics, durability, cooldown, networking and client presentation.

This is primarily an architecture/maintainability change: the existing tool/surface tags, interactive-block priority, enchantment behavior and validated climbing mechanics remain compatible with the 1.0.3 baseline.
