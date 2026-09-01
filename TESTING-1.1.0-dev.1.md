# Pick Climber 1.1.0-dev.1 — Phase 0 regression

This snapshot is structural only. Every gameplay result below must remain identical to the validated 1.0.3 baseline.

## Build / startup

- [ ] Build with Java 21.
- [ ] Client reaches the title screen with no mod-loading errors.
- [ ] Singleplayer world loads.
- [ ] Dedicated server starts and accepts a client.
- [ ] No new warnings/errors are emitted by Pick Climber during normal startup.

## Stable wall anchor

- [ ] Stable wall anchor attaches from the same distance as 1.0.3.
- [ ] Initial durability cost is unchanged.
- [ ] Anchor cooldown duration is unchanged.
- [ ] Crack overlay appears and clears correctly.
- [ ] Passive detach restores gravity/flight state correctly.
- [ ] Jump detach preserves the same launch behavior.

## Jump boost

- [ ] A real jump followed by a valid wall interaction can trigger the normal boost.
- [ ] The same real jump cannot be consumed twice.
- [ ] Landing clears stale jump authorization.
- [ ] Fake/old jump state does not authorize a boost.

## Unstable surfaces / braking

- [ ] Unstable anchors still slide at the same speed.
- [ ] Sturdy Latch behavior is unchanged.
- [ ] Fast-fall braking begins under the same conditions.
- [ ] Braking durability is charged at the same intervals/amounts.
- [ ] The optional second equipped support pickaxe is detected and damaged exactly as before.
- [ ] Breaking/loss of a support tool falls back safely.

## Ceiling / Strong Grip

- [ ] Ceiling anchors still require Strong Grip.
- [ ] Unstable ceiling anchors still require Sturdy Latch.
- [ ] Initial and sustained ceiling wear are unchanged.
- [ ] Ceiling swing radius, acceleration, damping and release momentum feel identical to 1.0.3.
- [ ] First- and third-person ceiling poses remain correct.

## Tool identity / hand transfer

- [ ] Tool UUID persists through normal inventory/hand movement.
- [ ] Vanilla `F` hand swap transfers the active anchor without a new durability charge.
- [ ] Off-hand anchor keeps the main hand free for normal gameplay.
- [ ] Breaking/removing the active physical tool detaches safely.
- [ ] Per-tool cooldown overlay still follows the correct physical ItemStack.

## HUD / interactions

- [ ] 1.0.2 shader isolation remains intact: Jade and other HUD overlays keep their own colors.
- [ ] 1.0.3 interactive-block suppression remains intact without Shift.
- [ ] Holding Shift over an interactive valid anchor reveals the indicator.
- [ ] No reflection/fake block interaction is executed by the HUD.
- [ ] Existing status colors/messages remain unchanged in this Phase 0 snapshot.

## Cleanup / multiplayer

- [ ] Logout clears server state and anchor visuals.
- [ ] Dimension change detaches/cleans correctly.
- [ ] Client disconnect clears local and remote pose state.
- [ ] Remote ceiling pose appears, refreshes and clears for another client.
- [ ] No stale crack overlay remains after detach/disconnect.

## Phase 0.1 acceptance

Do not continue into unified anchor evaluation until the exact `1.1.0-dev.1` JAR passes this regression list or any discovered difference has been explained and intentionally accepted.
