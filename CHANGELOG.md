## 1.2.0-dev.21

- Promotes dev.20 as the stable/build-clean baseline after a successful Windows clean build.
- Adds optional JEI 1.21.1 integration through an isolated `integration/jei` plugin.
- Adds optional EMI 1.21.1 integration through an isolated `integration/emi` plugin.
- Documents authoring as `[Book] + [Rules Table] => [White Rule Book]` without adding a survival recipe.
- Documents application as `[Rule Book] => [Rules Terminal]`, treating the Terminal as a catalyst/workstation.
- Keeps both recipe-viewer APIs compile-only and adds a build gate against accidental core/runtime hard-links.
- Adds the shared Climbing Rules category string to all eight supported locales.

## 1.2.0-dev.20

- Promotes dev.19 as the build-clean baseline after a successful Windows build.
- Adds final approved 64x64 visual textures for Rules Table, Rules Terminal and Rule Dispenser.
- Makes Rules Table horizontally facing so its authored front follows placement orientation.
- Splits Terminal/Dispenser block models into horizontal and vertical variants for clean six-direction presentation.
- Adds a 32x32 two-layer Climbing Rule Book model with perfectly aligned cover/details canvases.
- Tints only the Rule Book cover layer from the portable `DyeColor`; pages, hardware and emblem remain fixed.
- Reuses the exact existing HUD pickaxe outline texture as the Rule Book cover emblem instead of a rendered pickaxe.
- Temporary Rule Books inherit the same dynamic cover tint from their embedded Rule Book definition.
- Leaves a dedicated centered-aperture UP/DOWN Dispenser texture as the final visual refinement; dev.20 reuses the approved normal front.



## 1.2.0-dev.14

- Fixed Rule Book display-name compilation (`Component` vs `MutableComponent`).
- Removed deprecated explicit EventBusSubscriber bus selection for client rule registration.


## 1.2.0-dev.15

- Audited a real Windows project tree against the root-ready source: no stale 1.1.0 files or retired rule-system classes were present.
- Fixed `TemporaryRuleBookIssuanceService.removeOwnedBooks` failing `compileJava` by removing a lambda capture of the mutable inventory slot loop variable.
- Removed the harmless empty `net/minecraft/server/network/` directory from packaged snapshots.
## 1.2.0-dev.16

- Enabled ModDevGradle JUnit integration so `src/test` receives the Minecraft/NeoForge modding classpath.
- Added the JUnit Platform launcher runtime matching JUnit 5.11.4.
- Confirmed the external dev.15 build passes `compileJava`; the remaining build failure was isolated to `compileTestJava` configuration.


## 1.2.0-dev.17

- Promoted dev.16 as the stable/build-clean baseline after a successful external Windows clean build.
- Added a read-only Climbing Rule Book Viewer opened from item use, with Overview/Stable/Unstable/Unclimbable tabs.
- Viewer block tabs reuse the shared Search catalog and hide unavailable mod IDs without deleting portable data.
- Viewer closes through Escape or the player's actual Inventory keybind; no hardcoded `E`.
- Added Duplicate sub-GUI to the Rules Table with copy count, source/material/cover preview and optional Dye recolor.
- Duplicate is server-authoritative: source is never consumed, Books equal output count, and Dye is consumed only for a real recolor.
- Duplicate output goes to player inventory; safe overflow drops at the player instead of deleting output.
- Shared block Search now matches full `namespace:path` ResourceLocations in addition to localized name/namespace/path.


## 1.2.0-dev.19

- Finaliza UX de Rules Table previa al freeze: Restore World Defaults usa confirmación real y el Dye slot vacío muestra un ghost tenue.
- Reduce el máximo de overrides explícitos por Rule Book de 32768 a 8192.
- Limita Rule Book serializado a 512 KiB y usa NBT StreamCodec con `NbtAccounter` acotado.
- Añade preflight cliente y revalidación servidor para payloads completos de Rule Book; client NBT nunca se confía directamente.
- Reordena WAITLIST: integraciones JEI/EMI antes de assets; assets finales quedan deliberadamente al final.

## 1.2.0-dev.18

- Promoted dev.17 as the stable/build-clean baseline after a successful Windows clean build.
- Export filenames now derive only from the validated Rule Book `bookName`; the separate filename field was removed.
- Added a shared cross-platform Rule Book name policy for Windows/Linux/macOS-safe filenames and reserved device names.
- Rule Book validation canonicalizes `profile.name` to `bookName`, eliminating a hidden second name in portable identity.
- Rule Book JSON now defaults missing `cover_color` to White and gives specific feedback for unsupported future schemas.
- Added filesystem roundtrip/overwrite, legacy migration, manipulated JSON, cross-platform filename and portable identity tests.
- Terminal and Rule Dispenser now require current schema v2; legacy data must be repaired/saved through the Rules Table.
