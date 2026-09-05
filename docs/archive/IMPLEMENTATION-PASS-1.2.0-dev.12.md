# Pick Climber 1.2.0-dev.12 — implementation pass

## Scope

This pass completes the 1.2.0 Climbing Rule Dispenser / Temporary Rule Book transport path and fixes the first real
Gradle 9.2.1 Configuration Cache failure reported by an external build of dev.11.

## Build gate correction

The external dev.11 build reached Gradle 9.2.1 and NeoForge artifact preparation, then failed in
`verifyRulesIntegrity` because `file(...)` was invoked from a `doLast` Groovy closure while Configuration Cache was
serializing task actions.

The retired Terminal-admin file paths are now materialized during configuration via `layout.projectDirectory.file(...)`
and the execution action only consumes ordinary `File` instances. Configuration Cache remains enabled; the failure is
not hidden with a command-line bypass.

A new real build is still required to prove the fix and reach `compileJava`/tests.

## Climbing Rule Dispenser

Materialized:

- Creative-tab block/item registration;
- Survival break protection, Creative break allowed;
- six-direction `FACING` state with rotate/mirror support;
- BlockEntity with one master slot;
- master slot max stack 1;
- only validated normal Rule Books with `TEMPORARY` activation are accepted;
- master is never consumed by issuance;
- master and transport lifetime persist in BlockEntity NBT;
- mapmaker config menu/screen opened with Shift + right-click;
- normal right-click attempts per-player issuance;
- server-authoritative transport lifetime `1..60` seconds, default `30`;
- menu data-slot synchronization and permission-checked lifetime payload.

The current block model is a functional white/gray model using vanilla textures. Final original visual assets remain in
the visual-assets WAITLIST.

## Temporary Rule Book transport

Materialized runtime item data:

- copied final-schema Temporary Rule Book definition;
- owner UUID;
- issuance UUID/token;
- absolute `expiresAtGameTime`;
- source dimension;
- source dispenser BlockPos;
- inherited cover color through the copied definition.

Behavior:

- separate runtime item, max stack 1;
- absent from Creative tab;
- fixed localized `Temporary Rule Book` name;
- cannot enter the Rules Table authoring slot, therefore cannot be edited/duplicated/exported there;
- owner-only pickup via NeoForge pickup pre-event;
- owner-only Terminal use;
- one pending issuance per player while other players remain independent;
- dispenser rejects issuance when the requested rules are already mechanically effective for that player/scope;
- spawn is projected from the dispenser `FACING` direction and targeted to the owner;
- expiry works while held/in inventory through server sanitation and on ground through the item-entity hook;
- death removes owned transport books and releases issuance;
- destroyed ItemEntity releases issuance;
- successful Terminal use releases issuance immediately;
- stale/foreign possession is removed safely;
- issuance registry is intentionally runtime-only, avoiding permanent locks after restart;
- reconnect while the server/issuance remains alive resynchronizes the Book countdown.

`Persist on Pickup` is deliberately NOT part of this pass and remains a 1.3.0 feature.

## HUD

The timer overlay now supports the final 1.2 combinations:

```text
Rules only -> 0:42
Book only  -> 0:18
Both       -> Rules 0:42
              Book  0:18
```

Rules/Book labels are localized across all eight supported locales. No chat countdown is emitted.

## Verification performed in packaging workspace

Static gates/equivalents pass for:

- Java line length/tabs/trailing whitespace/debug markers;
- JSON validity;
- public class / filename alignment;
- localization parity: 156 keys in each of eight locales;
- no missing active UI translation keys;
- no active Card/Jukebox identity in source/resources;
- no direct packet transport from Rules screens;
- no `climb -> rules implementation` or `climb -> network` coupling;
- `ToolWearService` remains the only `hurtAndBreak` owner;
- Rules infrastructure still has no protected survival recipe/loot;
- Temporary Rule Book remains absent from Creative;
- no project-level `file(...)` invocation remains inside build-script `doLast` closures.

A dependency-free `javac` smoke check also passes for the new Temporary Rule Book client state. These checks do not
replace a real NeoForge build.
