package dev.maicra.pickclimber.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import dev.maicra.pickclimber.PickClimber;

public record RuntimePreferencePayload(
        boolean interactionsEnabled,
        boolean failureTextEnabled
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RuntimePreferencePayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "runtime_preference")
    );

    public static final StreamCodec<ByteBuf, RuntimePreferencePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            RuntimePreferencePayload::interactionsEnabled,
            ByteBufCodecs.BOOL,
            RuntimePreferencePayload::failureTextEnabled,
            RuntimePreferencePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
