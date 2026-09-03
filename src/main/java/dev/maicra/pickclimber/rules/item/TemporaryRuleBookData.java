package dev.maicra.pickclimber.rules.item;

import com.mojang.logging.LogUtils;
import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidationResult;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidator;
import dev.maicra.pickclimber.rules.RuleBookActivationMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;

public final class TemporaryRuleBookData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ROOT_KEY = "pickclimber_temporary_rule_book";
    private static final String DEFINITION_KEY = "definition";
    private static final String OWNER_KEY = "owner";
    private static final String ISSUANCE_KEY = "issuance";
    private static final String EXPIRES_KEY = "expires_at_game_time";
    private static final String SOURCE_DIMENSION_KEY = "source_dimension";
    private static final String SOURCE_POSITION_KEY = "source_position";

    private TemporaryRuleBookData() {
    }

    public static ItemStack create(TransportData data) {
        ItemStack stack = new ItemStack(ModItems.TEMPORARY_RULE_BOOK.get());
        return write(stack, data) ? stack : ItemStack.EMPTY;
    }

    public static Optional<TransportData> readValidated(ItemStack stack) {
        if (!stack.is(ModItems.TEMPORARY_RULE_BOOK.get())) {
            return Optional.empty();
        }
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!custom.contains(ROOT_KEY)) {
            return Optional.empty();
        }
        CompoundTag root = custom.getCompound(ROOT_KEY);
        if (!root.hasUUID(OWNER_KEY) || !root.hasUUID(ISSUANCE_KEY) || !root.contains(DEFINITION_KEY)) {
            return Optional.empty();
        }
        ResourceLocation sourceDimension = ResourceLocation.tryParse(root.getString(SOURCE_DIMENSION_KEY));
        if (sourceDimension == null) {
            return Optional.empty();
        }
        Optional<ClimbingRuleBookDefinition> decoded = ClimbingRuleBookCodec.decodeFromNbt(
                root.getCompound(DEFINITION_KEY)
        ).resultOrPartial(message -> LOGGER.warn("Invalid Temporary Rule Book definition: {}", message));
        if (decoded.isEmpty()) {
            return Optional.empty();
        }
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(decoded.get());
        if (!validation.valid()
                || validation.normalizedDefinition().activationMode() != RuleBookActivationMode.TEMPORARY) {
            return Optional.empty();
        }
        return Optional.of(new TransportData(
                root.getUUID(OWNER_KEY),
                root.getUUID(ISSUANCE_KEY),
                Math.max(0L, root.getLong(EXPIRES_KEY)),
                sourceDimension,
                BlockPos.of(root.getLong(SOURCE_POSITION_KEY)),
                validation.normalizedDefinition()
        ));
    }

    public static boolean write(ItemStack stack, TransportData data) {
        if (!stack.is(ModItems.TEMPORARY_RULE_BOOK.get()) || data == null) {
            return false;
        }
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(data.definition());
        if (!validation.valid()
                || validation.normalizedDefinition().activationMode() != RuleBookActivationMode.TEMPORARY) {
            return false;
        }
        Optional<CompoundTag> encoded = ClimbingRuleBookCodec.encodeToNbt(
                validation.normalizedDefinition()
        ).resultOrPartial(message -> LOGGER.warn("Failed to encode Temporary Rule Book definition: {}", message));
        if (encoded.isEmpty()) {
            return false;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, custom -> {
            CompoundTag root = new CompoundTag();
            root.putUUID(OWNER_KEY, data.owner());
            root.putUUID(ISSUANCE_KEY, data.issuanceToken());
            root.putLong(EXPIRES_KEY, Math.max(0L, data.expiresAtGameTime()));
            root.putString(SOURCE_DIMENSION_KEY, data.sourceDimension().toString());
            root.putLong(SOURCE_POSITION_KEY, data.sourcePosition().asLong());
            root.put(DEFINITION_KEY, encoded.get());
            custom.put(ROOT_KEY, root);
        });
        return true;
    }

    public static boolean isExpired(TransportData data, long gameTime) {
        return data.expiresAtGameTime() <= gameTime;
    }

    public record TransportData(
            UUID owner,
            UUID issuanceToken,
            long expiresAtGameTime,
            ResourceLocation sourceDimension,
            BlockPos sourcePosition,
            ClimbingRuleBookDefinition definition
    ) {
    }
}
