# Pick Climber Phase 0 — Handoff

## Current state

- Stable gameplay baseline: `1.0.3`.
- Current development source: `1.1.0-dev.11`.
- Intended development branch/workstream: `preparacionUpdate`.
- Minecraft: `1.21.1`.
- NeoForge: `21.1.235`.
- Java: `21`.
- Network protocol remains `13` during Phase 0.
- User decision: intermediate compile failures do **not** block modularization; full compilation/API repair is intentionally consolidated in Phase 0.9.

## Completed structural work

### 0.1 — foundations

- `ClimbTuning`
- `ClimbStateStore`
- `JumpTracker`
- `RemoteAnchorPoseState`
- `ToolLocator`
- first wear/cooldown boundaries
- Gradle source-quality gate
- `ClimbManager`: 1,723 -> ~1,570 lines

### 0.2 — unified anchor evaluation

- `AnchorEvaluation` / `AnchorHandEvaluation`
- `AnchorFailureReason`
- `AnchorEvaluator`
- `AnchorFeedbackResolver`
- `AnchorGeometry`
- `AnchorSurfaceResolver`
- `ClimbSessionView`
- side-effect-free query path
- `ClimbManager`: ~1,570 -> ~1,245 lines

### 0.3 — lifecycle/state

- `AnchorLifecycle`
- `AnchorStateValidator`
- coherent `AttachmentRestoreState`, `AnchorControlInput`, `BrakingRuntimeState`, `CeilingRuntimeState`
- centralized attach/detach/stale/logout cleanup

### 0.4 — wear/cooldown

- `ToolWearReason`
- complete `ToolWearService`
- complete `AnchorCooldownService`
- zero direct `hurtAndBreak` outside wear service
- 40-tick unstable remaining-cooldown reporting fix

### 0.5 — networking boundary

- `AnchorSyncSink`
- `ClimbSynchronization`
- `NeoForgeAnchorSyncSink`
- `ClientAnchorSync`
- `ClientClimbSynchronizer`
- no `climb -> network` imports / `PacketDistributor` ownership
- protocol remains 13

### 0.6 — physics decomposition

- `AnchorMotionService`
- `WallAnchorMotion`
- `CeilingAnchorMotion`
- `AnchorImpulseCalculator`
- `AnchorPositioning`
- `ClimbRuntimeTicker`
- `AnchorVisualService`
- `ClimbActionService`
- `AnchorInputStateService`
- `ClimbManager`: **154 lines**

### 0.7 — client/HUD boundaries

- `AnchorIndicatorRenderer`
- `AnchorIndicatorPolicy`
- `ClientClimbInputController`
- `ClientClimbDefaults`
- `ClimbRuntimeGate` / policy
- `ClimbPresentationGate` / policy
- `ClientEvents`: **49 lines**
- no actual 1.1.0 option behavior yet; Phase 0 defaults remain enabled/pass-through

### 0.8 — events/integration

- `AnchorInteractionService` / `AnchorUseDecision`
- `CommonEvents` delegates climbing decisions
- `ModCreativeTabs` split from `ModItems`
- tool/surface/interactive tags preserved
- no reflection/foreign-class inspection/fake interaction calls introduced

## Build failures already observed

The user's `1.1.0-dev.2` build used Temurin Java 21.0.12 / Gradle 9.2.1 and failed **before Java compilation** inside the first `verifyArchitectureBoundaries` implementation. Gradle configuration cache rejected a `file(...)` call captured from the build-script closure at task execution time.

`1.1.0-dev.3` repaired that verifier. The user's next build confirmed the architecture task and Minecraft artifact preparation both completed and Gradle reached `compileJava`. The remaining failure was four occurrences of the same client typing mistake: `ClientClimbInputController` referenced `player.input` through a base `Player` variable.

`1.1.0-dev.4` corrects those client input references by keeping `minecraft.player` as `LocalPlayer`, matching the direct `minecraft.player.input` access already used in 1.0.3. The next real build may expose additional integration errors; those belong to Phase 0.9 until the full tree compiles.

Documentation remains consolidated under `docs/` with only `README.md` kept as root documentation. The current overlay helper is `MIGRATE-DOCS-DEV8.bat`, which removes stale root copies and older migration helpers.

## Critical invariants

Do not intentionally change during Phase 0 unless isolated/documented as a bugfix:

- 1.0.3 physics/tuning/balance;
- off-hand priority;
- normal block-use priority;
- Shift + right-click force anchor;
- server authority;
- Strong Grip / Sturdy Latch semantics;
- public tool/surface/interactive tags and precedence;
- server authority and the intentional 1.1.0 protocol-14 runtime-preference extension;
- 1.0.2 shader-color isolation;
- 1.0.3 interactive-block HUD suppression.

Evaluation must remain side-effect-free. Surface consumers must use `AnchorSurfaceResolver`. Wear must use `ToolWearService`. The climbing domain must not own NeoForge packet transport.

## Current static status

- Java files: 90.
- `ClimbManager`: 154 lines.
- `ClientEvents`: 55 lines.
- Source-format/static quality violations: 0 in the current workspace scan.
- Invalid JSON/MCMeta: 0.
- Direct `hurtAndBreak` outside `ToolWearService`: 0.
- `climb -> network` imports/PacketDistributor: 0.
- Direct surface-classifier bypasses: 0.

This is not a substitute for the final NeoForge build.

## Exact next step

The structural decomposition (Phase 0.0-0.8) and the planned 1.1.0 options/runtime feature surface are now implemented source-side through `1.1.0-dev.11`. Intermediate build failures are intentionally accumulated until the integration pass.

1. Apply the current dev.11 patch/snapshot. Documentation migration is already complete; no new docs migrator is required.
2. Do not add new 1.1.0 scope unless QA exposes a concrete gap.
3. Run the consolidated Java 21 / NeoForge build repair pass.
4. Fix Gradle/API/import/type/deprecation issues without weakening the new architecture boundaries.
5. Run `docs/testing/TESTING-1.1.0-dev.11.md` plus the exact 1.0.3 regression and multiplayer/compatibility checks.
6. Regenerate the manifest after the accepted compile-repair tree.
7. Only after those gates are green, promote the development line toward the final 1.1.0 release.
