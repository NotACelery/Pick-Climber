package dev.maicra.pickclimber.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import dev.maicra.pickclimber.PickClimber;

public record DetachRequestPayload(
        boolean jump,
        float forward,
        float strafe,
        float yaw,
        float pitch
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DetachRequestPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "detach_request")
    );

    public static final StreamCodec<ByteBuf, DetachRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, DetachRequestPayload::jump,
            ByteBufCodecs.FLOAT, DetachRequestPayload::forward,
            ByteBufCodecs.FLOAT, DetachRequestPayload::strafe,
            ByteBufCodecs.FLOAT, DetachRequestPayload::yaw,
            ByteBufCodecs.FLOAT, DetachRequestPayload::pitch,
            DetachRequestPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
