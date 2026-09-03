# Pick Climber 1.2.0-dev.8 — Final QA Matrix

Record every result against the **exact dev.8 JAR**. A section is not accepted from source inspection alone.

Recommended result notation:

```text
PASS / FAIL / BLOCKED
```

## A. Build and startup

- [ ] Java 21 selected.
- [ ] `clean build` succeeds.
- [ ] JUnit tests pass.
- [ ] `verifySourceQuality` passes.
- [ ] `verifyArchitectureBoundaries` passes.
- [ ] `verifyLocalizationParity` passes.
- [ ] `verifyRulesIntegrity` passes.
- [ ] Client starts to title screen.
- [ ] Client joins a world.
- [ ] Dedicated server starts with the mod installed.
- [ ] Client joins dedicated server.

## B. Baseline regression — no active profile

Confirm 1.1.0 behavior:

- [ ] normal wall anchor;
- [ ] climbing boost;
- [ ] wall jump;
- [ ] fall braking;
- [ ] unstable descent;
- [ ] Sturdy Latch stable conversion;
- [ ] Strong Grip ceiling attach;
- [ ] ceiling swing;
- [ ] two-pickaxe behavior;
- [ ] hand transfer with F;
- [ ] detach;
- [ ] Shift force-anchor;
- [ ] range/collision checks;
- [ ] cooldown;
- [ ] normal 100% durability;
- [ ] Unbreaking;
- [ ] external climbing tool tags;
- [ ] surface tags;
- [ ] interactive blocks;
- [ ] Jade/overlays retain their colors;
- [ ] Pick Climber Options keybind/menu;
- [ ] hot-disable;
- [ ] HUD modes/styles/settings.

## C. Terminal access and slot

- [ ] Climbing Rules Terminal appears in Pick Climber creative tab.
- [ ] No survival recipe exists.
- [ ] Terminal/Card names never show “Jukebox”.
- [ ] Terminal model/textures load without missing-texture purple/black.
- [ ] Survival/non-admin cannot administer Terminal.
- [ ] Creative non-op cannot administer Terminal.
- [ ] Creative permission-level-2 player can administer Terminal.
- [ ] Paper inserts.
- [ ] Card inserts.
- [ ] unrelated items are rejected.
- [ ] slot persists save/reload.
- [ ] breaking Terminal does not duplicate its contents.
- [ ] eject goes to inventory or safely drops if inventory is full.

## D. Card lifecycle

- [ ] Paper -> Create New Card.
- [ ] New Card profile name is correct/localized.
- [ ] Card stack size is one.
- [ ] Card survives relog.
- [ ] Card tooltip is valid.
- [ ] Card copied/moved between players preserves profile.
- [ ] Card copied/moved between worlds preserves profile.
- [ ] removing/ejecting a Card does not change already-applied world rules.
- [ ] malformed Card does not crash client/server.
- [ ] malformed Card cannot be applied.
- [ ] missing mod ResourceLocation is preserved through edit/save/export.

## E. Surface editor

Test Stable, Unstable and Unclimbable with at least vanilla blocks and modded blocks.

- [ ] block catalog loads once and remains responsive;
- [ ] localized-name search;
- [ ] namespace search;
- [ ] ResourceLocation path search;
- [ ] grid scroll;
- [ ] tab changes;
- [ ] multi-select persists appropriately;
- [ ] Select Visible selects only current visible page;
- [ ] Clear Selection;
- [ ] Set Stable;
- [ ] Set Unstable;
- [ ] Set Unclimbable;
- [ ] moving classification removes previous classification;
- [ ] duplicate classifications cannot survive validation;
- [ ] Restore Selected to Default removes explicit override;
- [ ] missing IDs remain present but reported unavailable.

## F. Surface runtime rules

- [ ] explicit Stable gives firm normal wall behavior.
- [ ] explicit Unstable gives unstable semantics.
- [ ] explicit Unclimbable rejects anchoring.
- [ ] Unlisted Defaults delegates to baseline tags/fallback.
- [ ] Unlisted Unclimbable rejects unlisted surfaces.
- [ ] Stable ceiling without Strong Grip is still rejected.
- [ ] Unstable ceiling with Strong Grip but no Sturdy Latch still follows enchantment requirements.
- [ ] collision cannot be bypassed by Stable classification.
- [ ] reach cannot be bypassed by Stable classification.
- [ ] tool eligibility cannot be bypassed by rules.

## G. Apply / Restore / persistence

- [ ] Apply Card changes world runtime.
- [ ] applying a different active profile asks for confirmation.
- [ ] applied profile survives save/quit.
- [ ] applied profile survives dedicated-server restart.
- [ ] applied profile survives player logout/login.
- [ ] applied profile works after dimension travel.
- [ ] copying the world retains applied rules.
- [ ] destroying/ejecting source Card leaves applied rules active.
- [ ] Restore World Defaults asks for confirmation.
- [ ] Restore clears active profile.
- [ ] Restore returns surface behavior to baseline.
- [ ] Restore returns durability to 100%.
- [ ] Restore returns Player Mining to enabled.
- [ ] all connected clients receive Apply/Restore state.

## H. Durability multiplier

Run representative wall/ceiling/braking paths at:

- [ ] 0%;
- [ ] 50%;
- [ ] 100%;
- [ ] 150%;
- [ ] 200%;
- [ ] 500%.

Validate wear reasons:

- [ ] wall attach;
- [ ] ceiling attach;
- [ ] climbing boost;
- [ ] braking support attach;
- [ ] braking block/distance wear;
- [ ] sustained ceiling wear.

Additional:

- [ ] 50% repeated 1-point costs accumulate rather than staying free forever;
- [ ] changing profiles does not resurrect fractional carry from an old policy revision;
- [ ] Unbreaking still works through vanilla damage semantics;
- [ ] rejected anchor attempts spend no durability.

## I. Player Mining

With mining Enabled:

- [ ] Survival breaks normally.
- [ ] Creative breaks normally.

With mining Disabled:

- [ ] Survival manual breaking blocked.
- [ ] Creative non-admin manual breaking blocked.
- [ ] Creative permission-level-2 mapmaker can break.
- [ ] main-hand left-click detach while anchored still works.
- [ ] placement remains allowed.
- [ ] right-click interactions remain allowed.
- [ ] Create drill/representative automation remains intentionally unaffected.
- [ ] explosion remains intentionally unaffected.
- [ ] command-driven block change remains intentionally unaffected.

## J. JSON export/import

- [ ] export valid Card.
- [ ] exported JSON has `format_version: 1`.
- [ ] exported profile round-trips identically.
- [ ] custom filename works.
- [ ] invalid filename rejected/sanitized.
- [ ] `..` traversal rejected.
- [ ] `/` path injection rejected.
- [ ] `\\` path injection rejected.
- [ ] reserved names rejected.
- [ ] existing file requires overwrite confirmation.
- [ ] import file listing works.
- [ ] import valid JSON from Paper.
- [ ] import opens editor.
- [ ] import does not auto-apply.
- [ ] malformed JSON rejected without crash.
- [ ] unsupported format version rejected.
- [ ] invalid ResourceLocation rejected.
- [ ] duplicate classification conflict rejected.
- [ ] out-of-range durability rejected.
- [ ] missing mod IDs import and re-export intact.

## K. Multiplayer/security

Use at least two clients.

- [ ] admin can create/edit/apply/restore.
- [ ] non-admin cannot spoof create/edit/apply/restore payloads.
- [ ] too-far Terminal action rejected.
- [ ] wrong Terminal position rejected.
- [ ] missing/removed Terminal rejected.
- [ ] Card changed while editor open -> stale save rejected.
- [ ] same-revision but different Card replacement -> stale save rejected.
- [ ] stale rejection reloads latest Card into editor.
- [ ] two admins editing same Card: second stale writer cannot overwrite first.
- [ ] editor session invalid after dimension change.
- [ ] editor session invalid after respawn.
- [ ] editor session invalid after logout/reconnect.
- [ ] Card handoff to another player does not grant stale editor authority.
- [ ] reconnect receives current active world profile.

## L. UI/responsiveness

Test multiple GUI scales and both resized/windowed and normal resolutions.

Terminal:

- [ ] title/status readable;
- [ ] Paper flow buttons visible;
- [ ] Card flow buttons visible;
- [ ] world status visible;
- [ ] player inventory/hotbar fully reachable;
- [ ] no overlap in short windows.

Editor:

- [ ] wide layout works;
- [ ] narrow layout works;
- [ ] narrow page scroll reaches every control;
- [ ] grid scroll remains independent;
- [ ] no control escapes screen bounds;
- [ ] resizing preserves draft and selection;
- [ ] Done returns to Terminal;
- [ ] stale reload remains in editor with correct parent.

## M. Localization

For each locale:

```text
en_us en_gb es_cl es_es es_ar es_mx pt_br pt_pt
```

- [ ] Terminal strings translated;
- [ ] editor strings translated;
- [ ] Card tooltip translated;
- [ ] mining messages translated;
- [ ] JSON messages translated;
- [ ] permission/session/stale messages translated;
- [ ] no raw translation keys shown.

## N. Release decision

Only mark dev.8 accepted when every release-blocking item above is PASS or has a documented intentional exception.

After acceptance:

- [ ] set `mod_version=1.2.0`;
- [ ] clean build exact final source;
- [ ] smoke exact final client JAR;
- [ ] smoke exact final dedicated-server JAR;
- [ ] tag exact commit `1.2.0`;
- [ ] publish.
