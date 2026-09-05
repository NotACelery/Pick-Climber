package dev.maicra.pickclimber.client.rules;

import dev.maicra.pickclimber.rules.network.ClearRuleBookPayload;
import dev.maicra.pickclimber.rules.network.CreateRuleBookPayload;
import dev.maicra.pickclimber.rules.network.DispenseRuleBookTestPayload;
import dev.maicra.pickclimber.rules.network.ImportCurrentRulesPayload;
import dev.maicra.pickclimber.rules.network.ImportRuleBookPayload;
import dev.maicra.pickclimber.rules.network.OpenRuleBookProcessingPayload;
import dev.maicra.pickclimber.rules.network.OpenRuleBookExportRequestPayload;
import dev.maicra.pickclimber.rules.network.OpenRulesTablePayload;
import dev.maicra.pickclimber.rules.network.OpenRulesEditorRequestPayload;
import dev.maicra.pickclimber.rules.network.RestoreWorldDefaultsPayload;
import dev.maicra.pickclimber.rules.network.RuleBookNetworkLimits;
import dev.maicra.pickclimber.rules.network.UpdateRuleBookPayload;
import dev.maicra.pickclimber.rules.network.UpdateRuleDispenserLifetimePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClimbingRulesClientRequests {
    private ClimbingRulesClientRequests() {
    }

    public static void createRuleBook(BlockPos position, String profileName) {
        PacketDistributor.sendToServer(new CreateRuleBookPayload(position, profileName));
    }

    public static void importRuleBook(BlockPos position, CompoundTag definitionTag) {
        if (!allowDefinitionPayload(definitionTag)) {
            return;
        }
        PacketDistributor.sendToServer(new ImportRuleBookPayload(position, definitionTag));
    }

    public static void importCurrentRules(BlockPos position) {
        PacketDistributor.sendToServer(new ImportCurrentRulesPayload(position));
    }

    public static void openEditor(BlockPos position) {
        PacketDistributor.sendToServer(new OpenRulesEditorRequestPayload(position));
    }

    public static void openProcessing(BlockPos position) {
        PacketDistributor.sendToServer(new OpenRuleBookProcessingPayload(position));
    }

    public static void exportRuleBook(BlockPos position) {
        PacketDistributor.sendToServer(new OpenRuleBookExportRequestPayload(position));
    }

    public static void openRulesTable(BlockPos position) {
        PacketDistributor.sendToServer(new OpenRulesTablePayload(position));
    }

    public static void clearRuleBook(BlockPos position) {
        PacketDistributor.sendToServer(new ClearRuleBookPayload(position));
    }

    public static void updateRuleBook(BlockPos position, int sessionToken, CompoundTag definitionTag) {
        if (!allowDefinitionPayload(definitionTag)) {
            return;
        }
        PacketDistributor.sendToServer(new UpdateRuleBookPayload(position, sessionToken, definitionTag));
    }

    public static void updateRuleDispenserSettings(
            BlockPos position,
            int seconds,
            boolean startCounterOnPickup
    ) {
        PacketDistributor.sendToServer(
                new UpdateRuleDispenserLifetimePayload(position, seconds, startCounterOnPickup)
        );
    }

    public static void dispenseRuleBookTest(BlockPos position) {
        PacketDistributor.sendToServer(new DispenseRuleBookTestPayload(position));
    }

    public static void restoreWorldDefaults(BlockPos position) {
        PacketDistributor.sendToServer(new RestoreWorldDefaultsPayload(position));
    }

    private static boolean allowDefinitionPayload(CompoundTag definitionTag) {
        if (RuleBookNetworkLimits.accepts(definitionTag)) {
            return true;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                    Component.translatable("message.pickclimber.rules.network_payload_too_large"),
                    true
            );
        }
        return false;
    }
}
