# Pick Climber 1.2.0-dev.17 — Viewer + Duplicate pass

## Baseline

`1.2.0-dev.16` was confirmed by the user with a successful Windows `clean build` and is the accepted stable/build-clean
baseline for this pass.

## Rule Book Viewer

- Right-click use of a valid normal Climbing Rule Book opens a client read-only viewer.
- Normal block interaction keeps engine priority; the viewer is item-use fallback behavior.
- Tabs: Overview, Stable, Unstable, Unclimbable.
- Block tabs reuse `BlockCatalogService` and Search semantics.
- Search now also matches the full `namespace:path` ResourceLocation.
- Missing registry IDs are naturally absent from the catalog and remain untouched in portable data.
- No authoring controls or network mutations exist in the Viewer.
- Escape and the actual configured Inventory key both close the Viewer.

## Duplicate

Rules Table adds a Duplicate sub-screen:

```text
[Source Rule Book x1] + [Book xN] + [optional Dye xN] -> [Rule Book xN]
```

Server rules:

- source is validated and never consumed;
- copies are 1..64;
- material Books consumed exactly equal output count;
- no Dye means source cover is inherited;
- same-color Dye is not consumed;
- different Dye recolors every output and requires/consumes one Dye per copy;
- name/profile/activation/scope/configured duration are cloned unchanged;
- client preview is advisory only; server rereads all Table slots;
- output first enters player inventory; remaining overflow is dropped safely at the player.

## Next acceptance

Run the root-ready dev.17 through `build.bat`. dev.16 is known build-clean; dev.17 contains new code and must earn the
same status before becoming the next baseline.
