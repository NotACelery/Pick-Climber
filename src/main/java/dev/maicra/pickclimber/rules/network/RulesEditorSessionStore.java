package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class RulesEditorSessionStore {
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final AtomicInteger NEXT_TOKEN = new AtomicInteger(1);

    private RulesEditorSessionStore() {
    }

    public static int open(
            ServerPlayer player,
            BlockPos tablePosition,
            Operation operation,
            Optional<ClimbingRuleBookDefinition> sourceSnapshot,
            DyeColor allowedCoverColor
    ) {
        int token = NEXT_TOKEN.getAndUpdate(value -> value == Integer.MAX_VALUE ? 1 : value + 1);
        SESSIONS.put(
                player.getUUID(),
                new Session(
                        player.level().dimension().location().toString(),
                        tablePosition.immutable(),
                        token,
                        operation,
                        sourceSnapshot,
                        allowedCoverColor
                )
        );
        return token;
    }

    public static Optional<Session> get(ServerPlayer player) {
        return Optional.ofNullable(SESSIONS.get(player.getUUID()));
    }

    public static void invalidate(ServerPlayer player) {
        SESSIONS.remove(player.getUUID());
    }

    public static void invalidateAt(String dimensionId, BlockPos tablePosition) {
        SESSIONS.entrySet().removeIf(entry -> entry.getValue().dimensionId().equals(dimensionId)
                && entry.getValue().tablePosition().equals(tablePosition));
    }

    public enum Operation {
        CREATE,
        EDIT
    }

    public record Session(
            String dimensionId,
            BlockPos tablePosition,
            int token,
            Operation operation,
            Optional<ClimbingRuleBookDefinition> sourceSnapshot,
            DyeColor allowedCoverColor
    ) {
        public boolean matchesLocation(ServerPlayer player, BlockPos position) {
            return dimensionId.equals(player.level().dimension().location().toString())
                    && tablePosition.equals(position);
        }

        public boolean matchesSource(ClimbingRuleBookDefinition definition) {
            return sourceSnapshot.isPresent() && sourceSnapshot.get().equals(definition);
        }
    }
}
