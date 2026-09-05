package dev.maicra.pickclimber.rules.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.maicra.pickclimber.PickClimber;

public record ImportCurrentRulesPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<ImportCurrentRulesPayload> TYPE = new Type<>(PickClimber.id("import_current_rules"));
    public static final StreamCodec<ByteBuf, ImportCurrentRulesPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ImportCurrentRulesPayload::position,
            ImportCurrentRulesPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
