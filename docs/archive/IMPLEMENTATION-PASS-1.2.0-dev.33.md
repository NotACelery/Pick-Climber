# Pick Climber 1.2.0-dev.34 — reference-only transport completion

This pass completes the remaining architecture slice after dev.32.

## Completed

- Temporary Rule Books are now reference-only too. Their ItemStack stores `definition_id`, owner/token/expiry/source data
  and compact display metadata; it no longer embeds the full block profile.
- The Dispenser registers the source gameplay profile once in the world/server Rule Definition Library and emits a
  lightweight Temporary Rule Book pointing to that definition.
- The Rules Terminal resolves Temporary Rule Book definitions server-side from the world library before applying them.
- World/PLAYER rule applications register their effective gameplay definition in the same library, keeping active rules
  resolvable by ID across normal world play.
- Same-world multiplayer transfer requires no client file transfer: dropped/picked Rule Books keep a world-owned
  definition ID and the receiving player resolves/viewers it through the authoritative server registry.
- Personal `config/pickclimber/rules` remains explicit-only. Joining a server or picking up another player's book does not
  write JSON files to the user's personal library.
- The editor no longer exposes an Unlisted-policy toggle. Custom saves are always fail-closed (`UNCLIMBABLE`) for anything
  not explicitly Stable/Unstable, while unresolved ResourceLocation entries already present in imported JSON remain
  preserved by the profile codec.
- Added Rule Definition ID regression tests: display title does not alter gameplay identity; mechanical changes and
  unknown/missing-mod IDs do.

## Acceptance still required

- Windows `clean build` (this environment cannot resolve `services.gradle.org`).
- FPS benchmark with large Rule Books in Table, inventory/hand and dropped entities.
- Two-player LAN/server QA: drop/pick/view/apply the same reference-only book from another player.
- Hot WORLD rule replacement while attached to a block changed from Stable to Unclimbable.
