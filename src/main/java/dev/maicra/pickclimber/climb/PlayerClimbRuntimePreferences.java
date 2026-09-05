package dev.maicra.pickclimber.climb;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.entity.player.Player;

public final class PlayerClimbRuntimePreferences {
    private static final Set<UUID> DISABLED_PLAYERS = ConcurrentHashMap.newKeySet();

    private PlayerClimbRuntimePreferences() {
    }

    public static boolean interactionsEnabled(Player player) {
        return player == null || interactionsEnabled(player.getUUID());
    }

    public static boolean interactionsEnabled(UUID playerId) {
        return playerId == null || !DISABLED_PLAYERS.contains(playerId);
    }

    public static void setInteractionsEnabled(Player player, boolean enabled) {
        if (player != null) {
            setInteractionsEnabled(player.getUUID(), enabled);
        }
    }

    public static void setInteractionsEnabled(UUID playerId, boolean enabled) {
        if (playerId == null) {
            return;
        }
        if (enabled) {
            DISABLED_PLAYERS.remove(playerId);
        } else {
            DISABLED_PLAYERS.add(playerId);
        }
    }

    public static void clear(Player player) {
        if (player != null) {
            clear(player.getUUID());
        }
    }

    public static void clear(UUID playerId) {
        if (playerId != null) {
            DISABLED_PLAYERS.remove(playerId);
        }
    }
}
