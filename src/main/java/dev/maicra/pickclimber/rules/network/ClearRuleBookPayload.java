package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.PickClimber;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClearRuleBookPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<ClearRuleBookPayload> TYPE = new Type<>(PickClimber.id("clear_rule_book"));
    public static final StreamCodec<ByteBuf, ClearRuleBookPayload> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
            ClearRuleBookPayload::new,
            ClearRuleBookPayload::position
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
