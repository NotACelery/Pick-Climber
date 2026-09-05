# Pick Climber 1.2.0 — Final QA Matrix

Use the exact release-candidate source/JAR. Record Minecraft, NeoForge, Java, OS, GUI scale and optional mods.

## Build / startup

- `clean build` succeeds.
- Client reaches title screen and joins a world.
- Dedicated server starts and accepts clients.
- Launch succeeds without JEI/EMI and with supported optional viewer combinations.

## Base climbing regression

- Wall attach/detach, transfer, wall jump and fall braking.
- Unstable surfaces with/without Sturdy Latch.
- Strong Grip ceiling attach/swing/detach.
- Interactive-block suppression/Shift behavior.
- K options and indicators.

## Rule Book editor

- Create from vanilla Book.
- Edit existing Rule Book.
- ALL-tab unassigned cell saves as Unclimbable.
- Stable/Unstable tab removal saves as Unclimbable.
- Search, tabs, Select Visible, Clear Selection and Restore Selected.
- Pickaxe Wear `0`, `15`, `100`.
- Player Mining and Unmineable Terminals.
- Permanent WORLD; Temporary WORLD; Temporary PLAYER + duration.
- Save returns to Table; Exit Without Saving leaves source unchanged.

## Table / processing / JSON

- Table hitbox follows model and floor remains targetable between legs.
- No dark solid-block lighting artifacts.
- Axe is the efficient mining tool.
- Clone and Dye preview/result/take behavior.
- Default World Rules and Current World Rules import.
- Disk import lists internal titles.
- Delete and overwrite confirmations.
- Open rules folder button.
- JSON roundtrip and missing mod ID preservation.

## Terminal

- All six orientations.
- Pickaxe mining.
- Successful apply consumes exactly one book.
- Invalid/no-op/conflict application does not consume.
- Permanent WORLD, Temporary WORLD and Temporary PLAYER behavior.
- Unmineable Terminals vs mapmaker bypass.

## Dispenser / Temporary Rule Books

- GUI accepts a valid Rule Book source.
- Slider drag and `1-60 s` persistence.
- Redstone rising edge emits once; steady power does not spam.
- Timer starts at dispense when Start counter on pickup is off.
- Ground copy expires at absolute timer end.
- Start counter on pickup on: configured lifetime starts at first pickup.
- Drop after pickup does not reset/pause timer.
- Two different definitions can be owned simultaneously.
- Same definition cannot be owned twice simultaneously.
- Each owned book displays an independent HUD timer.
- Consuming/expiring one does not invalidate the other.
- Wrong player cannot use an owner-bound copy.
- Death clears owned copies.

## Runtime authority

- PLAYER overrides WORLD.
- Temporary expiry restores correct baseline.
- WORLD change clears PLAYER overlays.
- Stable -> Unclimbable hot replacement immediately detaches an affected anchor.
- Player Mining applies to the effective profile.

## UI / presentation

Test at small window and multiple GUI scales:

- K menu and Rule Book viewer do not pause physics.
- Editor controls remain reachable.
- Table/Dispenser inventories and stack counts stay within panels.
- Timer text does not overlap selected-item text or event feedback.
- Rule Book dyes remain legible.
- No z-fighting/missing textures on Table, Terminal, Dispenser or books.

## Performance

Compare the same scene before/after carrying Rule Books and while looking at a Table containing one. The reference-first architecture must not reproduce the historical severe FPS collapse caused by embedding full rule lists in item stacks.

## Release acceptance

All P0 items in `WAITLIST.md` must be closed. Any P1 failure that changes gameplay, persistence, multiplayer authority, filesystem safety or normal presentation blocks release.
