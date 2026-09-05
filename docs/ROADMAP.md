# Pick Climber — Roadmap

Current source: **1.2.0-dev.53**.

## 1.2.0 — Mapmaker Climbing Rules

### Release definition

1.2.0 is complete when mapmakers can author, inspect, transport and apply climbing rules without changing base Pick Climber gameplay when no rules are active.

### Implemented

- [x] Rule Book format 2 and mechanical profile format 1.
- [x] Reference-first Rule Books backed by world `RuleDefinitionLibrarySavedData`.
- [x] Stable / Unstable / Unclimbable authoring.
- [x] Fail-closed custom save: unassigned authorable blocks -> Unclimbable.
- [x] Flat Pickaxe Wear, Player Mining and Unmineable Terminals.
- [x] Climbing Rules Table create/edit/import/export/clone/dye/default restore workflow.
- [x] Read-only Rule Book viewer.
- [x] Permanent WORLD, Temporary WORLD and Temporary PLAYER runtime.
- [x] No-op/conflict/refresh handling.
- [x] Anchor revalidation after effective rule changes.
- [x] Rule Dispenser redstone/manual temporary issuance.
- [x] Ground expiry and optional Start counter on pickup.
- [x] Multiple different temporary books, duplicate same-definition prevention and per-book HUD timers.
- [x] JSON filesystem hardening and missing mod ID preservation.
- [x] Optional JEI/EMI integration boundaries.
- [x] Final Rules Table model, orientation, hitbox, lighting and mining tool.
- [x] Terminal/Dispenser pickaxe tags; Table axe tag.
- [x] Non-pausing K options and Rule Book viewer.
- [x] 8-locale key parity.
- [x] Automated source/architecture/localization/rules/integration gates.

### Remaining before release

No new 1.2.0 gameplay feature is planned. Remaining work is acceptance and release hardening:

- [ ] external Windows `clean build` for dev.53;
- [ ] unit/custom gate results from the exact dev.53 tree;
- [ ] singleplayer regression run;
- [ ] dedicated server + two-player multiplayer run;
- [ ] Temporary Rule Book concurrency/drop/death/redstone matrix;
- [ ] JSON filesystem QA;
- [ ] high/small GUI-scale QA;
- [ ] JEI/EMI present and absent launch QA;
- [ ] final Rule Book/Terminal/Dispenser visual pass;
- [ ] FPS benchmark with Rule Books before/after pickup;
- [ ] release candidate, final changelog and publication.

Exact actionable items live in `WAITLIST.md`.

## 1.3.0 — Dimensional Rules

Deferred until 1.2.0 is released. Do not pull these features into 1.2.0.

Planned direction:

- global profile plus per-dimension overrides;
- dynamic discovery of vanilla and modded dimension IDs;
- preservation of missing dimension IDs;
- Rule Book schema migration for dimensional data;
- dimension-aware editor/viewer;
- runtime resolution `PLAYER -> dimension WORLD override -> global WORLD -> defaults`;
- cross-dimension temporary-rule behavior and revalidation;
- advanced default classifier/cache evaluation.

Out of scope unless explicitly reconsidered: checkpoints, run manager, lives, objectives, teleport routing, region editor, scoreboard framework and redstone rule networks.
