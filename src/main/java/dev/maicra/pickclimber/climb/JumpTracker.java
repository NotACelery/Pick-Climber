package dev.maicra.pickclimber.climb;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class JumpTracker {
    private static final Map<UUID, Long> LAST_REAL_JUMP = new HashMap<>();
    private static final Map<UUID, Long> CONSUMED_JUMP = new HashMap<>();

    private JumpTracker() {
    }

    static void record(ServerPlayer player) {
        LAST_REAL_JUMP.put(player.getUUID(), player.level().getGameTime());
    }

    static boolean hasFreshUnconsumed(ServerPlayer player) {
        long jumpTime = LAST_REAL_JUMP.getOrDefault(player.getUUID(), Long.MIN_VALUE);
        long consumedTime = CONSUMED_JUMP.getOrDefault(player.getUUID(), Long.MIN_VALUE);
        long age = player.level().getGameTime() - jumpTime;
        return jumpTime != Long.MIN_VALUE
                && jumpTime != consumedTime
                && age >= 0L
                && age <= ClimbTuning.JUMP_BOOST_WINDOW_TICKS;
    }

    static void consume(ServerPlayer player) {
        Long jumpTime = LAST_REAL_JUMP.get(player.getUUID());
        if (jumpTime != null) {
            CONSUMED_JUMP.put(player.getUUID(), jumpTime);
        }
    }

    static void markCurrentJumpUnconsumed(ServerPlayer player) {
        LAST_REAL_JUMP.put(player.getUUID(), player.level().getGameTime());
        CONSUMED_JUMP.remove(player.getUUID());
    }

    static void clear(ServerPlayer player) {
        UUID playerId = player.getUUID();
        LAST_REAL_JUMP.remove(playerId);
        CONSUMED_JUMP.remove(playerId);
    }
}
