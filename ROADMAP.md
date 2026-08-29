# Pick Climber — Roadmap

Current stable line: **1.0.1**. Completed beta-era implementation history belongs in `CHANGELOG.md`; this file contains only work that is still relevant after the 1.0.x stabilization pass.

## Release validation

- Run the final Java 21 build and test the exact 1.0.1 JAR.
- Complete `TESTING-1.0.1.md`.
- Validate the already implemented remote ceiling-arm pose with two clients, including `F`, wall/ceiling transitions, detach, dimension change, disconnect, and late tracking.
- Recheck Shift + right-click force-anchor behavior on vanilla menu blocks and representative modded machines.

## Compatibility

- Validate a climbing tool added only through `pickclimber:climbing_tools`, without membership in `#minecraft:pickaxes`.
- Validate simultaneous inclusion/exclusion through datapacks; exclusion must win.
- Expand manual compatibility coverage only through tags/normal APIs when possible, without hard dependencies.

## Balance

- Measure actual jump height for Pick Climber I–III in game and revisit the current post-jump authorization window only if measurements justify it.
- Recheck the final anvil-cost budget for Pick Climber / Strong Grip / Sturdy Latch on an endgame pickaxe.
- Evaluate sustained unstable-surface wear only if gameplay testing shows the current durability economy is too forgiving.

## Visual polish

- Evaluate a third-person wall pose equivalent to the existing ceiling presentation.
- Consider subtle tension sounds and material-specific particles after physics and multiplayer QA are fully stable.

## Structural maintenance

`ClimbManager` remains intentionally intact for the 1.0.1 release line. A future structural pass may extract anchor validation, movement/physics, lifecycle/cleanup, and synchronization services using the boundaries documented in `docs/DEVELOPMENT.md`. Do not mix that refactor with physics or balance changes.

## Non-goals

- Do not move movement authority to the client.
- Do not bypass collision or range validation for convenience.
- Do not add hardcoded compatibility lists when tags or normal interaction contracts can express the same rule.
