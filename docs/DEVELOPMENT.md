# Pick Climber — Development Reference 1.2.0-dev.53

## Baseline

- Minecraft `1.21.1`
- NeoForge `21.1.235`
- Java `21`
- network protocol `15`
- Rule Book format `2`
- mechanical profile format `1`
- current source `1.2.0-dev.53`

The last explicitly confirmed in-game baseline in this development sequence is dev.50. dev.51-dev.53 require the normal external clean-build/runtime promotion pass.

## Architecture boundaries

The climb package remains independent from the Rule-system implementation and NeoForge packet transport. The intended seams are:

- `AnchorEvaluator` — side-effect-free anchor evaluation;
- `AnchorSurfaceResolver` — only surface-policy entry point;
- `AnchorLifecycle` — attach/detach transitions;
- `ToolWearService` — only `hurtAndBreak` owner;
- `AnchorCooldownService` — cooldown ownership;
- `ClimbSynchronization` — transport-neutral synchronization;
- `ClimbRulesBridge` — narrow bridge from climbing mechanics to effective rules.

Rules must not duplicate wall/ceiling physics.

## Mechanical profile

`ClimbingRulesProfile` stores ResourceLocations rather than registry objects so missing mod IDs can survive import/export.

Fields:

- `stableBlocks`
- `unstableBlocks`
- `unclimbableBlocks`
- `unlistedPolicy`
- `pickaxeWear` (`0-100`)
- `playerMiningEnabled`
- `unmineableTerminals`

The active editor is fail-closed: authorable blocks left without Stable/Unstable become Unclimbable at save. `USE_PICK_CLIMBER_DEFAULTS` remains in the codec for compatibility/default profiles; custom authoring persists `UNCLIMBABLE` policy.

Pickaxe Wear is a flat durability loss for standard wall/braking/boost interactions. Ceiling-specific costs remain owned by their existing reasons. The retired fractional durability-multiplier runtime has been removed; only legacy codec migration remains.

## Rule Book identity and persistence

`ClimbingRuleBookDefinition` format 2 holds title/cover/activation/scope/duration/author plus a profile.

Heavy profiles are content-addressed by `RuleDefinitionId` (SHA-256 of mechanical content) and stored in `RuleDefinitionLibrarySavedData`. Normal Rule Books carry a lightweight reference. Editing mechanical rules registers a new immutable definition ID and repoints only the edited book.

World rule runtime does not depend on the physical item after application.

## Runtime authority

Server authority resolves:

```text
Temporary PLAYER -> WORLD -> defaults
```

WORLD persistence uses `ClimbingRulesSavedData`. PLAYER temporary sessions use `PlayerRulesSessionStore` and are not permanent SavedData.

Applying a WORLD mutation clears PLAYER overlays and revalidates active anchors. Expiring temporary rules also triggers synchronization/revalidation.

## Mapmaker permissions

Rules Table administrative actions require:

```text
creative mode + permission level >= 2 + within 8 blocks
```

The Terminal is a gameplay consumer and validates the book/application server-side. `Unmineable Terminals` can lock terminal mining for normal players while mapmakers retain the bypass.

## Temporary Rule Book issuance

The Rule Dispenser stores one source Rule Book, lifetime `1-60` seconds and `startCounterOnPickup`.

Temporary transport data contains owner UUID, issuance UUID, absolute expiry, source dimension/position, definition ID and display metadata.

Rules:

- redstone copies begin unclaimed;
- first eligible pickup binds ownership;
- unchecked start-on-pickup uses an absolute expiry from dispense time;
- checked start-on-pickup starts the configured lifetime on first pickup;
- after ownership, dropping never pauses the timer;
- ground entities are discarded at expiry;
- death removes owned temporary books;
- multiple different definition IDs are allowed;
- a player cannot own two active copies of the same definition ID;
- server inventory sanitation removes invalid/expired/wrong-owner/duplicate copies.

The client renders timers from the actual Temporary Rule Books in inventory, sorted by expiry. The existing server-synchronized nearest expiry remains a fallback during synchronization.

## Table / Terminal / Dispenser blocks

- Table: axe mineable, `noOcclusion`, custom orientation-aware voxel shape, ambient occlusion disabled for the non-solid furniture model.
- Terminal: pickaxe mineable, six-direction facing.
- Dispenser: pickaxe mineable, facing-based redstone output behavior.

Protected rule infrastructure has no survival recipe/loot exposure.

## Client UI

Screens own presentation/input only. Packet transport goes through `ClimbingRulesClientRequests`; Rule-system screens must not call `PacketDistributor` directly.

The K options screen and Rule Book viewer return `isPauseScreen() == false`.

The Rule Book editor uses ALL/Stable/Unstable/Unclimbable tabs. A white selected/unassigned cell in ALL is converted to Unclimbable on Save. The server repeats fail-closed completion as a second safety layer.

## JSON/filesystem

Local directory: `config/pickclimber/rules/`.

Security/portability behavior includes bounded size, path confinement, portable filename normalization, overwrite confirmation and validation before persistence. Current exports are `*.rules.json`; legacy profile-only JSON can be migrated on import.

See `RULE-BOOK-JSON.md`.

## Optional integrations

JEI imports are isolated to `integration/jei`; EMI imports to `integration/emi`. Both dependencies remain `compileOnly`. Loader metadata must not require either mod.

## Code standards

`.editorconfig` is authoritative:

- UTF-8;
- LF for Java/Gradle/JSON/Markdown shell sources as configured;
- spaces, 4-space Java/Gradle indentation;
- 2-space JSON indentation;
- no tabs or trailing whitespace;
- Java lines <= 120 characters;
- no direct `System.out`, `printStackTrace`, TODO/FIXME/HACK markers in committed runtime source;
- comments only where they explain a non-obvious invariant or compatibility constraint.

Development snapshots exclude generated `.gradle-dist/`, `.gradle/`, `build/` and run directories.

## Automated gates

`gradle check` owns the canonical verification tasks:

- `verifySourceQuality`
- `verifyArchitectureBoundaries`
- `verifyLocalizationParity`
- `verifyRulesIntegrity`
- `verifyOptionalIntegrations`
- JUnit tests through ModDevGradle

`tools/audit-source.py` is a lightweight offline preflight for formatting, JSON, localization parity, stale artifacts and documentation version drift. It does not replace a real Gradle build.

## Pre-release rule

Do not promote a dev snapshot to release candidate from static inspection alone. Final acceptance requires the external `clean build` plus the runtime matrix in `TESTING-1.2.0.md`.
