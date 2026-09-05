package dev.maicra.pickclimber.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.rules.ClimbingRulesClientState;
import dev.maicra.pickclimber.rules.TemporaryRuleBookClientState;

@EventBusSubscriber(modid = PickClimber.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientClimbInputController.onLogout(event.getPlayer());
        ClimbingRulesClientState.clear();
        TemporaryRuleBookClientState.clear();
        if (event.getPlayer() instanceof net.minecraft.client.player.LocalPlayer localPlayer) {
            ClientRuntimePreferenceController.onLogout(localPlayer);
        }
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) {
            return;
        }
        if (ClientClimbInputController.handleAttack(Minecraft.getInstance())) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPickClimberBootstrap.ensureInstalled();
        ClientRuntimePreferenceController.tick(minecraft);
        handleOptionsKey(minecraft);
        ClientClimbInputController.tick(minecraft);
    }

    private static void handleOptionsKey(Minecraft minecraft) {
        while (PickClimberKeyMappings.OPEN_OPTIONS.consumeClick()) {
            if (minecraft.level != null && minecraft.player != null && minecraft.screen == null) {
                minecraft.setScreen(new PickClimberOptionsScreen(null));
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }
        AnchorIndicatorRenderer.render(minecraft, player, event.getGuiGraphics());
        ClimbingRulesTimerHudRenderer.render(minecraft, event.getGuiGraphics());
    }
}
