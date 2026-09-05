package dev.maicra.pickclimber.rules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public final class ClimbingRulesProfileCodec {
    private static final Codec<Set<ResourceLocation>> BLOCK_SET_CODEC = ResourceLocation.CODEC
            .listOf()
            .xmap(LinkedHashSet::new, ClimbingRulesProfileCodec::sortedBlocks);

    private static final Codec<ClimbingRulesProfile> CURRENT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
            Codec.INT.fieldOf("format_version").forGetter(ClimbingRulesProfile::formatVersion),
            Codec.STRING.fieldOf("name").forGetter(ClimbingRulesProfile::profileName),
            BLOCK_SET_CODEC.fieldOf("stable").forGetter(ClimbingRulesProfile::stableBlocks),
            BLOCK_SET_CODEC.fieldOf("unstable").forGetter(ClimbingRulesProfile::unstableBlocks),
            BLOCK_SET_CODEC.fieldOf("unclimbable").forGetter(ClimbingRulesProfile::unclimbableBlocks),
            UnlistedPolicy.CODEC.fieldOf("unlisted_policy").forGetter(ClimbingRulesProfile::unlistedPolicy),
            Codec.INT.fieldOf("pickaxe_wear").forGetter(ClimbingRulesProfile::pickaxeWear),
            Codec.BOOL.fieldOf("player_mining").forGetter(ClimbingRulesProfile::playerMiningEnabled),
            Codec.BOOL.optionalFieldOf("unmineable_terminals", false)
                    .forGetter(ClimbingRulesProfile::unmineableTerminals)
            ).apply(instance, ClimbingRulesProfile::new)
    );

    private static final Codec<ClimbingRulesProfile> LEGACY_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
            Codec.INT.fieldOf("format_version").forGetter(ClimbingRulesProfile::formatVersion),
            Codec.STRING.fieldOf("name").forGetter(ClimbingRulesProfile::profileName),
            BLOCK_SET_CODEC.fieldOf("stable").forGetter(ClimbingRulesProfile::stableBlocks),
            BLOCK_SET_CODEC.fieldOf("unstable").forGetter(ClimbingRulesProfile::unstableBlocks),
            BLOCK_SET_CODEC.fieldOf("unclimbable").forGetter(ClimbingRulesProfile::unclimbableBlocks),
            UnlistedPolicy.CODEC.fieldOf("unlisted_policy").forGetter(ClimbingRulesProfile::unlistedPolicy),
            Codec.DOUBLE.fieldOf("durability_multiplier")
                    .forGetter(profile -> profile.pickaxeWear() / 15.0D),
            Codec.BOOL.fieldOf("player_mining").forGetter(ClimbingRulesProfile::playerMiningEnabled),
            Codec.BOOL.optionalFieldOf("unmineable_terminals", false)
                    .forGetter(ClimbingRulesProfile::unmineableTerminals)
            ).apply(instance, ClimbingRulesProfileCodec::fromLegacy)
    );

    public static final Codec<ClimbingRulesProfile> CODEC = Codec.either(CURRENT_CODEC, LEGACY_CODEC)
            .xmap(either -> either.map(profile -> profile, profile -> profile), Either::left);

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

    private static ClimbingRulesProfile fromLegacy(
            int formatVersion,
            String profileName,
            Set<ResourceLocation> stable,
            Set<ResourceLocation> unstable,
            Set<ResourceLocation> unclimbable,
            UnlistedPolicy policy,
            double multiplier,
            boolean playerMining,
            boolean unmineableTerminals
    ) {
        int migratedWear = (int) Math.round(ClimbingRulesProfile.DEFAULT_PICKAXE_WEAR * multiplier);
        migratedWear = Math.max(0, Math.min(ClimbingRulesProfile.MAX_PICKAXE_WEAR, migratedWear));
        return new ClimbingRulesProfile(
                formatVersion,
                profileName,
                stable,
                unstable,
                unclimbable,
                policy,
                migratedWear,
                playerMining,
                unmineableTerminals
        );
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
