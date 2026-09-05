package dev.maicra.pickclimber.client.rules;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonElement;

import net.minecraft.world.item.DyeColor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.ClimbingRulesProfileCodec;
import dev.maicra.pickclimber.rules.RuleBookActivationMode;
import dev.maicra.pickclimber.rules.RuleBookScope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClimbingRulesJsonFilesTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void sanitizesTraversalAndSeparatorsForLegacyImportNames() {
        assertEquals("parkour.route", ClimbingRulesJsonFiles.sanitizeFileName("../parkour..route.json"));
        assertEquals("route_main", ClimbingRulesJsonFiles.sanitizeFileName("route/main"));
        assertEquals("route_main", ClimbingRulesJsonFiles.sanitizeFileName("route\\main"));
    }

    @Test
    void rejectsReservedOrEmptyNamesOnEveryPlatform() {
        assertEquals("", ClimbingRulesJsonFiles.sanitizeFileName(".."));
        assertEquals("", ClimbingRulesJsonFiles.sanitizeFileName("CON.json"));
        assertEquals("", ClimbingRulesJsonFiles.sanitizeFileName("CON.rules.json"));
        assertEquals("", ClimbingRulesJsonFiles.sanitizeFileName("com1.backup"));
        assertEquals("", ClimbingRulesJsonFiles.sanitizeFileName("   "));
    }

    @Test
    void stripsSupportedJsonExtensionsWithoutDuplicatingThem() {
        assertEquals("my-rules", ClimbingRulesJsonFiles.sanitizeFileName("my-rules.json"));
        assertEquals("my-rules", ClimbingRulesJsonFiles.sanitizeFileName("my-rules.JSON"));
        assertEquals("my-rules", ClimbingRulesJsonFiles.sanitizeFileName("my-rules.rules.json"));
    }

    @Test
    void filesystemRoundTripUsesValidatedBookNameAsFilename() throws Exception {
        ClimbingRuleBookDefinition source = temporaryDefinition("Timed Route", DyeColor.BLUE, 90);

        var exported = ClimbingRulesJsonFiles.exportRuleBook(temporaryDirectory, source, false);
        assertTrue(exported.success());
        assertEquals("Timed Route.rules.json", exported.value().getFileName().toString());
        assertTrue(Files.isRegularFile(exported.value()));

        var imported = ClimbingRulesJsonFiles.importRuleBook(
                temporaryDirectory,
                exported.value().getFileName().toString()
        );
        assertTrue(imported.success());
        assertEquals(source, imported.value());
    }

    @Test
    void exportCanUseCustomFilenameWithoutChangingInternalBookTitle() throws Exception {
        ClimbingRuleBookDefinition source = temporaryDefinition("Championship Rules", DyeColor.GREEN, 50);

        var exported = ClimbingRulesJsonFiles.exportRuleBook(
                temporaryDirectory,
                source,
                "event-round-4.json",
                false
        );

        assertTrue(exported.success());
        assertEquals("event-round-4.rules.json", exported.value().getFileName().toString());
        var imported = ClimbingRulesJsonFiles.importRuleBook(
                temporaryDirectory,
                exported.value().getFileName().toString()
        );
        assertTrue(imported.success());
        assertEquals("Championship Rules", imported.value().bookName());
    }

    @Test
    void exportRequiresExplicitOverwriteForExistingBookName() {
        ClimbingRuleBookDefinition source = temporaryDefinition("Existing", DyeColor.WHITE, 60);

        assertTrue(ClimbingRulesJsonFiles.exportRuleBook(temporaryDirectory, source, false).success());
        var blocked = ClimbingRulesJsonFiles.exportRuleBook(temporaryDirectory, source, false);
        assertFalse(blocked.success());
        assertEquals("message.pickclimber.rules.json_exists", blocked.errorKey());
        assertTrue(ClimbingRulesJsonFiles.exportRuleBook(temporaryDirectory, source, true).success());
    }

    @Test
    void missingCoverColorMigratesToWhite() {
        ClimbingRuleBookDefinition source = temporaryDefinition("No Color", DyeColor.RED, 45);
        JsonElement encoded = ClimbingRuleBookCodec.encodeToJson(source).getOrThrow();
        encoded.getAsJsonObject().remove("cover_color");

        var imported = ClimbingRulesJsonFiles.decodeImportedJson(encoded, "No Color");

        assertTrue(imported.success());
        assertEquals(DyeColor.WHITE, imported.value().coverColor());
        assertEquals(45, imported.value().durationSeconds());
    }

    @Test
    void unsupportedRuleBookVersionGetsSpecificFeedback() {
        ClimbingRuleBookDefinition source = temporaryDefinition("Future", DyeColor.WHITE, 30);
        JsonElement encoded = ClimbingRuleBookCodec.encodeToJson(source).getOrThrow();
        encoded.getAsJsonObject().addProperty("format_version", 99);

        var imported = ClimbingRulesJsonFiles.decodeImportedJson(encoded, "Future");

        assertFalse(imported.success());
        assertEquals("message.pickclimber.rules.json_invalid_version", imported.errorKey());
    }

    @Test
    void legacyProfileJsonMigratesToWhitePermanentWorldBook() {
        ClimbingRulesProfile legacy = ClimbingRulesProfile.defaults("Legacy Name");
        JsonElement encoded = ClimbingRulesProfileCodec.encodeToJson(legacy).getOrThrow();
        encoded.getAsJsonObject().remove("unmineable_terminals");

        var imported = ClimbingRulesJsonFiles.decodeImportedJson(encoded, "Migrated Route");

        assertTrue(imported.success());
        assertEquals("Migrated Route", imported.value().bookName());
        assertEquals("Migrated Route", imported.value().profile().profileName());
        assertEquals(DyeColor.WHITE, imported.value().coverColor());
        assertEquals(RuleBookActivationMode.PERMANENT, imported.value().activationMode());
        assertEquals(RuleBookScope.WORLD, imported.value().scope());
        assertEquals(0, imported.value().durationSeconds());
        assertFalse(imported.value().profile().unmineableTerminals());
    }

    @Test
    void manipulatedTemporaryWithoutDurationIsRejected() {
        ClimbingRuleBookDefinition source = temporaryDefinition("Broken", DyeColor.WHITE, 30);
        JsonElement encoded = ClimbingRuleBookCodec.encodeToJson(source).getOrThrow();
        encoded.getAsJsonObject().addProperty("duration_seconds", 0);

        var imported = ClimbingRulesJsonFiles.decodeImportedJson(encoded, "Broken");

        assertFalse(imported.success());
        assertEquals("message.pickclimber.rules.json_invalid_profile", imported.errorKey());
    }

    private static ClimbingRuleBookDefinition temporaryDefinition(
            String name,
            DyeColor color,
            int durationSeconds
    ) {
        return new ClimbingRuleBookDefinition(
                ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                name,
                color,
                ClimbingRulesProfile.defaults(name),
                RuleBookActivationMode.TEMPORARY,
                RuleBookScope.PLAYER,
                durationSeconds
        );
    }
}
