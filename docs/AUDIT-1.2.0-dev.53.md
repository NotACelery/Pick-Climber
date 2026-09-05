# Pick Climber 1.2.0-dev.53 — Source Audit

## Scope

Repository-wide cleanup of the dev.52 tree before 1.2.0 feature freeze: runtime source, tests, resources, tooling and active documentation.

## Removed dead development code

- `ClimbingRuleBookDuplicateScreen` compatibility tombstone.
- Unregistered `DuplicateRuleBookPayload`.
- Unregistered `EjectRuleBookPayload`.
- Unused fractional-wear `ToolWearState`.
- Unused fractional-wear `ToolWearMath` and its obsolete test.

Flat Pickaxe Wear remains the current 1.2.0 behavior; only legacy codec migration from `durability_multiplier` remains intentionally.

## Removed stale resources/tooling

- four superseded Rules Table textures no longer referenced by the final user-authored model;
- unused `climbing_rule_book_backing.png`;
- dev.46-only asset preparation PowerShell script.

The empty Rules Table model also no longer declares the unused open-book texture slot.

## Correctness findings fixed during audit

The audit found that two previously intended Temporary Rule Book behaviors were not present in the current dev.52 tree:

1. same-definition duplicate ownership prevention;
2. independent HUD timers for multiple different temporary books.

Dev.53 restores both. Multiple different definition IDs remain allowed; a second active copy of the same mechanical definition is rejected. The HUD enumerates owned inventory books and sorts their independent timers by expiry.

## Formatting / code hygiene

Current source is normalized to the repository `.editorconfig` conventions. Runtime comments were kept only for non-obvious invariants/compatibility behavior.

Offline audit result:

- 200 main Java files;
- 20 Java test files;
- 0 tabs/trailing whitespace violations;
- 0 Java lines over 120 characters;
- 0 TODO/FIXME/HACK/direct debug markers;
- all JSON/mcmeta parse successfully;
- exact key parity across all 8 locale files;
- simulated architecture/rules/optional integration boundary checks: 0 violations.

## Build limitation

This packaging environment cannot resolve `services.gradle.org`, so Gradle did not run. The exact dev.53 source still requires an external Java 21 `clean build` before it can be called build-clean.

## Documentation cleanup

Active docs were rewritten against dev.53 behavior. Historical implementation passes/audits/testing notes were moved under `docs/archive/`. The remaining 1.2.0 scope is now acceptance/QA/release hardening only and is listed in `WAITLIST.md`.
