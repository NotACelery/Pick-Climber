# Pick Climber 1.1.0-dev.2 — Phase 0.2 regression

Baseline: validated `1.0.3` behavior. This development build must not intentionally change physics, balance, controls, interaction priority, tags or protocol 13.

## Build / startup

- [ ] Build with Java 21 and NeoForge 21.1.235.
- [ ] Exact artifact is `pickclimber-1.21.1-1.1.0-dev.2.jar`.
- [ ] Client reaches title screen and loads an existing test world.
- [ ] Dedicated server starts with the same JAR and accepts a client.
- [ ] No new warnings/errors from `AnchorEvaluator`, `AnchorGeometry`, `AnchorSurfaceResolver` or networking.

## Basic anchor evaluation

- [ ] Stable vertical block + valid pickaxe: anchor succeeds exactly as 1.0.3.
- [ ] Up-facing/top face: cannot anchor.
- [ ] No climbing tool in either hand: no anchor is consumed and no durability/cooldown changes.
- [ ] Off-hand valid tool keeps priority over main-hand valid tool.
- [ ] Active hand cannot be reused as a second free anchor candidate.
- [ ] Swapping/duplicated tool identities still separate correctly when an attach is actually committed.
- [ ] A rejected attempt does not rewrite tool UUID/cooldown NBT.

## HUD / failure parity

- [ ] READY remains green on a valid firm anchor.
- [ ] UNSTABLE remains cyan when the anchor is valid but not reinforced by Sturdy Latch.
- [ ] UNCLIMBABLE reports the same state as 1.0.3.
- [ ] OUT OF RANGE appears in the same 1.5–3 block HUD band.
- [ ] OBSTRUCTED still detects invalid support / blocked target space.
- [ ] Missing Strong Grip on a ceiling still reports the Strong Grip requirement.
- [ ] Unstable ceiling with Strong Grip but without Sturdy Latch still reports Sturdy Latch.
- [ ] Tool cooldown still reports COOLDOWN.
- [ ] Failure action-bar messages remain identical to 1.0.3.
- [ ] String tint remains isolated; Jade and other overlays do not inherit its color.

## Interactive block priority

- [ ] Normal right click on crafting table/chest/furnace opens the block and does not anchor.
- [ ] Normal right click on Villager Noise Switch / representative BlockEntity machine hides the String and keeps block priority.
- [ ] Shift restores force-anchor preview on a valid face.
- [ ] Shift + right click anchors when valid.
- [ ] Invalid Shift anchor does not steal the target block's own Shift behavior.

## Geometry parity

- [ ] Wall anchor target position matches 1.0.3 around block edges.
- [ ] Adjacent-block collision correction still finds the same safe vertical adjustment.
- [ ] Occupied destination returns `not enough space` rather than teleporting into collision.
- [ ] Changing from one anchor point to another keeps the same 1.5-block movement limit.
- [ ] Wall-to-ceiling and ceiling-to-wall transitions retain the validated clearance behavior.

## Surface behavior

- [ ] Stable tagged block remains stable.
- [ ] Unstable tagged block retains controlled slide without Sturdy Latch.
- [ ] Sturdy Latch fixes an unstable wall exactly as before.
- [ ] Unclimbable tagged block cannot anchor.
- [ ] Untagged fallback block preserves legacy fallback behavior.
- [ ] Sliding across block boundaries updates stable/unstable classification without detach regressions.

## Boost / braking / Strong Grip smoke regression

- [ ] Real jump + rising airborne wall attempt triggers boost.
- [ ] Positive velocity without a fresh real jump does not trigger boost.
- [ ] Hard fall enters BRAKING with the same feel and wear.
- [ ] Two-tool braking still works and uses the support tool.
- [ ] Strong Grip ceiling attach, swing and Space release work.
- [ ] Ceiling durability and cooldown remain unchanged.
- [ ] `F` hand transfer remains stable on wall and ceiling.

## Multiplayer / cleanup

- [ ] Remote ceiling pose remains synchronized between two clients.
- [ ] Detach clears local/remote pose state.
- [ ] Dimension change cleans the anchor.
- [ ] Logout/reconnect leaves no stale anchor/crack state.
- [ ] Breaking/removing the anchor block detaches safely.

## Phase 0.2 acceptance

Phase 0.2 is accepted only when the exact `1.1.0-dev.2` JAR builds and the sections above show no behavior regression against 1.0.3. Functional tuning changes belong in a later, separately reviewed pass.
