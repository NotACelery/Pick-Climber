# Pick Climber 1.2.0-dev.21 — Optional JEI / EMI Integration Pass

## Baseline

- dev.20 was confirmed with `clean build` SUCCESS on Windows and is the stable baseline for this pass.
- dev.21 is not promoted until its own external build succeeds.

## JEI

- Target API: JEI `19.32.0.359` for Minecraft 1.21.1.
- Plugin lives only in `integration/jei`.
- One synthetic `Climbing Rules` category exposes two documentation relations.
- Authoring shows Book + Rules Table -> current-schema White Rule Book.
- Application shows Rule Book -> Rules Terminal while keeping the Terminal semantically a catalyst.
- Rules Table and Rules Terminal are registered as category catalysts.

## EMI

- Target API: EMI `1.1.24+1.21.1` NeoForge.
- Plugin lives only in `integration/emi`.
- The same two relations are exposed with slots/icons and no long explanatory page.
- Synthetic relations opt out of recipe trees and craftable behavior.
- Table and Terminal are registered as workstations.

## Optional-loading boundary

- JEI and EMI APIs are declared `compileOnly`.
- No loader dependency is added to `neoforge.mods.toml`.
- Pick Climber core never imports either external API.
- `verifyOptionalIntegrations` rejects accidental external-API leakage or non-compileOnly dependency declarations.

## Remaining implementation before feature freeze

- Replace the already-separated Dispenser UP/DOWN front texture with the centered-aperture final art.
- Runtime JEI-only / EMI-only / both / absent checks remain QA, not implementation.
