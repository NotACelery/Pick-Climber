package dev.maicra.pickclimber.rules.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.maicra.pickclimber.PickClimber;

public record OpenRulesTablePayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<OpenRulesTablePayload> TYPE = new Type<>(
            PickClimber.id("open_rules_table")
    );
    public static final StreamCodec<ByteBuf, OpenRulesTablePayload> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
            OpenRulesTablePayload::new,
            OpenRulesTablePayload::position
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
