package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.rules.MapmakerPermissions;
import dev.maicra.pickclimber.rules.block.ClimbingRuleDispenserBlockEntity;
import dev.maicra.pickclimber.rules.menu.ClimbingRuleDispenserMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ClimbingRuleDispenserNetworking {
    private ClimbingRuleDispenserNetworking() {
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(
                UpdateRuleDispenserLifetimePayload.TYPE,
                UpdateRuleDispenserLifetimePayload.STREAM_CODEC,
                ClimbingRuleDispenserNetworking::handleLifetimeUpdate
        );
    }

    private static void handleLifetimeUpdate(UpdateRuleDispenserLifetimePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)
                || !(serverPlayer.containerMenu instanceof ClimbingRuleDispenserMenu menu)
                || !menu.blockPos().equals(payload.position())
                || !MapmakerPermissions.canManage(serverPlayer)) {
            return;
        }
        if (serverPlayer.level().getBlockEntity(payload.position())
                instanceof ClimbingRuleDispenserBlockEntity blockEntity) {
            blockEntity.set(0, payload.seconds());
        }
    }
}
