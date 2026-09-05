package dev.maicra.pickclimber.rules.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.ClimbingRulesClientUi;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;

public final class ClimbingRuleBookViewerNetworking {
    private ClimbingRuleBookViewerNetworking() {
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(
                OpenRuleBookViewerRequestPayload.TYPE,
                OpenRuleBookViewerRequestPayload.STREAM_CODEC,
                ClimbingRuleBookViewerNetworking::handleRequest
        );
        registrar.playToClient(
                OpenRuleBookViewerPayload.TYPE,
                OpenRuleBookViewerPayload.STREAM_CODEC,
                ClimbingRuleBookViewerNetworking::handleOpen
        );
    }

    public static void request(InteractionHand hand) {
        PacketDistributor.sendToServer(new OpenRuleBookViewerRequestPayload(hand.ordinal()));
    }

    private static void handleRequest(OpenRuleBookViewerRequestPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        InteractionHand hand = payload.handOrdinal() == InteractionHand.OFF_HAND.ordinal()
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack stack = player.getItemInHand(hand);
        ClimbingRuleBookData.resolveDefinition(player.serverLevel().getServer(), stack).ifPresent(definition ->
                ClimbingRuleBookCodec.encodeToNbt(definition).result().ifPresent(tag ->
                        PacketDistributor.sendToPlayer(player, new OpenRuleBookViewerPayload(tag))
                )
        );
    }

    private static void handleOpen(OpenRuleBookViewerPayload payload, IPayloadContext context) {
        if (!RuleBookNetworkLimits.accepts(payload.definitionTag())) {
            return;
        }
        ClimbingRuleBookCodec.decodeFromNbt(payload.definitionTag()).result()
                .ifPresent(ClimbingRulesClientUi::openViewer);
    }
}
