# Pick Climber — Development Reference 1.2.0

## 1. Baseline

- Minecraft: `1.21.1`
- NeoForge: `21.1.235`
- Java: `21`
- Stable source baseline: `1.1.0`
- 1.1.0 commit: `857a0e5544d83be1bb0d8240a2e7deaeda8dc69b`
- Network protocol: `15`
- Rule Book portable format: `2`
- Mechanical profile format: `1`
- Client config: `3`

`1.2.0-dev.41` is the current materialized development cut after the Rule Definition architecture migration started during dev.22 QA. It includes full Rule Book authoring, Permanent/Temporary
WORLD, Temporary PLAYER overlays, Climbing Rule Dispenser transport issuance, Rules/Book countdown HUD, Structural
Geometry Safety, the read-only Viewer, portable JSON hardening, strict legacy migration, final visual assets and optional
JEI/EMI documentation integrations. dev.22 additionally completes the contextual one-slot Rules Table, separate Clone/Dye
Processing menu, flat Pickaxe Wear semantics, filtered 10-column catalog with scrollbar, visible Dispenser configuration
GUI and corrected player-look placement. `1.2.0-dev.20` remains the last user-confirmed Windows build-clean baseline;
dev.33 requires a fresh `clean build`, FPS regression benchmark and multiplayer/runtime QA before promotion.

## 2. Authority model

The server owns all gameplay-affecting climbing rules.

Permanent WORLD and an optional Temporary WORLD overlay live in world SavedData. Temporary PLAYER overlays live in
server-side per-player session state. Neither the client, a physical Rule Book nor an external JSON file is runtime
authority after Apply. Temporary Rule Books are transport tokens only and remain server-validated through issuance state.

Client responsibilities are presentation, input collection, local JSON file access and editing a draft that must be
validated again by the server before persistence.

## 3. Existing climb boundaries

The 1.1.0 decomposition remains mandatory:

- `AnchorEvaluator`: side-effect-free anchor evaluation;
- `AnchorSurfaceResolver`: only surface-policy entry point;
- `AnchorLifecycle`: authoritative attach/detach transitions;
- `ToolWearService`: only Pick Climber wear boundary;
- `AnchorCooldownService`: cooldown boundary;
- focused wall/ceiling/motion classes;
- `ClimbSynchronization`: transport-independent synchronization sink;
- thin `CommonEvents` / `ClientEvents` adapters.

The `climb` package may consume `ClimbRulesBridge`, but it must not depend on the rules implementation, SavedData or
NeoForge packet transport.

## 4. World rules domain

Core types:

```text
ClimbingRulesProfile
ClimbingRulesValidator
ClimbingRulesRuntimeView
UnlistedPolicy
SurfaceClassification
ClimbingRulesService
```

`ClimbingRulesProfile` stores ResourceLocations, not registry objects. This is required to preserve missing mod IDs.

Explicit classification precedence:

```text
UNCLIMBABLE > UNSTABLE > STABLE > unlisted policy
```

A profile with duplicate IDs across explicit categories is invalid.

When `UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS` is selected, baseline classification remains delegated to the existing
`AnchorSurfaceClassifier` through `AnchorSurfaceResolver`.

## 5. Persistent state

`ClimbingRulesSavedData` is attached to the server Overworld as world-global data.

It owns:

- optional active profile;
- policy revision used to invalidate stale wear carry state.

Apply and Restore operate on the profile as one logical unit and immediately refresh the runtime view plus client sync.

No active profile must be structurally equivalent to 1.1.0 defaults.

## 6. Runtime bridge

`ClimbRulesBridge` is the dependency-inversion seam exposed to the climbing package.

Runtime queries include:

- surface resolution;
- durability multiplier percent;
- durability policy revision.

The default bridge implementation must always preserve baseline behavior:

```text
surface -> baseline classifier
durability -> 100%
mining -> enabled
```

## 7. Durability

All logical wear reasons still reach `ToolWearService`.

The multiplier is represented as integer percent `0..500` inside the runtime profile.

Fractional carry is stored per physical tool in `DataComponents.CUSTOM_DATA`. Carry data is namespaced under the
existing Pick Climber tool data root and includes the policy revision so fractions from an older world profile cannot
resurface after rules are changed and later restored.

Effective damage continues through `ItemStack.hurtAndBreak`.

## 8. Player Mining

`PlayerMiningPolicyEvents` is the integration adapter for the world rule.

When mining is disabled it protects both:

- the normal left-click start path;
- final `BlockEvent.BreakEvent` as a server-side backstop.

It deliberately does not hook generic block destruction by machines, explosions, commands or automation.

Administrative bypass is centralized in `MapmakerPermissions`:

```text
Creative && permission level >= 2
```

## 9. Card

`ClimbingRulesCardData` owns Card serialization and revisioning.

Rules:

- stack size 1;
- profile uses the shared canonical codec;
- every successful write increments Card revision;
- malformed/invalid Rule Books are rejected safely;
- missing block IDs survive reads/writes.

A Card is never queried by climbing runtime directly.

## 10. Climbing Rules Terminal

Registry identity:

```text
pickclimber:climbing_rules_terminal
```

The earlier development-only `jukebox` identity is retired and must not reappear in source/resources.

The Terminal is a Creative-only administration station with one persistent Paper/Card slot.

Server-sensitive operations validate:

- player is a `ServerPlayer`;
- mapmaker permission;
- maximum Terminal distance;
- expected open menu where applicable;
- BlockEntity identity;
- current Card;
- profile validation;
- editor session/revision where applicable.

## 11. Editor sessions and stale protection

Opening an editor creates a server session containing:

```text
player UUID
dimension
Terminal BlockPos
Card revision
validated profile snapshot
```

Saving requires the session location and current Card snapshot/revision to still match. This protects against concurrent
editors and manual Card replacement, including same-revision replacement.

Sessions are invalidated on:

- eject;
- invalid Card;
- logout;
- respawn;
- dimension change;
- detected location/session mismatch.

On a stale Card, the server rejects the write and pushes the latest valid Card state back to the editor.

## 12. Client transport boundary

Rules screens do not own NeoForge packet calls.

`ClimbingRulesClientRequests` is the client transport adapter. `PacketDistributor` is allowed there and inside the
rules/network package, not in screen classes.

The editor draft remains client-local until Save. The server decodes, validates and normalizes the submitted profile
before writing the Card.

## 13. JSON

`ClimbingRulesProfileCodec` is shared by:

- SavedData adapter;
- Card adapter;
- network profile payloads;
- JSON files.

Rules JSON is client-local at:

```text
config/pickclimber/rules/
```

`format_version` is independent from mod/config/network versions.

Filename sanitation rejects traversal and invalid/reserved names. Import never auto-applies to world state.

## 14. Responsive UI

`ClimbingRulesTerminalScreen` uses a compact fixed container footprint sized to preserve the player inventory at high GUI
scale.

`ClimbingRulesEditorScreen` has two modes:

- wide: grid + side rules panel;
- narrow: stacked controls with page scrolling.

The block grid has its own scroll independent from narrow-page scrolling. Narrow content is clipped using
`GuiGraphics.enableScissor` / `disableScissor` rather than scaling text down.

## 15. Localization

Supported locales remain:

```text
en_us en_gb es_cl es_es es_ar es_mx pt_br pt_pt
```

`en_us` is the canonical key set. `verifyLocalizationParity` rejects missing or extra keys in supported locales.

## 16. Automated gates

`gradle check` includes:

### verifySourceQuality

Rejects Java tabs, trailing whitespace, lines over 120 columns, direct debug output, temporary markers and invalid
JSON/MCMeta.

### verifyArchitectureBoundaries

Protects evaluator purity, resolver ownership, wear ownership, `climb -> network`, `climb -> rules implementation`,
bridge seam ownership and adapter size budgets.

### verifyLocalizationParity

Requires exact key parity across all eight locales.

### verifyRulesIntegrity

Protects 1.2-specific rules:

- every rules translation key referenced by Java exists in `en_us`;
- rules screens cannot own direct packet transport;
- rules implementation outside `rules/network` cannot own `PacketDistributor`;
- persistence cannot depend on client classes;
- the retired Jukebox source/resource identity cannot return;
- protocol `15` remains frozen for 1.2.0.

Do not weaken a verifier to make a build pass.

## 17. Build/release gate

Before changing the current 1.2 development version to `1.2.0`:

1. run Java 21 clean build;
2. repair compile/API errors without collapsing boundaries;
3. pass all automated gates and tests;
4. run dedicated server smoke;
5. run client smoke;
6. complete the final `TESTING-1.2.0` matrix after it is rewritten for Rule Books/Table/Terminal/Dispenser;
7. build the exact final `1.2.0` source;
8. smoke the exact final JAR;
9. tag that source `1.2.0`.
