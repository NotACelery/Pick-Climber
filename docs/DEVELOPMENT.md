# Pick Climber — Development Reference 1.1.0

This document describes the current 1.1.0 runtime and the boundaries that future work must preserve.

## 1. Release baseline

- Minecraft: `1.21.1`
- NeoForge: `21.1.235`
- Java: `21`
- Mod id: `pickclimber`
- Release version: `1.1.0`
- Gameplay regression baseline: `1.0.3`
- Network registrar protocol: `14`
- Runtime target: client and dedicated server

`gradle.properties` is the canonical source for release/dependency metadata.

## 2. Authority model

Pick Climber is server-authoritative for physical climbing state.

The server owns:

- anchor attach/detach decisions;
- authoritative position and movement;
- braking and ceiling movement;
- cooldown state;
- durability and Unbreaking interaction;
- remote-pose publication;
- safe lifecycle cleanup.

The client owns presentation and input collection. Client options may hide or restyle feedback, but they never make an invalid anchor mechanically valid.

## 3. Architecture overview

### Root package

`PickClimber` registers the mod.

`ModItems` owns item registration.

`ModCreativeTabs` owns the creative tab and enchanted-book entries.

### Client package

`ClientEvents` is a thin NeoForge event/keybind adapter.

`ClientClimbInputController` owns local input edge detection and climbing input forwarding.

`AnchorIndicatorPolicy` resolves what indicator state should be presented.

`AnchorIndicatorRenderer` owns HUD placement, preview rendering, opacity and box rendering.

`AnchorIndicatorIconRenderer` dispatches to independent icon implementations:

- `StringIndicatorIconRenderer`
- `PickaxeIndicatorIconRenderer`

`PickClimberOptionsScreen` owns the dedicated in-world options GUI and its responsive layout.

`PickClimberClientOptionsStore` owns client config loading, migration, normalization and persistence.

`ClientOptionsPresentationPolicy` maps client options to `ClimbPresentationGate`.

`ClientRuntimePreferenceController` mirrors runtime/failure-text preferences that the server must know.

### Climb package

`ClimbManager` is a compatibility façade, not a home for new mechanics.

`AnchorEvaluator` is the side-effect-free mechanical evaluation entry point.

`AnchorEvaluation`, `AnchorHandEvaluation` and `AnchorFailureReason` are immutable evaluation results.

`AnchorFeedbackResolver` derives HUD/failure feedback from the same evaluated facts used by gameplay.

`AnchorGeometry` owns face/target/reach/collision geometry.

`AnchorSurfaceResolver` is the only world-aware surface-policy seam used by runtime consumers.

`AnchorLifecycle` owns authoritative attach/detach/cleanup transitions.

`AnchorStateValidator` validates active attachment/tool coherence.

`ToolWearService` is the only Pick Climber durability boundary.

`AnchorCooldownService` is the cooldown persistence/query boundary.

`WallAnchorMotion`, `CeilingAnchorMotion`, `AnchorImpulseCalculator`, `AnchorPositioning` and
`ClimbRuntimeTicker` own focused physics responsibilities.

`ClimbRuntimeGate` controls whether Pick Climber interactions are enabled for a player.

`ClimbPresentationGate` controls presentation filtering without changing mechanical authority.

### Network package

`ModNetworking` registers protocol `14`.

The network package may depend on `climb`; `climb` must not depend on transport classes.

`RuntimePreferencePayload` carries only the transient preferences that server-side code must know:

- interactions enabled/disabled;
- failure-text visibility.

HUD mode/style/scale/transparency/colors/box visibility remain client-local.

## 4. Interaction ownership

Normal right click belongs to Minecraft/the target block first.

Pick Climber acts after normal block use when that interaction did not consume the click.

`Shift + right click` is the explicit force-anchor gesture. It may pre-empt a normally interactive block only when the target is actually a valid anchor.

Rejected anchors must not:

- spend durability;
- start cooldown;
- create cracks;
- move the player;
- steal an otherwise valid normal interaction.

`BlockInteractionClassifier` keeps the HUD conservative around `MenuProvider`, loaded BlockEntity and
`pickclimber:interactive_blocks` targets without reflection or fake interaction calls.

## 5. Tool identity and hand priority

Climbing tools are selected through tags.

The off hand is preferred among valid climbing tools so the main hand can keep normal gameplay duties.

Each physical climbing tool receives a persistent UUID only when runtime identity is needed.

If duplicated ItemStack data causes two equipped tools to share one UUID, commit-time logic separates the candidate identity before it becomes a second active tool.

Vanilla `F` hand swapping reconciles the active anchor by UUID. Moving the same physical tool to the other hand does not charge durability, restart cooldown or recreate the anchor.

## 6. Surface policy

Current block tags:

- `pickclimber:stable_anchor_blocks`
- `pickclimber:unstable_anchor_blocks`
- `pickclimber:unclimbable_blocks`
- `pickclimber:interactive_blocks`

Current item tags:

- `pickclimber:climbing_tools`
- `pickclimber:excluded_climbing_tools`

Exclusions have priority over inclusions.

Runtime surface consumers must call `AnchorSurfaceResolver`; direct calls to `AnchorSurfaceClassifier.classify`
outside the resolver are an architecture violation.

## 7. Core movement invariants

The validated 1.0.3 behavior remains the balance baseline.

Important values include:

- physical anchor movement reach: `1.5` blocks;
- stable initial cooldown: `20` ticks;
- unstable initial cooldown: `40` ticks;
- braking activation: fall distance greater than `5.0` and vertical speed below `-0.40`;
- unstable controlled descent without Sturdy Latch: `-0.136` blocks/tick;
- wall attach/boost base durability cost: `15`;
- braking additional wear: `10` per full vertical block physically travelled;
- Strong Grip ceiling attach cost: `20`;
- sustained Strong Grip wear: `1` every `20` ticks.

Sturdy Latch converts supported unstable surfaces to firm anchor behavior and reduces their initial cooldown to the stable value.

Strong Grip enables ceiling anchors. Unstable ceilings still require Sturdy Latch.

## 8. HUD and renderer isolation

Indicator states come from shared anchor evaluation rather than a second client-only ruleset.

`StringIndicatorIconRenderer` and `PickaxeIndicatorIconRenderer` use isolated tinting:

1. flush pending GUI work;
2. enable/apply local blend/tint;
3. draw and flush the icon while tinted;
4. restore shader color to white immediately.

The surrounding box uses local ARGB fills.

Jade and unrelated overlays must never inherit Pick Climber tint state.

The Pickaxe style uses `textures/gui/pickaxe_indicator.png`, a dedicated 16x16 monochrome mask.

## 9. Client options

The options screen opens from `PickClimberKeyMappings` (default `K`) only when:

- a local world exists;
- a local player exists;
- no other screen is active.

Options are applied immediately.

The GUI uses:

- two columns when enough Screen width exists;
- one column below the width breakpoint;
- row-based scrolling when height is insufficient;
- a fixed preview;
- a fixed footer;
- stacked footer buttons on extremely narrow widths.

The preview clamps only its own icon draw size. The actual gameplay HUD still honors the configured 50%–200% scale.

## 10. Client config and migration

Path:

```text
config/pickclimber-client.json
```

Canonical schema: `configVersion: 3`.

Current persisted keys:

- `configVersion`
- `indicatorMode`
- `indicatorStyle`
- `showUnclimbableIndicator`
- `iconScale`
- `iconTransparency`
- `iconColorIntensity`
- `showIndicatorBox`
- `boxTransparency`
- `boxColorIntensity`
- `showFailureText`
- `interactionsEnabled`

Legacy aliases are intentionally contained only inside `PickClimberClientOptionsStore`:

- `pickaxe_outline`
- `iconOpacity`
- `boxOpacity`
- `colorIntensity`

For config versions up to v3, loading produces an effective `PickClimberClientOptions` record and compares the input
against the canonical v3 JSON. Any difference is immediately rewritten. This:

- upgrades unversioned/v1/v2 files;
- converts opacity to transparency semantics;
- migrates the old Pickaxe style name;
- expands legacy shared color intensity;
- fills missing fields;
- clamps numeric values through the options record;
- removes obsolete/unknown v3-or-older fields.

A config whose declared version is newer than the current schema is read defensively using known fields but is not
automatically rewritten or downgraded.

Writes use a temporary file and atomic replacement when supported, with a non-atomic fallback and `.tmp` cleanup.

## 11. Hot-disable

`Enable Pick Climber Interactions` is per-player.

Client state is mirrored to the server through `RuntimePreferencePayload`.

Disabling while attached requests the normal lifecycle detach instead of reproducing cleanup logic in the options code.

The runtime gate then blocks new Pick Climber actions while leaving unrelated Minecraft interactions unchanged.

## 12. Clean-code boundaries

The release keeps these hard rules:

1. `AnchorEvaluator` and feedback evaluation remain query-only.
2. Surface policy enters through `AnchorSurfaceResolver`.
3. All Pick Climber `hurtAndBreak` calls live in `ToolWearService`.
4. Cooldown persistence goes through `AnchorCooldownService`.
5. Physics stays split by responsibility; do not rebuild a god-class `ClimbManager`.
6. `climb` must not import NeoForge networking/`PacketDistributor`.
7. `ClientEvents` and `CommonEvents` remain adapters.
8. Presentation config cannot change server/world authority.
9. Hot-disable uses `ClimbRuntimeGate` and the authoritative lifecycle.
10. Future persistent world rules belong to the 1.2.x world-policy layer.

## 13. Automated quality gates

`gradle check` includes:

### `verifySourceQuality`

Rejects:

- tabs in Java;
- trailing whitespace;
- Java lines over 120 columns;
- `System.out`;
- `printStackTrace`;
- `TODO`, `FIXME`, `HACK`;
- invalid JSON/MCMeta.

### `verifyArchitectureBoundaries`

Protects:

- evaluator side-effect freedom;
- surface resolver ownership;
- durability ownership;
- `climb -> network` separation;
- `ClimbManager` façade size;
- `ClientEvents` thin-adapter role.

Do not weaken these verifiers to make a future build pass.

## 14. Build and release

Windows:

```text
build.bat
```

Linux/WSL:

```text
./build.sh
```

The scripts derive the version and Minecraft version from `gradle.properties`.

For 1.1.0:

```text
build/libs/pickclimber-1.21.1-1.1.0.jar
```

The authoritative release identity should be the Git commit/tag used to build the JAR. The old manually maintained
`SOURCE-MANIFEST.json` is intentionally retired in 1.1.0 because it could become stale independently of Git.

## 15. Future extension point

1.2.x mapmaker rules must extend existing seams rather than add checks throughout physics:

- world surface policy -> `AnchorSurfaceResolver`;
- durability multiplier -> `ToolWearService`;
- persistent map state -> dedicated server/world data;
- synchronization -> network adapters;
- client editor -> presentation/UI layer.

The detailed planned feature set lives in `docs/ROADMAP.md`.
