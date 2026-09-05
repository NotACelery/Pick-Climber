# Pick Climber 1.1.0 — Release QA

This file records the accepted 1.1.0 QA state. Development-snapshot checklists are retired by the release cleanup.

## Gameplay / enchantments

- [x] Wall anchoring and movement.
- [x] Dangerous-fall braking.
- [x] Unstable-surface behavior.
- [x] Strong Grip ceiling anchoring/swing.
- [x] Sturdy Latch stabilization.
- [x] Pick Climber enchantment behavior.
- [x] Tool wear/cooldown behavior remains functional.
- [x] Hand transfer and compatible-pickaxe behavior remain functional.

## Interaction compatibility

- [x] Normal interactive blocks preserve their right-click behavior.
- [x] Shift force-anchor remains functional.
- [x] Existing modded-block compatibility remains intact.

## HUD / options

- [x] Contextual / Always / Off.
- [x] Show Unclimbable.
- [x] Failure Messages.
- [x] String renderer.
- [x] Pickaxe renderer.
- [x] Icon Size.
- [x] Icon Transparency.
- [x] Icon color intensity.
- [x] Indicator Box.
- [x] Box Transparency.
- [x] Box color intensity.
- [x] Reset to Defaults.
- [x] Preview remains inside its panel at tested size extremes.
- [x] Jade/other overlays do not inherit Pick Climber tint.

## Runtime enable/disable

- [x] Hot-disable while unattached.
- [x] Hot-enable without restart.
- [x] Hot-disable while actively attached.
- [x] Active detach restores/cleans runtime presentation correctly.

## Responsive GUI

- [x] Wide two-column layout.
- [x] Narrow one-column layout.
- [x] Low-height scrolling.
- [x] Preview remains fixed while scrolling.
- [x] Footer remains available while scrolling.
- [x] Layout responds correctly to the tested Minecraft GUI scaling/window sizes.

## Localization

- [x] `en_us`
- [x] `en_gb`
- [x] `es_cl`
- [x] `es_es`
- [x] `es_ar`
- [x] `es_mx`
- [x] `pt_br`
- [x] `pt_pt`
- [x] No raw Pick Climber translation keys were observed in the tested options flow.

## Config migration implementation audit

- [x] Unversioned config follows legacy migration rules.
- [x] v1 opacity -> transparency conversion is implemented.
- [x] Legacy `pickaxe_outline` -> `pickaxe` conversion is implemented.
- [x] Legacy shared `colorIntensity` expansion is implemented.
- [x] Missing/out-of-range/current fields normalize through the current options record.
- [x] v3-or-older input is rewritten to canonical current JSON when it differs.
- [x] Future config versions are not automatically downgraded.
- [x] `.tmp` cleanup remains in the fallback save path.

## Final release build gate

- [ ] Run `build.bat` after applying the release cleanup.
- [ ] Confirm `verifySourceQuality` passes.
- [ ] Confirm `verifyArchitectureBoundaries` passes.
- [ ] Confirm exact output exists:
      `build/libs/pickclimber-1.21.1-1.1.0.jar`.
- [ ] Start that exact JAR once in NeoForge 1.21.1 before uploading.
