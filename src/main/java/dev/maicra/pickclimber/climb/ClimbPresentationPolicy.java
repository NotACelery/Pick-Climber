package dev.maicra.pickclimber.climb;

import net.minecraft.world.entity.player.Player;

public interface ClimbPresentationPolicy {
    AnchorIndicatorStatus filterIndicator(Player player, AnchorIndicatorStatus status);

    boolean showFailureText(Player player);
}
