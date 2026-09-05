# Pick Climber 1.2.0-dev.20 — Visual Assets Pass

## Baseline

- Base: `1.2.0-dev.19`.
- dev.19 was confirmed with Windows `clean build` SUCCESS by the user.
- dev.20 is not promoted until its own build passes.

## Rule Book

The approved Rule Book concept was materialized as a real inventory asset instead of embedding the generated sheet.

Final asset contract:

```text
32x32 canvas
layer0 = neutral grayscale cover/ribbon mask, tintable
layer1 = fixed pages + hardware + outline + diamond + pickaxe indicator
```

Both PNGs are generated from the same aligned canvas, so the fixed layer cannot become narrower than the tinted cover.
The old single-layer `climbing_rule_book.png` was removed.

The central emblem does not contain a rendered pickaxe or a climbing/person symbol. It uses the exact existing
`textures/gui/pickaxe_indicator.png` outline already used by the in-game HUD.

Client tint registration reads `ClimbingRuleBookDefinition.coverColor()` for normal Rule Books and the embedded definition
for Temporary Rule Books. Only tint index 0 is colored; the details layer always renders white/no-tint.

## Rules Table

The approved white/gray authoring concept was converted to 64x64 textures:

- top;
- front;
- side;
- bottom.

The Table now has `HORIZONTAL_FACING` purely for visual placement. Gameplay/menu authority is unchanged.

## Rules Terminal

The approved reader concept was converted to 64x64 top/front/side/bottom textures.

Rendering now uses:

- one normal horizontal model with the front slot on north;
- one vertical model with the front slot on up;
- six blockstate variants following the vanilla directional-device pattern.

The inventory item uses the vertical model so the reader face is visible from above.

## Rule Dispenser

The approved dispenser concept was converted to 64x64 top/front/side/bottom textures.

Rendering uses the same horizontal/vertical model split as the Terminal. A dedicated
`climbing_rule_dispenser_front_vertical.png` resource already exists and is referenced by the vertical model. In dev.20 it
is intentionally an exact copy of the approved normal front, as requested. A later visual refinement can replace only this
PNG with the centered UP/DOWN aperture without touching block logic or blockstate models.

## Static acceptance

Local static equivalents after the pass:

```text
0 invalid JSON resources
0 Java lines > 120
0 tabs / trailing whitespace
0 TODO / FIXME / HACK / direct debug output
0 architecture-boundary violations
0 Rules-integrity violations
8 locale files with exact canonical-key parity
Rule Book cover/details both exactly 32x32
old single-layer Rule Book texture absent
182 main Java files
16 test files
192 localization keys
ClimbManager = 161 lines
ClientEvents = 69 lines
```

A real Windows `clean build` remains the promotion gate for dev.20.
