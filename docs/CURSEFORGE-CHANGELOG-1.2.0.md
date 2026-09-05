# Pick Climber 1.2.0 — Mapmaker Climbing Rules

Pick Climber 1.2.0 adds a complete Creative/mapmaker rules workflow while keeping normal climbing unchanged when no custom rules are active.

## Climbing Rule Books

Create portable Rule Books with custom Stable, Unstable and Unclimbable surfaces, flat pickaxe wear, Player Mining, Unmineable Terminals, activation mode, duration, title and dye color. Books can be inspected in a read-only viewer and imported/exported as JSON.

Custom authoring is fail-closed: blocks left unassigned are saved as Unclimbable.

## Climbing Rules Table

A dedicated Creative-only authoring station can create/edit books, import current/default world rules, clone/dye books and manage JSON files. Its final model has a non-full hitbox and correct axe mining behavior.

## Climbing Rules Terminal

Apply Rule Books during gameplay. Supports Permanent WORLD, Temporary WORLD and Temporary PLAYER rules, server-side no-op/conflict validation and immediate anchor revalidation when rules change.

## Climbing Rule Dispenser

Automate Temporary PLAYER book distribution with redstone, a configurable 1-60 second lifetime and optional Start counter on pickup behavior. Ground copies expire with their timer. Players may hold different temporary rule definitions simultaneously, while duplicate active copies of the same mechanical definition are rejected. Each owned temporary book has its own HUD countdown.

## Performance and multiplayer

Rule Books are reference-first: large rule definitions live once in world/server SavedData instead of being copied into every item stack. Rule application, ownership, timers and persistence are server-authoritative.

## Quality of life

- K options and Rule Book viewer no longer pause singleplayer physics.
- Rules Table uses an axe; Terminal and Dispenser use pickaxes.
- JSON supports portable names, overwrite/delete confirmation and missing mod IDs.
- Optional JEI/EMI documentation integration without hard dependencies.
- Localization parity across English, Spanish and Portuguese variants.
