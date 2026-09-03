package dev.maicra.pickclimber.rules;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeColor;

import java.util.Arrays;

public final class ClimbingRuleBookCodec {
    private static final Codec<DyeColor> DYE_COLOR_CODEC = Codec.STRING.comapFlatMap(
            ClimbingRuleBookCodec::decodeDyeColor,
            DyeColor::getName
    );
    private static final Codec<RuleBookActivationMode> ACTIVATION_CODEC = Codec.STRING.comapFlatMap(
            value -> decodeEnum(RuleBookActivationMode.class, value, "activation mode"),
            value -> value.name().toLowerCase()
    );
    private static final Codec<RuleBookScope> SCOPE_CODEC = Codec.STRING.comapFlatMap(
            value -> decodeEnum(RuleBookScope.class, value, "scope"),
            value -> value.name().toLowerCase()
    );

    public static final Codec<ClimbingRuleBookDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("format_version").forGetter(ClimbingRuleBookDefinition::formatVersion),
            Codec.STRING.fieldOf("book_name").forGetter(ClimbingRuleBookDefinition::bookName),
            DYE_COLOR_CODEC.optionalFieldOf("cover_color", DyeColor.WHITE)
                    .forGetter(ClimbingRuleBookDefinition::coverColor),
            ClimbingRulesProfileCodec.CODEC.fieldOf("profile").forGetter(ClimbingRuleBookDefinition::profile),
            ACTIVATION_CODEC.fieldOf("activation").forGetter(ClimbingRuleBookDefinition::activationMode),
            SCOPE_CODEC.fieldOf("scope").forGetter(ClimbingRuleBookDefinition::scope),
            Codec.INT.optionalFieldOf("duration_seconds", 0).forGetter(ClimbingRuleBookDefinition::durationSeconds)
    ).apply(instance, ClimbingRuleBookDefinition::new));

    private ClimbingRuleBookCodec() {
    }

    public static DataResult<CompoundTag> encodeToNbt(ClimbingRuleBookDefinition definition) {
        return CODEC.encodeStart(NbtOps.INSTANCE, definition).flatMap(ClimbingRuleBookCodec::requireCompound);
    }

    public static DataResult<ClimbingRuleBookDefinition> decodeFromNbt(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag);
    }

    public static DataResult<JsonElement> encodeToJson(ClimbingRuleBookDefinition definition) {
        return CODEC.encodeStart(JsonOps.INSTANCE, definition);
    }

    public static DataResult<ClimbingRuleBookDefinition> decodeFromJson(JsonElement json) {
        return CODEC.parse(JsonOps.INSTANCE, json);
    }

    private static DataResult<DyeColor> decodeDyeColor(String value) {
        return Arrays.stream(DyeColor.values())
                .filter(color -> color.getName().equalsIgnoreCase(value))
                .findFirst()
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "Unknown dye color: " + value));
    }

    private static <E extends Enum<E>> DataResult<E> decodeEnum(Class<E> type, String value, String label) {
        try {
            return DataResult.success(Enum.valueOf(type, value.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return DataResult.error(() -> "Unknown Rule Book " + label + ": " + value);
        }
    }

    private static DataResult<CompoundTag> requireCompound(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            return DataResult.success(compoundTag);
        }
        return DataResult.error(() -> "Climbing Rule Book did not encode to a compound tag");
    }
}
