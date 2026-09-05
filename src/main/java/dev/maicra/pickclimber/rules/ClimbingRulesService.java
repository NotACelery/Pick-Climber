package dev.maicra.pickclimber.rules;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import dev.maicra.pickclimber.climb.ClimbManager;
import dev.maicra.pickclimber.rules.persistence.ClimbingRulesSavedData;
import dev.maicra.pickclimber.rules.persistence.RuleDefinitionLibrarySavedData;

public final class ClimbingRulesService {
    private ClimbingRulesService() {
    }

    public static ClimbingRulesValidationResult apply(
            MinecraftServer server,
            ClimbingRulesProfile profile
    ) {
        ClimbingRulesValidationResult validation = ClimbingRulesValidator.validateAndNormalize(profile);
        if (!validation.valid()) {
            return validation;
        }
        ClimbingRuleBookDefinition definition = ClimbingRuleBookDefinition.permanentWorld(
                validation.normalizedProfile().profileName(),
                validation.normalizedProfile()
        );
        applyPermanent(server, definition);
        return validation;
    }

    public static RuleBookApplicationResult applyRuleBook(
            ServerPlayer player,
            ClimbingRuleBookDefinition definition
    ) {
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(definition);
        if (!validation.valid()) {
            return RuleBookApplicationResult.rejected("message.pickclimber.rules.invalid_profile");
        }

        ClimbingRuleBookDefinition normalized = validation.normalizedDefinition();
        if (normalized.authorName().isBlank()) {
            normalized = normalized.withAuthor(player.getUUID().toString(), player.getGameProfile().getName());
        }
        if (normalized.activationMode() == RuleBookActivationMode.PERMANENT) {
            return applyPermanent(player.serverLevel().getServer(), normalized);
        }
        if (normalized.scope() == RuleBookScope.PLAYER) {
            return applyTemporaryPlayer(player, normalized);
        }
        return applyTemporaryWorld(player.serverLevel().getServer(), normalized);
    }

    public static boolean isAlreadyEffectiveFor(ServerPlayer player, ClimbingRuleBookDefinition definition) {
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(definition);
        if (!validation.valid()) {
            return false;
        }
        ClimbingRuleBookDefinition normalized = validation.normalizedDefinition();
        if (normalized.scope() == RuleBookScope.PLAYER) {
            Optional<PlayerRulesSessionStore.Session> playerRules = PlayerRulesSessionStore.get(player);
            if (playerRules.isPresent()) {
                return playerRules.get().definition().profile().mechanicallyEquals(normalized.profile());
            }
        }
        ClimbingRulesSavedData data = ClimbingRulesSavedData.get(player.serverLevel().getServer());
        return data.effectiveDefinition()
                .map(active -> active.profile().mechanicallyEquals(normalized.profile()))
                .orElseGet(() -> isMechanicalDefaults(normalized.profile()));
    }

    public static void restoreDefaults(MinecraftServer server) {
        ClimbingRulesSavedData data = ClimbingRulesSavedData.get(server);
        boolean worldChanged = data.snapshot().active();
        boolean playerChanged = PlayerRulesSessionStore.hasAny();
        if (!worldChanged && !playerChanged) {
            return;
        }
        if (worldChanged) {
            data.restoreDefaults();
        }
        synchronizeWorldAndCancelPlayerOverlays(server, worldChanged);
    }

    public static void tick(MinecraftServer server) {
        ClimbingRulesSavedData data = ClimbingRulesSavedData.get(server);
        long gameTime = server.overworld().getGameTime();
        if (data.expireTemporaryIfDue(gameTime)) {
            synchronizeWorldAndCancelPlayerOverlays(server, true);
        }
        expirePlayerRules(server, gameTime);
    }

    public static void cancelPlayerRules(ServerPlayer player, boolean synchronize) {
        if (PlayerRulesSessionStore.remove(player).isEmpty()) {
            return;
        }
        if (synchronize) {
            PlayerRulesSynchronization.sendToPlayer(player, PlayerRulesSnapshot.inactive());
        }
        ClimbManager.revalidateRules(player);
    }

    public static Optional<ClimbingRulesProfile> activeProfile(MinecraftServer server) {
        return ClimbingRulesSavedData.get(server).activeProfile();
    }

    public static Optional<ClimbingRuleBookDefinition> effectiveDefinition(MinecraftServer server) {
        return ClimbingRulesSavedData.get(server).effectiveDefinition();
    }

    public static WorldRulesSnapshot snapshot(MinecraftServer server) {
        return ClimbingRulesSavedData.get(server).snapshot();
    }

    public static PlayerRulesSnapshot playerSnapshot(ServerPlayer player) {
        ClimbingRulesSavedData data = ClimbingRulesSavedData.get(player.serverLevel().getServer());
        return PlayerRulesSessionStore.snapshot(player, data.policyRevision());
    }

    public static ClimbingRulesRuntimeView runtimeView(Level level) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return ClimbingRulesSavedData.get(serverLevel.getServer()).runtimeView();
        }
        return level.isClientSide()
                ? ClimbingRulesClientState.worldRuntimeView()
                : ClimbingRulesRuntimeView.defaults();
    }

    private static RuleBookApplicationResult applyPermanent(
            MinecraftServer server,
            ClimbingRuleBookDefinition definition
    ) {
        ClimbingRulesSavedData data = ClimbingRulesSavedData.get(server);
        boolean temporaryActive = data.temporaryDefinition().isPresent();
        Optional<ClimbingRuleBookDefinition> current = data.effectiveDefinition();
        if (!temporaryActive && mechanicallyEquals(current, Optional.of(definition))) {
            return RuleBookApplicationResult.rejected("message.pickclimber.rules.no_effective_change");
        }
        if (!temporaryActive && current.isEmpty() && isMechanicalDefaults(definition.profile())) {
            return RuleBookApplicationResult.rejected("message.pickclimber.rules.no_effective_change");
        }

        RuleDefinitionLibrarySavedData.get(server).register(definition.profile());
        data.applyPermanent(definition);
        synchronizeWorldAndCancelPlayerOverlays(server, true);
        return RuleBookApplicationResult.applied("message.pickclimber.rules.permanent_applied");
    }

    private static RuleBookApplicationResult applyTemporaryWorld(
            MinecraftServer server,
            ClimbingRuleBookDefinition definition
    ) {
        ClimbingRulesSavedData data = ClimbingRulesSavedData.get(server);
        long expiresAt = safeExpiry(server.overworld().getGameTime(), definition.durationSeconds());
        Optional<ClimbingRuleBookDefinition> activeTemporary = data.temporaryDefinition();
        if (activeTemporary.isPresent()) {
            if (!activeTemporary.get().profile().mechanicallyEquals(definition.profile())) {
                return RuleBookApplicationResult.rejected("message.pickclimber.rules.temporary_conflict");
            }
            RuleDefinitionLibrarySavedData.get(server).register(definition.profile());
            data.refreshTemporary(definition, expiresAt);
            synchronizeWorldAndCancelPlayerOverlays(server, true);
            return RuleBookApplicationResult.refreshed("message.pickclimber.rules.temporary_refreshed");
        }

        Optional<ClimbingRuleBookDefinition> baseline = data.permanentDefinition();
        if (baseline.isPresent() && baseline.get().profile().mechanicallyEquals(definition.profile())) {
            return RuleBookApplicationResult.rejected("message.pickclimber.rules.no_effective_change");
        }
        if (baseline.isEmpty() && isMechanicalDefaults(definition.profile())) {
            return RuleBookApplicationResult.rejected("message.pickclimber.rules.no_effective_change");
        }

        RuleDefinitionLibrarySavedData.get(server).register(definition.profile());
        data.startTemporary(definition, expiresAt);
        synchronizeWorldAndCancelPlayerOverlays(server, true);
        return RuleBookApplicationResult.applied("message.pickclimber.rules.temporary_applied");
    }

    private static RuleBookApplicationResult applyTemporaryPlayer(
            ServerPlayer player,
            ClimbingRuleBookDefinition definition
    ) {
        MinecraftServer server = player.serverLevel().getServer();
        ClimbingRulesSavedData worldData = ClimbingRulesSavedData.get(server);
        long expiresAt = safeExpiry(server.overworld().getGameTime(), definition.durationSeconds());
        Optional<PlayerRulesSessionStore.Session> active = PlayerRulesSessionStore.get(player);
        if (active.isPresent()) {
            if (!active.get().definition().profile().mechanicallyEquals(definition.profile())) {
                return RuleBookApplicationResult.rejected("message.pickclimber.rules.player_temporary_conflict");
            }
            RuleDefinitionLibrarySavedData.get(server).register(definition.profile());
            PlayerRulesSessionStore.Session refreshed = PlayerRulesSessionStore.refresh(player, definition, expiresAt);
            PlayerRulesSynchronization.sendToPlayer(player, refreshed.snapshot());
            return RuleBookApplicationResult.refreshed("message.pickclimber.rules.player_temporary_refreshed");
        }

        if (worldData.runtimeView().active()) {
            Optional<ClimbingRuleBookDefinition> world = worldData.effectiveDefinition();
            if (world.isPresent() && world.get().profile().mechanicallyEquals(definition.profile())) {
                return RuleBookApplicationResult.rejected("message.pickclimber.rules.no_effective_change");
            }
        } else if (isMechanicalDefaults(definition.profile())) {
            return RuleBookApplicationResult.rejected("message.pickclimber.rules.no_effective_change");
        }

        RuleDefinitionLibrarySavedData.get(server).register(definition.profile());
        PlayerRulesSessionStore.Session started = PlayerRulesSessionStore.start(
                player,
                definition,
                expiresAt,
                worldData.policyRevision()
        );
        PlayerRulesSynchronization.sendToPlayer(player, started.snapshot());
        ClimbManager.revalidateRules(player);
        return RuleBookApplicationResult.applied("message.pickclimber.rules.player_temporary_applied");
    }

    private static void expirePlayerRules(MinecraftServer server, long gameTime) {
        for (ServerPlayer player : PlayerRulesSessionStore.expireDue(server, gameTime)) {
            PlayerRulesSynchronization.sendToPlayer(player, PlayerRulesSnapshot.inactive());
            ClimbManager.revalidateRules(player);
        }
    }

    private static boolean mechanicallyEquals(
            Optional<ClimbingRuleBookDefinition> first,
            Optional<ClimbingRuleBookDefinition> second
    ) {
        if (first.isEmpty() || second.isEmpty()) {
            return first.isEmpty() && second.isEmpty();
        }
        return first.get().profile().mechanicallyEquals(second.get().profile());
    }

    private static boolean isMechanicalDefaults(ClimbingRulesProfile profile) {
        return profile.mechanicallyEquals(ClimbingRulesProfile.defaults("defaults"));
    }

    private static long safeExpiry(long currentGameTime, int durationSeconds) {
        long durationTicks = Math.max(1L, (long) durationSeconds * 20L);
        if (Long.MAX_VALUE - currentGameTime < durationTicks) {
            return Long.MAX_VALUE;
        }
        return currentGameTime + durationTicks;
    }

    private static void synchronizeWorldAndCancelPlayerOverlays(
            MinecraftServer server,
            boolean broadcastWorld
    ) {
        List<UUID> affectedPlayers = PlayerRulesSessionStore.clearAll();
        if (broadcastWorld) {
            ClimbingRulesSynchronization.broadcast(server, snapshot(server));
        }
        for (UUID playerId : affectedPlayers) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null) {
                PlayerRulesSynchronization.sendToPlayer(player, PlayerRulesSnapshot.inactive());
            }
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ClimbManager.revalidateRules(player);
        }
    }
}
