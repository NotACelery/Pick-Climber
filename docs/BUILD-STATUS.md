# Pick Climber 1.2.0-dev.21 — Build / Acceptance Status

## Current source

```text
Minecraft 1.21.1
NeoForge 21.1.235
Java 21
mod_version 1.2.0-dev.21
network protocol 15
Rule Book portable format 2
mechanical profile format 1
```

Source history for this reconstruction:

```text
1.1.0 stable baseline
  -> recovered cumulative 1.2.0-dev.8 code floor
  -> dev.9 Rule Book / Table / Terminal reconstruction
  -> dev.10 authoring + JSON v2 + Effective Rules seam + Temporary WORLD
  -> dev.11 Temporary PLAYER + per-player sync + rules countdown HUD
  -> dev.12 Rule Dispenser + Temporary Rule Book transport + dual timer HUD
  -> dev.13 Structural Geometry Safety + Windows Rules integrity path fix
```

## dev.21 optional integration pass

The user confirmed the exact dev.20 root-ready source completes `clean build` successfully on Windows. dev.20 is therefore
the current build-clean baseline. dev.21 layers the final optional code integration pass on top:

- JEI `19.32.0.359` APIs as `compileOnly`, isolated under `integration/jei`;
- EMI `1.1.24+1.21.1` NeoForge API as `compileOnly`, isolated under `integration/emi`;
- shared viewer-neutral documentation relations under `integration/recipeviewer`;
- synthetic authoring relation: Book + Rules Table -> current-schema White Rule Book;
- synthetic application relation: Rule Book -> Rules Terminal, with the Terminal kept as catalyst/workstation;
- no survival recipe JSON, no recipe-transfer handler and no loader dependency on either viewer;
- `verifyOptionalIntegrations` to prevent accidental external API leakage into core packages.

A fresh external build is required before promoting dev.21 to build-clean.

## Real dev.11 build result supplied 2026-09-02

The external Windows build successfully downloaded Gradle 9.2.1 and progressed through NeoForge/Minecraft artifact
preparation. It then failed at the project-owned `verifyRulesIntegrity` task before source compilation acceptance:

```text
Invocation of 'file' references a Gradle script object from a Groovy closure at execution time,
which is unsupported with the configuration cache.
```

The failure pointed to `build.gradle` line 420 in the dev.11 source.

## dev.12 correction

The `file(...)` calls used by `verifyRulesIntegrity` were moved out of the `doLast` execution closure and are now resolved
through `layout.projectDirectory.file(...).asFile` during configuration. Configuration Cache remains enabled.

A fresh external `clean build` is required to confirm this fix and expose any subsequent `compileJava`, test or runtime
issues. The packaging environment was retried after dev.12 and still cannot resolve `services.gradle.org`; this local
network limitation is separate from the Configuration Cache fix and from the external Windows build path.

## Second external dev.12 build result supplied 2026-09-02

The next Windows build confirmed that the Configuration Cache fix itself worked: the cache entry was stored successfully.
`verifyRulesIntegrity` then failed on four false positives, all of which were actually located under
`rules/network`. The gate normalized paths with:

```groovy
.replace('\\\\', '/')
```

which only replaces two consecutive backslashes, while Windows relative paths contain one separator. Therefore
`network\Foo.java` did not satisfy `startsWith('network/')`.

The gate now normalizes with the platform separator instead:

```groovy
.replace(java.io.File.separator, '/')
```

A source-level simulation of the complete `verifyRulesIntegrity` logic reports 0 violations after this correction. A fresh
external `clean build` is still required to reach source compilation and discover any subsequent Java/API issues.

## dev.13 correction and materialized pass

The second external dev.12 build confirmed that Configuration Cache storage now succeeds, but exposed a Windows-only
false positive in `verifyRulesIntegrity`: relative paths used `\` while the gate checked for `network/`. The previous
normalization attempted to replace two consecutive backslashes. dev.13 now uses:

```groovy
.replace(java.io.File.separator, '/')
```

The full Rules-integrity logic was simulated against the dev.13 tree after the change and reports 0 violations.

dev.13 also materializes Structural Geometry Safety:

- central `StructuralAnchorSafety`;
- concrete-state `isCollisionShapeFullBlock` criterion;
- partial/no-collision states are structurally non-anchorable while Rules are active;
- explicit Stable/Unstable cannot override structural rejection;
- no active Rules profile preserves the 1.1.0 baseline contract;
- state-sensitive handling allows a full-collision state of the same block ID, such as a DOUBLE slab;
- representative tests cover full blocks, slab/stair/fence/wall/pane/door/trapdoor/no-collision states.

## dev.12 materialized

- Climbing Rule Dispenser block/item/BE/menu/screen;
- Creative-only acquisition and Survival break protection;
- six-direction facing;
- validated TEMPORARY master slot, max 1, persisted and never consumed;
- server-authoritative transport lifetime 1..60 s, default 30 s;
- per-player issuance registry with UUID/token/source/absolute expiry;
- Temporary Rule Book runtime item, owner-only pickup/use and safe invalid-possession cleanup;
- inventory/hand/ground expiry, death cleanup and ItemEntity destruction release;
- successful Terminal use releases issuance;
- reconnect Book-timer synchronization;
- Rules + Book dual HUD timer;
- all new messages/labels localized across eight locales.

See `IMPLEMENTATION-PASS-1.2.0-dev.12.md` and `WAITLIST.md` for exact scope.

## Static validation

Packaging-workspace equivalents of source quality, architecture, localization and Rules integrity gates pass for dev.13.

Current counts/signals:

```text
8 locales x 156 canonical keys
0 missing active translation references
0 invalid JSON resources
0 Java lines > 120
0 tabs / trailing whitespace / TODO / FIXME / HACK / direct debug output
0 active Card/Jukebox identities in source/resources
0 public-class / filename mismatches
0 Rules screens owning PacketDistributor directly
ToolWearService remains the only hurtAndBreak owner
```

## Not yet accepted

Do not call dev.13 build-clean until the exact source passes:

```text
clean build
JUnit tests
verifySourceQuality
verifyArchitectureBoundaries
verifyLocalizationParity
verifyRulesIntegrity
client smoke
dedicated server smoke
WORLD temporary runtime QA
PLAYER temporary multiplayer QA
Rule Dispenser multiplayer/expiry/death/destruction QA
```
## 1.2.0-dev.14

External dev.13 reached `compileJava`. Fixed its sole reported Java error in `ClimbingRuleBookItem#getName` and removed the two deprecated EventBusSubscriber bus arguments. Fresh external build required.


## 1.2.0-dev.15

The user's dev.14 Windows build passed the custom integrity gates and reached `compileJava`. It then failed because `TemporaryRuleBookIssuanceService.removeOwnedBooks` captured the changing `slot` loop variable inside a lambda. dev.15 removes that capture. A new real build is required to discover the next compiler/API/test result.

## 1.2.0-dev.16

The external dev.15 Windows build now passes `compileJava`. `compileTestJava` then failed because the default Gradle test source set did not have Minecraft/NeoForge modding dependencies on its classpath, producing 62 downstream missing-type errors (`BlockPos`, `ResourceLocation`, `DyeColor`, `DataResult`, etc.).

dev.16 enables ModDevGradle's supported JUnit integration:

```groovy
neoForge {
    unitTest {
        enable()
        testedMod = mods.pickclimber
    }
}
```

and adds the JUnit Platform launcher runtime. This is the supported ModDevGradle path for unit tests that reference Minecraft classes; no Minecraft jars are manually copied into `testImplementation`. A fresh external `clean build` is required to validate test compilation/execution and expose any subsequent runtime or packaging issue.


## Baseline dev.16 accepted

The user ran the exact root-ready dev.16 source through the Windows `build.bat` (`clean build`) and reported **SUCCESS**.
This accepts dev.16 as the stable/build-clean baseline for the remaining 1.2.0 implementation. Runtime/client/multiplayer
smoke QA remains a separate release-acceptance layer.

## 1.2.0-dev.17

Built on the accepted dev.16 baseline. Materializes the read-only Rule Book Viewer and the Duplicate sub-GUI/server
transaction. A new external clean build is required because these are new source changes after the accepted baseline.

## dev.17 static acceptance before external build

```text
179 main Java files
13 JUnit tests
8 locales x 184 canonical keys
0 source-quality violations in the packaging workspace
0 JSON parse failures
0 active Card/Jukebox identities
0 Rules screens owning packet transport
0 Rules transport outside rules/network
ToolWearService remains the only hurtAndBreak owner
ClimbManager = 161 lines
ClientEvents = 69 lines
```

The exact dev.17 source still needs the external Windows clean build before it can replace dev.16 as the stable baseline.


## Baseline dev.17 accepted

The user ran the exact root-ready dev.17 source through the Windows `build.bat` (`clean build`) and reported **SUCCESS**.
dev.17 therefore replaces dev.16 as the accepted stable/build-clean baseline.

## 1.2.0-dev.18

Built on accepted dev.17. This pass closes portable Rule Book/JSON hardening:

- canonical Rule Book name/profile-name invariant;
- cross-platform filename policy and exact export basename from validated `bookName`;
- missing JSON `cover_color` migrates to White;
- unsupported Rule Book schema gives explicit localized feedback;
- legacy profile JSON migrates to v2 White/Permanent/WORLD;
- Terminal/Dispenser accept only current portable schema, while Table remains the repair path;
- expanded JUnit coverage for portable identity and actual filesystem roundtrips.

dev.18 was confirmed with Windows build SUCCESS and remained the accepted baseline until dev.19 was later accepted.


## 1.2.0-dev.19

Source materialized from the build-clean dev.18 baseline. Adds final Table confirmation/ghost UX and Rule Book network
hardening (8192 explicit overrides, 512 KiB bounded NBT transport, client/server validation). The user subsequently
confirmed the exact dev.19 root-ready source with Windows `clean build` SUCCESS, promoting dev.19 to baseline.
