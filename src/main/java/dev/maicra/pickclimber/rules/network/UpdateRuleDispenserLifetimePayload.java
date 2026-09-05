package dev.maicra.pickclimber.rules.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.maicra.pickclimber.PickClimber;

public record UpdateRuleDispenserLifetimePayload(
        BlockPos position,
        int seconds,
        boolean startCounterOnPickup
) implements CustomPacketPayload {
    public static final Type<UpdateRuleDispenserLifetimePayload> TYPE = new Type<>(
            PickClimber.id("update_rule_dispenser_lifetime")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateRuleDispenserLifetimePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeBlockPos(payload.position());
                        buffer.writeVarInt(payload.seconds());
                        buffer.writeBoolean(payload.startCounterOnPickup());
                    },
                    buffer -> new UpdateRuleDispenserLifetimePayload(
                            buffer.readBlockPos(),
                            buffer.readVarInt(),
                            buffer.readBoolean()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
