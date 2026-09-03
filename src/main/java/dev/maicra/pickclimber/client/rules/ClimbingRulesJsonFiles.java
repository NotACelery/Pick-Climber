package dev.maicra.pickclimber.client.rules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidationResult;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidator;
import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.ClimbingRulesProfileCodec;
import dev.maicra.pickclimber.rules.ClimbingRulesValidationResult;
import dev.maicra.pickclimber.rules.ClimbingRulesValidator;
import dev.maicra.pickclimber.rules.RuleBookNamePolicy;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class ClimbingRulesJsonFiles {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final long MAX_JSON_BYTES = 4L * 1024L * 1024L;

    private ClimbingRulesJsonFiles() {
    }

    public static Path directory() {
        return FMLPaths.CONFIGDIR.get().resolve("pickclimber").resolve("rules");
    }

    public static List<String> listFiles() {
        return listFiles(directory());
    }

    static List<String> listFiles(Path directory) {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (var stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(ClimbingRulesJsonFiles::isSupportedJsonName)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        } catch (IOException exception) {
            return List.of();
        }
    }

    public static FileResult<ClimbingRuleBookDefinition> importRuleBook(String fileName) {
        return importRuleBook(directory(), fileName);
    }

    static FileResult<ClimbingRuleBookDefinition> importRuleBook(Path directory, String fileName) {
        Optional<Path> resolved = resolveExistingFile(directory, fileName);
        if (resolved.isEmpty()) {
            return FileResult.error("message.pickclimber.rules.json_not_found");
        }
        try {
            if (Files.size(resolved.get()) > MAX_JSON_BYTES) {
                return FileResult.error("message.pickclimber.rules.json_invalid");
            }
            String source = Files.readString(resolved.get(), StandardCharsets.UTF_8);
            JsonElement json = JsonParser.parseString(source);
            String importedName = importedBookName(resolved.get().getFileName().toString());
            if (importedName.isEmpty()) {
                return FileResult.error("message.pickclimber.rules.json_invalid_filename");
            }
            return decodeImportedJson(json, importedName);
        } catch (Exception exception) {
            return FileResult.error("message.pickclimber.rules.json_invalid");
        }
    }

    static FileResult<ClimbingRuleBookDefinition> decodeImportedJson(JsonElement json, String importedName) {
        if (hasUnsupportedRuleBookVersion(json)) {
            return FileResult.error("message.pickclimber.rules.json_invalid_version");
        }

        Optional<ClimbingRuleBookDefinition> decoded = ClimbingRuleBookCodec.decodeFromJson(json).result();
        if (decoded.isPresent()) {
            return validateImported(withBookName(decoded.get(), importedName));
        }

        Optional<ClimbingRulesProfile> legacy = ClimbingRulesProfileCodec.decodeFromJson(json).result();
        if (legacy.isEmpty()) {
            return FileResult.error("message.pickclimber.rules.json_invalid");
        }
        ClimbingRulesValidationResult legacyValidation = ClimbingRulesValidator.validateAndNormalize(legacy.get());
        if (!legacyValidation.valid()) {
            return FileResult.error("message.pickclimber.rules.json_invalid_profile");
        }

        ClimbingRulesProfile profile = legacyValidation.normalizedProfile();
        ClimbingRuleBookDefinition migrated = ClimbingRuleBookDefinition.permanentWorld(importedName, profile);
        return validateImported(withProfileName(migrated, importedName));
    }

    public static FileResult<Path> exportRuleBook(
            ClimbingRuleBookDefinition definition,
            boolean overwrite
    ) {
        return exportRuleBook(directory(), definition, overwrite);
    }

    static FileResult<Path> exportRuleBook(
            Path directory,
            ClimbingRuleBookDefinition definition,
            boolean overwrite
    ) {
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(definition);
        if (!validation.valid()) {
            return FileResult.error(validationErrorKey(validation));
        }
        ClimbingRuleBookDefinition normalized = validation.normalizedDefinition();
        String safeName = RuleBookNamePolicy.portableFileStem(normalized.bookName()).orElse("");
        if (safeName.isEmpty()) {
            return FileResult.error("message.pickclimber.rules.json_invalid_filename");
        }

        Optional<JsonElement> encoded = ClimbingRuleBookCodec.encodeToJson(normalized).result();
        if (encoded.isEmpty()) {
            return FileResult.error("message.pickclimber.rules.json_export_failed");
        }

        Path normalizedDirectory = directory.normalize();
        Path target = normalizedDirectory.resolve(safeName + ".rules.json").normalize();
        if (!target.getParent().equals(normalizedDirectory)) {
            return FileResult.error("message.pickclimber.rules.json_invalid_filename");
        }
        try {
            Files.createDirectories(normalizedDirectory);
            if (Files.exists(target) && !overwrite) {
                return FileResult.error("message.pickclimber.rules.json_exists");
            }
            Files.writeString(target, GSON.toJson(encoded.get()) + System.lineSeparator(), StandardCharsets.UTF_8);
            return FileResult.success(target);
        } catch (IOException exception) {
            return FileResult.error("message.pickclimber.rules.json_export_failed");
        }
    }

    public static String sanitizeFileName(String requestedName) {
        return RuleBookNamePolicy.normalizeImportedFileStem(requestedName);
    }

    private static FileResult<ClimbingRuleBookDefinition> validateImported(
            ClimbingRuleBookDefinition definition
    ) {
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(definition);
        return validation.valid()
                ? FileResult.success(validation.normalizedDefinition())
                : FileResult.error(validationErrorKey(validation));
    }

    private static String validationErrorKey(ClimbingRuleBookValidationResult validation) {
        boolean unsupportedVersion = validation.issues().stream()
                .anyMatch(issue -> "unsupported_rule_book_format_version".equals(issue.code()));
        if (unsupportedVersion) {
            return "message.pickclimber.rules.json_invalid_version";
        }
        boolean invalidName = validation.issues().stream()
                .anyMatch(issue -> issue.code().equals("empty_book_name")
                        || issue.code().equals("book_name_too_long")
                        || issue.code().equals("book_name_not_portable"));
        return invalidName
                ? "message.pickclimber.rules.json_invalid_filename"
                : "message.pickclimber.rules.json_invalid_profile";
    }

    private static boolean hasUnsupportedRuleBookVersion(JsonElement json) {
        if (!json.isJsonObject()) {
            return false;
        }
        var object = json.getAsJsonObject();
        if (!object.has("profile") || !object.has("format_version")) {
            return false;
        }
        try {
            return object.get("format_version").getAsInt() != ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION;
        } catch (RuntimeException exception) {
            return true;
        }
    }

    private static boolean isSupportedJsonName(String fileName) {
        return fileName != null && fileName.toLowerCase(java.util.Locale.ROOT).endsWith(".json");
    }

    private static String importedBookName(String fileName) {
        return sanitizeFileName(fileName);
    }

    private static ClimbingRuleBookDefinition withBookName(
            ClimbingRuleBookDefinition definition,
            String bookName
    ) {
        return new ClimbingRuleBookDefinition(
                definition.formatVersion(),
                bookName,
                definition.coverColor(),
                withProfileName(definition, bookName).profile(),
                definition.activationMode(),
                definition.scope(),
                definition.durationSeconds()
        );
    }

    private static ClimbingRuleBookDefinition withProfileName(
            ClimbingRuleBookDefinition definition,
            String profileName
    ) {
        ClimbingRulesProfile profile = definition.profile();
        ClimbingRulesProfile renamedProfile = new ClimbingRulesProfile(
                profile.formatVersion(),
                profileName,
                profile.stableBlocks(),
                profile.unstableBlocks(),
                profile.unclimbableBlocks(),
                profile.unlistedPolicy(),
                profile.durabilityMultiplierPercent(),
                profile.playerMiningEnabled(),
                profile.unmineableTerminals()
        );
        return new ClimbingRuleBookDefinition(
                definition.formatVersion(),
                definition.bookName(),
                definition.coverColor(),
                renamedProfile,
                definition.activationMode(),
                definition.scope(),
                definition.durationSeconds()
        );
    }

    private static Optional<Path> resolveExistingFile(Path directory, String fileName) {
        if (fileName == null || fileName.isBlank() || !isSupportedJsonName(fileName)) {
            return Optional.empty();
        }
        try {
            Path file = Path.of(fileName);
            if (!file.getFileName().toString().equals(fileName)) {
                return Optional.empty();
            }
            Path normalizedDirectory = directory.normalize();
            Path path = normalizedDirectory.resolve(file).normalize();
            return path.getParent().equals(normalizedDirectory) && Files.isRegularFile(path)
                    ? Optional.of(path)
                    : Optional.empty();
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    public record FileResult<T>(T value, String errorKey) {
        public static <T> FileResult<T> success(T value) {
            return new FileResult<>(value, null);
        }

        public static <T> FileResult<T> error(String errorKey) {
            return new FileResult<>(null, errorKey);
        }

        public boolean success() {
            return errorKey == null;
        }
    }
}
