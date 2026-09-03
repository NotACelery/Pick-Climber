# Pick Climber 1.2.0-dev.9 — implementation pass

Date: 2026-09-02

## Materialized in this pass

- Renamed the active portable rules object from Climbing Rules Card to Climbing Rule Book.
- Final registry id is now `pickclimber:climbing_rule_book`.
- Normal Rule Books stack to 64 when their item components/data are identical.
- Removed portable per-item revision data.
- Editor stale protection now uses a server-side session token plus an exact Rule Book definition snapshot.
- Added `ClimbingRuleBookDefinition` portable schema v2 with:
  - `bookName`;
  - vanilla `DyeColor coverColor`;
  - mechanical profile;
  - PERMANENT/TEMPORARY activation;
  - WORLD/PLAYER scope;
  - `durationSeconds`.
- PERMANENT definitions normalize to WORLD with zero duration.
- TEMPORARY definitions require a positive duration.
- Added `unmineableTerminals` to the mechanical profile and codec; legacy profile data defaults it to false.
- Split the dev.8 administrative Terminal implementation into `ClimbingRulesTable*` classes and registries.
- Added a separate lightweight `ClimbingRulesTerminalBlock` consumer with six-direction `FACING`.
- Table inventory now has three slots:
  - Rule Book x1;
  - material Book x64;
  - Dye x64.
- Creating/importing a Rule Book now requires `minecraft:book`, not Paper.
- Added initial Survival protection for Rules Table and rule-controlled Survival protection for Rules Terminal.
- Renamed active Rule Book models/textures/lang terminology and removed active Card references from source/resources.
- Added initial Rule Book codec/validator tests.
- Documented the 1.3.0 Rule Dispenser `Persist on Pickup` design in ROADMAP and WAITLIST.
- Bumped `mod_version` from `1.2.0-dev.8` to the first materialized `1.2.0-dev.9`.

## Deliberately still partial

- Table authoring UI still edits the mechanical profile from the dev.8 editor; full metadata controls (cover, activation,
  scope, duration, Unmineable Terminals toggle) still need their final UI.
- Dye slot exists but does not yet apply/consume cover dye in the authoring flow.
- Material Book is currently consumed after successful Rule Book materialization; the final create-draft/save transaction
  still needs to defer consumption until final Save.
- JSON UI still serializes the mechanical profile format; final Rule Book JSON schema/migration remains pending.
- Temporary WORLD/PLAYER runtime is not implemented yet.
- Terminal currently accepts only valid PERMANENT Rule Books; temporary application is intentionally rejected until the
  temporary runtime exists.
- Structural Geometry Safety and Effective Rules per player remain pending.

## Verification

Static source/resource quality pass completed with:

- zero Java lines over 120 columns;
- zero tabs/trailing whitespace/debug/TODO markers under the project quality rules;
- zero invalid JSON resources;
- zero public-class/filename mismatches;
- zero active `Card` references in `src/main/java` or `src/main/resources`.

A real build was attempted using `build.sh`, but the environment could not resolve `services.gradle.org`, so Gradle
9.2.1 could not be downloaded. This source is therefore **not claimed build-clean yet**.
