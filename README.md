# Pick Climber

Pick Climber turns compatible pickaxes into climbing tools for **Minecraft 1.21.1**, **NeoForge 21.1.235** and **Java 21**.

The current public release line is **1.1.0**. This source snapshot is **1.2.0-dev.53**, the Mapmaker Climbing Rules update under final audit and QA.

## Gameplay

Pick Climber keeps the existing 1.1.x movement system intact: wall anchors, wall jumps, fall braking, unstable surfaces, two-pickaxe movement, Strong Grip ceiling movement, Sturdy Latch, client HUD/options and server-authoritative climbing state.

The 1.2.0 rule system does not replace those mechanics. It changes policy through central seams for surface classification, pickaxe wear, mining permission and terminal behavior. With no active map rules, normal Pick Climber defaults remain authoritative.

## 1.2.0 Mapmaker Climbing Rules

The 1.2.0 line adds Creative/mapmaker infrastructure for authoring and applying portable rule sets.

### Climbing Rule Book

A normal Rule Book is a portable reference to a server/world rule definition. The heavy block lists live in world SavedData; the item carries a `definition_id` plus lightweight metadata such as title, cover color, author, activation mode and duration.

Rule Books support:

- custom title and author metadata;
- all 16 vanilla dye colors;
- Stable, Unstable and Unclimbable block classifications;
- flat Pickaxe Wear per standard climbing interaction (`0-100`);
- Player Mining enabled/disabled;
- Unmineable Terminals;
- Permanent WORLD, Temporary WORLD and Temporary PLAYER activation;
- read-only viewer on normal use;
- JSON import/export;
- Clone/Dye processing in the Rules Table;
- missing mod block IDs preserved when imported.

Authoring is **fail-closed**. Any authorable block left without an explicit Stable or Unstable assignment is saved as **Unclimbable**.

### Climbing Rules Table

Creative-only authoring station. A Creative player with permission level 2 may:

- create a Rule Book from a vanilla Book;
- edit an existing Rule Book;
- import Default World Rules or Current World Rules;
- import/export local JSON files;
- clone or recolor a Rule Book;
- restore world defaults.

The final table model uses an orientation-aware non-full-block hitbox and does not occlude the floor between its legs. It is mined efficiently with an **axe**.

### Climbing Rules Terminal

Gameplay application block. It is not an editor.

Using a valid Rule Book on the Terminal asks the server to apply it. Successful applications consume one book. Rejected/no-op applications do not consume it. Permanent rules affect WORLD; temporary rules may affect WORLD or one PLAYER according to the book definition.

When world rules change, active anchors are revalidated immediately. An anchor that becomes invalid is detached instead of remaining attached to a newly Unclimbable surface.

The Terminal uses six-direction placement and is mined efficiently with a **pickaxe**.

### Climbing Rule Dispenser

Creative-only distribution block for temporary PLAYER copies. It accepts any valid current-schema Rule Book as its source and never consumes that source.

- lifetime: `1-60` seconds;
- redstone rising edge dispenses a copy;
- manual test dispense is available from its GUI;
- `Start counter on pickup` optionally delays the gameplay timer until first pickup;
- otherwise the timer starts when the copy is dispensed and the ground item expires with it;
- after pickup, dropping the book does not pause its timer;
- multiple **different** temporary definitions may coexist in one player's inventory;
- another active copy of the **same mechanical definition** is rejected;
- each owned temporary book has its own HUD countdown;
- temporary books are owner-bound, vanish on death and are validated server-side.

The Dispenser is mined efficiently with a **pickaxe**.

## Rule priority

Effective gameplay policy resolves as:

```text
Temporary PLAYER
    -> WORLD rules (temporary or permanent)
    -> Pick Climber defaults
```

Permanent PLAYER rules do not exist in 1.2.0.

## Options and HUD

The default Pick Climber options key is `K`. Client options include indicator presentation, visibility and local runtime toggles. Opening the K menu or the Rule Book viewer does **not** pause singleplayer; physics continue like an inventory screen.

Temporary rules and owned Temporary Rule Books use countdown HUD text above the hotbar/event area.

## JSON files

Local Rule Book files live under:

```text
config/pickclimber/rules/
```

Exports use `*.rules.json`. The internal Rule Book title is independent from the filesystem filename. See [`docs/RULE-BOOK-JSON.md`](docs/RULE-BOOK-JSON.md).

## Optional integrations

JEI and EMI integrations are compile-only/optional. Pick Climber must load normally when neither is installed. The integrations document the Creative-only mapmaker workflow without giving survival recipes to the protected rule infrastructure.

## Languages

The source keeps exact key parity for:

- `en_us`, `en_gb`;
- `es_cl`, `es_es`, `es_ar`, `es_mx`;
- `pt_br`, `pt_pt`.

## Build

Use Java 21. On Windows:

```bat
build.bat
```

On Linux/macOS/WSL:

```bash
./build.sh
```

The scripts download Gradle 9.2.1 into local `.gradle-dist/` when needed. `.gradle-dist/`, `.gradle/`, `build/` and run folders are generated local state and are intentionally excluded from development snapshots.

A release candidate must pass:

```text
clean build
check
```

`check` includes source quality, architecture boundaries, localization parity, Rule-system integrity and optional integration guards.

## Documentation

Current documentation starts at [`docs/INDEX.md`](docs/INDEX.md).

The operational remaining work for 1.2.0 is [`docs/WAITLIST.md`](docs/WAITLIST.md). Historical implementation passes are kept under `docs/archive/` and are not current specifications.
