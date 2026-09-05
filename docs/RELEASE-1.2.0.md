# Pick Climber 1.2.0 — Release Procedure

## 1. Freeze

- Close every P0 item in `WAITLIST.md`.
- Stop adding features; only release-blocking fixes after freeze.
- Confirm `gradle.properties` targets the intended RC/release version.

## 2. Source audit

Run:

```text
python tools/audit-source.py
```

Confirm no obsolete dev artifacts, generated build/cache directories or stale documentation are included in the source snapshot.

## 3. Build

Java 21:

```text
build.bat
```

or:

```text
./build.sh
```

Require `clean build` SUCCESS, including all custom `check` gates and JUnit tests. Record the exact generated JAR.

## 4. Runtime QA

Execute `TESTING-1.2.0.md` on the exact JAR. Include singleplayer, dedicated server/two-player, JSON filesystem and optional JEI/EMI cases.

## 5. Documentation

- `README.md` matches released behavior.
- `BUILD-STATUS.md` records the exact successful build/QA baseline.
- `WAITLIST.md` has no release-blocking item open.
- `CURSEFORGE-CHANGELOG-1.2.0.md` is finalized.
- Remove `-dev.N` from the release version only after acceptance.

## 6. Git / publication

- Commit the exact accepted tree.
- Tag the 1.2.0 release.
- Upload the JAR for Minecraft 1.21.1 / NeoForge.
- Publish the final changelog and supported language/integration notes.

Do not publish `.gradle-dist`, `.gradle`, `build`, run directories or local IDE caches.
