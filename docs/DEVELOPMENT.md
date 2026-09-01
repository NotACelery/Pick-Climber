# Pick Climber — Development Reference

This document is the technical reference for the current `1.0.3` source tree.
It centralizes the implementation decisions that used to live in Java comments so the runtime code can stay compact and readable without losing design context.

## 1. Baseline

- Minecraft: `1.21.1`
- NeoForge: `21.1.235`
- Java: `21`
- Mod id: `pickclimber`
- Release: `1.0.3`
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

## 3. Source layout

### Root package

`dev.maicra.pickclimber.PickClimber`

Defines the mod id and initialization entry point.

`dev.maicra.pickclimber.ModItems`

Registers the Pick Climber creative tab and the icon item used by the tab.

### Client package

`client.ClientEvents`

Owns client-only input and HUD behavior:

- disconnect cleanup;
- left-click interception for the active anchor hand;
- jump-key latch used to avoid phantom wall jumps;
- periodic slide input transmission;
- reach/status indicator rendering.

The reach indicator must not leak render state into the shared GUI batch. `ClientEvents` flushes pending GUI draws before applying the String tint, flushes the icon while that tint is active, and restores the shader color to white in a `finally` block. The border uses its own ARGB fill color and does not modify shader state.

`client.ClientModEvents`

Registers the item decorator for all items. Eligibility is filtered at render time through the climbing-tool classifier so modded tools can participate without a fixed registration list.

`client.PickClimberItemDecorator`

Draws the per-tool cooldown overlay stored on the ItemStack.

`client.PinnedPickaxeRenderer`

Replaces only the active hand's first-person rendering while anchored. It supports:

- the pinned wall pose;
- the elevated Strong Grip ceiling pose;
- main hand, off hand and left-handed players;
- stable transfer when the same tool moves through vanilla `F` hand swap.

`client.CeilingPlayerPoseRenderer`

Applies the elevated arm pose in third person for local and remotely synchronized ceiling anchors.

### Climb package

`climb.ClimbManager`

The current orchestration hub. It owns anchor validation, boost selection, attach/detach transitions, server physics, client synchronization, cracks, cooldown coordination, wall jump calculations and ceiling swing behavior.

This class is intentionally not split during formatting-only cleanup because it is the most regression-sensitive part of the mod. See section 20 for safe future extraction boundaries.

`climb.ServerClimbState`

Immutable server-side record describing one authoritative anchor.

Important fields include:

- anchor dimension, block and face;
- collision-validated target position;
- active hand and tool UUID;
- synthetic crack id;
- ability restoration flags;
- attach time;
- classified surface and wall motion state;
- slide velocity and input;
- exact contact offset;
- committed braking direction;
- Sturdy Latch state;
- optional second braking tool;
- braking distance and charged blocks;
- ceiling center, swing velocity and release momentum.

`climb.ClientClimbState`

Client presentation state for the local player. It stores the synchronized target, anchor block, crack id, hand, tool id, restoration flags, sync age, pose start time and whether the anchor is a ceiling anchor.

`climb.AnchorMotion`

Wall anchor physical modes:

- `FIXED`
- `BRAKING`
- `UNSTABLE_SLIDING`

`climb.AnchorSurface`

Surface classifications:

- `UNCLIMBABLE`
- `UNSTABLE`
- `STABLE`
- `FALLBACK`

`FALLBACK` preserves compatibility for blocks not covered by any Pick Climber surface tag.

`climb.AnchorSurfaceClassifier`

Single surface-classification entry point. Priority is part of the contract:

1. `unclimbable_blocks`
2. `unstable_anchor_blocks`
3. `stable_anchor_blocks`
4. fallback

An exclusion therefore wins if the same block is accidentally included by a broader tag.

`climb.ClimbingToolClassifier`

Single item eligibility entry point. `excluded_climbing_tools` has priority over `climbing_tools`.

`climb.ClimbingHandSelector`

Centralizes hand priority without bypassing Minecraft's interaction sequence. Off hand wins among available climbing tools, but a successful main-hand block/item interaction is allowed to finish first.

`climb.ToolIdentity`

Persists per-tool runtime identity and cooldown in `DataComponents.CUSTOM_DATA` under the `pickclimber` root.

Stored keys:

- `anchor_tool_id`
- `cooldown_until`
- `cooldown_duration`

The UUID belongs to the ItemStack, not to the hand or slot.

`climb.ModEnchantments`

Central resource keys and lookup helpers for:

- `pick_climber`
- `sturdy_latch`
- `strong_grip`

`climb.AnchorIndicatorStatus`

Client-facing result used by the HUD. Current states include ready, unstable, unclimbable, Strong Grip requirement, Sturdy Latch requirement, cooldown, range and obstruction.

### Event package

`event.CommonEvents`

Connects NeoForge events to climbing logic.

Key responsibilities:

- preserve normal block interaction priority;
- implement the explicit Shift + right-click force-anchor path;
- allow Pick Climber to own the click only after the target block declines normal interaction;
- allow the free main hand to mine/attack while the off hand anchors;
- detach if the player attacks/mines with the same tool supporting the anchor;
- capture real `LivingJumpEvent` jumps for boost authorization;
- tick server state and cleanup on dimension/logout events.

### Network package

`network.ModNetworking`

Registers protocol `13` payloads.

Server to client:

- `AnchorSyncPayload`
- `BoostSyncPayload`
- `RemoteAnchorPosePayload`

Client to server:

- `DetachRequestPayload`
- `SlideInputPayload`

`AnchorSyncPayload`

Synchronizes local anchor presentation and lifecycle flags. Flags include restoration state, jump detach, new anchor, cooldown refund and ceiling-anchor state. Cooldown ticks are packed into the flags integer.

`BoostSyncPayload`

Synchronizes a server-authorized velocity after a boost or ceiling release.

`RemoteAnchorPosePayload`

Observer-only state for remote third-person ceiling poses. It intentionally carries no durability, position authority or movement physics.

`DetachRequestPayload`

Carries the detach request plus movement/camera input from the same Space action so the server calculates the wall or ceiling release from the triggering input rather than an older periodic packet.

`SlideInputPayload`

Carries clamped movement intent and camera angles while an anchor requires server-authoritative movement integration.

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

## 20. Clean-code boundaries and future refactor

`ClimbManager` currently contains more than half of the Java source lines. It works, but it is the clearest architectural debt in the project.

Do not split it casually. A safe future refactor should preserve behavior and tests while extracting cohesive responsibilities in small passes.

Suggested boundaries:

### `AnchorValidator`

Candidate methods and rules:

- `canAttemptAnchor`
- indicator validation
- obstruction/range messages
- surface/enchantment requirements
- collision-safe target resolution

### `AnchorPhysics`

Candidate responsibilities:

- wall braking;
- unstable descent;
- lateral slide integration;
- ceiling swing integration;
- collision-limited target movement.

### `AnchorLifecycle`

Candidate responsibilities:

- attach;
- detach;
- cleanup;
- state replacement;
- ability restoration;
- crack lifecycle.

### `ClimbMovementCalculator`

Candidate pure calculations:

- wall jump velocity;
- additional-rise velocity;
- ceiling release vector;
- predicted vertical rise.

### `AnchorSyncService`

Candidate responsibilities:

- local anchor sync;
- remote pose sync;
- periodic refresh;
- detach payloads.

### Refactor rule

Never combine a structural extraction with a gameplay balance or physics change in the same pass. First prove token/behavior equivalence or run the full regression matrix, then change mechanics separately.

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

`SOURCE-MANIFEST.json` is a integrity snapshot of the release-relevant working tree.

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

`TESTING-1.0.3.md`

Manual in-game beta/regression procedure.

`ROADMAP.md`

Remaining validation and future ideas. Completed historical phases are retained as project history.

`CHANGELOG.md`

User-visible release history. Formatting-only cleanup should not create a fake gameplay changelog entry.
