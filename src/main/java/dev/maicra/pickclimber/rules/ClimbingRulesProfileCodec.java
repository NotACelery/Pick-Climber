package dev.maicra.pickclimber.rules;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ClimbingRulesProfileCodec {
    private static final Codec<Integer> MULTIPLIER_CODEC = Codec.DOUBLE.comapFlatMap(
            ClimbingRulesProfileCodec::decodeMultiplier,
            percent -> percent / 100.0D
    );
    private static final Codec<Set<ResourceLocation>> BLOCK_SET_CODEC = ResourceLocation.CODEC
            .listOf()
            .xmap(LinkedHashSet::new, ClimbingRulesProfileCodec::sortedBlocks);

    public static final Codec<ClimbingRulesProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("format_version").forGetter(ClimbingRulesProfile::formatVersion),
            Codec.STRING.fieldOf("name").forGetter(ClimbingRulesProfile::profileName),
            BLOCK_SET_CODEC.fieldOf("stable").forGetter(ClimbingRulesProfile::stableBlocks),
            BLOCK_SET_CODEC.fieldOf("unstable").forGetter(ClimbingRulesProfile::unstableBlocks),
            BLOCK_SET_CODEC.fieldOf("unclimbable").forGetter(ClimbingRulesProfile::unclimbableBlocks),
            UnlistedPolicy.CODEC.fieldOf("unlisted_policy").forGetter(ClimbingRulesProfile::unlistedPolicy),
            MULTIPLIER_CODEC.fieldOf("durability_multiplier")
                    .forGetter(ClimbingRulesProfile::durabilityMultiplierPercent),
            Codec.BOOL.fieldOf("player_mining").forGetter(ClimbingRulesProfile::playerMiningEnabled),
            Codec.BOOL.optionalFieldOf("unmineable_terminals", false)
                    .forGetter(ClimbingRulesProfile::unmineableTerminals)
    ).apply(instance, ClimbingRulesProfile::new));

    private ClimbingRulesProfileCodec() {
    }

    public static DataResult<CompoundTag> encodeToNbt(ClimbingRulesProfile profile) {
        return CODEC.encodeStart(NbtOps.INSTANCE, profile).flatMap(ClimbingRulesProfileCodec::requireCompound);
    }

    public static DataResult<ClimbingRulesProfile> decodeFromNbt(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag);
    }

    public static DataResult<JsonElement> encodeToJson(ClimbingRulesProfile profile) {
        return CODEC.encodeStart(JsonOps.INSTANCE, profile);
    }

    public static DataResult<ClimbingRulesProfile> decodeFromJson(JsonElement json) {
        return CODEC.parse(JsonOps.INSTANCE, json);
    }

    private static DataResult<Integer> decodeMultiplier(double value) {
        if (!Double.isFinite(value)) {
            return DataResult.error(() -> "Durability multiplier must be finite");
        }
        double percent = value * 100.0D;
        long rounded = Math.round(percent);
        if (Math.abs(percent - rounded) > 0.000001D) {
            return DataResult.error(() -> "Durability multiplier supports 1% increments");
        }
        if (rounded < Integer.MIN_VALUE || rounded > Integer.MAX_VALUE) {
            return DataResult.error(() -> "Durability multiplier is outside the supported numeric range");
        }
        return DataResult.success((int) rounded);
    }

    private static DataResult<CompoundTag> requireCompound(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            return DataResult.success(compoundTag);
        }
        return DataResult.error(() -> "Climbing rules profile did not encode to a compound tag");
    }

    private static List<ResourceLocation> sortedBlocks(Set<ResourceLocation> blocks) {
        List<ResourceLocation> sorted = new ArrayList<>(blocks);
        sorted.sort(Comparator.comparing(ResourceLocation::toString));
        return sorted;
    }
}
