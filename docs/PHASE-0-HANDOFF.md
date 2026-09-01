# Pick Climber Phase 0 — Handoff

## Current state

- Stable gameplay baseline: `1.0.3`.
- Current structural source: `1.1.0-dev.3`.
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

## Build failure already observed

The user's `1.1.0-dev.2` build used Temurin Java 21.0.12 / Gradle 9.2.1 and failed **before Java compilation** inside the first `verifyArchitectureBoundaries` implementation. Gradle configuration cache rejected a `file(...)` call captured from the build-script closure at task execution time.

`1.1.0-dev.3` rewrites that verifier so source directories/file trees are resolved at Gradle configuration time. This correction still requires a real 0.9 build to prove it.

## Critical invariants

Do not intentionally change during Phase 0 unless isolated/documented as a bugfix:

- 1.0.3 physics/tuning/balance;
- off-hand priority;
- normal block-use priority;
- Shift + right-click force anchor;
- server authority;
- Strong Grip / Sturdy Latch semantics;
- public tool/surface/interactive tags and precedence;
- protocol 13;
- 1.0.2 shader-color isolation;
- 1.0.3 interactive-block HUD suppression.

Evaluation must remain side-effect-free. Surface consumers must use `AnchorSurfaceResolver`. Wear must use `ToolWearService`. The climbing domain must not own NeoForge packet transport.

## Current static status

- Java files: 73.
- `ClimbManager`: 154 lines.
- `ClientEvents`: 49 lines.
- Source-format/static quality violations: 0 in the current workspace scan.
- Invalid JSON/MCMeta: 0.
- Direct `hurtAndBreak` outside `ToolWearService`: 0.
- `climb -> network` imports/PacketDistributor: 0.
- Direct surface-classifier bypasses: 0.

This is not a substitute for the final NeoForge build.

## Exact next step — Phase 0.9

1. Finish/update Phase 0 documentation and `TESTING-1.1.0-dev.3.md`.
2. Regenerate the source manifest after the documentation pass.
3. Run the first full Java 21 `clean build --stacktrace` of the completely decomposed tree.
4. Repair every compile/API/import/type error without collapsing the new boundaries.
5. Confirm both Gradle quality/architecture gates under configuration cache.
6. Run the complete 1.0.3 regression, including multiplayer and representative mod compatibility.
7. Regenerate manifest after any source repair.
8. Mark Phase 0 complete only after the exact compiled JAR passes regression.
9. Then begin **1.1.0 — Player Customization & Runtime Control**.

Full detailed status and future 1.1.0/1.2.0 scope lives in `ROADMAP.md`.
