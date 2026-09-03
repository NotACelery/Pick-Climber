# Pick Climber 1.2.0 — Mapmaker Rules

Pick Climber 1.2.0 adds a server-authoritative mapmaking layer while keeping normal 1.1.0 climbing behavior unchanged
when no world profile is active.

## New: Climbing Rules Terminal

A Creative-only control station lets map authors/operators create and manage **Climbing Rules Cards**.

With a Card you can:

- classify blocks as Stable, Unstable or Unclimbable;
- choose how unlisted blocks behave;
- set Pick Climber durability usage from 0% to 500%;
- enable/disable normal player mining;
- apply the profile persistently to the world;
- restore normal Pick Climber defaults;
- import/export portable JSON profiles.

## Visual editor

The new responsive block editor includes search, scrolling, multi-selection, bulk classification and missing-mod ID
preservation for modpack profiles.

## Portable and persistent

Applied rules are copied into the world and survive restarts independently of the original Card or JSON file. Cards can
also travel between worlds/players.

JSON profiles are stored locally under:

```text
config/pickclimber/rules/
```

Importing a JSON opens it for review first and never automatically changes world rules.

## Multiplayer safety

Sensitive mapmaking actions are validated by the server. The default mapmaker permission is Creative + permission level
2. Card revisions and editor sessions prevent stale/concurrent edits from silently overwriting newer changes.

## Compatibility

World profiles change only the intended map-rule layer. Strong Grip, Sturdy Latch, reach, collisions, tool eligibility,
cooldown and existing climbing physics remain intact.

Player Mining restrictions target manual player block breaking only; machines, explosions, commands and automation are
intentionally outside that rule.

## Languages

Full UI coverage remains available for English US/UK, Spanish Chile/Spain/Argentina/Mexico and Portuguese Brazil/Portugal.
