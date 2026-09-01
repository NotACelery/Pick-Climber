# Pick Climber — Development Reference

This document is the technical reference for the current `1.1.0-dev.4` structural source tree.
The gameplay and networking comparison baseline remains the validated `1.0.3` release.
It centralizes the implementation decisions that used to live in Java comments so the runtime code can stay compact and readable without losing design context.

## 1. Baseline

- Minecraft: `1.21.1`
- NeoForge: `21.1.235`
- Java: `21`
- Mod id: `pickclimber`
- Development version: `1.1.0-dev.4`
- Stable behavior baseline: `1.0.3`
- Network registrar protocol: `13`
- Runtime target: client and dedicated server

`gradle.properties` is the canonical source for the version and dependency values used by the build scripts and generated mod metadata.

## 2. Design goals

Pick Climber turns data-driven climbing tools into mobility and rescue tools without replacing Minecraft's normal interaction pipeline.

The implementation follows these priorities:

1. The server owns physical state, authoritative positions, movement, durability and detach results.
2. Block interactions keep priority unless the player explicitly requests a forced anchor with Shift + right click.
3. A rejected anchor never spends durability, starts cooldown, creates cracks or changes physics.
4. Each physical tool owns an individual UUID and cooldown.
5. The client may predict or render presentation state, but it must not invent physical state.
6. Data packs and mod packs extend compatible tools and surfaces through tags instead of hardcoded compatibility lists.
7. Cleanup must be safe across disconnects, dimension changes, broken tools and lost packets.
8. Anchor evaluation is query-only: validation may read state but must not mutate ItemStacks, movement, cooldowns, visuals or networking.
9. Surface policy consumers go through `AnchorSurfaceResolver`; direct classification outside that boundary is an architecture violation.

## 3. Source layout

Phase 0 intentionally organizes code by responsibility rather than by feature version. `ClimbManager` remains as a compatibility façade for existing callers, but new implementation logic belongs in the focused services below.

### Root package

`dev.maicra.pickclimber.PickClimber`

Defines the mod id and registers the root mod components.

`dev.maicra.pickclimber.ModItems`

Owns item registration and the creative-tab icon item only.

`dev.maicra.pickclimber.ModCreativeTabs`

Owns the Pick Climber creative tab and the enchanted-book entries. Keeping this separate prevents `ModItems` from becoming the future home of unrelated 1.2.0 block/menu registration.

### Client package

`client.ClientEvents`

Thin NeoForge client adapter. It delegates disconnect, attack, client-tick and HUD-render events and must not own packet transport or shader operations.

`client.ClientClimbInputController`

Owns local input policy/edge detection used by climbing runtime: active-tool left click, slide input, double-Shift detach, jump latch and logout-local input reset. Network transport remains outside the climbing domain.

`client.AnchorIndicatorRenderer`

Owns the current String indicator draw. It preserves the 1.0.2 batching invariant: flush pending GUI draws, apply tint, render+flush while tinted, and restore shader white immediately. Its box uses local ARGB color.

`client.AnchorIndicatorPolicy`

Chooses the indicator status to present and passes it through `ClimbPresentationGate`. During Phase 0 the default policy is behavior-equivalent to 1.0.3.

`client.ClientClimbDefaults`

Contains current presentation defaults such as indicator size/offset and double-Shift window. These are defaults, not yet persisted 1.1.0 user options.

`client.ClientModEvents`, `client.PickClimberItemDecorator`, `client.PinnedPickaxeRenderer`, `client.CeilingPlayerPoseRenderer`

Retain item-decoration, first-person pinned-tool and local/remote ceiling-pose presentation responsibilities.

### Climb package — evaluation and policy

`climb.ClimbManager`

A 154-line compatibility façade/coordinator in `1.1.0-dev.4`. It exposes established entry points while delegating evaluation, action commit, lifecycle, input, ticking and synchronization to focused services. New physics or configuration logic must not be added back into this class.

`climb.AnchorEvaluator`

Side-effect-free mechanical evaluation entry point. It evaluates both hands from one target/session snapshot. It must never spend durability, mutate tool identity/cooldown, move players, create visuals or send packets.

`climb.AnchorEvaluation`, `climb.AnchorHandEvaluation`, `climb.AnchorFailureReason`

Immutable evaluation/result types consumed by gameplay, hand selection, HUD and failure feedback.

`climb.AnchorFeedbackResolver`

Derives indicator/failure feedback from `AnchorEvaluation` instead of maintaining a second mechanical ruleset.

`climb.AnchorGeometry`

Side-effect-free face support, target, collision-safe correction and movement-range geometry.

`climb.AnchorSurfaceResolver`

The only world-aware runtime entry point for surface classification. During Phase 0 it delegates to `AnchorSurfaceClassifier`. Future 1.2.0 world profiles extend this seam instead of adding card checks to HUD/physics/actions.

`climb.ClimbSessionView`

Read-only view of the current active anchor used by evaluation without depending on orchestration internals.

`climb.ClimbRuntimePolicy` / `climb.ClimbRuntimeGate`

Policy seam for whether Pick Climber runtime interaction is enabled for a player. Phase 0 installs an always-enabled default. 1.1.0 may back this with a synchronized per-player hot-disable preference.

`climb.ClimbPresentationPolicy` / `climb.ClimbPresentationGate`

Presentation seam for indicator filtering and action-bar feedback. Phase 0 is pass-through; 1.1.0 will supply user customization without changing server authority.

### Climb package — state and lifecycle

`climb.ClimbStateStore`

Owns transient server sessions, local client states and remote pose snapshots.

`climb.ServerClimbState`

Immutable authoritative anchor state. Large coherent groups are stored through `AttachmentRestoreState`, `AnchorControlInput`, `BrakingRuntimeState` and `CeilingRuntimeState`, with compatibility accessors retained for call sites during the refactor.

`climb.ClientClimbState`, `climb.RemoteAnchorPoseState`

Local synchronized presentation state and remote observer pose state.

`climb.AnchorLifecycle`

Single authoritative attach/detach/cleanup boundary. It owns ability restoration, state removal, crack cleanup, cooldown outcome and remote-pose detach publication.

`climb.AnchorStateValidator`

Checks active-hand/tool coherence and attachment validity before the runtime continues.

`climb.JumpTracker`

Tracks real-jump authorization timestamps for boosts.

### Climb package — tools, wear and cooldown

`climb.ClimbingToolClassifier`

Data-driven tool eligibility; exclusion tags win over inclusion tags.

`climb.ToolIdentity`

Persists physical ItemStack UUID and cooldown metadata in custom data.

`climb.ToolLocator`

Locates a physical climbing tool by its persistent UUID.

`climb.ToolWearReason` / `climb.ToolWearService`

Typed server-authoritative durability boundary. All Pick Climber `hurtAndBreak` calls must live here so future world durability rules can be centralized while preserving vanilla Unbreaking semantics.

`climb.AnchorCooldownService`

Owns cooldown start/clear/query/propagation for server and synchronized local presentation. Remaining ticks are not capped at the 20-tick base, preserving real longer cooldowns such as the 40-tick unstable case.

### Climb package — action, physics and presentation state

`climb.ClimbActionService`

Consumes one `AnchorEvaluation`, chooses boost versus attach and commits the action without re-running a separate permission ruleset.

`climb.AnchorInteractionService` / `climb.AnchorUseDecision`

Centralizes interaction-pipeline decisions for forced Shift-anchor and the post-normal-block-use path.

`climb.AnchorInputStateService`

Validates/clamps synchronized movement/camera input before updating server anchor state.

`climb.AnchorMotionService`

Small dispatcher that routes the current anchor state to wall or ceiling motion logic.

`climb.WallAnchorMotion`

Owns fixed wall maintenance, dangerous-fall braking, unstable sliding, lateral movement, surface transitions and associated wear decisions.

`climb.CeilingAnchorMotion`

Owns Strong Grip ceiling movement, swing acceleration/damping/radius limits and collision-aware ceiling motion.

`climb.AnchorImpulseCalculator`

Contains boost, wall-jump, ceiling-release and prediction/vector calculations. Keep pure calculations here whenever practical.

`climb.AnchorPositioning`

Owns hold/correct positioning operations shared by runtime paths.

`climb.ClimbRuntimeTicker`

Coordinates per-tick state validation, lifecycle detach, sustained wear, motion advancement, crack/sync refresh and remote pose refresh.

`climb.AnchorVisualService`

Owns anchor cracks and sounds. Physics services request visual actions rather than implementing crack-id/sound details.

`climb.ClimbTuning`

Central source of truth for 1.0.3 physical/timing/durability/sound constants. Phase 0 changes location, not values.

### Climb package — synchronization boundary

`climb.AnchorSyncSink`

Domain interface for publishing attach/detach/boost/remote-pose synchronization without importing NeoForge transport classes.

`climb.ClimbSynchronization`

Domain-side sink holder/forwarder. The installed implementation is supplied by the network package.

`climb.ClientAnchorSync` / `climb.ClientClimbSynchronizer`

Transport-independent synchronized client DTO/application logic, including timeout cleanup.

### Event package

`event.CommonEvents`

NeoForge common event adapter. It remains responsible for event cancellation/result mechanics while delegating climbing decisions/actions to `AnchorInteractionService`, `ClimbActionService`, lifecycle and policy gates.

### Network package

`network.ModNetworking`

Registers protocol `13`, payload codecs/handlers and installs the NeoForge sync sink. Payload handlers translate transport data to domain arguments rather than exposing payload types to the `climb` package.

`network.NeoForgeAnchorSyncSink`

Only transport adapter used by domain synchronization. It owns `PacketDistributor` and construction of attach/detach/boost/remote-pose payloads.

Payload records:

- `AnchorSyncPayload`
- `BoostSyncPayload`
- `DetachRequestPayload`
- `RemoteAnchorPosePayload`
- `SlideInputPayload`

Dependency direction is intentional: **network may depend on climb; climb must not depend on network**.

## 4. Interaction ownership

Normal right click must first belong to the target block and vanilla/modded item pipeline.

The regular path is:

1. main-hand/block interaction receives its normal opportunity;
2. if it consumes the interaction, Pick Climber does nothing;
3. if it returns `PASS`, the pipeline may continue to the other hand;
4. Pick Climber may anchor only in the post-block interaction phase.

Shift + right click is the explicit force-anchor gesture. It may pre-empt the block only when a valid climbing tool and genuinely valid anchor target exist. Invalid anchors must not steal the Shift interaction.

This policy is important for chests, machines, Easy Villagers Farmers, Easy Farmer's Delight Compat Farmers and other modded blocks that handle interaction directly rather than only exposing a vanilla menu provider.

The HUD performs a side-effect-free preventive check before showing an anchor state. `BlockInteractionClassifier` treats a block as normally interactive when it exposes a `MenuProvider`, has a loaded `BlockEntity`, or belongs to the extensible `pickclimber:interactive_blocks` block tag. The BlockEntity branch is intentionally conservative: machine-style blocks with direct use handlers are hidden even when they expose no menu, without inspecting foreign classes or executing their interaction hooks from the render loop. Holding Shift disables that suppression so the force-anchor preview can appear. Modpacks can extend the tag through datapacks for additional non-menu, non-BlockEntity blocks that should preserve normal right-click use.

## 5. Hand priority

Among available climbing tools, the off hand is preferred. This keeps the main hand available for normal gameplay.

While the off hand anchors, the main hand may:

- mine;
- attack;
- place blocks;
- use food, buckets and tools;
- interact with blocks;
- use another valid climbing tool to create a new point.

Left click with the same tool currently supporting the player intentionally detaches it. Left click with the free hand does not.

Vanilla `F` hand swapping is reconciled by tool UUID. Moving the exact same tool between hands transfers active-hand state without charging durability, restarting cooldown, recreating cracks or replaying attach effects.

## 6. Tool identity and cooldown

Each climbing tool receives a persistent UUID only when required by runtime state.

Two distinct ItemStacks must never intentionally share active anchor identity. If duplicated NBT causes two equipped tools to present the same UUID, the candidate tool receives a new identity before it participates in a second anchor or braking support role.

Cooldown is persistent per ItemStack and starts when attachment or boost is confirmed.

Important behavior:

- stable anchor cooldown: `20` ticks;
- unstable anchor cooldown: `40` ticks;
- Sturdy Latch reduces an unstable anchor to the stable `20`-tick cooldown;
- remaining cooldown continues decreasing while the tool remains pinned;
- releasing or swapping hands does not restart the timer;
- a client sync may repair the expected UUID on the exact active tool after a transient ItemStack rebuild;
- a missing cooldown key means no cooldown, not an extremely old timestamp.

## 7. Real-jump boost authorization

Positive Y velocity alone is not enough to trigger the climbing boost.

The server records actual `LivingJumpEvent` times. A boost is allowed only when:

- the player is airborne;
- the player is rising above `0.08` blocks/tick;
- a real jump exists within the `8`-tick authorization window;
- that jump has not already been consumed;
- the player is not actively flying;
- the player is not already anchored;
- the target is a lateral face rather than a ceiling/floor face.

This prevents steps, teleports, network corrections and other positive velocity from being interpreted as an intentional jump.

Base boost height is approximately `1.0` extra block, plus `0.5` per Pick Climber enchantment level.

## 8. Anchor validation

The HUD and actual interaction share the same validation rules as much as possible.

Core checks include:

- valid climbing tool;
- tool cooldown;
- real block hit rather than a synthetic MISS position;
- allowed face;
- surface classification;
- enchantment requirements;
- physical reach;
- collision-safe final player position;
- active-tool identity rules;
- availability of a free second tool when moving from an existing anchor.

The physical anchor movement limit is `1.5` blocks, represented by `MAX_ANCHOR_MOVE_SQR = 2.25`.

The HUD may remain visible out to `3` blocks (`MAX_INDICATOR_DISTANCE_SQR = 9`) so it can communicate `OUT OF RANGE` without implying the click is valid.

## 9. Wall targets and collision correction

The exact click point on the block face is preserved so horizontal and diagonal movement is possible instead of snapping to block centers.

When a wall target would partially insert the player's hitbox into a nearby floor or ceiling, the target-height resolver searches from the ideal target toward the current height. It may correct only the vertical component and must not increase the allowed anchor reach.

A target that remains colliding is rejected without cost.

The server uses the collision-validated target as authority.

## 10. Surface behavior

### Stable surfaces

Stable and fallback surfaces produce a fixed anchor unless a high-speed fall requires initial braking.

### Unstable surfaces

Without Sturdy Latch, unstable surfaces allow attachment but do not become an infinite fixed point. After braking they continue controlled descent at `-0.136` blocks/tick.

### Unclimbable surfaces

These reject the anchor completely.

### Data-driven tags

Block tags:

- `pickclimber:stable_anchor_blocks`
- `pickclimber:unstable_anchor_blocks`
- `pickclimber:unclimbable_blocks`

Item tags:

- `pickclimber:climbing_tools`
- `pickclimber:excluded_climbing_tools`

The default tool tag includes vanilla pickaxes and the optional Eternal Starlight thermal springstone hammer. The wooden pickaxe is excluded by default.

## 11. Braking dangerous falls

Braking begins only when both conditions are met:

- `fallDistance > 5.0`
- vertical speed `< -0.40`

Current constants:

- stop threshold: `-0.08`
- drag: `0.75`
- recovery: `0.035`
- maximum physical braking movement per tick: `0.60` blocks
- initial durability cost: `15`
- additional wear: `10` per full vertical block physically travelled during `BRAKING`

The maximum per-tick travel prevents an extreme internal fall velocity from skipping contact blocks or crossing collision boundaries.

During braking, the first meaningful lateral direction is committed as the diagonal rescue trajectory. This avoids continuous input changes turning braking into free steering.

## 12. Two-tool braking

If both hands contain valid climbing tools when a hard fall begins braking, the second tool can become braking support.

Effects:

- both tools pay the initial effort cost;
- braking is approximately doubled;
- both tools receive proportional wear for travelled blocks;
- the second tool receives an effort cooldown so alternating hands cannot bypass the rescue cost;
- if the support tool breaks or disappears, the main anchor safely continues with single-tool braking;
- an unrelated replacement tool must never inherit support wear.

## 13. Sturdy Latch

`Sturdy Latch I` reinforces an unstable surface after braking.

It does not remove:

- the initial braking requirement for dangerous falls;
- proportional durability wear;
- collision checks;
- server authority.

It changes the post-braking result from continued unstable sliding to a fixed anchor and reduces the initial unstable cooldown from `40` to `20` ticks.

An unstable ceiling requires both Strong Grip and Sturdy Latch.

## 14. Strong Grip ceiling anchors

`Strong Grip I` enables attachment to the underside of valid ceilings.

Current costs:

- `20` durability for a newly confirmed ceiling anchor;
- `1` durability every `20` complete ticks while suspended.

Both costs use normal item damage handling so Unbreaking can affect effective wear.

Pick Climber and Strong Grip are specializations and are data-defined as mutually exclusive. Sturdy Latch may accompany either specialization.

## 15. Ceiling swing

Ceiling motion is server-authoritative.

Current values:

- swing radius: `0.665`
- acceleration: `0.025`
- return force: `0.012`
- damping: `0.96`
- maximum swing speed: `0.18`

The player hitbox remains upright. The system therefore simulates the pivot on a safe horizontal plane rather than literally rotating the body like a pendulum into the ceiling.

`W/A/S/D` supplies intent. The server integrates movement, limits radius, resolves collisions and sends the validated target to the client.

Small ceiling movements are applied smoothly with `setPos`, while periodic absolute teleport confirmations preserve hard server authority.

## 16. Ceiling release and wall jump

Wall jump horizontal impulse: `0.65`.

Ceiling release can combine:

- up to `0.65` voluntary directional impulse from the triggering movement input;
- up to `0.38` stored swing-release momentum.

The combined safety ceiling is therefore `1.03` horizontal speed.

The two vectors may reinforce or oppose each other. If there is no directional input and no accumulated swing momentum, a release receives no artificial horizontal velocity.

Passive release paths, including double Shift or left click with the active tool, must not invent horizontal launch velocity.

The ceiling release result is sent explicitly from the server because the client cannot correctly derive server-authoritative swing momentum from presentation state alone.

## 17. Position and ability authority

While fixed to an anchor, the server owns the target position and temporarily suppresses gravity as needed.

State remembers the original ability flags required for restoration.

Creative flight does not invalidate an anchor. This is intentional so the mechanic can still function as a safety catch during a fall. If the player tries to reactivate flight while attached, the anchor keeps authority until release.

Cleanup restores the remembered ability state instead of assuming vanilla defaults.

## 18. Cracks and visual cleanup

Anchor cracks use a synthetic breaker id so the player is not accidentally excluded by vanilla breaker-id behavior.

Cracks must be removed when:

- changing anchor point;
- detaching;
- the tool breaks or disappears;
- the anchor block is no longer valid;
- changing dimension;
- disconnecting;
- client synchronization times out.

Dimension cleanup must target the level where the anchor was created, because the player may already belong to the destination level by the time the dimension-change event runs.

The client also clears its own synthetic overlay during logout because the final server cleanup packet may arrive after `ClientLevel` is already disappearing.

## 19. Rendering and remote pose state

### Wall pose

The active tool uses a stable first-person swing point instead of letting vanilla animation continue while pinned.

The fixed swing progress is `0.5`, with a `4`-tick visual ramp into the pose.

### Ceiling pose

Strong Grip raises the active arm and tool above the camera. The transform mirrors through the actual arm so main hand, off hand and left-handed players behave consistently.

### Remote third-person pose

Remote ceiling pose synchronization is implemented through `RemoteAnchorPosePayload`.

Properties:

- presentation-only;
- refreshed every `20` ticks;
- expires client-side after `40` ticks without refresh;
- detach/change transitions explicitly clear it;
- it does not transport authoritative position, velocity or durability.

The remaining work is multiplayer regression validation, not initial implementation.

## 20. Clean-code boundaries after Phase 0

The original 1,723-line `ClimbManager` has been reduced to a 154-line compatibility façade. The previous plan to someday create generic `AnchorValidator`, `AnchorPhysics` or `AnchorSyncService` god classes is superseded by the focused boundaries already implemented in `1.1.0-dev.4`.

The architecture rules are now:

1. **Mechanical permission is evaluated once.** `AnchorEvaluator` is query-only and gameplay/HUD/messages consume its results.
2. **World surface policy has one insertion point.** Runtime consumers use `AnchorSurfaceResolver`.
3. **Commit-time side effects stay outside evaluation.** Attach/detach live in `AnchorLifecycle`; action selection/commit lives in `ClimbActionService`.
4. **Durability and cooldown are infrastructure.** `ToolWearService` and `AnchorCooldownService` are the only persistence boundaries for those concerns.
5. **Physics is decomposed by behavior.** Wall, ceiling, impulse math, positioning and ticking are separate; do not recreate a monolithic `AnchorPhysics` class.
6. **Transport is outside the climb domain.** `climb` publishes through `AnchorSyncSink`; NeoForge payload construction lives in `network`.
7. **Events are adapters.** `CommonEvents` and `ClientEvents` should remain small and delegate decisions rather than accumulate mechanics.
8. **Client presentation is not authority.** `ClimbPresentationGate` may hide/change visuals, but it cannot make an invalid anchor valid or alter world rules.
9. **Runtime enablement has one seam.** 1.1.0 hot-disable must use `ClimbRuntimeGate` and a safe lifecycle detach, not scattered booleans.
10. **World rules belong to 1.2.0.** Persistent Rules Cards/Jukebox state must enter through dedicated world policy/services and `AnchorSurfaceResolver`, not through hardcoded checks in physics.

### Architecture gates

`verifyArchitectureBoundaries` protects key invariants under Gradle `check`:

- evaluator/feedback source may not perform known side effects;
- direct surface classification outside `AnchorSurfaceResolver` is rejected;
- direct `hurtAndBreak` outside `ToolWearService` is rejected;
- the `climb` package may not import the `network` package or own `PacketDistributor`;
- `ClimbManager` has a 250-line compatibility-façade budget;
- `ClientEvents` has a 90-line thin-adapter budget and may not own renderer/transport details.

These gates are intentionally architectural, not a substitute for compilation or in-game regression.

### Refactor rule

Do not combine structural movement with balance changes. The only intentional functional correction currently carried by Phase 0 is the audited remaining-cooldown reporting fix for cooldowns longer than 20 ticks; it must be regression-tested separately in 0.9.

## 21. Comment policy

Runtime Java should remain nearly comment-free.

Use comments only when the code cannot reasonably express a non-obvious external constraint. Design rationale belongs here instead.

Examples of information that belongs in this document rather than inline comments:

- why a real jump event is required for boost authorization;
- why tool identity belongs to ItemStack UUID rather than hand;
- why unstable tags outrank stable tags;
- why the server sends exact ceiling release velocity;
- why client logout removes its own crack overlay;
- why periodic teleport confirmation remains even with smooth `setPos` updates.

## 22. Resource/data ownership

Enchantments are data-driven under:

`src/main/resources/data/pickclimber/enchantment/`

Tags are under:

`src/main/resources/data/pickclimber/tags/`

Translations are under:

`src/main/resources/assets/pickclimber/lang/`

The model/icon resources are under:

`src/main/resources/assets/pickclimber/`

The FML logo is:

`src/main/resources/pickclimber_logo.png`

Generated mod metadata comes from:

`src/main/templates/META-INF/neoforge.mods.toml`

## 23. Build system

`build.gradle` uses NeoForge ModDev and Java 21 toolchains.

`build.sh` and `build.bat` read `mod_version` and `minecraft_version` from `gradle.properties` and expect:

`build/libs/pickclimber-<minecraft_version>-<mod_version>.jar`

Both helpers use Gradle `9.2.1` from `.gradle-dist` and download it when absent.

The Windows helper searches for Java through:

1. `PATH`;
2. `JAVA_HOME`;
3. common Prism Launcher Java directories;
4. common Eclipse Adoptium / Java installation roots.

That search logic is operational behavior and should be preserved even though explanatory `REM` blocks are not kept in the script.

## 24. Source manifest

`docs/SOURCE-MANIFEST.json` is an integrity snapshot of the release-relevant working tree.

It should contain:

- current release version;
- relative path;
- byte size;
- SHA-256 for each tracked release file.

Do not include `.git`, build output, `.gradle`, `.gradle-dist`, IDE state or generated caches.

Regenerate the manifest after an intentional source/documentation/resource change.

## 25. Minimum regression checklist

Before publishing a source-cleanup or refactor build, validate at least:

### Basic wall anchors

- normal stable wall attach;
- invalid face rejected without wear/cooldown;
- reach boundary at `1.5` blocks;
- HUD visible through `3` blocks with `OUT OF RANGE` beyond physical reach;
- Jade/tooltips/other HUD overlays keep their own colors while the anchor indicator changes state;
- move between nearby valid anchor points;
- collision rejection near floors/ceilings.

### Interaction priority

- chest/furnace/crafting table normal right click;
- modded machine normal right click;
- Easy Villagers / Easy FD Farmer interaction if present;
- Shift + right-click force anchor;
- invalid Shift anchor allows the target block's normal behavior.

### Hands

- off-hand anchor + main-hand mining;
- off-hand anchor + main-hand attack;
- main-hand anchor + left click passive detach;
- `F` transfer of the same anchored tool;
- switching to a different tool detaches rather than inheriting identity.

### Boost and wall jump

- real jump authorizes boost;
- positive velocity without a real jump does not;
- jump authorization is consumed once;
- wall jump requires release/repress of Space;
- Pick Climber I–III progression.

### Cooldown and identity

- independent cooldown on two tools;
- cooldown continues while pinned;
- `F` does not reset cooldown;
- duplicated UUID tools are separated when needed;
- item rebuild/hand swap does not spuriously detach the active exact tool.

### Falling and braking

- short fall anchors directly;
- hard fall enters `BRAKING`;
- no block skipping at extreme fall velocity;
- stable surface stops after braking;
- unstable surface transitions to controlled slide;
- Sturdy Latch stops on unstable surface;
- two-tool braking shortens rescue and damages both participants;
- loss of support tool returns safely to single-tool braking.

### Strong Grip

- firm ceiling requires Strong Grip;
- unstable ceiling requires Strong Grip + Sturdy Latch;
- initial `20` durability cost;
- sustained `1 / 20 ticks` wear;
- Unbreaking interaction;
- W/A/S/D swing;
- radius/collision limits;
- directional Space release;
- release with stored momentum;
- passive release produces no horizontal launch.

### Rendering

- wall first-person pinned pose;
- ceiling first-person raised pose;
- main/off/left-handed transforms;
- local third-person raised arm;
- remote third-person ceiling pose with two clients;
- `F` transfer while observed remotely;
- detach and timeout cleanup.

### Cleanup

- disconnect while attached;
- reconnect after disconnect;
- dimension change;
- death;
- creative flight toggles;
- tool break;
- anchor block removal;
- stale crack overlay cleanup;
- remote-pose cleanup.

## 26. Documentation map

`README.md`

User-facing behavior, controls, compatibility and build entry point.

`docs/DEVELOPMENT.md`

Current implementation architecture, invariants, maintenance rules and regression reference.

`docs/STRONG-GRIP-DESIGN.md`

Historical/design specification for Strong Grip, Sturdy Latch and ceiling traversal. It remains useful for rationale and planned behavior, but current runtime truth should be checked against `DEVELOPMENT.md` and the 1.0.3 source.

`docs/testing/TESTING-1.0.3.md`

Manual in-game beta/regression procedure.

`docs/ROADMAP.md`

Remaining validation and future ideas. Completed historical phases are retained as project history.

`docs/CHANGELOG.md`

User-visible release history. Formatting-only cleanup should not create a fake gameplay changelog entry.

## 1.1.0 client options and runtime preference boundary

`1.1.0-dev.11` contains the current options implementation consuming the Phase 0 seams. Client HUD preferences are owned by `PickClimberClientOptionsStore` and persisted in `config/pickclimber-client.json`; rendering reads them through `ClientOptionsPresentationPolicy` and `AnchorIndicatorRenderer` rather than through event adapters. `IndicatorStyle` selects between independent String and Pickaxe render paths through `AnchorIndicatorIconRenderer`, so adding future styles does not add branches to NeoForge event adapters or anchor evaluation.

The in-world entry point is the configurable `PickClimberKeyMappings` binding (default `K`). It opens `PickClimberOptionsScreen` only while a local world/player is active and no other screen is open. The screen uses an adaptive layout and writes changes immediately. Child controls become inactive when their parent state makes them irrelevant, and the footer exposes one complete Reset to Defaults action.

Runtime opt-out is intentionally separate from HUD rendering. `PlayerClimbRuntimePreferences` is a common, transient per-player store whose default is enabled. The client mirrors its local preference through protocol 14 using `RuntimePreferencePayload`; the server applies the preference and detaches an active climb through the normal lifecycle. No persistent server/world rule is created by 1.1.0.

Failure-text visibility remains a client preference but is mirrored transiently to the server because some authoritative interaction failures originate server-side. Other HUD properties (mode, style, scale, opacity, box and unclimbable visibility) never leave the client. The JSON format currently writes `configVersion: 3`; legacy files without a version, v1 opacity fields, v2 transparency fields and the former `pickaxe_outline` style value remain readable. Known fields from newer versions are loaded defensively.

The Pickaxe indicator is implemented as a dedicated monochrome 16x16 texture mask behind the same icon-rendering boundary as String. The mask deliberately resembles a tool-slot pickaxe silhouette instead of a procedural hook/curve, and it must not be replaced by a vanilla-item renderer; the separation keeps future styles presentation-only.

## 1.1.0-dev.9 — options entry and presentation boundaries

The options GUI is no longer injected into vanilla `OptionsScreen`. `PickClimberKeyMappings` owns a configurable in-game key mapping, registered through `RegisterKeyMappingsEvent`, while `ClientEvents` only opens `PickClimberOptionsScreen` when a local world/player exists and no other screen is active.

`PickClimberOptionsScreen` owns its preview surface and draws it after an opaque-enough GUI backdrop. `AnchorIndicatorRenderer` suppresses the normal HUD indicator while this screen is open, preventing the live-world indicator from being blurred behind the configuration UI.

User-facing alpha is expressed as transparency. The persisted v2 format stores `iconTransparency` and `boxTransparency`; runtime renderers still receive opacity derived as `1 - transparency`. Legacy `iconOpacity`/`boxOpacity` files remain readable. String rendering uses the vanilla String texture directly so shader alpha affects the actual sprite instead of being reset by item rendering.

`IndicatorColorIntensity` and `IndicatorColorPalette` form a single shared color policy for both icon and border. The modes are `MUTED`, `NORMAL`, and `NEON`; they transform the existing status color rather than replacing status semantics or letting client config choose arbitrary RGB values.


## 1.1.0-dev.10 — slider callback integration repair

`DoubleOptionSlider` deliberately uses primitive-specialized `DoubleConsumer` / `DoubleFunction<Component>` callbacks. The shared transparency-slider factory in `PickClimberOptionsScreen` must therefore also accept `DoubleConsumer`; do not reintroduce `Consumer<Double>` at that boundary, as doing so creates an incompatible functional-interface conversion during `compileJava`.

This repair is presentation/integration-only and does not change persisted transparency semantics, runtime preferences, HUD policy, anchor evaluation or networking.

## 1.1.0-dev.11 — pickaxe asset and reset semantics

The user-facing style name is now `Pickaxe`. `IndicatorStyle.PICKAXE` renders `textures/gui/pickaxe_indicator.png`, a white-alpha mask tinted by the existing status palette. Config v3 maps the legacy serialized value `pickaxe_outline` to `PICKAXE` before normal enum parsing.

The client-options API exposes one `resetToDefaults()` operation. It restores the entire client preference record, including runtime interactions, then `PickClimberOptionsScreen` requests an immediate runtime-preference sync. Do not reintroduce separate HUD/full reset semantics unless a future settings domain genuinely requires different persistence ownership.
