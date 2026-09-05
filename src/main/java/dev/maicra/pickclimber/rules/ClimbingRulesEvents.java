package dev.maicra.pickclimber.rules;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import dev.maicra.pickclimber.rules.item.TemporaryRuleBookData;
import dev.maicra.pickclimber.rules.network.RulesEditorSessionStore;

@EventBusSubscriber(modid = PickClimber.MOD_ID)
public final class ClimbingRulesEvents {
    private ClimbingRulesEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ClimbingRulesService.tick(event.getServer());
        TemporaryRuleBookIssuanceService.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        MinecraftServer server = serverPlayer.getServer();
        if (server == null) {
            return;
        }
        migrateRuleBooks(serverPlayer);
        ClimbingRulesSynchronization.sendToPlayer(serverPlayer, ClimbingRulesService.snapshot(server));
        PlayerRulesSynchronization.sendToPlayer(serverPlayer, PlayerRulesSnapshot.inactive());
        TemporaryRuleBookIssuanceService.syncPlayer(serverPlayer);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            RulesEditorSessionStore.invalidate(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TemporaryRuleBookIssuanceService.removeOwnedBooks(serverPlayer);
            ClimbingRulesService.cancelPlayerRules(serverPlayer, true);
            RulesEditorSessionStore.invalidate(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ClimbingRulesService.cancelPlayerRules(serverPlayer, true);
            RulesEditorSessionStore.invalidate(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onTemporaryBookPickup(ItemEntityPickupEvent.Pre event) {
        if (!(event.getPlayer() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ClimbingRuleBookData.resolveDefinition(
                serverPlayer.serverLevel().getServer(), event.getItemEntity().getItem()
        );
        TemporaryRuleBookData.readValidated(event.getItemEntity().getItem()).ifPresent(data -> {
            long gameTime = serverPlayer.serverLevel().getServer().overworld().getGameTime();
            if (TemporaryRuleBookIssuanceService.isUnclaimed(data)) {
                if (!TemporaryRuleBookIssuanceService.claim(event.getItemEntity().getItem(), serverPlayer)) {
                    event.setCanPickup(TriState.FALSE);
                }
                return;
            }
            if (!data.owner().equals(serverPlayer.getUUID())
                    || data.expiresAtGameTime() <= gameTime
                    || !TemporaryRuleBookIssuanceService.isActive(data.owner(), data.issuanceToken(), gameTime)) {
                event.setCanPickup(TriState.FALSE);
            }
        });
    }

    private static void migrateRuleBooks(ServerPlayer player) {
        MinecraftServer server = player.serverLevel().getServer();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ClimbingRuleBookData.resolveDefinition(server, player.getInventory().getItem(slot));
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        TemporaryRuleBookIssuanceService.clear();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ClimbingRulesService.cancelPlayerRules(serverPlayer, false);
            RulesEditorSessionStore.invalidate(serverPlayer);
        }
    }
}
