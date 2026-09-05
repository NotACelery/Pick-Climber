package dev.maicra.pickclimber;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import dev.maicra.pickclimber.rules.block.ClimbingRuleDispenserBlock;
import dev.maicra.pickclimber.rules.block.ClimbingRulesTableBlock;
import dev.maicra.pickclimber.rules.block.ClimbingRulesTerminalBlock;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PickClimber.MOD_ID);

    public static final DeferredBlock<ClimbingRulesTableBlock> CLIMBING_RULES_TABLE = BLOCKS.registerBlock(
            "climbing_rules_table",
            ClimbingRulesTableBlock::new,
            BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.METAL)
    );

    public static final DeferredBlock<ClimbingRulesTerminalBlock> CLIMBING_RULES_TERMINAL = BLOCKS.registerBlock(
            "climbing_rules_terminal",
            ClimbingRulesTerminalBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.METAL)
    );

    public static final DeferredBlock<ClimbingRuleDispenserBlock> CLIMBING_RULE_DISPENSER = BLOCKS.registerBlock(
            "climbing_rule_dispenser",
            ClimbingRuleDispenserBlock::new,
            BlockBehaviour.Properties.of().strength(3.5F, 6.0F).sound(SoundType.METAL)
    );

    private ModBlocks() {
    }
}
