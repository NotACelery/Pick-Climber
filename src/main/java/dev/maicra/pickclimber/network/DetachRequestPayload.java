package dev.maicra.pickclimber.network;

import dev.maicra.pickclimber.PickClimber;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DetachRequestPayload(boolean jump) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DetachRequestPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "detach_request")
    );

    public static final StreamCodec<ByteBuf, DetachRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, DetachRequestPayload::jump,
            DetachRequestPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
