package dev.maicra.pickclimber.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.function.UnaryOperator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import net.neoforged.fml.loading.FMLPaths;

import org.slf4j.Logger;

public final class PickClimberClientOptionsStore {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int CONFIG_VERSION = 3;
    private static final String LEGACY_PICKAXE_STYLE = "pickaxe_outline";
    private static final String LEGACY_ICON_OPACITY_KEY = "iconOpacity";
    private static final String LEGACY_BOX_OPACITY_KEY = "boxOpacity";
    private static final String LEGACY_COLOR_INTENSITY_KEY = "colorIntensity";
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("pickclimber-client.json");

    private static PickClimberClientOptions current = PickClimberClientOptions.defaults();
    private static boolean loaded;

    private PickClimberClientOptionsStore() {
    }

    public static synchronized PickClimberClientOptions current() {
        ensureLoaded();
        return current;
    }

    public static synchronized void update(UnaryOperator<PickClimberClientOptions> update) {
        ensureLoaded();
        PickClimberClientOptions next = update.apply(current);
        next = next == null ? PickClimberClientOptions.defaults() : next;
        if (next.equals(current)) {
            return;
        }
        current = next;
        save();
    }

    public static synchronized void resetToDefaults() {
        ensureLoaded();
        PickClimberClientOptions next = current.resetToDefaults();
        if (!next.equals(current)) {
            current = next;
            save();
        }
    }

    public static synchronized void reload() {
        loaded = false;
        ensureLoaded();
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        current = PickClimberClientOptions.defaults();
        if (!Files.isRegularFile(CONFIG_PATH)) {
            return;
        }

        boolean normalizeAfterLoad = false;
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            int configVersion = intValue(json, "configVersion", 0);
            if (configVersion > CONFIG_VERSION) {
                LOGGER.warn(
                        "Pick Climber client options use newer config version {}; known fields will still be loaded",
                        configVersion
                );
            }
            current = fromJson(json, current, configVersion);
            normalizeAfterLoad = requiresNormalization(json, configVersion, current);
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Unable to read Pick Climber client options; defaults will be used", exception);
            current = PickClimberClientOptions.defaults();
        }

        if (normalizeAfterLoad) {
            save();
        }
    }

    private static PickClimberClientOptions fromJson(
            JsonObject json,
            PickClimberClientOptions defaults,
            int configVersion
    ) {
        return new PickClimberClientOptions(
                enumValue(json, "indicatorMode", IndicatorMode.class, defaults.indicatorMode()),
                indicatorStyle(json, defaults.indicatorStyle()),
                booleanValue(json, "showUnclimbableIndicator", defaults.showUnclimbableIndicator()),
                doubleValue(json, "iconScale", defaults.iconScale()),
                transparencyValue(
                        json,
                        "iconTransparency",
                        LEGACY_ICON_OPACITY_KEY,
                        defaults.iconTransparency(),
                        configVersion
                ),
                colorIntensity(
                        json,
                        "iconColorIntensity",
                        defaults.iconColorIntensity(),
                        configVersion
                ),
                booleanValue(json, "showIndicatorBox", defaults.showIndicatorBox()),
                transparencyValue(
                        json,
                        "boxTransparency",
                        LEGACY_BOX_OPACITY_KEY,
                        defaults.boxTransparency(),
                        configVersion
                ),
                colorIntensity(
                        json,
                        "boxColorIntensity",
                        defaults.boxColorIntensity(),
                        configVersion
                ),
                booleanValue(json, "showFailureText", defaults.showFailureText()),
                booleanValue(json, "interactionsEnabled", defaults.interactionsEnabled())
        );
    }

    private static double transparencyValue(
            JsonObject json,
            String transparencyKey,
            String legacyOpacityKey,
            double fallback,
            int configVersion
    ) {
        if (json.has(transparencyKey)) {
            return doubleValue(json, transparencyKey, fallback);
        }
        if (configVersion <= 1 && json.has(legacyOpacityKey)) {
            return 1.0D - doubleValue(json, legacyOpacityKey, 1.0D - fallback);
        }
        return fallback;
    }

    private static IndicatorStyle indicatorStyle(JsonObject json, IndicatorStyle fallback) {
        if (!json.has("indicatorStyle")) {
            return fallback;
        }
        try {
            String value = json.get("indicatorStyle").getAsString();
            if (LEGACY_PICKAXE_STYLE.equalsIgnoreCase(value)) {
                return IndicatorStyle.PICKAXE;
            }
            return IndicatorStyle.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private static IndicatorColorIntensity colorIntensity(
            JsonObject json,
            String key,
            IndicatorColorIntensity fallback,
            int configVersion
    ) {
        if (json.has(key)) {
            return enumValue(json, key, IndicatorColorIntensity.class, fallback);
        }
        if (configVersion <= 2 && json.has(LEGACY_COLOR_INTENSITY_KEY)) {
            return enumValue(
                    json,
                    LEGACY_COLOR_INTENSITY_KEY,
                    IndicatorColorIntensity.class,
                    fallback
            );
        }
        return fallback;
    }

    private static boolean requiresNormalization(
            JsonObject json,
            int configVersion,
            PickClimberClientOptions options
    ) {
        if (configVersion > CONFIG_VERSION) {
            return false;
        }
        return !toJson(options).equals(json);
    }

    private static <T extends Enum<T>> T enumValue(
            JsonObject json,
            String key,
            Class<T> type,
            T fallback
    ) {
        if (!json.has(key)) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, json.get(key).getAsString().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | UnsupportedOperationException exception) {
            return fallback;
        }
    }

    private static boolean booleanValue(JsonObject json, String key, boolean fallback) {
        try {
            return json.has(key) ? json.get(key).getAsBoolean() : fallback;
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private static double doubleValue(JsonObject json, String key, double fallback) {
        try {
            return json.has(key) ? json.get(key).getAsDouble() : fallback;
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private static int intValue(JsonObject json, String key, int fallback) {
        try {
            return json.has(key) ? json.get(key).getAsInt() : fallback;
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private static void save() {
        JsonObject json = toJson(current);
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Path temporaryPath = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporaryPath)) {
                GSON.toJson(json, writer);
            }
            Files.move(
                    temporaryPath,
                    CONFIG_PATH,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (IOException atomicMoveFailure) {
            saveWithoutAtomicMove(json, atomicMoveFailure);
            deleteTemporaryFile();
        }
    }

    private static JsonObject toJson(PickClimberClientOptions options) {
        JsonObject json = new JsonObject();
        json.addProperty("configVersion", CONFIG_VERSION);
        json.addProperty("indicatorMode", options.indicatorMode().name().toLowerCase(Locale.ROOT));
        json.addProperty("indicatorStyle", options.indicatorStyle().name().toLowerCase(Locale.ROOT));
        json.addProperty("showUnclimbableIndicator", options.showUnclimbableIndicator());
        json.addProperty("iconScale", options.iconScale());
        json.addProperty("iconTransparency", options.iconTransparency());
        json.addProperty("iconColorIntensity", options.iconColorIntensity().name().toLowerCase(Locale.ROOT));
        json.addProperty("showIndicatorBox", options.showIndicatorBox());
        json.addProperty("boxTransparency", options.boxTransparency());
        json.addProperty("boxColorIntensity", options.boxColorIntensity().name().toLowerCase(Locale.ROOT));
        json.addProperty("showFailureText", options.showFailureText());
        json.addProperty("interactionsEnabled", options.interactionsEnabled());
        return json;
    }

    private static void saveWithoutAtomicMove(JsonObject json, IOException atomicMoveFailure) {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(json, writer);
        } catch (IOException exception) {
            exception.addSuppressed(atomicMoveFailure);
            LOGGER.warn("Unable to save Pick Climber client options", exception);
        }
    }

    private static void deleteTemporaryFile() {
        Path temporaryPath = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
        try {
            Files.deleteIfExists(temporaryPath);
        } catch (IOException exception) {
            LOGGER.debug("Unable to remove temporary Pick Climber options file {}", temporaryPath, exception);
        }
    }
}
