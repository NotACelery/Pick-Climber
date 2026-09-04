package dev.maicra.pickclimber.rules;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClimbingRulesProfileCodecTest {
    @Test
    void nbtRoundTripPreservesMissingModIds() {
        ResourceLocation missing = ResourceLocation.fromNamespaceAndPath("missing_mod", "route_block");
        ClimbingRulesProfile source = new ClimbingRulesProfile(
                1,
                "Portable",
                Set.of(missing),
                Set.of(),
                Set.of(),
                UnlistedPolicy.UNCLIMBABLE,
                75,
                false,
                false
        );

        CompoundTag encoded = ClimbingRulesProfileCodec.encodeToNbt(source).getOrThrow();
        ClimbingRulesProfile decoded = ClimbingRulesProfileCodec.decodeFromNbt(encoded).getOrThrow();

        assertEquals(source, decoded);
    }

    @Test
    void jsonRoundTripUsesSameCanonicalProfileCodec() {
        ClimbingRulesProfile source = new ClimbingRulesProfile(
                1,
                "JSON Route",
                Set.of(ResourceLocation.fromNamespaceAndPath("minecraft", "stone")),
                Set.of(),
                Set.of(ResourceLocation.fromNamespaceAndPath("minecraft", "bedrock")),
                UnlistedPolicy.USE_PICK_CLIMBER_DEFAULTS,
                75,
                false,
                false
        );

        var encoded = ClimbingRulesProfileCodec.encodeToJson(source).getOrThrow();
        ClimbingRulesProfile decoded = ClimbingRulesProfileCodec.decodeFromJson(encoded).getOrThrow();

        assertEquals(source, decoded);
        String serialized = encoded.toString();
        org.junit.jupiter.api.Assertions.assertTrue(serialized.contains("pickaxe_wear"));
        org.junit.jupiter.api.Assertions.assertFalse(serialized.contains("durability_multiplier"));
    }

    @Test
    void legacyDurabilityMultiplierMigratesToFlatPickaxeWear() {
        String json = """
                {
                  "format_version": 1,
                  "name": "Legacy",
                  "stable": [],
                  "unstable": [],
                  "unclimbable": [],
                  "unlisted_policy": "use_pick_climber_defaults",
                  "durability_multiplier": 1.5,
                  "player_mining": true,
                  "unmineable_terminals": false
                }
                """;
        var element = com.google.gson.JsonParser.parseString(json);
        ClimbingRulesProfile decoded = ClimbingRulesProfileCodec.decodeFromJson(element).getOrThrow();

        assertEquals(23, decoded.pickaxeWear());
    }

}
