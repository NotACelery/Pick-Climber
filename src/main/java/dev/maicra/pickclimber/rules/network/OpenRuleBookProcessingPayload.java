package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.PickClimber;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenRuleBookProcessingPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<OpenRuleBookProcessingPayload> TYPE = new Type<>(
            PickClimber.id("open_rule_book_processing")
    );
    public static final StreamCodec<ByteBuf, OpenRuleBookProcessingPayload> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
            OpenRuleBookProcessingPayload::new,
            OpenRuleBookProcessingPayload::position
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
