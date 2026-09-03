package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.PickClimber;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EjectRuleBookPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<EjectRuleBookPayload> TYPE = new Type<>(PickClimber.id("eject_rule_book"));
    public static final StreamCodec<ByteBuf, EjectRuleBookPayload> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
            EjectRuleBookPayload::new,
            EjectRuleBookPayload::position
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
