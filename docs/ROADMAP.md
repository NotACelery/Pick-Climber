# Pick Climber — Roadmap

Current stable line: **1.0.3**.

Current development line: **1.1.0-dev.11** on the `preparacionUpdate` workstream.

The 1.0.x line is the behavioral regression baseline. Development is intentionally split into:

- **Phase 0 — Structural preparation / modularization**
- **1.1.0 — Player Customization & Runtime Control**
- **1.2.0 — Mapmaker Rules / Climbing Rules Jukebox**

Phase 0 is not a feature release. Its purpose is to make the current mod flexible enough to support 1.1.0 and 1.2.0 without layering configuration, world rules and new GUIs directly on top of the original monolithic runtime.

## Phase 0 — Structural preparation

Overall status: **SOURCE-SIDE STRUCTURAL WORK 0.0–0.8 COMPLETE; 0.9 IN PROGRESS**.

Integration policy agreed for this workstream: intermediate `dev` snapshots may temporarily fail to compile while the original runtime is being decomposed. Compile repair, Gradle integration and the complete gameplay regression are intentionally consolidated in **Phase 0.9**, after the structural boundaries are in place. This does not relax the behavioral rule below: every intentional behavior change must still be isolated and documented.

Hard rule for the whole phase: **do not intentionally change the validated 1.0.3 physics, balance, interaction priority, enchantment behavior, tags or controls while extracting architecture**. Functional fixes discovered during the audit must be isolated, documented and regression-tested separately from refactors.

### Phase 0.0 — Baseline freeze and architecture audit

Status: **COMPLETE for structural work**.

Completed:

- [x] Freeze 1.0.3 as the gameplay/interaction baseline.
- [x] Preserve a recoverable 1.0.3 source snapshot for side-by-side regression.
- [x] Audit the complete Java/resource tree for formatting, hardcodes, coupling and extension risks.
- [x] Identify the original `ClimbManager` as the main architectural hotspot: **1,723 lines / 78 methods** before Phase 0.
- [x] Identify the original bidirectional `climb <-> network` dependency.
- [x] Identify duplicated anchor validation between gameplay, HUD and failure feedback.
- [x] Identify direct durability/cooldown persistence spread across runtime paths.
- [x] Identify the future separation between client preferences, transient player runtime state and persistent world/map rules.
- [x] Preserve protocol `13` as the Phase 0 networking baseline.
- [x] Preserve data-driven tool, surface and interactive-block tags as public compatibility seams.
- [x] Correct the original source-format violation found by the audit.

Deferred to final acceptance:

- [ ] Keep the exact stable 1.0.3 JAR/source available during final 0.9 gameplay comparison.

### Phase 0.1 — Low-risk foundations and quality gate

Status: **IMPLEMENTED in `1.1.0-dev.1`; integrated into current source**.

Completed:

- [x] Add `ClimbTuning` as the central home for the existing 1.0.3 tuning values without changing their defaults.
- [x] Move server, local-client and remote-pose runtime maps into `ClimbStateStore`.
- [x] Extract real-jump authorization bookkeeping into `JumpTracker`.
- [x] Extract remote pose data into immutable `RemoteAnchorPoseState`.
- [x] Extract physical tool UUID lookup into `ToolLocator`.
- [x] Introduce the first `ToolWearService` boundary.
- [x] Introduce the first `AnchorCooldownService` boundary.
- [x] Preserve public `ClimbManager` tuning aliases while moving canonical values into `ClimbTuning`.
- [x] Add `verifySourceQuality` and make it part of Gradle `check`.
- [x] Reject tabs, trailing whitespace, Java lines over 120 columns, temporary debug output, code markers and invalid JSON/MCMeta.
- [x] Reduce `ClimbManager` from **1,723 -> ~1,570 lines** without intentional gameplay changes.

### Phase 0.2 — Unified anchor evaluation and geometry

Status: **IMPLEMENTED in `1.1.0-dev.2`; integrated into current source**.

Completed:

- [x] Add immutable `AnchorEvaluation` and `AnchorHandEvaluation`.
- [x] Add explicit `AnchorFailureReason` values.
- [x] Extract side-effect-free mechanical decisions into `AnchorEvaluator`.
- [x] Make `canAttemptAnchor`, hand selection, real attach/boost, HUD and failure feedback consume the same evaluated facts.
- [x] Extract face support, target calculation and collision-safe target correction into `AnchorGeometry`.
- [x] Extract HUD/failure-state derivation into `AnchorFeedbackResolver`.
- [x] Add `ClimbSessionView` as a read-only session view for evaluation.
- [x] Remove ItemStack UUID assignment/cooldown cleanup from query-only anchor checks.
- [x] Keep duplicate-tool UUID correction in commit-time logic, where mutation belongs.
- [x] Add `AnchorSurfaceResolver` as the single world-aware surface-policy seam.
- [x] Keep `AnchorSurfaceResolver` delegating to the exact 1.0.3 tags during Phase 0.
- [x] Add the first `verifyArchitectureBoundaries` checks for side effects and surface-policy bypasses.
- [x] Reduce `ClimbManager` from ~1,570 -> **~1,245 lines**.

Integration note:

- The `dev.2` build attempt exposed a Gradle 9.2.1 configuration-cache incompatibility inside the first implementation of `verifyArchitectureBoundaries`, before Java compilation. Per the Phase 0 integration policy, the verifier itself is repaired in 0.9 rather than stopping the decomposition halfway through.

### Phase 0.3 — Runtime state ownership and anchor lifecycle

Status: **IMPLEMENTED in `1.1.0-dev.3`; final build/QA pending in 0.9**.

Completed:

- [x] Add `AnchorLifecycle` as the authoritative attach/detach transition and cleanup boundary.
- [x] Centralize gravity/flying restoration, crack cleanup, pose cleanup, cooldown handling and state removal.
- [x] Centralize stale-attachment recovery.
- [x] Centralize logout cleanup and reusable detach behavior for dimension/block/tool loss paths.
- [x] Keep local presentation cleanup distinct from server-authoritative lifecycle state.
- [x] Add `AnchorStateValidator` for active-hand reconciliation and authoritative attachment validity.
- [x] Decompose the former 25-field state constructor into coherent immutable substates while keeping compatibility accessors:
  - `AttachmentRestoreState`
  - `AnchorControlInput`
  - `BrakingRuntimeState`
  - `CeilingRuntimeState`
- [x] Keep runtime stores transient; no SavedData/world persistence is introduced in Phase 0.
- [x] Establish a single safe lifecycle path that 1.1.0 hot-disable can later request instead of reproducing cleanup logic.

### Phase 0.4 — Tool wear, cooldown and tool-runtime completion

Status: **IMPLEMENTED in `1.1.0-dev.3`; final build/QA pending in 0.9**.

Completed:

- [x] Add typed `ToolWearReason` values for wall attach, ceiling attach, boost, braking support, braking blocks and sustained ceiling use.
- [x] Route every runtime `hurtAndBreak` through `ToolWearService`.
- [x] Route tool-by-hand and tool-by-UUID wear through the same service.
- [x] Preserve vanilla `hurtAndBreak`/Unbreaking semantics.
- [x] Route cooldown start, clear, propagation, fraction and remaining-tick queries through `AnchorCooldownService`.
- [x] Add local/client cooldown helpers behind the same service boundary.
- [x] Fix the audited unstable-cooldown sync mismatch: a real 40-tick cooldown is no longer truncated to 20 when reporting remaining ticks.
- [x] Preserve all default 1.0.3 wear and cooldown values.
- [x] Preserve boost semantics by calculating the boost/enchantment result before durability is committed, matching the original path if the tool breaks from the cost.

Future seam established:

- 1.2.0 can apply a world durability multiplier inside `ToolWearService` without teaching physics classes about map profiles.

### Phase 0.5 — Networking and synchronization boundary

Status: **IMPLEMENTED in `1.1.0-dev.3`; final build/QA pending in 0.9**.

Completed:

- [x] Add domain-side `AnchorSyncSink` and `ClimbSynchronization`.
- [x] Add NeoForge transport adapter `NeoForgeAnchorSyncSink` in the network package.
- [x] Move attach/detach/boost/remote-pose packet construction and `PacketDistributor` use out of the `climb` package.
- [x] Add `ClientAnchorSync` as a transport-independent synchronized-state DTO.
- [x] Add `ClientClimbSynchronizer` for client-side synchronized-state application and timeout cleanup.
- [x] Make `ModNetworking` translate payloads into domain arguments rather than making the climbing domain depend on payload types.
- [x] Change slide-input application to primitive/domain parameters instead of passing `SlideInputPayload` into `ClimbManager`.
- [x] Break the original bidirectional package dependency: **network -> climb is allowed; climb -> network is removed**.
- [x] Preserve protocol **13** throughout Phase 0.

### Phase 0.6 — Physics and movement extraction

Status: **IMPLEMENTED in `1.1.0-dev.3`; final build/QA pending in 0.9**.

Completed:

- [x] Add `AnchorMotionService` as a small motion dispatcher rather than a replacement god class.
- [x] Extract wall fixed/braking/unstable behavior into `WallAnchorMotion`.
- [x] Extract Strong Grip ceiling swing/maintenance motion into `CeilingAnchorMotion`.
- [x] Extract boost, wall-jump, ceiling-release and vertical prediction math into pure-oriented `AnchorImpulseCalculator` helpers.
- [x] Extract hold/correction positioning into `AnchorPositioning`.
- [x] Add `ClimbRuntimeTicker` to coordinate server/client runtime ticking without owning the mechanics themselves.
- [x] Extract cracks/sounds into `AnchorVisualService`.
- [x] Add `ClimbActionService` for the evaluated boost-vs-attach commit path.
- [x] Add `AnchorInputStateService` for validated/clamped W/A/S/D and camera input state.
- [x] Centralize additional formerly embedded geometry/physics/sound constants into `ClimbTuning` without changing their values.
- [x] Reduce `ClimbManager` to a **154-line compatibility façade**, from the original 1,723 lines.
- [x] Keep server authority and all 1.0.3 tuning values as the intended behavioral baseline.

### Phase 0.7 — Client input and HUD presentation boundaries

Status: **IMPLEMENTED in `1.1.0-dev.3`; final build/QA pending in 0.9**.

Completed:

- [x] Extract String/icon drawing into `AnchorIndicatorRenderer`.
- [x] Preserve the 1.0.2 render invariant: flush before tint, draw+flush while tinted, restore shader white immediately, local ARGB box.
- [x] Add `AnchorIndicatorPolicy` as the status/presentation decision boundary.
- [x] Extract logout, active-tool left click, slide input, double-Shift and jump-latch handling into `ClientClimbInputController`.
- [x] Move client presentation constants into `ClientClimbDefaults`.
- [x] Reduce `ClientEvents` to a **49-line NeoForge adapter**.
- [x] Add `ClimbRuntimePolicy` / `ClimbRuntimeGate` with a default-always-enabled policy.
- [x] Add `ClimbPresentationPolicy` / `ClimbPresentationGate` with default pass-through behavior.
- [x] Route indicator status through the presentation gate and action-bar feedback through the same presentation policy boundary.
- [x] Do **not** add the real 1.1.0 options GUI or persistence during Phase 0.

Future seams established:

- 1.1.0 can implement `Contextual / Always / Off`, icon style/size/opacity, box/text toggles and hot-disable without scattering config checks through renderer/events/gameplay.

### Phase 0.8 — Event/integration cleanup and extension seams

Status: **IMPLEMENTED in `1.1.0-dev.3`; final build/QA pending in 0.9**.

Completed:

- [x] Add `AnchorUseDecision` and `AnchorInteractionService` to centralize forced Shift-anchor and `ITEM_AFTER_BLOCK` decisions.
- [x] Reuse one `AnchorEvaluation` for interaction decisions instead of re-evaluating through event branches.
- [x] Keep `CommonEvents` focused on NeoForge event transaction/cancellation and delegation.
- [x] Keep `BlockInteractionClassifier` side-effect-free and data-driven.
- [x] Preserve `pickclimber:interactive_blocks` for direct-use blocks not otherwise detected.
- [x] Preserve climbing-tool include/exclude tags and surface tags as extension APIs.
- [x] Keep `AnchorSurfaceResolver` as the only direct runtime consumer of `AnchorSurfaceClassifier.classify`.
- [x] Split creative-tab registration from `ModItems` into `ModCreativeTabs`.
- [x] Avoid creating empty 1.2.0 block/menu/item registries before that feature exists.
- [x] Keep reflection, foreign-class inspection and fake interaction calls out of runtime compatibility logic.

### Phase 0.9 — Build integration, automated verification, documentation and final acceptance

Current integration status:

- `dev.3` confirmed that the Gradle architecture verifier now runs under configuration cache and reaches `compileJava`.
- The first Java failure was isolated to four client-input references typed as `Player` instead of `LocalPlayer`; corrected in `dev.4`.
- `dev.4` also moves all root documentation except `README.md` into `docs/` and provides `MIGRATE-DOCS-DEV4.bat` for stale-root cleanup after ZIP extraction.
- Additional compiler/API errors, if exposed by the next build, remain normal Phase 0.9 integration work until the full tree compiles and passes regression.

Status: **IN PROGRESS — FINAL PHASE 0 GATE**.

Already implemented/prepared:

- [x] Add GitHub Actions Java 21 CI for `clean build` on `main`, `preparacionUpdate` and pull requests.
- [x] Keep `verifySourceQuality` and `verifyArchitectureBoundaries` mandatory under `check`.
- [x] Repair the architecture verifier design exposed by the `dev.2` build: project directories/file trees are now resolved at configuration time rather than invoking Gradle script `file(...)` from the execution closure.
- [x] Expand architecture gates to reject:
  - evaluation/feedback side effects;
  - direct surface-classifier bypasses;
  - direct `hurtAndBreak` outside `ToolWearService`;
  - `climb -> network` imports / `PacketDistributor` ownership;
  - `ClimbManager` growing beyond the compatibility-façade budget;
  - `ClientEvents` growing back into renderer/transport ownership.
- [x] Static source-quality scan currently reports zero violations.
- [x] JSON/MCMeta resources currently parse successfully.
- [x] Static architecture scan currently finds zero direct wear, surface or climb->network bypasses.
- [x] `ClimbManager` is 154 lines; `ClientEvents` is 49 lines.
- [x] Raw Java parser sanity checks find no obvious syntax-structure errors independent of the Minecraft classpath.

Still required before Phase 0 can be declared complete:

- [ ] Continue Java 21 / NeoForge 21.1.235 compile repair from the fully decomposed `1.1.0-dev.4` tree.
- [ ] Repair all compile/API/import/type errors produced by the full integration build; do not paper over them by weakening architecture boundaries.
- [ ] Confirm `verifySourceQuality` and the revised `verifyArchitectureBoundaries` pass under the real Gradle configuration cache.
- [ ] Add focused unit tests for extracted pure logic where they provide real value without mocking the entire Minecraft world.
- [ ] Run the exact full 1.0.3 gameplay regression against the compiled Phase 0 JAR.
- [ ] Validate the 40-tick unstable cooldown fix on both server authority and client presentation.
- [ ] Run two-client multiplayer regression for remote pose, detach, hand transfer, timeout and cleanup.
- [ ] Run representative compatibility regression: vanilla interactive blocks, VNS/Cutter-style BlockEntity machines, external climbing tools and tag overrides.
- [ ] Update final build status after the real build and QA results.
- [ ] Regenerate and validate `docs/SOURCE-MANIFEST.json` after the final compile-repair/doc pass.

### Phase 0 exit criteria

The original gate required build/QA before beginning user-facing 1.1.0 work. During the workstream this was intentionally relaxed: source-side 1.1.0 implementation proceeded on the completed structural boundaries, while compile repair and gameplay acceptance remain mandatory before release. This does not waive any unchecked gate below.

Structural architecture:

- [x] Anchor evaluation is unique and side-effect-free.
- [x] HUD/failure feedback consumes the unified evaluation rather than owning a second ruleset.
- [x] Surface classification flows through `AnchorSurfaceResolver`.
- [x] Runtime state ownership and lifecycle cleanup have focused boundaries.
- [x] Every Pick Climber durability path flows through `ToolWearService`.
- [x] Every Pick Climber cooldown path flows through `AnchorCooldownService`.
- [x] Physics/lifecycle no longer constructs or sends network payloads directly.
- [x] Core wall/ceiling movement logic is separated from the compatibility façade.
- [x] Client input/render events are thin adapters with explicit policy seams.
- [x] A default-enabled runtime gate exists for future 1.1.0 hot-disable.
- [x] A presentation policy seam exists for future 1.1.0 HUD customization.
- [x] A world surface-rule seam exists for future 1.2.0 (`AnchorSurfaceResolver`).
- [x] Client visual policy is separate from server/world authority.
- [x] Source-quality and architecture checks are defined and statically green.

Final integration/behavior:

- [ ] Exact final Phase 0 JAR builds with Java 21 / NeoForge 21.1.235.
- [ ] Real Gradle `check` passes, including the configuration-cache-safe verifier.
- [ ] Full validated 1.0.3 gameplay remains behaviorally equivalent except for explicitly documented fixes.
- [ ] Multiplayer/compatibility regression is green.
- [ ] Final documentation, build status and source manifest match the accepted snapshot.

---

## 1.1.0 — Player Customization & Runtime Control

Status: **SOURCE-COMPLETE CANDIDATE in `1.1.0-dev.11`**. No additional 1.1.0 feature is planned before the integration/QA pass unless testing exposes a concrete UX gap.

### In-world options entry

Add **Pick Climber Options** to the vanilla Options screen only while a world/server is loaded.

- [x] Do not inject a Pick Climber button into vanilla Options or the title-screen options flow.
- [x] Add the entry only to `OptionsScreen` while a world/player is loaded.
- [x] Use adaptive columns so the first options screen remains usable across narrow/small GUI layouts.
- [x] Apply visual changes immediately while the screen is open when technically safe.

### HUD controls

Provide:

- [x] Indicator mode: `Contextual`, `Always`, `Off`.
- [x] Show unclimbable indicator: On/Off, default Off.
- [x] Icon style: `String`, `Pickaxe`; Pickaxe uses a dedicated monochrome 16x16 tool-slot mask and no vanilla-item fallback.
- [x] Icon size (50%–200%).
- [x] Icon opacity.
- [x] Border/box visibility.
- [x] Border/box opacity.
- [x] Anchor failure messages: On/Off, mirrored transiently to the server so server-originated feedback respects the client choice.
- [x] One `Reset to Defaults` action restores the complete client option set and resynchronizes runtime preferences.
- [x] Disable visual child controls when their parent state makes them irrelevant.
- [x] Version the client-options JSON (`configVersion: 1`) while retaining defensive loading of legacy/malformed known fields.
- [x] Avoid redundant writes when an option value did not actually change.

Possible later polish after the core controls are stable:

- text opacity/scale if persistent text is retained or expanded;
- additional icon styles only if they remain readable at small GUI scales.

### Default contextual behavior

Show actionable/useful states:

- READY;
- UNSTABLE;
- REQUIRES_STRONG_GRIP;
- REQUIRES_STURDY_LATCH;
- COOLDOWN;
- OUT_OF_RANGE;
- OBSTRUCTED, red.

Hide by default:

- UNCLIMBABLE;
- interactive blocks without Shift;
- MISS/entities/outside HUD range;
- states that communicate no useful action to the player.

### HUD rendering architecture

Use the Phase 0 `AnchorIndicatorRenderer`/policy boundary.

Preserve the 1.0.2 shader/batch isolation invariant so Jade and other overlays never inherit Pick Climber colors.

### Hot-disable

Status: **source-side implementation updated through `dev.11`; build/QA pending**.

Add a personal **Enable Pick Climber interactions** toggle.

When disabled while attached:

- [x] perform a safe authoritative detach via the existing lifecycle path;
- [x] clear client attachment immediately and detach authoritatively on the server;
- [x] stop climbing input/network actions through the runtime gate;
- [x] hide Pick Climber feedback through the same gate;
- [x] leave all non-Pick-Climber Minecraft interactions untouched;
- [ ] verify gravity/abilities/cracks/remote pose cleanup in multiplayer QA.

In multiplayer this preference is per-player. Visual preferences remain client-only; the enabled/disabled interaction state must be known by the server so server event handlers respect the opt-out.

### 1.1.0 network

Protocol bump implemented source-side: **13 -> 14**.

`RuntimePreferencePayload` mirrors only interaction enable/disable and failure-text visibility. Size, opacity, box and indicator-mode preferences remain client-local. Server runtime preferences are transient and cleared on logout.

### 1.1.0 QA

Validate:

- configurable options keybind that opens the dedicated GUI only in-world;
- responsive screen;
- size/opacity extremes;
- String and Pickaxe;
- border on/off;
- messages on/off;
- Contextual/Always/Off behavior;
- unclimbable visibility option;
- hot-disable while attached/unattached;
- hot-enable without restart;
- two multiplayer clients with different visual/enable preferences;
- Reset to Defaults and immediate runtime resync;
- disabled/enabled dependent controls at each indicator mode;
- legacy client config without `configVersion` and future-version known-field loading;
- complete 1.0.3 physics/interaction regression;
- Jade/overlay color isolation.

---


### 1.1.0 dev.9 UX correction pass

- [x] Remove the injected vanilla Options-screen entry.
- [x] Open the dedicated GUI from a configurable key mapping available in Controls.
- [x] Keep the dedicated options GUI available only while a world/player is loaded.
- [x] Move interactions to the first control and disable all dependent controls when runtime interactions are OFF.
- [x] Place Failure Messages immediately under Indicator Mode.
- [x] Place Indicator Style immediately above Indicator Box.
- [x] Replace user-facing opacity semantics with transparency semantics.
- [x] Apply String alpha through direct texture rendering instead of ItemRenderer.
- [x] Add independent Muted / Normal / Neon color-intensity presets for icon and box.
- [x] Replace the provisional Pickaxe renderer with a dedicated 16x16 tool-slot texture mask.
- [x] Draw preview icon/text inside the options screen and suppress the normal HUD indicator while that screen is open.

### 1.1.0 dev.10 integration repair

- [x] Record the exact dev.9 compile failure: `Consumer<Double>` passed where `DoubleConsumer` is required.
- [x] Change the shared transparency-slider factory to `DoubleConsumer`.
- [x] Audit the client options package for additional boxed/primitive double callback mismatches.
- [ ] Compile the exact dev.11 tree with Java 21 in the release environment.
- [ ] Run the full dev.11 options/runtime regression once the build is green.

## 1.2.0 — Mapmaker Rules / Climbing Rules Jukebox

Status: **PLANNED AFTER 1.1.0**.

### 1.1.0 dev.11 pickaxe/reset simplification

- [x] Rename the player-facing `Pickaxe Outline` style to `Pickaxe`.
- [x] Replace the provisional procedural pixels with a dedicated 16x16 monochrome tool-slot texture mask.
- [x] Preserve old `indicatorStyle: pickaxe_outline` configs through config-v3 migration.
- [x] Replace `Reset HUD` and `Reset All` with one `Reset to Defaults` action.
- [ ] Compile the exact dev.11 tree and verify the new texture-backed renderer in-game.
- [ ] Confirm the Pickaxe silhouette reads immediately as a pickaxe at 50%, 100%, 164% and 200% size.


### Creative-only control block

Add a custom white/gray jukebox-style block for map authoring.

- Creative-only acquisition.
- No survival recipe.
- Custom GUI.
- Server-authorized editing.
- Recommended permission: Creative + permission level >= 2.
- Visual inspiration may resemble a jukebox/record player, but it does not need to subclass vanilla jukebox/music behavior.

### Paper -> Climbing Rules Card

Insert paper into the station and choose:

- **Create New Card**.
- **Import JSON**.

The resulting **Climbing Rules Card** stores a validated rule profile.

Existing cards can be inserted to edit, apply or export them.

The station also provides:

- eject/remove current paper/card;
- restore world Pick Climber defaults;
- clear the currently applied rules profile with confirmation.

### Block rule editor

Tabs:

- Stable / Climbable.
- Unstable.
- Unclimbable.

Provide:

- search;
- scrollable creative-style block grid;
- multi-select;
- Select All Visible;
- Clear Selection;
- Set Stable;
- Set Unstable;
- Set Unclimbable;
- Restore Selected to Default.

Only list reasonable full-block anchor candidates by default. Cache the catalog for large modpacks.

### Unlisted policy

Profile option:

- `Unclimbable` — recommended for strict parkour/puzzle maps.
- `Use Pick Climber Defaults` — partial override mode.

### Rule precedence

With no world profile:

`unclimbable tag > unstable tag > stable tag > fallback`

With a profile:

`explicit unclimbable > explicit unstable > explicit stable > unlisted policy`

If the unlisted policy is `Use Pick Climber Defaults`, delegate to the normal baseline classifier through `AnchorSurfaceResolver`.

### Enchantment invariant

Rules only change the base surface classification.

They must not bypass:

- Strong Grip ceiling requirement;
- Sturdy Latch unstable behavior;
- collision validation;
- range validation;
- tool/cooldown rules.

### Durability rule

Add one initial world-profile control:

**Pickaxe durability multiplier** — default `100%`.

Apply it centrally through `ToolWearService` to all Pick Climber wear paths while continuing to use vanilla `hurtAndBreak`, preserving Unbreaking.

Do not expose arbitrary physics/cooldown tuning in 1.2.0.

### Player mining restriction

Add profile rule:

**Player Mining: Enabled / Disabled**.

When disabled:

- prevent normal player-driven block breaking/mining for the map rules context;
- do not pretend this also blocks every modded machine, Create drill, explosion or automation unless those systems are explicitly supported later;
- keep placement and unrelated interactions unchanged unless a later mapmaking feature intentionally adds separate restrictions.

This prevents players from bypassing a designed parkour route by mining the map.

### Apply / restore behavior

`Apply to World` copies the validated card profile into persistent server/world data. The map must keep working after restart and when distributed without the original external JSON.

`Restore World Defaults` must atomically restore:

- normal Pick Climber surface/tag behavior;
- durability multiplier to 100%;
- Player Mining to Enabled;
- no active card overrides.

Ejecting the physical card **does not automatically erase an already applied world profile**; restore/clear is an explicit action.

### JSON import/export

- JSON is the canonical portable profile format.
- Let the user name the profile/file from the in-game GUI before export.
- Sanitize filenames; never accept arbitrary paths or traversal.
- Include a `format_version` for migration.
- Validate resource locations, enum values, ranges and conflicts on import.
- Import into the editor first; do not apply automatically.
- Unknown/missing mod block IDs should not crash loading and should remain recoverable for packs where the mod later returns.
- Support tags in advanced JSON profiles if they can be represented safely without making the GUI confusing.

### Persistence / authority

- Active rules are server/world authority, not client config.
- Use persistent world `SavedData` or the appropriate NeoForge/Minecraft world data mechanism.
- Editing/applying requires appropriate mapmaker/admin authority.
- Clients receive only the synchronized state necessary for correct gameplay/UI.

### 1.2.0 QA

Validate:

- create card from paper;
- import/export and filename sanitation;
- edit an imported card before applying;
- stable/unstable/unclimbable tab movement;
- unlisted = blocked vs defaults;
- durability multiplier across every wear path;
- Unbreaking still works;
- Player Mining restriction prevents normal mining cheats;
- Strong Grip/Sturdy Latch remain authoritative;
- world restart persists applied rules;
- map transfer preserves applied world rules;
- restore defaults fully reverts classification, wear multiplier and mining restriction;
- missing mod IDs do not corrupt the profile;
- multiplayer permission enforcement;
- complete 1.0.3 + 1.1.0 regression with no active world profile.

---

## Explicitly deferred beyond 1.2.0

Do not let these expand the first mapmaking release unless a real requirement appears:

- regional/area-specific rule profiles;
- multiple overlapping Jukebox rule zones;
- per-dimension cards;
- redstone-driven rule swapping;
- arbitrary physics tuning from cards;
- placement restrictions;
- explosion/automation protection;
- command scripting or route objectives;
- card inheritance/composition.

These remain valid candidates for later 1.3.x+ work once the world-rule model is proven in real maps.

---

## Current handoff / exact next step

Current source target: **`1.1.0-dev.11`**.

Current stable comparison target: **`1.0.3`**.

Structural passes **0.0 through 0.8 are implemented source-side**. The remaining work is Phase 0.9 integration/acceptance:

1. Finish documentation/manifest for the current structural tree.
2. Run the full Java 21 / NeoForge `clean build --stacktrace`.
3. Repair compile/API/import/type errors without collapsing the new module boundaries.
4. Confirm both Gradle verification tasks under configuration cache.
5. Run `docs/testing/TESTING-1.1.0-dev.11.md` against the exact 1.0.3 baseline.
6. Run multiplayer and representative mod/tag compatibility regression.
7. Regenerate the manifest after any final repair.
8. Mark Phase 0 complete only when the exact compiled JAR passes regression.
9. Treat 1.1.0 as releasable only after the compiled dev.11-equivalent tree passes the full 1.0.3 + options/runtime regression.

This handoff is intentionally explicit so Phase 0 can continue safely in a new conversation without reconstructing the architecture from chat history.
