package dev.maicra.pickclimber.rules.item;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.RuleBookActivationMode;
import dev.maicra.pickclimber.rules.RuleBookScope;
import dev.maicra.pickclimber.rules.RuleDefinitionId;
import dev.maicra.pickclimber.rules.TemporaryRuleBookIssuanceService;
import dev.maicra.pickclimber.rules.persistence.RuleDefinitionLibrarySavedData;

public final class TemporaryRuleBookData {
    private static final String ROOT_KEY = "pickclimber_temporary_rule_book";
    private static final String DEFINITION_ID_KEY = "definition_id";
    private static final String OWNER_KEY = "owner";
    private static final String ISSUANCE_KEY = "issuance";
    private static final String EXPIRES_KEY = "expires_at_game_time";
    private static final String SOURCE_DIMENSION_KEY = "source_dimension";
    private static final String SOURCE_POSITION_KEY = "source_position";
    private static final String BOOK_NAME_KEY = "book_name";
    private static final String COVER_COLOR_KEY = "cover_color";
    private static final String DURATION_SECONDS_KEY = "duration_seconds";
    private static final String AUTHOR_UUID_KEY = "author_uuid";
    private static final String AUTHOR_NAME_KEY = "author_name";
    private static final String START_COUNTER_ON_PICKUP_KEY = "start_counter_on_pickup";

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
        if (!root.hasUUID(OWNER_KEY) || !root.hasUUID(ISSUANCE_KEY)) {
            return Optional.empty();
        }
        String definitionId = root.getString(DEFINITION_ID_KEY);
        if (!RuleDefinitionId.isValid(definitionId)) {
            return Optional.empty();
        }
        ResourceLocation sourceDimension = ResourceLocation.tryParse(root.getString(SOURCE_DIMENSION_KEY));
        if (sourceDimension == null) {
            return Optional.empty();
        }
        int durationSeconds = Math.max(1, Math.min(60, root.getInt(DURATION_SECONDS_KEY)));
        return Optional.of(new TransportData(
                root.getUUID(OWNER_KEY),
                root.getUUID(ISSUANCE_KEY),
                Math.max(0L, root.getLong(EXPIRES_KEY)),
                sourceDimension,
                BlockPos.of(root.getLong(SOURCE_POSITION_KEY)),
                definitionId,
                root.getString(BOOK_NAME_KEY),
                dyeColor(root.getString(COVER_COLOR_KEY)),
                durationSeconds,
                root.getString(AUTHOR_UUID_KEY),
                root.getString(AUTHOR_NAME_KEY),
                root.getBoolean(START_COUNTER_ON_PICKUP_KEY)
        ));
    }

    public static Optional<ClimbingRuleBookDefinition> resolveDefinition(
            MinecraftServer server,
            TransportData data
    ) {
        return RuleDefinitionLibrarySavedData.get(server).resolve(data.definitionId()).map(profile ->
                new ClimbingRuleBookDefinition(
                        ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                        data.bookName(),
                        data.coverColor(),
                        profile,
                        RuleBookActivationMode.TEMPORARY,
                        RuleBookScope.PLAYER,
                        data.durationSeconds(),
                        data.authorUuid(),
                        data.authorName()
                )
        );
    }

    public static boolean write(ItemStack stack, TransportData data) {
        if (!stack.is(ModItems.TEMPORARY_RULE_BOOK.get())
                || data == null
                || !RuleDefinitionId.isValid(data.definitionId())) {
            return false;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, custom -> {
            CompoundTag root = new CompoundTag();
            root.putUUID(OWNER_KEY, data.owner());
            root.putUUID(ISSUANCE_KEY, data.issuanceToken());
            root.putLong(EXPIRES_KEY, Math.max(0L, data.expiresAtGameTime()));
            root.putString(SOURCE_DIMENSION_KEY, data.sourceDimension().toString());
            root.putLong(SOURCE_POSITION_KEY, data.sourcePosition().asLong());
            root.putString(DEFINITION_ID_KEY, data.definitionId());
            root.putString(BOOK_NAME_KEY, data.bookName());
            root.putString(COVER_COLOR_KEY, data.coverColor().getName());
            root.putInt(DURATION_SECONDS_KEY, Math.max(1, Math.min(60, data.durationSeconds())));
            root.putString(AUTHOR_UUID_KEY, data.authorUuid());
            root.putString(AUTHOR_NAME_KEY, data.authorName());
            root.putBoolean(START_COUNTER_ON_PICKUP_KEY, data.startCounterOnPickup());
            custom.put(ROOT_KEY, root);
        });
        return true;
    }

    public static Optional<Long> readExpiry(ItemStack stack) {
        if (!stack.is(ModItems.TEMPORARY_RULE_BOOK.get())) {
            return Optional.empty();
        }
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!custom.contains(ROOT_KEY)) {
            return Optional.empty();
        }
        return Optional.of(Math.max(0L, custom.getCompound(ROOT_KEY).getLong(EXPIRES_KEY)));
    }

    public static Optional<DisplayMetadata> readDisplayMetadata(ItemStack stack) {
        if (!stack.is(ModItems.TEMPORARY_RULE_BOOK.get())) {
            return Optional.empty();
        }
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!custom.contains(ROOT_KEY)) {
            return Optional.empty();
        }
        CompoundTag root = custom.getCompound(ROOT_KEY);
        String bookName = root.getString(BOOK_NAME_KEY);
        if (bookName.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new DisplayMetadata(bookName, dyeColor(root.getString(COVER_COLOR_KEY))));
    }

    public static boolean isExpired(TransportData data, long gameTime) {
        if (data.startCounterOnPickup()
                && TemporaryRuleBookIssuanceService.UNCLAIMED_OWNER
                .equals(data.owner())) {
            return false;
        }
        return data.expiresAtGameTime() <= gameTime;
    }

    private static DyeColor dyeColor(String value) {
        for (DyeColor color : DyeColor.values()) {
            if (color.getName().equalsIgnoreCase(value)) {
                return color;
            }
        }
        return DyeColor.WHITE;
    }

    public record DisplayMetadata(String bookName, DyeColor coverColor) {
    }

    public record TransportData(
            UUID owner,
            UUID issuanceToken,
            long expiresAtGameTime,
            ResourceLocation sourceDimension,
            BlockPos sourcePosition,
            String definitionId,
            String bookName,
            DyeColor coverColor,
            int durationSeconds,
            String authorUuid,
            String authorName,
            boolean startCounterOnPickup
    ) {
        public TransportData {
            definitionId = definitionId == null ? "" : definitionId;
            bookName = bookName == null ? "" : bookName;
            coverColor = coverColor == null ? DyeColor.WHITE : coverColor;
            authorUuid = authorUuid == null ? "" : authorUuid;
            authorName = authorName == null ? "" : authorName;
        }
    }
}
