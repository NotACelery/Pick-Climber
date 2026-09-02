# Pick Climber 1.1.0 — Release procedure

## Apply

Extract the final release patch over the validated dev.13 source.

Run once:

```text
CLEANUP-RELEASE-1.1.0.bat
```

The cleanup removes obsolete development-only files and then deletes itself.

## Build

Run:

```text
build.bat
```

Expected artifact:

```text
build/libs/pickclimber-1.21.1-1.1.0.jar
```

Do not publish a differently named JAR as the 1.1.0 release.

## Final smoke test

Start the exact release JAR in Minecraft 1.21.1 / NeoForge 21.1.235.

The full gameplay/GUI QA is already recorded in `docs/testing/TESTING-1.1.0.md`; this final smoke pass only verifies that
release metadata/cleanup did not introduce a startup regression.

## Publish

Use `docs/CURSEFORGE-CHANGELOG-1.1.0.md` as the CurseForge changelog.

Recommended Git release/tag:

```text
1.1.0
```

The Git commit/tag is the canonical source identity for the release.
