# Pick Climber 1.2.0-dev.11 — implementation pass

Date: 2026-09-02

## Goal of this pass

Complete the Temporary PLAYER runtime that was started after dev.10, keep all gameplay policy behind the central
Effective Rules seam, and materialize the first rules countdown HUD without starting Rule Dispenser transport logic.

## Materialized

### Temporary PLAYER authority

- Added one in-memory Temporary PLAYER session per player UUID.
- PLAYER sessions are server-authoritative and are intentionally not persisted across logout/server restart.
- Each session stores:
  - validated Temporary + PLAYER Rule Book definition;
  - absolute `expiresAtGameTime`;
  - WORLD policy revision captured at start;
  - a PLAYER-specific policy revision for Tool Wear carry invalidation;
  - compiled runtime view.
- Effective precedence is now:

```text
Temporary PLAYER
      -> WORLD
      -> Pick Climber defaults
```

- `EffectiveClimbingRulesService` remains the single player-aware seam used by surface classification, durability,
  Player Mining and Unmineable Terminals.
- A mechanically identical active PLAYER temporary refreshes its timer; a different active PLAYER temporary conflicts.
- A PLAYER temporary identical to current WORLD/default mechanics is rejected as a no-op.

### Lifecycle and WORLD interaction

- Expiry removes only the affected PLAYER overlay, syncs that player and immediately revalidates their anchor.
- Logout cancels the PLAYER session and does not persist/restart it.
- Death cancels the PLAYER session; respawn performs defensive cleanup as well.
- WORLD mutations clear every PLAYER overlay before effective gameplay continues.
- WORLD sync is sent before PLAYER-inactive sync during a WORLD transition, so clients never briefly fall back to stale
  WORLD rules.
- In 1.2, changing dimension keeps the Temporary PLAYER session because WORLD rules are still world-global. Dimensional
  override interaction remains 1.3 scope.

### Per-player synchronization

- Added `PlayerRulesSnapshot`, synchronization sink and NeoForge payload path.
- PLAYER state is sent only to the affected client.
- Login sends current WORLD state and an explicit inactive PLAYER snapshot to eliminate residual client state.
- Client state independently retains WORLD and PLAYER definitions, so PLAYER expiry cleanly falls back to the currently
  synchronized WORLD state.

### Rules countdown HUD

- Added a compact HUD countdown above the hotbar for the currently effective temporary rules timer.
- Remaining time is derived from the absolute server-synchronized game-time expiry.
- Partial seconds round upward, avoiding an early visual zero.
- Formatting is `M:SS` and expands to `H:MM:SS` for long durations.
- The rules-only countdown has no label, matching the frozen HUD design; Rules/Book dual labels remain pending until the
  Temporary Rule Book transport item exists.
- The renderer does not send action-bar/chat messages every tick.

### Localization / tests

- Added localized apply/refresh/conflict messages for Temporary PLAYER to all eight supported locales.
- Removed obsolete “Temporary PLAYER not implemented” / “Temporary Rule Books not active” development messages.
- Added tests for:
  - PLAYER snapshot absolute expiry/revision semantics;
  - client precedence PLAYER -> WORLD and fallback after PLAYER clear;
  - countdown rounding and minute/hour formatting.

## Deliberately still pending

- Real multiplayer isolation/runtime QA.
- Temporary Rule Book transport item and Climbing Rule Dispenser.
- Rules + Book simultaneous HUD labels.
- Structural Geometry Safety.
- Viewer and Duplicate GUI.
- Final Table/Terminal/Dispenser/Book visual assets.
- JEI/EMI visual documentation.
- Full NeoForge build/runtime acceptance.

## Build status

Static source/resource checks and Java parser-oriented scanning pass in the packaging environment. A full NeoForge Gradle
build is still not accepted because the environment cannot resolve/download Gradle from `services.gradle.org`.
