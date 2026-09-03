# Pick Climber 1.2.0-dev.10 — implementation pass

Date: 2026-09-02

## Goal of this pass

Finish the Rule Book authoring transaction, materialize full portable metadata in the editor/JSON path, and establish the
WORLD half of the final effective-rules runtime before adding PLAYER overlays and the Rule Dispenser.

## Materialized

### Rule Book authoring

- Editor now receives and saves the full `ClimbingRuleBookDefinition`, not only the mechanical profile.
- Added authoring controls for:
  - Rule Book name;
  - cover color supplied by the Table Dye slot;
  - `Unmineable Terminals`;
  - PERMANENT/TEMPORARY activation;
  - WORLD/PLAYER scope for Temporary definitions;
  - configured duration in seconds.
- PERMANENT forces WORLD and duration `0` both visually and server-side.
- New Temporary drafts default to 60 seconds when switching from Permanent.
- Create/Import opens a server-owned draft and consumes nothing.
- Final Save revalidates location, permission, session token, definition, material Book, output slot and Dye requirements.
- Material Book/Dye consumption occurs only after a valid Rule Book has been materialized; every newly created
  non-White Rule Book requires and consumes one matching Dye, including JSON/current-rules imports.
- Edit sessions retain the exact original Rule Book snapshot; replacement/removal becomes stale and is rejected.
- Table removal invalidates sessions for that Table.
- Added `Import Current Rules`:
  - disabled for WORLD defaults or missing material Book;
  - copies the effective WORLD Rule Book mechanics;
  - Temporary WORLD copies configured duration, never remaining runtime time;
  - does not copy runtime expiry/session state;
  - cover is White unless a Dye is currently supplied in the Table; a non-White materialized Book consumes one
    matching Dye on Save.

### Effective rules seam

- Added central `EffectiveClimbingRulesService`.
- Surface resolution, Tool Wear, Player Mining and Unmineable Terminals now resolve through player-aware effective rules.
- `climb` continues to access rules through policy/bridge seams rather than SavedData.
- PLAYER overlay precedence is intentionally not materialized yet; the seam currently resolves WORLD -> defaults.

### Permanent / Temporary WORLD runtime

- SavedData now owns:
  - optional Permanent WORLD definition;
  - optional Temporary WORLD overlay;
  - absolute `expiresAtGameTime`;
  - policy revision.
- Temporary WORLD overlays Permanent WORLD and falls back to it on expiry.
- Game-time expiry means server-offline time does not consume the temporary duration.
- Restart/load preserves the remaining game-time duration.
- Different nested Temporary WORLD rules are rejected.
- Reapplying mechanically identical Temporary WORLD rules refreshes the timer and counts as a successful Terminal use.
- Temporary rules identical to the effective Permanent/default mechanics are rejected as a no-op.
- Applying Permanent while Temporary is active cancels the temporary overlay and installs the new Permanent baseline.
- Restore Defaults cancels both Permanent and Temporary WORLD state.
- Persisted WORLD slots reject definitions with incompatible activation/scope metadata.
- WORLD transitions broadcast immediately and revalidate active anchors; only invalid anchors detach through the normal
  `AnchorLifecycle.detachServer` path.

### Terminal

- The lightweight Terminal can apply:
  - Permanent WORLD Rule Books;
  - Temporary WORLD Rule Books.
- Exactly one Rule Book is consumed on successful apply/refresh.
- Reject/no-op/conflict consumes nothing.
- Temporary PLAYER remains explicitly rejected until PLAYER runtime is implemented.

### JSON/filesystem

- JSON payload is now the complete Rule Book schema v2.
- Export uses `*.rules.json`.
- Import continues accepting old `*.json` profile files for migration.
- Imported Book/profile name comes from the normalized filename basename.
- Legacy profile JSON migrates to White + Permanent + WORLD + `unmineableTerminals=false` before opening the draft.
- Missing ResourceLocations remain serialized even when their mods are not installed.
- Existing 4 MiB/path traversal/Windows reserved-name hardening remains in place.

### Tests / static confidence

- Added mechanical equality coverage proving display name is ignored while `unmineableTerminals` is mechanical state.
- Runtime-view tests now cover `unmineableTerminals`.
- Filename tests cover `.rules.json` stripping/reserved names.
- Existing Rule Book codec and activation validator tests remain.

## Deliberately still pending

- Temporary PLAYER runtime and per-player sync.
- WORLD mutation cancellation semantics for PLAYER overlays once they exist.
- Temporary Rule Book transport item and Rule Dispenser.
- HUD countdowns.
- Structural Geometry Safety.
- Viewer and Duplicate GUI.
- Final visual assets and six-direction Terminal models.
- JEI/EMI visual documentation.
- Full NeoForge build/runtime acceptance.

## Build status

This environment still cannot download Gradle/dependencies from `services.gradle.org`. Static and syntax-oriented checks are
useful confidence only; dev.10 is not claimed NeoForge build-clean until the real Gradle campaign runs.
