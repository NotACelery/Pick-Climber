# Pick Climber 1.2.0 — Release Procedure

Do not execute this release procedure until the 1.2.0 feature scope is complete and the final development/RC source
passes the full QA matrix.

## Pre-release

1. Confirm branch/source is the accepted final 1.2.0 development/RC tree.
2. Confirm no retired Rules Jukebox/Card source/resource identity remains.
3. Confirm the final Rule Book/Table/Terminal/Dispenser testing matrix is complete.
4. Change only:

```text
mod_version=1.2.0
```

5. Run a clean Java 21 build.
6. Confirm all Gradle verification tasks and tests pass.

## Exact artifact

Expected file:

```text
build/libs/pickclimber-1.21.1-1.2.0.jar
```

Do not substitute an earlier dev JAR.

## Smoke

Using the exact final JAR:

- start client;
- load a world with no active profile and verify baseline anchor/HUD;
- author/edit one Rule Book in the Rules Table, apply it through the Terminal, then Restore Defaults;
- restart world and verify persistence;
- start dedicated server;
- join with a client and verify synchronized rules;
- verify a non-admin cannot administer the Rules Table while normal players can use the Terminal.

## Git identity

After exact-JAR smoke:

1. commit final documentation/metadata;
2. verify working tree clean;
3. tag the exact source:

```text
1.2.0
```

The tag/commit is the canonical source identity for the published JAR.

## Publish

Publish the exact smoke-tested JAR with `docs/CURSEFORGE-CHANGELOG-1.2.0.md` as the release notes source.
