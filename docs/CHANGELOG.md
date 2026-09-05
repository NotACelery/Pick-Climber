# Pick Climber 1.2.0 development changelog

This is the cleaned current history. Raw per-build implementation notes remain under `archive/`.

## 1.2.0-dev.53

- Performed repository-wide source/documentation audit and synchronized active documentation to current behavior.
- Removed dead development artifacts: retired Duplicate/Eject payloads, Duplicate screen tombstone and unused fractional-wear runtime/tests.
- Removed obsolete Rules Table textures and the unused Rule Book backing texture.
- Added `tools/audit-source.py` for offline formatting/JSON/localization/stale-artifact preflight.
- Restored server-authoritative prevention of a second active Temporary Rule Book with the same mechanical definition while still allowing different definitions simultaneously.
- Temporary Rule Book HUD now renders independent timers from the actual owned inventory items, sorted by expiry, with server nearest-expiry state retained only as synchronization fallback.
- Reorganized documentation: current specs stay at `docs/` root; superseded implementation/build notes moved to `docs/archive/`.

## 1.2.0-dev.52

- Rules Table added to `minecraft:mineable/axe`.
- Rules Terminal and Rule Dispenser added to `minecraft:mineable/pickaxe`.

## 1.2.0-dev.51

- K Options screen and Rule Book viewer no longer pause singleplayer physics.

## 1.2.0-dev.50

- Fixed ALL-tab white/unassigned save semantics so deselected blocks persist as Unclimbable.

## 1.2.0-dev.49

- Added server-side fail-closed completion for authorable blocks.
- Added final orientation-aware Rules Table voxel shape.
- Fixed floor targeting/occlusion and excessive table lighting darkness.

## 1.2.0-dev.48

- Finalized Rules Table held-item transforms and upper geometry, removing the visible rail seam/z-fighting.
- Preserved transparent book rendering and final table orientation.

## 1.2.0-dev.46-dev.47

- Integrated the final user-authored Rules Table model/textures.
- Corrected table orientation, floor occlusion, open-book alpha and remaining coplanar faces.

## 1.2.0-dev.39-dev.41

- Made Dispenser lifetime server-synchronized.
- Allowed multiple independent Temporary Rule Book issuance tokens instead of one global book per player.
- Added ground expiry and `Start counter on pickup` behavior.

## 1.2.0-dev.32-dev.38

- Migrated Rule Books to reference-first/content-addressed definitions stored in world SavedData.
- Removed full-profile decode from item render/name/tooltip hot paths.
- Added author metadata and K-menu World Rules export.
- Hardened Rule Book edit persistence and fail-closed authoring.
- Reworked temporary transport to carry definition IDs plus compact metadata.

## 1.2.0-dev.22

- Reworked Rules Table around one contextual work slot.
- Added separate Clone/Dye processing.
- Switched custom durability semantics to flat `pickaxe_wear`.
- Added Dispenser GUI/lifetime controls and corrected placement orientation.
- Added direct JSON import/export confirmation flows and editor scrollbar/responsiveness improvements.

## 1.2.0-dev.17-dev.21

- Added Rule Book viewer, JSON hardening/portable names and final network size bounds.
- Added optional JEI/EMI documentation integrations with compile-only isolation.
- This range contains the last historical clean-build baselines before the later UX/reference architecture work.

## 1.2.0-dev.9-dev.16

- Reconstructed the Rule Book/Table/Terminal domain from the earlier experimental branch.
- Added Permanent/Temporary WORLD, Temporary PLAYER, Dispenser transport, HUD timers and Structural Geometry Safety.
- Established ModDevGradle JUnit integration and the custom architecture/source/rules verification gates.
