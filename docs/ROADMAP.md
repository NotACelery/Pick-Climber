# Pick Climber — Roadmap

Current release target: **1.1.0**

Gameplay regression baseline carried into 1.1.0: **1.0.3**

## Completed — Phase 0 / architecture

Status: **COMPLETE**

The original monolithic runtime was decomposed before expanding player/world configuration.

Completed boundaries include:

- unified side-effect-free anchor evaluation;
- shared HUD/gameplay feedback facts;
- surface policy through `AnchorSurfaceResolver`;
- authoritative lifecycle/cleanup;
- typed durability service;
- cooldown service;
- split wall/ceiling/impulse/positioning/ticking responsibilities;
- network transport outside the `climb` package;
- thin common/client event adapters;
- data-driven tool/surface/interactive tags;
- Gradle source-quality and architecture gates.

Phase 0 is historical architecture work, not a separately published gameplay release.

## Completed — 1.1.0 Player Customization & Runtime Control

Status: **COMPLETE / RELEASE CANDIDATE**

Implemented:

- configurable in-world options keybind, default `K`;
- dedicated Pick Climber options screen;
- `Contextual / Always / Off` indicator mode;
- `String / Pickaxe` indicator styles;
- 50%–200% icon sizing;
- independent icon/box transparency;
- independent Muted/Normal/Neon intensity;
- Indicator Box toggle;
- Show Unclimbable;
- failure-message toggle;
- Reset to Defaults;
- per-player runtime hot-disable;
- safe detach when hot-disabled while attached;
- protocol 14 runtime preference sync;
- versioned client config with migration/normalization;
- responsive two-column/one-column options layout;
- vertical scrolling on short screens;
- bounded preview rendering;
- eight localization files;
- shader isolation preserved for Jade/other overlays.

The release intentionally does **not** change the validated 1.0.3 physics/balance model except previously documented fixes
found during the architecture audit.

## Next — 1.2.0 Mapmaker Rules / Climbing Rules Jukebox

Status: **PLANNED AFTER 1.1.0**

### Creative-only control block

Add a white/gray jukebox-style map-authoring block.

Requirements:

- Creative-only acquisition.
- No survival recipe.
- Custom GUI.
- Server-authorized editing.
- Recommended permission: Creative + permission level >= 2.
- It may visually reference a jukebox/record player without inheriting vanilla music behavior.

### Paper -> Climbing Rules Card

Insert paper and choose:

- Create New Card.
- Import JSON.

The resulting Climbing Rules Card stores a validated rule profile.

Existing cards can be inserted to edit, apply or export them.

The station also needs:

- eject/remove card;
- restore world Pick Climber defaults;
- clear the currently applied profile with confirmation.

### Block rule editor

Tabs:

- Stable / Climbable.
- Unstable.
- Unclimbable.

Tools:

- search;
- scrollable creative-style block grid;
- multi-select;
- Select All Visible;
- Clear Selection;
- Set Stable;
- Set Unstable;
- Set Unclimbable;
- Restore Selected to Default.

Only reasonable anchor candidates should be catalogued by default. Cache the catalog for large modpacks.

### Unlisted policy

Profile option:

- `Unclimbable` for strict maps;
- `Use Pick Climber Defaults` for partial overrides.

### Rule precedence

Without an active world profile:

```text
unclimbable tag > unstable tag > stable tag > fallback
```

With a profile:

```text
explicit unclimbable > explicit unstable > explicit stable > unlisted policy
```

When the unlisted policy is `Use Pick Climber Defaults`, delegate through `AnchorSurfaceResolver`.

### Enchantment invariant

World rules may change the base surface classification only.

They must not bypass:

- Strong Grip ceiling requirement;
- Sturdy Latch unstable behavior;
- collision validation;
- reach validation;
- tool identity;
- cooldown rules.

### Durability rule

Initial world-profile option:

**Pickaxe durability multiplier** — default `100%`.

Apply centrally through `ToolWearService` while preserving vanilla `hurtAndBreak` / Unbreaking behavior.

Do not expose arbitrary physics/cooldown tuning in the first mapmaker release.

### Player mining restriction

Profile rule:

**Player Mining: Enabled / Disabled**

When disabled, prevent normal player-driven block breaking in the map-rules context.

Do not claim this blocks every modded drill, explosion or automation unless explicitly supported.

Placement and unrelated interactions remain unchanged unless later features add separate restrictions.

### Apply / restore

`Apply to World` copies the validated profile into persistent server/world data.

Applied rules must survive:

- restart;
- player reconnect;
- distribution of the map without the original external JSON.

Ejecting the physical card does not erase an already applied world profile.

`Restore World Defaults` must atomically restore:

- normal tag/fallback classification;
- durability multiplier 100%;
- Player Mining enabled;
- no active card overrides.

### JSON import/export

JSON remains the canonical portable profile format.

Requirements:

- user-chosen profile/file name;
- sanitized filenames;
- no arbitrary paths/traversal;
- `format_version`;
- resource-location validation;
- enum/range/conflict validation;
- import into editor before applying;
- missing mod IDs must remain recoverable;
- advanced tag support only if it can stay understandable.

### Persistence / authority

- active rules are server/world authority;
- use persistent world data;
- editing/applying requires mapmaker/admin authority;
- clients receive only synchronized state required for gameplay/UI.

### 1.2.0 QA

Validate:

- card creation/import/export;
- filename sanitation;
- editor flow;
- block classification;
- unlisted policies;
- durability multiplier across every wear path;
- Unbreaking;
- mining restriction;
- Strong Grip/Sturdy Latch invariants;
- restart/map-transfer persistence;
- restore defaults;
- missing mod IDs;
- multiplayer permissions;
- full 1.1.0 regression with no active world profile.

## Explicitly deferred beyond 1.2.0

Do not expand the first mapmaking release with these unless a real requirement appears:

- regional rule zones;
- overlapping rule stations;
- per-dimension cards;
- redstone rule swapping;
- arbitrary physics tuning;
- placement restrictions;
- explosion/automation protection;
- command scripting/objectives;
- card inheritance/composition.
