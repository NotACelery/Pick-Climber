# Pick Climber — 1.2.0 Work Queue

Current source: **1.2.0-dev.53**.

This file contains only unresolved work. Completed implementation history was removed from the active queue and preserved under `archive/`/`CHANGELOG.md`.

## P0 — Exact dev.53 build acceptance

- [ ] Run `build.bat` / `clean build` on the exact dev.53 source with Java 21.
- [ ] Confirm `check`, JUnit and all custom verification tasks pass.
- [ ] Record the resulting JAR name and promote dev.53 only after SUCCESS.
- [ ] If compilation exposes API drift, batch fixes before further feature work.

## P0 — Temporary Rule Book final QA

- [ ] Pick up two Temporary Rule Books with different definition IDs; both must remain valid.
- [ ] Their HUD timers must render independently and continue counting.
- [ ] Attempt a second active copy of the same definition; pickup/issuance must be rejected.
- [ ] Consume one temporary book in a Terminal; the other book/timer must remain intact.
- [ ] Timer unchecked: time on the ground counts and the entity disappears at expiry.
- [ ] `Start counter on pickup` checked: configured lifetime begins on first pickup.
- [ ] Drop after pickup: timer must continue, never pause/reset.
- [ ] Death: owned Temporary Rule Books disappear.
- [ ] Redstone rising edge emits one copy; held signal must not spam.
- [ ] Two-player ownership test: one player's claimed copy cannot be used by another.
- [ ] Receiving player can view/apply a definition they never authored locally.

## P0 — Rule runtime / multiplayer QA

- [ ] Permanent WORLD apply/no-op behavior.
- [ ] Temporary WORLD apply, refresh, conflict and expiry restore.
- [ ] Temporary PLAYER precedence over WORLD and expiry restore.
- [ ] WORLD mutation clears PLAYER overlays as designed.
- [ ] Change an attached block from Stable to Unclimbable and confirm immediate detach.
- [ ] Player Mining and Unmineable Terminals behavior with normal player vs mapmaker bypass.

## P1 — Rules Table / authoring QA

- [ ] Create book from vanilla Book and verify current baseline classifications.
- [ ] ALL-tab white/unassigned save -> Unclimbable regression test.
- [ ] Stable/Unstable tab removal -> Unclimbable regression test.
- [ ] Save/reopen title, cover, pickaxe wear, mining, terminals, activation, scope and duration.
- [ ] Clone/Dye preview and take-result behavior.
- [ ] Import Default World Rules / Current World Rules.
- [ ] Import disk JSON by internal title, delete with confirmation, open rules folder.
- [ ] Export filename prompt, overwrite confirmation and internal title preservation.
- [ ] Small window / high GUI scale / narrow editor scroll QA.
- [ ] Table hitbox, floor rendering, lighting, orientation and axe mining regression.

## P1 — Visual QA

- [ ] Rule Book inventory/ground/first-person readability across representative light/dark dye colors.
- [ ] Terminal front readability/orientation in all six placements.
- [ ] Dispenser front/orientation and GUI alignment after all timer controls.
- [ ] Confirm no remaining z-fighting, missing texture or transparent-black artifacts.

## P1 — Filesystem / compatibility QA

- [ ] JSON roundtrip on Windows.
- [ ] JSON path/filename rejection cases.
- [ ] Legacy profile JSON migration.
- [ ] Missing mod block IDs survive import/export and become visible again if the mod returns.
- [ ] Start without JEI/EMI.
- [ ] Start with JEI only.
- [ ] Start with EMI only.
- [ ] Start with both if the target pack supports both.

## P1 — Performance

- [ ] Baseline FPS with no Rule Book.
- [ ] FPS while looking at Rules Table containing a custom Rule Book.
- [ ] FPS while carrying normal and temporary Rule Books.
- [ ] Confirm reference-first transport no longer reproduces the earlier severe profile-NBT FPS collapse.

## P2 — Release closeout

- [ ] Feature freeze after P0/P1 acceptance.
- [ ] Final `clean build` and dedicated server startup.
- [ ] Update BUILD-STATUS with external results.
- [ ] Finalize CurseForge changelog from `CURSEFORGE-CHANGELOG-1.2.0.md`.
- [ ] Tag/release 1.2.0 and archive dev-only notes.

## Deferred to 1.3.0

Dimensional rules, dimensional editor/viewer, dimension discovery and advanced dimension-aware defaults. See `ROADMAP.md`.
