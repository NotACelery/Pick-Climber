package dev.maicra.pickclimber.rules;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PlayerRulesSessionStore {
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static long nextSessionRevision = Long.MAX_VALUE;

    private PlayerRulesSessionStore() {
    }

    public static Optional<Session> get(ServerPlayer player) {
        return Optional.ofNullable(SESSIONS.get(player.getUUID()));
    }

    public static Optional<ClimbingRuleBookDefinition> definition(ServerPlayer player) {
        return get(player).map(Session::definition);
    }

    public static ClimbingRulesRuntimeView resolve(
            ServerPlayer player,
            ClimbingRulesRuntimeView worldView,
            long worldRevision
    ) {
        return validSession(player, worldRevision)
                .map(Session::runtimeView)
                .orElse(worldView);
    }

    public static long policyRevision(ServerPlayer player, long worldRevision) {
        return validSession(player, worldRevision)
                .map(Session::policyRevision)
                .orElse(worldRevision);
    }

    public static Session start(
            ServerPlayer player,
            ClimbingRuleBookDefinition definition,
            long expiresAtGameTime,
            long worldRevision
    ) {
        Session session = new Session(
                definition,
                Math.max(0L, expiresAtGameTime),
                worldRevision,
                allocateRevision(),
                ClimbingRulesRuntimeView.fromProfile(definition.profile())
        );
        SESSIONS.put(player.getUUID(), session);
        return session;
    }

    public static Session refresh(
            ServerPlayer player,
            ClimbingRuleBookDefinition definition,
            long expiresAtGameTime
    ) {
        Session current = SESSIONS.get(player.getUUID());
        if (current == null) {
            throw new IllegalStateException("Cannot refresh a missing PLAYER rules session");
        }
        Session refreshed = new Session(
                definition,
                Math.max(0L, expiresAtGameTime),
                current.worldRevisionAtStart(),
                current.policyRevision(),
                current.runtimeView()
        );
        SESSIONS.put(player.getUUID(), refreshed);
        return refreshed;
    }

    public static Optional<Session> remove(ServerPlayer player) {
        return Optional.ofNullable(SESSIONS.remove(player.getUUID()));
    }

    public static List<UUID> clearAll() {
        if (SESSIONS.isEmpty()) {
            return List.of();
        }
        List<UUID> affected = List.copyOf(SESSIONS.keySet());
        SESSIONS.clear();
        return affected;
    }

    public static List<ServerPlayer> expireDue(MinecraftServer server, long currentGameTime) {
        List<ServerPlayer> expiredPlayers = new ArrayList<>();
        var iterator = SESSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Session session = entry.getValue();
            if (currentGameTime < session.expiresAtGameTime()) {
                continue;
            }
            iterator.remove();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null) {
                expiredPlayers.add(player);
            }
        }
        return List.copyOf(expiredPlayers);
    }

    public static PlayerRulesSnapshot snapshot(ServerPlayer player, long worldRevision) {
        return validSession(player, worldRevision)
                .map(Session::snapshot)
                .orElseGet(PlayerRulesSnapshot::inactive);
    }

    public static boolean hasAny() {
        return !SESSIONS.isEmpty();
    }

    private static Optional<Session> validSession(ServerPlayer player, long worldRevision) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || session.worldRevisionAtStart() != worldRevision) {
            return Optional.empty();
        }
        return Optional.of(session);
    }

    private static long allocateRevision() {
        long revision = nextSessionRevision;
        nextSessionRevision = nextSessionRevision <= 1L ? Long.MAX_VALUE : nextSessionRevision - 1L;
        return revision;
    }

    public record Session(
            ClimbingRuleBookDefinition definition,
            long expiresAtGameTime,
            long worldRevisionAtStart,
            long policyRevision,
            ClimbingRulesRuntimeView runtimeView
    ) {
        public PlayerRulesSnapshot snapshot() {
            return new PlayerRulesSnapshot(Optional.of(definition), expiresAtGameTime, policyRevision);
        }
    }
}
