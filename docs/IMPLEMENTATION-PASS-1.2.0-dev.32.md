# Pick Climber 1.2.0-dev.32 — Rule Definition Architecture / performance pass

This pass starts the post-dev.22 architecture migration agreed during QA.

## Core changes

- Rule Books are reference-first: rendering/name/tooltips consume a compact `definition_id` + metadata record instead of decoding the full rule profile every frame.
- A server/world `RuleDefinitionLibrarySavedData` stores normalized rule profiles once and resolves books by SHA-256 gameplay-definition ID.
- Existing embedded books migrate lazily server-side the first time a gameplay system resolves them; migration strips the embedded profile from the stack.
- Server-created/edited/imported/processed Rule Books are written directly as compact references.
- Right-click Rule Book viewing resolves server-side and sends the full definition only on demand.
- Rule authorship (`author_uuid`, `author_name`) is persistent JSON metadata; creating/editing legacy-authorless books stamps the acting player. Applying somebody else's authored book preserves that author.
- K options gains `Export World Rules`, exporting the already server-synchronized active world definition to the user's explicit rules library without changing title/author.
- World rule updates already broadcast immediately and `ClimbManager.revalidateRules` detaches anchors that become invalid under the new policy.
- Editor saves are fail-closed: every currently-known authorable block not Stable/Unstable is explicitly stored as Unclimbable, and future/unlisted blocks use `UNCLIMBABLE`.
- Editor grid cells now have real visual spacing so Stable/Unstable/Unclimbable borders do not merge into continuous color fields.

## Migration bridge

Creative/viewer-only Rule Books created without a server context retain a bootstrap definition only until the server first resolves them. Normal gameplay-created books do not carry the heavy block lists. This keeps old saves and creative acquisition recoverable while moving runtime objects to reference-only storage.

## Acceptance targets

- Windows clean build.
- Compare FPS with a large Rule Book hidden, in Table, in inventory/hand, and dropped.
- Verify edited/imported/processed books contain compact reference metadata after a server interaction.
- Apply world rules while attached to a surface changed to Unclimbable and verify immediate detach.
- Verify world-rule export preserves original title and author while allowing an independent filename.
