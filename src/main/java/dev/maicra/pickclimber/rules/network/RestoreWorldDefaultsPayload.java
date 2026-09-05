package dev.maicra.pickclimber.rules.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.maicra.pickclimber.PickClimber;

public record RestoreWorldDefaultsPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<RestoreWorldDefaultsPayload> TYPE = new Type<>(PickClimber.id("restore_world_defaults"));
    public static final StreamCodec<ByteBuf, RestoreWorldDefaultsPayload> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
            RestoreWorldDefaultsPayload::new,
            RestoreWorldDefaultsPayload::position
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
