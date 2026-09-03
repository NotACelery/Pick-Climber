# Pick Climber 1.2.0-dev.15 — Build unblock pass

## Scope

This pass does not add gameplay features. It validates the user's real project tree and fixes the next compileJava error exposed by dev.14.

## Tree audit

The user's Windows project tree was compared against the dev.14 root-ready snapshot. File contents matched exactly; there were no stale 1.1.0 source files, retired Card/Jukebox classes, generated build artifacts, or duplicate Java sources. The only extra entry was an empty `net/minecraft/server/network/` directory, which is harmless and is removed from this snapshot.

## compileJava fix

`TemporaryRuleBookIssuanceService.removeOwnedBooks` captured the indexed `slot` loop variable from an `Optional.ifPresent` lambda. Java requires variables captured by lambdas to be final or effectively final, but the loop variable changes each iteration. The code now reads the Optional explicitly and performs the ownership cleanup without capturing the loop variable.

## Build status

The dev.14 user build reached `compileJava`, confirming the Gradle configuration-cache and Windows path fixes from previous passes. dev.15 still requires a real Windows build rerun to expose any later compiler/API/test failures.
