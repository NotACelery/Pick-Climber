package dev.maicra.pickclimber.climb;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.player.Player;

public final class PlayerClimbPresentationPreferences {
    private static final Set<UUID> HIDDEN_FAILURE_TEXT = ConcurrentHashMap.newKeySet();

    private PlayerClimbPresentationPreferences() {
    }

    public static boolean failureTextEnabled(Player player) {
        return player == null || failureTextEnabled(player.getUUID());
    }

    public static boolean failureTextEnabled(UUID playerId) {
        return playerId == null || !HIDDEN_FAILURE_TEXT.contains(playerId);
    }

    public static void setFailureTextEnabled(Player player, boolean enabled) {
        if (player != null) {
            setFailureTextEnabled(player.getUUID(), enabled);
        }
    }

    public static void setFailureTextEnabled(UUID playerId, boolean enabled) {
        if (playerId == null) {
            return;
        }
        if (enabled) {
            HIDDEN_FAILURE_TEXT.remove(playerId);
        } else {
            HIDDEN_FAILURE_TEXT.add(playerId);
        }
    }

    public static void clear(Player player) {
        if (player != null) {
            clear(player.getUUID());
        }
    }

    public static void clear(UUID playerId) {
        if (playerId != null) {
            HIDDEN_FAILURE_TEXT.remove(playerId);
        }
    }
}
