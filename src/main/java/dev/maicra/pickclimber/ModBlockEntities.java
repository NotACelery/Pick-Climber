package dev.maicra.pickclimber;

import dev.maicra.pickclimber.rules.block.ClimbingRulesTableBlockEntity;
import dev.maicra.pickclimber.rules.block.ClimbingRuleDispenserBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE,
            PickClimber.MOD_ID
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClimbingRulesTableBlockEntity>>
            CLIMBING_RULES_TABLE = BLOCK_ENTITIES.register(
                    "climbing_rules_table",
                    () -> BlockEntityType.Builder.of(
                            ClimbingRulesTableBlockEntity::new,
                            ModBlocks.CLIMBING_RULES_TABLE.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClimbingRuleDispenserBlockEntity>>
            CLIMBING_RULE_DISPENSER = BLOCK_ENTITIES.register(
                    "climbing_rule_dispenser",
                    () -> BlockEntityType.Builder.of(
                            ClimbingRuleDispenserBlockEntity::new,
                            ModBlocks.CLIMBING_RULE_DISPENSER.get()
                    ).build(null)
            );

    private ModBlockEntities() {
    }
}
