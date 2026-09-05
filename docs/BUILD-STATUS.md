# Pick Climber 1.2.0-dev.53 — Build / Acceptance Status

## Current source

`gradle.properties` declares `1.2.0-dev.53` for Minecraft 1.21.1 / NeoForge 21.1.235 / Java 21.

This audit pass removes obsolete development artifacts, refreshes current documentation, adds an offline audit tool, restores same-definition Temporary Rule Book duplicate prevention and renders independent timers from the player's actual inventory.

## Runtime acceptance inherited from recent QA

Explicitly confirmed in-game during the current sequence:

- Rules Table final geometry/visuals at dev.48;
- Rules Table hitbox and lighting at dev.49;
- ALL-tab unassigned save behavior at dev.50.

Changes introduced after that confirmation still require exact-tree smoke testing:

- dev.51 non-pausing Options/Rule Book viewer;
- dev.52 axe/pickaxe mineable tags;
- dev.53 audit cleanup + temporary duplicate/timer corrections.

## Static audit dev.53

The packaging environment can inspect source but cannot resolve `services.gradle.org`, so it cannot execute Gradle or claim build-clean status.

Offline/static acceptance for dev.53 must report:

- no Java tabs/trailing whitespace;
- no Java lines over 120 characters;
- no TODO/FIXME/HACK/direct debug markers;
- valid JSON/mcmeta;
- exact localization key parity across 8 locales;
- retired tombstone/payload/fractional-wear artifacts absent;
- current docs synchronized to dev.53.

Use:

```text
python tools/audit-source.py
```

## External acceptance required

Run the exact source on the normal Windows Java 21 environment:

```text
build.bat
```

The expected Gradle build includes `check`, which owns:

- `verifySourceQuality`
- `verifyArchitectureBoundaries`
- `verifyLocalizationParity`
- `verifyRulesIntegrity`
- `verifyOptionalIntegrations`
- JUnit tests

Do not call dev.53 build-clean until that external build returns SUCCESS.

After build acceptance, execute `TESTING-1.2.0.md`. Remaining runtime work is QA/release hardening, not planned new 1.2.0 features.
