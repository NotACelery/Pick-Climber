# Climbing Rule Book JSON — format 2

Pick Climber stores local user-selected Rule Book files in:

```text
config/pickclimber/rules/
```

Current exports use `<filename>.rules.json`. The filename is chosen separately from the internal `book_name`.

## Current schema

Example shape:

```json
{
  "format_version": 2,
  "book_name": "Championship Route 4",
  "cover_color": "blue",
  "profile": {
    "format_version": 1,
    "name": "Championship Route 4",
    "stable": ["minecraft:stone"],
    "unstable": ["minecraft:sand"],
    "unclimbable": ["minecraft:oak_planks"],
    "unlisted_policy": "unclimbable",
    "pickaxe_wear": 15,
    "player_mining": true,
    "unmineable_terminals": false
  },
  "activation": "permanent",
  "scope": "world",
  "duration_seconds": 0,
  "author_uuid": "",
  "author_name": ""
}
```

`cover_color` defaults to white when omitted. `author_uuid`/`author_name` are optional in the codec and are filled by server-side authoring when missing.

Permanent definitions normalize to WORLD with duration `0`. Temporary definitions require duration greater than zero and may use WORLD or PLAYER.

## Fail-closed authoring

The editor/server save path classifies every currently authorable block left outside Stable/Unstable as Unclimbable. Imported IDs from absent mods are preserved explicitly instead of being silently discarded.

## Compatibility

The importer can migrate older profile-only JSON and the legacy `durability_multiplier` profile field. New exports always use the current flat `pickaxe_wear` field and Rule Book format 2.

## Filesystem safety

The file layer validates portable names, confines paths to the rules directory, limits file size, requires confirmation before overwrite/delete and validates the decoded profile before use.

The JSON file is portable authoring data. Runtime authority after a Rule Book is applied is server/world state, not the file on disk.
