# Pick Climber 1.1.0-dev.11 — Pickaxe indicator + simplified reset QA

## Build / startup

- [ ] `build.bat` compiles the v3 options/config migration and the texture-backed Pickaxe renderer.
- [ ] `build.bat` completes with Java 21.
- [ ] The exact dev.11 JAR starts in NeoForge 1.21.1 without client startup errors.
- [ ] Controls contains a Pick Climber key mapping and it defaults to `K`.
- [ ] There is no Pick Climber button injected into vanilla Options.

## Dedicated options GUI

- [ ] Pressing the configured key while playing opens Pick Climber Options.
- [ ] The key does not open the screen from the title screen or while another screen is active.
- [ ] Title, subtitle, preview panel, preview text and preview icon are crisp and belong to the GUI layer.
- [ ] The live HUD anchor indicator is not visible or blurred behind this screen.
- [ ] Done/Escape returns to gameplay correctly.

## Ordering / dependencies

- [ ] Enable Pick Climber Interactions is the first option.
- [ ] Indicator Mode is followed by Failure Messages in the left column / compact ordering.
- [ ] Indicator Style is directly above Indicator Box.
- [ ] With interactions OFF, every other setting and reset control is disabled; Done remains usable.
- [ ] Re-enabling interactions restores the appropriate controls.
- [ ] Indicator Mode OFF still leaves Failure Messages configurable.
- [ ] Box Transparency disables when Indicator Box is OFF.
- [ ] Show Unclimbable is editable only in Contextual mode.
- [ ] The footer contains only `Reset to Defaults` and `Done`.
- [ ] Reset to Defaults restores all option defaults and immediately resynchronizes runtime preferences.

## Indicator visuals

- [ ] String renders like the known 1.0.3 icon at 0% transparency.
- [ ] String Transparency visibly changes at 25%, 50%, 75% and 100%.
- [ ] 100% transparency makes the String invisible without affecting the box.
- [ ] Pickaxe is clearly recognizable as a Minecraft-style pickaxe: broad horizontal head with a diagonal handle, not a hook/scythe shape.
- [ ] Pickaxe Transparency works independently of the box.
- [ ] Icon Size works for String and Pickaxe without shifting the crosshair.
- [ ] Muted, Normal and Neon visibly alter both icon and surrounding box while preserving the status hue.
- [ ] READY, UNSTABLE, enchantment-required, OUT_OF_RANGE, COOLDOWN and OBSTRUCTED remain distinguishable.
- [ ] OBSTRUCTED remains red-family in all three intensity modes.

## Config migration

- [ ] Existing dev.8/dev.10 `pickclimber-client.json` loads without resetting unrelated preferences.
- [ ] A saved `indicatorStyle: "pickaxe_outline"` migrates to Pickaxe and is rewritten as `pickaxe` on the next save.
- [ ] Legacy opacity fields migrate to the new transparency semantics correctly.
- [ ] Saved config uses `configVersion: 3`, `iconTransparency`, `boxTransparency` and the current color-intensity fields.

## Runtime / regression

- [ ] Interactions OFF detaches safely if currently anchored and prevents new Pick Climber interactions.
- [ ] Interactions ON restores normal behavior without restart.
- [ ] Full 1.0.3 anchor/physics/enchantment regression still passes.
- [ ] Interactive blocks still hide the indicator without Shift and permit force-anchor preview with Shift where valid.
- [ ] Jade and other HUD overlays do not inherit Pick Climber shader colors.
