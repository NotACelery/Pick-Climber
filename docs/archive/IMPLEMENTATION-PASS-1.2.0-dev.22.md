# Pick Climber 1.2.0-dev.22 — Authoring UX / placement correctness

## Scope

This pass reconstructs and completes the interrupted dev.22 authoring/UX work on top of the full dev.21 source.

### Rules Table

- Furnace-style horizontal placement remains player-facing; approved top artwork is rotated to match the logical front.
- One work slot accepts one vanilla Book or one Climbing Rule Book, including natural single-item extraction from a carried stack.
- Vanilla Book actions: Create, Import JSON, Import Current Rules.
- Valid Rule Book actions: Edit, Clone/Dye Processing, Export, Clear Rules.
- Clearing a Rule Book converts it back to `minecraft:book`; empty Rule Books are not persisted.

### Rule Book Processing

Separate anvil-style menu: Rule Book(s) + Book(s)/Dye(s) -> Result.

- Clone keeps the source and consumes one vanilla Book per output.
- Dye consumes one Rule Book plus one Dye per output.
- Dye changes only `cover_color`; all rule semantics remain identical.
- Normal output clicks process one result; Shift-click processes the maximum safe amount.

### Pickaxe Wear

`pickaxe_wear` is flat durability damage, not a percentage. Range 0..100, default 15.

- Wall/standard configurable attach interactions use the configured value.
- Ceiling attach stays fixed at 20.
- Ceiling sustained wear stays fixed at 1 per second.
- Legacy `durability_multiplier` input remains readable and migrates to flat wear.

### Editor

- 10-column catalog with visible draggable scrollbar and mouse wheel support.
- Only default states with full collision and no BlockEntity are offered in the catalog.
- Missing mod IDs remain portable but hidden from the live catalog.
- Wear uses a 0..100 slider with visible value and Reset 15.
- Temporary duration is explicitly labelled in seconds.
- Layout/status/action labels are redistributed to avoid previous overlap/truncation.

### Rule Dispenser / placement

- Terminal and Dispenser use player look direction/inclination like directional vanilla blocks, independent of clicked face.
- Dispenser top arrow artwork aligns with the actual output facing.
- Mapmaker right-click opens the Dispenser GUI directly.
- GUI exposes Temporary master, 1..60 second transport lifetime and a test-copy button.

## Acceptance state

Static gates pass in the packaging workspace. A Windows `clean build` and in-game verification are required before dev.22
replaces the last accepted build-clean baseline.
