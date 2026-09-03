# Pick Climber 1.2.0-dev.13 — implementation pass

## Scope

This pass closes the 1.2 Structural Geometry Safety block and folds in the second external-build correction for
`verifyRulesIntegrity`.

## Build gate correction

The dev.12 external build stored the Gradle Configuration Cache successfully, proving the previous `doLast` capture fix.
It then produced four false positives for files that were actually inside `rules/network` because the gate normalized
Windows paths with a two-backslash literal.

The relative path now uses the platform separator:

```groovy
.replace(java.io.File.separator, '/')
```

This keeps the same architecture invariant on Windows and Unix-like systems without disabling Configuration Cache.

## Structural Geometry Safety

Added `StructuralAnchorSafety` as the single structural classifier used by the Rules integration.

The criterion is deliberately state-based rather than class/registry based:

```text
current BlockState has full collision shape -> structurally anchorable
partial / empty collision shape             -> STRUCTURALLY_NON_ANCHORABLE
```

The structural check runs only after confirming that an effective Rules profile is active. Therefore an unconfigured
world preserves the Pick Climber 1.1.0 baseline, while an active Rule Book cannot use Stable/Unstable to bypass unsafe
geometry.

This covers slabs, stairs, fences, walls, panes, doors, trapdoors and arbitrary partial/no-collision modded states without
hardcoded `instanceof` lists. A single slab is rejected, while the same slab block ID in `DOUBLE` state is accepted because
the concrete collision shape is full.

Dynamic states are evaluated against the current level/state. Existing active-anchor validation resolves the surface
again, so a shape that becomes partial is rejected on revalidation.

## Tests

Added `StructuralAnchorSafetyTest` covering:

- stone and sand as full-collision controls;
- slab;
- stair;
- fence;
- wall;
- glass pane;
- door;
- trapdoor;
- torch/no-collision geometry;
- single slab versus DOUBLE slab state sensitivity.

The tests are persisted but require the next external Gradle build for execution.

## Deferred

Still pending after this pass:

- runtime QA with modded partial/dynamic shapes;
- Rule Book Viewer;
- Duplicate GUI;
- final visual assets/tint pass;
- JEI/EMI visual documentation;
- remaining migration/filesystem/hardening tests;
- full build + client/dedicated-server/multiplayer QA.
