package dev.maicra.pickclimber.network;

import dev.maicra.pickclimber.PickClimber;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Entrada de cámara y movimiento solicitada durante un anclaje deslizante. */
public record SlideInputPayload(
        float forward,
        float strafe,
        float yaw,
        float pitch
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SlideInputPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "slide_input")
    );

    public static final StreamCodec<ByteBuf, SlideInputPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SlideInputPayload::forward,
            ByteBufCodecs.FLOAT, SlideInputPayload::strafe,
            ByteBufCodecs.FLOAT, SlideInputPayload::yaw,
            ByteBufCodecs.FLOAT, SlideInputPayload::pitch,
            SlideInputPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
