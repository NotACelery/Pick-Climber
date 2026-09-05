package dev.maicra.pickclimber.client;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import dev.maicra.pickclimber.climb.ClimbManager;
import dev.maicra.pickclimber.climb.PlayerClimbPresentationPreferences;
import dev.maicra.pickclimber.climb.PlayerClimbRuntimePreferences;
import dev.maicra.pickclimber.network.RuntimePreferencePayload;

final class ClientRuntimePreferenceController {
    private static UUID activePlayerId;
    private static Boolean lastSentInteractionsEnabled;
    private static Boolean lastSentFailureTextEnabled;

    private ClientRuntimePreferenceController() {
    }

    static void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.getConnection() == null) {
            clearLocalState();
            return;
        }

        PickClimberClientOptions options = PickClimberClientOptionsStore.current();
        boolean interactionsEnabled = options.interactionsEnabled();
        boolean failureTextEnabled = options.showFailureText();
        UUID playerId = player.getUUID();
        if (!playerId.equals(activePlayerId)) {
            clearActivePlayerPreferences();
            activePlayerId = playerId;
            lastSentInteractionsEnabled = null;
            lastSentFailureTextEnabled = null;
        }

        PlayerClimbRuntimePreferences.setInteractionsEnabled(playerId, interactionsEnabled);
        PlayerClimbPresentationPreferences.setFailureTextEnabled(playerId, failureTextEnabled);
        if (preferencesChanged(interactionsEnabled, failureTextEnabled)) {
            if (!interactionsEnabled && ClimbManager.isAttached(player)) {
                ClimbManager.detachClient(player, false);
            }
            PacketDistributor.sendToServer(new RuntimePreferencePayload(
                    interactionsEnabled,
                    failureTextEnabled
            ));
            lastSentInteractionsEnabled = interactionsEnabled;
            lastSentFailureTextEnabled = failureTextEnabled;
        }
    }

    static void onLogout(LocalPlayer player) {
        if (player != null) {
            PlayerClimbRuntimePreferences.clear(player);
            PlayerClimbPresentationPreferences.clear(player);
        }
        clearLocalState();
    }

    static void requestImmediateSync(Minecraft minecraft) {
        lastSentInteractionsEnabled = null;
        lastSentFailureTextEnabled = null;
        tick(minecraft);
    }

    private static boolean preferencesChanged(boolean interactionsEnabled, boolean failureTextEnabled) {
        return lastSentInteractionsEnabled == null
                || lastSentFailureTextEnabled == null
                || lastSentInteractionsEnabled != interactionsEnabled
                || lastSentFailureTextEnabled != failureTextEnabled;
    }

    private static void clearLocalState() {
        clearActivePlayerPreferences();
        activePlayerId = null;
        lastSentInteractionsEnabled = null;
        lastSentFailureTextEnabled = null;
    }

    private static void clearActivePlayerPreferences() {
        if (activePlayerId != null) {
            PlayerClimbRuntimePreferences.clear(activePlayerId);
            PlayerClimbPresentationPreferences.clear(activePlayerId);
        }
    }
}
