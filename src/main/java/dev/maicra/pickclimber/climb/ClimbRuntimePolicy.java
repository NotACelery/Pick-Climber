package dev.maicra.pickclimber.climb;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface ClimbRuntimePolicy {
    boolean interactionsEnabled(Player player);
}
