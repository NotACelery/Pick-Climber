## 1.2.0-dev.37

- Rule Book editor now treats every explicitly cleared classification as Unclimbable on save, including removals performed from filtered Stable/Unstable/Unclimbable tabs.
- Rule-defined Unclimbable surfaces now use a dedicated feedback message instead of the geometry-obstruction wording.
- Rule Book rendering reduced from three overlapping generated layers to the standard two-layer tint/detail layout to eliminate coplanar pixel fighting in-hand.

## 1.2.0-dev.36

- Fixed Rule Book classification edits not persisting reliably after Save.
- Save now rewrites the existing table ItemStack in place, verifies the new content-addressed `definition_id`, resolves the just-written definition back from the world library, and only reports success after both checks match.
- Forces a block-entity client update after a successful Rule Book edit so the reopened table cannot reuse stale slot data.

## 1.2.0-dev.35

- Fixes Rule Book block-classification edits not surviving reopen by committing edits as a fresh reference-only ItemStack pointing at the newly registered content-addressed definition.
- Moves Temporary Rule Book HUD timing away from vanilla selected-item text and into the lower event-message gap.
- Makes the Rule Dispenser react to redstone rising edges; automated copies are dispensed unclaimed, bind to the first eligible player who picks them up, and start their configured lifetime on pickup.
- Reworks Rule Book rendering into three layers: solid tinted backing, tinted cover shading and untinted high-contrast details, eliminating accidental translucent holes and restoring emblem/diamond readability across DyeColors.

## 1.2.0-dev.34

- Completes reference-only transport for Temporary Rule Books; their ItemStacks now carry only definition ID, issuance/expiry/source data and compact display metadata.
- Resolves Temporary Rule Book gameplay definitions from the authoritative world/server Rule Definition Library at the Terminal.
- Registers active WORLD/PLAYER gameplay definitions in the same persistent library, keeping same-world multiplayer transfers independent of client JSON files.
- Removes the obsolete Unlisted authoring toggle; custom Rule Book saves are always fail-closed while unresolved imported ResourceLocations remain preserved.
- Adds regression coverage for content-addressed definition IDs, including missing-mod block IDs and title-independent gameplay identity.

## 1.2.0-dev.32

- Begins the Rule Definition Registry migration: Rule Books become lightweight references to server/world definitions instead of render-time containers of full block lists.
- Removes full Rule Book decode/validation from item color, name and tooltip hot paths.
- Adds persistent author metadata and manual `Export World Rules` from the K options screen.
- Makes authored rule profiles fail closed: currently unclassified blocks and future unlisted blocks are Unclimbable.
- Adds visual spacing between classified block cells in the Rule Book editor.
- Keeps automatic hot world-rule synchronization and anchor revalidation on policy changes.

## 1.2.0-dev.22

- Reworks Rule authoring UX around one contextual Table work slot accepting vanilla Books or Rule Books.
- Adds atomic `minecraft:book` -> Rule Book creation/import and Rule Book -> vanilla Book clearing.
- Replaces the old Duplicate screen with a dedicated anvil-style Clone/Dye processing menu with one-at-a-time and Shift-click bulk output.
- Dye processing preserves all rule data and changes only the portable `cover_color`.
- Reinterprets `pickaxe_wear` as flat durability loss 0..100 (default 15), preserving ceiling attach at 20 and sustained ceiling wear at +1/s.
- Migrates legacy `durability_multiplier` JSON into the new flat wear value.
- Adds a visible Rule Dispenser configuration GUI with Rule Book source slot, transport lifetime seconds and test-copy action.
- Fixes Table cardinal artwork orientation and Terminal/Dispenser six-direction placement from player look direction instead of clicked face.
- Makes Rule Book save return to the main Rules Table instead of reopening the editor.
- Adds named JSON export, confirmed direct JSON import, saved-rule deletion and a working Processing Back action.
- Lets the Rule Dispenser use any valid Rule Book as the source for generated Temporary Rule Book copies.
- Filters the editor catalog to full-collision, non-BlockEntity default states and adds a visible draggable scrollbar.
- Reflows editor/Table/Processing labels and controls to reduce truncated or overlapping text.
- Removes obsolete percentage-wear helpers and retired Duplicate/Eject payload/screen code.

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
