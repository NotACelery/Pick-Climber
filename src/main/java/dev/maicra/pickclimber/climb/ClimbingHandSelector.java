package dev.maicra.pickclimber.climb;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Centraliza la prioridad entre manos sin alterar el pipeline de interacción.
 *
 * La mano secundaria gana entre herramientas disponibles. La principal conserva
 * su oportunidad vanilla completa y, si devuelve PASS, Minecraft continúa con
 * la secundaria, donde Pick Climber puede consumir el clic.
 *
 * Los bloques que exponen un menú conservan su clic derecho vanilla. Para usar
 * una de sus caras como ancla hay que mantener Shift, igual que al colocar un
 * bloque sobre un cofre, horno o mesa de crafteo sin abrir su interfaz.
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

        if (preferred == null || mustPreserveVanillaMenuUse(player, hit)) {
            return null;
        }

        return preferred;
    }

    /**
     * Un menú vanilla/modded identificado mediante {@link BlockState#getMenuProvider}
     * mantiene prioridad mientras el jugador no use la acción secundaria (Shift).
     *
     * La consulta no ejecuta la interacción ni abre el menú; solo comprueba que
     * el bloque declare un proveedor. De este modo no hay listas hardcodeadas de
     * hornos, cofres o mesas y el indicador visual comparte la misma regla que el
     * clic real.
     */
    private static boolean mustPreserveVanillaMenuUse(Player player, BlockHitResult hit) {
        if (player.isSecondaryUseActive()) {
            return false;
        }

        BlockState state = player.level().getBlockState(hit.getBlockPos());
        return state.getMenuProvider(player.level(), hit.getBlockPos()) != null;
    }
}
