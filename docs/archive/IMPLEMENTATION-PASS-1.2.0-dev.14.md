# Pick Climber 1.2.0-dev.14 — compile hotfix

## Scope

This pass intentionally contains no new gameplay feature. It isolates the first real Java compilation failure
reported from the root-ready dev.13 source before continuing with Viewer/Duplicate.

## Fixed

- `ClimbingRuleBookItem#getName` no longer mixes `Optional<MutableComponent>` with the base `Component` return
  type. The method now branches explicitly and returns `Component` on both paths.
- `ClientRulesRegistration` no longer uses the deprecated `EventBusSubscriber.bus` / `Bus.MOD` annotation
  arguments. NeoForge resolves `RegisterMenuScreensEvent` to the mod bus from the event type.

## Build evidence from dev.13

The external Windows build reached `compileJava`, proving that the previous Gradle configuration-cache and
`verifyRulesIntegrity` fixes are working. The only compile error reported was the `Component` / `MutableComponent`
mismatch fixed in this pass. Two EventBusSubscriber deprecation warnings were also cleaned up.

## Acceptance status

A fresh external build is still required. The assistant environment cannot currently resolve
`services.gradle.org`, so this pass is not marked build-clean until the root-ready dev.14 source is built on a
machine with the Gradle/NeoForge dependencies available.
