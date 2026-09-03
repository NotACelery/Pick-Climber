# Pick Climber 1.2.0-dev.18 — Portable / JSON Hardening Pass

Date: 2026-09-02

## Baseline

- dev.17 confirmed `clean build` SUCCESS on Windows.
- dev.17 is the accepted stable baseline underneath this pass.

## Materialized in dev.18

- Export JSON filename is derived exclusively from validated `bookName`.
- Removed the independent filename EditBox from the Rules Table.
- Added `RuleBookNamePolicy` using a cross-platform Windows/Linux/macOS-safe subset.
- Windows reserved device names (`CON`, `PRN`, `COM1`, `LPT1`, etc.) are rejected on every OS.
- Rule Book validator canonicalizes `profile.name` to `bookName`.
- Missing JSON `cover_color` defaults to White.
- Unsupported/future Rule Book format versions return a specific localized error.
- Legacy profile JSON migrates to v2 White/Permanent/WORLD with `unmineable_terminals=false` when absent.
- Terminal and Rule Dispenser require current schema v2; Table remains the explicit repair/migration path.
- Added real temporary-directory filesystem roundtrip and overwrite tests.
- Added canonical portable identity tests for name, dye, rules, duration, scope, activation and missing IDs.
- Added cross-platform filename/name-policy tests and manipulated JSON validation tests.

## Acceptance

Static source-quality/localization/JSON checks pass in the packaging workspace. The exact dev.18 root-ready source still
requires the external Windows `clean build` before promotion over dev.17.
