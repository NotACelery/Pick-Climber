package dev.maicra.pickclimber.rules.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.maicra.pickclimber.PickClimber;

public record OpenRuleBookExportRequestPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<OpenRuleBookExportRequestPayload> TYPE = new Type<>(
            PickClimber.id("open_rule_book_export_request")
    );
    public static final StreamCodec<ByteBuf, OpenRuleBookExportRequestPayload> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
            OpenRuleBookExportRequestPayload::new,
            OpenRuleBookExportRequestPayload::position
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
