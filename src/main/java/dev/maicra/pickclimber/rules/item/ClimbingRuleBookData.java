package dev.maicra.pickclimber.rules.item;

import com.mojang.logging.LogUtils;
import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidationResult;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidator;
import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.ClimbingRulesProfileCodec;
import dev.maicra.pickclimber.rules.ClimbingRulesValidationResult;
import dev.maicra.pickclimber.rules.ClimbingRulesValidator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.slf4j.Logger;

import java.util.Optional;

public final class ClimbingRuleBookData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEFINITION_KEY = "pickclimber_climbing_rule_book";
    private static final String LEGACY_PROFILE_KEY = "pickclimber_climbing_rules_profile";

    private ClimbingRuleBookData() {
    }

    public static ItemStack create(ClimbingRuleBookDefinition definition) {
        ItemStack stack = new ItemStack(ModItems.CLIMBING_RULE_BOOK.get());
        if (!write(stack, definition)) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    public static ItemStack create(ClimbingRulesProfile profile) {
        return create(ClimbingRuleBookDefinition.permanentWorld(profile.profileName(), profile));
    }

    public static boolean hasCurrentSchema(ItemStack stack) {
        if (!stack.is(ModItems.CLIMBING_RULE_BOOK.get())) {
            return false;
        }
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return root.contains(DEFINITION_KEY);
    }

    public static Optional<ClimbingRuleBookDefinition> readCurrentDefinitionValidated(ItemStack stack) {
        if (!hasCurrentSchema(stack)) {
            return Optional.empty();
        }
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return ClimbingRuleBookCodec.decodeFromNbt(root.getCompound(DEFINITION_KEY))
                .resultOrPartial(message -> LOGGER.warn("Invalid Climbing Rule Book data: {}", message))
                .flatMap(definition -> {
                    ClimbingRuleBookValidationResult validation =
                            ClimbingRuleBookValidator.validateAndNormalize(definition);
                    return validation.valid()
                            ? Optional.of(validation.normalizedDefinition())
                            : Optional.empty();
                });
    }

    public static Optional<ClimbingRuleBookDefinition> readDefinition(ItemStack stack) {
        if (!stack.is(ModItems.CLIMBING_RULE_BOOK.get())) {
            return Optional.empty();
        }
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (root.contains(DEFINITION_KEY)) {
            return ClimbingRuleBookCodec.decodeFromNbt(root.getCompound(DEFINITION_KEY))
                    .resultOrPartial(message -> LOGGER.warn("Invalid Climbing Rule Book data: {}", message));
        }
        if (root.contains(LEGACY_PROFILE_KEY)) {
            return ClimbingRulesProfileCodec.decodeFromNbt(root.getCompound(LEGACY_PROFILE_KEY))
                    .resultOrPartial(message -> LOGGER.warn("Invalid legacy climbing rules data: {}", message))
                    .map(profile -> new ClimbingRuleBookDefinition(
                            ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                            profile.profileName(),
                            DyeColor.WHITE,
                            profile,
                            dev.maicra.pickclimber.rules.RuleBookActivationMode.PERMANENT,
                            dev.maicra.pickclimber.rules.RuleBookScope.WORLD,
                            0
                    ));
        }
        return Optional.empty();
    }

    public static Optional<ClimbingRuleBookDefinition> readDefinitionValidated(ItemStack stack) {
        return readDefinition(stack).flatMap(definition -> {
            ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(definition);
            return validation.valid() ? Optional.of(validation.normalizedDefinition()) : Optional.empty();
        });
    }

    public static Optional<ClimbingRulesProfile> read(ItemStack stack) {
        return readDefinition(stack).map(ClimbingRuleBookDefinition::profile);
    }

    public static Optional<ClimbingRulesProfile> readValidated(ItemStack stack) {
        return readDefinitionValidated(stack).map(ClimbingRuleBookDefinition::profile);
    }

    public static boolean write(ItemStack stack, ClimbingRuleBookDefinition definition) {
        if (!stack.is(ModItems.CLIMBING_RULE_BOOK.get())) {
            return false;
        }
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(definition);
        if (!validation.valid()) {
            return false;
        }
        Optional<CompoundTag> encoded = ClimbingRuleBookCodec.encodeToNbt(validation.normalizedDefinition())
                .resultOrPartial(message -> LOGGER.warn("Failed to encode Climbing Rule Book: {}", message));
        if (encoded.isEmpty()) {
            return false;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(LEGACY_PROFILE_KEY);
            tag.remove("pickclimber_climbing_rules_revision");
            tag.put(DEFINITION_KEY, encoded.get());
        });
        return true;
    }

    public static boolean writeProfile(ItemStack stack, ClimbingRulesProfile profile) {
        ClimbingRulesValidationResult profileValidation = ClimbingRulesValidator.validateAndNormalize(profile);
        if (!profileValidation.valid()) {
            return false;
        }
        ClimbingRuleBookDefinition definition = readDefinitionValidated(stack)
                .map(existing -> new ClimbingRuleBookDefinition(
                        existing.formatVersion(),
                        profileValidation.normalizedProfile().profileName(),
                        existing.coverColor(),
                        profileValidation.normalizedProfile(),
                        existing.activationMode(),
                        existing.scope(),
                        existing.durationSeconds()
                ))
                .orElseGet(() -> ClimbingRuleBookDefinition.permanentWorld(
                        profileValidation.normalizedProfile().profileName(),
                        profileValidation.normalizedProfile()
                ));
        return write(stack, definition);
    }
}
