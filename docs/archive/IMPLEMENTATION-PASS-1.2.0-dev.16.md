# Pick Climber 1.2.0-dev.16 — NeoForge unit-test classpath pass

## Scope

This pass does not add gameplay features. It fixes the test compilation infrastructure exposed after dev.15 successfully passed `compileJava`.

## External build finding

The dev.15 Windows build reached and completed `compileJava`. `compileTestJava` then emitted 62 errors because `src/test` could see JUnit and the project's main output, but did not receive the Minecraft/NeoForge modding classpath. The missing types were all from that dependency family, including `BlockPos`, `Blocks`, `ResourceLocation`, `DyeColor`, `CompoundTag`, and Mojang serialization classes.

## Fix

ModDevGradle's supported JUnit integration is now enabled:

```groovy
neoForge {
    unitTest {
        enable()
        testedMod = mods.pickclimber
    }
}
```

This attaches the modding dependencies to the Gradle `test` source set and configures the tested mod correctly. The JUnit Platform launcher runtime is also declared explicitly at version 1.11.4 to match JUnit Jupiter 5.11.4.

## Build status

The source-main compiler is known to pass from the external dev.15 build. dev.16 requires a fresh real Windows `clean build` to confirm `compileTestJava`, execute the 13 JUnit tests, and expose any later packaging/runtime issue.
