package dev.maicra.pickclimber.rules.item;

import com.mojang.logging.LogUtils;
import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidationResult;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidator;
import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.DefaultRuleProfileFactory;
import dev.maicra.pickclimber.rules.ClimbingRulesProfileCodec;
import dev.maicra.pickclimber.rules.ClimbingRulesValidationResult;
import dev.maicra.pickclimber.rules.ClimbingRulesValidator;
import dev.maicra.pickclimber.rules.RuleBookActivationMode;
import dev.maicra.pickclimber.rules.RuleBookScope;
import dev.maicra.pickclimber.rules.RuleDefinitionId;
import dev.maicra.pickclimber.rules.persistence.RuleDefinitionLibrarySavedData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.Optional;

public final class ClimbingRuleBookData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String REFERENCE_KEY = "pickclimber_rule_reference";
    private static final String DEFINITION_KEY = "pickclimber_climbing_rule_book";
    private static final String LEGACY_PROFILE_KEY = "pickclimber_climbing_rules_profile";

    private ClimbingRuleBookData() {
    }

    public static ItemStack create(MinecraftServer server, ClimbingRuleBookDefinition definition) {
        ItemStack stack = new ItemStack(ModItems.CLIMBING_RULE_BOOK.get());
        return write(server, stack, definition) ? stack : ItemStack.EMPTY;
    }

    /**
     * Creates a portable reference-only stack when no server context exists (creative/viewer docs).
     * Known Pick Climber default definitions are reconstructed and registered server-side on first use.
     */
    public static ItemStack create(ClimbingRuleBookDefinition definition) {
        ItemStack stack = new ItemStack(ModItems.CLIMBING_RULE_BOOK.get());
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(definition);
        if (!validation.valid()) {
            return ItemStack.EMPTY;
        }
        ClimbingRuleBookDefinition normalized = validation.normalizedDefinition();
        writeReference(stack, Reference.from(RuleDefinitionId.of(normalized.profile()), normalized));
        return stack;
    }

    public static ItemStack create(ClimbingRulesProfile profile) {
        return create(ClimbingRuleBookDefinition.permanentWorld(profile.profileName(), profile));
    }

    public static boolean hasCurrentSchema(ItemStack stack) {
        return readReference(stack).isPresent() || hasEmbeddedDefinition(stack);
    }

    public static Optional<Reference> readReference(ItemStack stack) {
        if (!stack.is(ModItems.CLIMBING_RULE_BOOK.get())) {
            return Optional.empty();
        }
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!custom.contains(REFERENCE_KEY)) {
            return Optional.empty();
        }
        CompoundTag tag = custom.getCompound(REFERENCE_KEY);
        String id = tag.getString("definition_id");
        if (!RuleDefinitionId.isValid(id)) {
            return Optional.empty();
        }
        DyeColor color = dyeColor(tag.getString("cover_color"));
        RuleBookActivationMode activation = enumValue(
                RuleBookActivationMode.class, tag.getString("activation"), RuleBookActivationMode.PERMANENT
        );
        RuleBookScope scope = enumValue(RuleBookScope.class, tag.getString("scope"), RuleBookScope.WORLD);
        return Optional.of(new Reference(
                id,
                tag.getString("book_name"),
                color,
                tag.getString("author_uuid"),
                tag.getString("author_name"),
                activation,
                scope,
                Math.max(0, tag.getInt("duration_seconds")),
                Math.max(0, tag.getInt("assigned_blocks")),
                Math.max(0, tag.getInt("pickaxe_wear")),
                tag.getBoolean("player_mining_enabled"),
                tag.getBoolean("unmineable_terminals")
        ));
    }

    public static Optional<ClimbingRuleBookDefinition> resolveDefinition(
            MinecraftServer server,
            ItemStack stack
    ) {
        Optional<Reference> reference = readReference(stack);
        if (reference.isPresent()) {
            RuleDefinitionLibrarySavedData library = RuleDefinitionLibrarySavedData.get(server);
            Optional<ClimbingRulesProfile> profile = library.resolve(reference.get().definitionId());
            if (profile.isPresent()) {
                return Optional.of(reference.get().toDefinition(profile.get()));
            }
            Optional<ClimbingRulesProfile> knownDefault = resolveKnownDefault(reference.get());
            if (knownDefault.isPresent()) {
                library.register(knownDefault.get());
                return Optional.of(reference.get().toDefinition(knownDefault.get()));
            }
        }

        Optional<ClimbingRuleBookDefinition> embedded = readEmbeddedDefinitionValidated(stack);
        if (embedded.isEmpty()) {
            return Optional.empty();
        }
        if (!write(server, stack, embedded.get())) {
            return Optional.empty();
        }
        return Optional.of(embedded.get());
    }

    public static Optional<ClimbingRuleBookDefinition> readCurrentDefinitionValidated(ItemStack stack) {
        return readEmbeddedDefinitionValidated(stack);
    }

    public static Optional<ClimbingRuleBookDefinition> readDefinition(ItemStack stack) {
        return readEmbeddedDefinition(stack);
    }

    public static Optional<ClimbingRuleBookDefinition> readDefinitionValidated(ItemStack stack) {
        return readEmbeddedDefinitionValidated(stack);
    }

    public static Optional<ClimbingRulesProfile> read(ItemStack stack) {
        return readDefinition(stack).map(ClimbingRuleBookDefinition::profile);
    }

    public static Optional<ClimbingRulesProfile> readValidated(ItemStack stack) {
        return readDefinitionValidated(stack).map(ClimbingRuleBookDefinition::profile);
    }

    public static boolean write(MinecraftServer server, ItemStack stack, ClimbingRuleBookDefinition definition) {
        if (!stack.is(ModItems.CLIMBING_RULE_BOOK.get())) {
            return false;
        }
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(definition);
        if (!validation.valid()) {
            return false;
        }
        ClimbingRuleBookDefinition normalized = validation.normalizedDefinition();
        String id = RuleDefinitionLibrarySavedData.get(server).register(normalized.profile());
        if (id.isEmpty()) {
            return false;
        }
        writeReference(stack, Reference.from(id, normalized));
        return true;
    }

    public static boolean write(ItemStack stack, ClimbingRuleBookDefinition definition) {
        return writeBootstrap(stack, definition);
    }

    public static boolean writeProfile(ItemStack stack, ClimbingRulesProfile profile) {
        ClimbingRulesValidationResult validation = ClimbingRulesValidator.validateAndNormalize(profile);
        if (!validation.valid()) {
            return false;
        }
        ClimbingRuleBookDefinition definition = readEmbeddedDefinitionValidated(stack)
                .map(existing -> new ClimbingRuleBookDefinition(
                        existing.formatVersion(),
                        validation.normalizedProfile().profileName(),
                        existing.coverColor(),
                        validation.normalizedProfile(),
                        existing.activationMode(),
                        existing.scope(),
                        existing.durationSeconds(),
                        existing.authorUuid(),
                        existing.authorName()
                ))
                .orElseGet(() -> ClimbingRuleBookDefinition.permanentWorld(
                        validation.normalizedProfile().profileName(), validation.normalizedProfile()
                ));
        return writeBootstrap(stack, definition);
    }

    private static boolean writeBootstrap(ItemStack stack, ClimbingRuleBookDefinition definition) {
        if (!stack.is(ModItems.CLIMBING_RULE_BOOK.get())) {
            return false;
        }
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(definition);
        if (!validation.valid()) {
            return false;
        }
        ClimbingRuleBookDefinition normalized = validation.normalizedDefinition();
        String id = RuleDefinitionId.of(normalized.profile());
        Optional<CompoundTag> encoded = ClimbingRuleBookCodec.encodeToNbt(normalized).result();
        if (encoded.isEmpty()) {
            return false;
        }
        writeReference(stack, Reference.from(id, normalized));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(DEFINITION_KEY, encoded.get()));
        return true;
    }

    private static void writeReference(ItemStack stack, Reference reference) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, custom -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("definition_id", reference.definitionId());
            tag.putString("book_name", reference.bookName());
            tag.putString("cover_color", reference.coverColor().getName());
            tag.putString("author_uuid", reference.authorUuid());
            tag.putString("author_name", reference.authorName());
            tag.putString("activation", reference.activationMode().name().toLowerCase(Locale.ROOT));
            tag.putString("scope", reference.scope().name().toLowerCase(Locale.ROOT));
            tag.putInt("duration_seconds", reference.durationSeconds());
            tag.putInt("assigned_blocks", reference.assignedBlocks());
            tag.putInt("pickaxe_wear", reference.pickaxeWear());
            tag.putBoolean("player_mining_enabled", reference.playerMiningEnabled());
            tag.putBoolean("unmineable_terminals", reference.unmineableTerminals());
            custom.remove(DEFINITION_KEY);
            custom.remove(LEGACY_PROFILE_KEY);
            custom.remove("pickclimber_climbing_rules_revision");
            custom.put(REFERENCE_KEY, tag);
        });
    }

    private static boolean hasEmbeddedDefinition(ItemStack stack) {
        if (!stack.is(ModItems.CLIMBING_RULE_BOOK.get())) {
            return false;
        }
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return root.contains(DEFINITION_KEY) || root.contains(LEGACY_PROFILE_KEY);
    }

    private static Optional<ClimbingRuleBookDefinition> readEmbeddedDefinitionValidated(ItemStack stack) {
        return readEmbeddedDefinition(stack).flatMap(definition -> {
            ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(definition);
            return validation.valid() ? Optional.of(validation.normalizedDefinition()) : Optional.empty();
        });
    }

    private static Optional<ClimbingRuleBookDefinition> readEmbeddedDefinition(ItemStack stack) {
        if (!stack.is(ModItems.CLIMBING_RULE_BOOK.get())) {
            return Optional.empty();
        }
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (root.contains(DEFINITION_KEY)) {
            return ClimbingRuleBookCodec.decodeFromNbt(root.getCompound(DEFINITION_KEY))
                    .resultOrPartial(message -> LOGGER.warn("Invalid Climbing Rule Book data: {}", message));
        }
        if (root.contains(LEGACY_PROFILE_KEY)) {
            return ClimbingRulesProfileCodec.decodeFromNbt(root.getCompound(LEGACY_PROFILE_KEY)).result()
                    .map(profile -> ClimbingRuleBookDefinition.permanentWorld(profile.profileName(), profile));
        }
        return Optional.empty();
    }

    private static Optional<ClimbingRulesProfile> resolveKnownDefault(Reference reference) {
        ClimbingRulesProfile explicitDefaults = DefaultRuleProfileFactory.create(reference.bookName());
        if (RuleDefinitionId.of(explicitDefaults).equals(reference.definitionId())) {
            return Optional.of(explicitDefaults);
        }
        ClimbingRulesProfile delegatedDefaults = ClimbingRulesProfile.defaults(reference.bookName());
        if (RuleDefinitionId.of(delegatedDefaults).equals(reference.definitionId())) {
            return Optional.of(delegatedDefaults);
        }
        return Optional.empty();
    }

    private static DyeColor dyeColor(String value) {
        for (DyeColor color : DyeColor.values()) {
            if (color.getName().equalsIgnoreCase(value)) {
                return color;
            }
        }
        return DyeColor.WHITE;
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value, E fallback) {
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    public record Reference(
            String definitionId,
            String bookName,
            DyeColor coverColor,
            String authorUuid,
            String authorName,
            RuleBookActivationMode activationMode,
            RuleBookScope scope,
            int durationSeconds,
            int assignedBlocks,
            int pickaxeWear,
            boolean playerMiningEnabled,
            boolean unmineableTerminals
    ) {
        public static Reference from(String id, ClimbingRuleBookDefinition definition) {
            ClimbingRulesProfile profile = definition.profile();
            int assigned = profile.stableBlocks().size()
                    + profile.unstableBlocks().size()
                    + profile.unclimbableBlocks().size();
            return new Reference(
                    id,
                    definition.bookName(),
                    definition.coverColor(),
                    definition.authorUuid(),
                    definition.authorName(),
                    definition.activationMode(), definition.scope(), definition.durationSeconds(), assigned,
                    profile.pickaxeWear(), profile.playerMiningEnabled(), profile.unmineableTerminals()
            );
        }

        public ClimbingRuleBookDefinition toDefinition(ClimbingRulesProfile profile) {
            ClimbingRulesProfile named = new ClimbingRulesProfile(
                    profile.formatVersion(), bookName, profile.stableBlocks(), profile.unstableBlocks(),
                    profile.unclimbableBlocks(), profile.unlistedPolicy(), profile.pickaxeWear(),
                    profile.playerMiningEnabled(), profile.unmineableTerminals()
            );
            return new ClimbingRuleBookDefinition(
                    ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION, bookName, coverColor, named,
                    activationMode, scope, durationSeconds, authorUuid, authorName
            );
        }
    }
}
