package dev.maicra.pickclimber.climb;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Centralizes hand priority without altering the interaction pipeline.
 *
 * The off hand wins among available tools. The main hand retains its full vanilla
 * opportunity and, if it returns PASS, Minecraft continues with the off hand,
 * where Pick Climber may consume the click.
 *
 * Runtime right-click priority is enforced later in NeoForge's ITEM_AFTER_BLOCK
 * phase, so every block gets its normal interaction before Pick Climber anchors.
 * The menu-provider check here is retained only as a conservative HUD/selection
 * hint for blocks that expose a provider directly.
 */
public final class ClimbingHandSelector {
    private ClimbingHandSelector() {
    }

    public static InteractionHand preferred(Player player, BlockHitResult hit) {
        InteractionHand preferred = null;

        if (ClimbManager.canAttemptAnchor(player, InteractionHand.OFF_HAND, hit)) {
            preferred = InteractionHand.OFF_HAND;
        } else if (ClimbManager.canAttemptAnchor(player, InteractionHand.MAIN_HAND, hit)) {
            preferred = InteractionHand.MAIN_HAND;
        }

        if (preferred == null || preservesVanillaMenuUse(player, hit)) {
            return null;
        }

        return preferred;
    }

    /**
     * A vanilla or modded menu identified through {@link BlockState#getMenuProvider}
     * keeps priority unless the player uses the secondary action (Shift).
     *
     * This query neither executes the interaction nor opens the menu; it only
     * checks whether the block declares a provider. This avoids hardcoded lists
     * of furnaces, chests, or tables and keeps the visual indicator consistent
     * with the actual click.
     */
    public static boolean preservesVanillaMenuUse(Player player, BlockHitResult hit) {
        if (player.isSecondaryUseActive()) {
            return false;
        }

        BlockState state = player.level().getBlockState(hit.getBlockPos());
        return state.getMenuProvider(player.level(), hit.getBlockPos()) != null;
    }
}
