package dev.maicra.pickclimber.client;

import dev.maicra.pickclimber.climb.AnchorIndicatorStatus;
import dev.maicra.pickclimber.climb.ClimbManager;
import dev.maicra.pickclimber.climb.ClimbPresentationGate;
import dev.maicra.pickclimber.climb.ClimbRuntimeGate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

final class AnchorIndicatorPolicy {
    private AnchorIndicatorPolicy() {
    }

    static AnchorIndicatorStatus statusFor(Player player, BlockHitResult hit) {
        if (!ClimbRuntimeGate.interactionsEnabled(player)) {
            return AnchorIndicatorStatus.NONE;
        }
        return ClimbPresentationGate.filterIndicator(
                player,
                ClimbManager.anchorIndicatorStatus(player, hit)
        );
    }
}
