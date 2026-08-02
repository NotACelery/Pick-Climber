package dev.maicra.pickclimber.climb;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Centraliza la prioridad entre manos sin alterar el pipeline de interacción.
 *
 * La mano secundaria gana entre herramientas disponibles. La principal conserva
 * su oportunidad vanilla completa y, si devuelve PASS, Minecraft continúa con
 * la secundaria, donde Pick Climber puede consumir el clic.
 */
public final class ClimbingHandSelector {
    private ClimbingHandSelector() {
    }

    public static InteractionHand preferred(Player player, BlockHitResult hit) {
        if (ClimbManager.canAttemptAnchor(player, InteractionHand.OFF_HAND, hit)) {
            return InteractionHand.OFF_HAND;
        }
        if (ClimbManager.canAttemptAnchor(player, InteractionHand.MAIN_HAND, hit)) {
            return InteractionHand.MAIN_HAND;
        }
        return null;
    }
}
