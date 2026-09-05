package dev.maicra.pickclimber.rules.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.logging.LogUtils;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import org.slf4j.Logger;

import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.ClimbingRulesProfileCodec;
import dev.maicra.pickclimber.rules.ClimbingRulesValidationResult;
import dev.maicra.pickclimber.rules.ClimbingRulesValidator;
import dev.maicra.pickclimber.rules.RuleDefinitionId;

public final class RuleDefinitionLibrarySavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DATA_NAME = "pickclimber_rule_definition_library";
    private static final String DEFINITIONS_KEY = "definitions";
    private static final Factory<RuleDefinitionLibrarySavedData> FACTORY = new Factory<>(
            RuleDefinitionLibrarySavedData::new,
            RuleDefinitionLibrarySavedData::load
    );

    private final Map<String, ClimbingRulesProfile> definitions = new HashMap<>();

    public static RuleDefinitionLibrarySavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public String register(ClimbingRulesProfile profile) {
        ClimbingRulesValidationResult validation = ClimbingRulesValidator.validateAndNormalize(profile);
        if (!validation.valid()) {
            return "";
        }
        ClimbingRulesProfile normalized = validation.normalizedProfile();
        String id = RuleDefinitionId.of(normalized);
        if (!definitions.containsKey(id)) {
            definitions.put(id, normalized);
            setDirty();
        }
        return id;
    }

    public Optional<ClimbingRulesProfile> resolve(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag definitionsTag = new CompoundTag();
        definitions.forEach((id, profile) -> ClimbingRulesProfileCodec.encodeToNbt(profile)
                .resultOrPartial(message -> LOGGER.warn("Failed to save rule definition {}: {}", id, message))
                .ifPresent(profileTag -> definitionsTag.put(id, profileTag)));
        tag.put(DEFINITIONS_KEY, definitionsTag);
        return tag;
    }

    private static RuleDefinitionLibrarySavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        RuleDefinitionLibrarySavedData data = new RuleDefinitionLibrarySavedData();
        if (!tag.contains(DEFINITIONS_KEY)) {
            return data;
        }
        CompoundTag definitionsTag = tag.getCompound(DEFINITIONS_KEY);
        for (String id : definitionsTag.getAllKeys()) {
            if (!RuleDefinitionId.isValid(id)) {
                continue;
            }
            ClimbingRulesProfileCodec.decodeFromNbt(definitionsTag.getCompound(id))
                    .resultOrPartial(message -> LOGGER.warn("Ignoring rule definition {}: {}", id, message))
                    .ifPresent(profile -> {
                        ClimbingRulesValidationResult validation = ClimbingRulesValidator.validateAndNormalize(profile);
                        if (validation.valid() && RuleDefinitionId.of(validation.normalizedProfile()).equals(id)) {
                            data.definitions.put(id, validation.normalizedProfile());
                        }
                    });
        }
        return data;
    }
}
