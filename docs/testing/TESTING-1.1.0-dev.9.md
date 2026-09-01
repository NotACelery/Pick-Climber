# Pick Climber 1.1.0-dev.9 — Options GUI correction QA

## Build / startup

- [ ] `build.bat` completes with Java 21.
- [ ] The exact dev.9 JAR starts in NeoForge 1.21.1 without client startup errors.
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

## Indicator visuals

- [ ] String renders like the known 1.0.3 icon at 0% transparency.
- [ ] String Transparency visibly changes at 25%, 50%, 75% and 100%.
- [ ] 100% transparency makes the String invisible without affecting the box.
- [ ] Pickaxe Outline is clearly recognizable as a pickaxe and resembles a tool-slot outline.
- [ ] Pickaxe Transparency works independently of the box.
- [ ] Icon Size works for String and Pickaxe Outline without shifting the crosshair.
- [ ] Muted, Normal and Neon visibly alter both icon and surrounding box while preserving the status hue.
- [ ] READY, UNSTABLE, enchantment-required, OUT_OF_RANGE, COOLDOWN and OBSTRUCTED remain distinguishable.
- [ ] OBSTRUCTED remains red-family in all three intensity modes.

## Config migration

- [ ] Existing dev.8 `pickclimber-client.json` loads without resetting unrelated preferences.
- [ ] Legacy opacity fields migrate to the new transparency semantics correctly.
- [ ] Saved config uses `configVersion: 2`, `iconTransparency`, `boxTransparency` and `colorIntensity`.

## Runtime / regression

- [ ] Interactions OFF detaches safely if currently anchored and prevents new Pick Climber interactions.
- [ ] Interactions ON restores normal behavior without restart.
- [ ] Full 1.0.3 anchor/physics/enchantment regression still passes.
- [ ] Interactive blocks still hide the indicator without Shift and permit force-anchor preview with Shift where valid.
- [ ] Jade and other HUD overlays do not inherit Pick Climber shader colors.
