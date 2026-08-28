package dev.maicra.pickclimber.network;

import dev.maicra.pickclimber.PickClimber;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BoostSyncPayload(
        double velocityX,
        double velocityY,
        double velocityZ,
        int handOrdinal,
        int cooldownTicks
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BoostSyncPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "boost_sync")
    );

    public static final StreamCodec<ByteBuf, BoostSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, BoostSyncPayload::velocityX,
            ByteBufCodecs.DOUBLE, BoostSyncPayload::velocityY,
            ByteBufCodecs.DOUBLE, BoostSyncPayload::velocityZ,
            ByteBufCodecs.VAR_INT, BoostSyncPayload::handOrdinal,
            ByteBufCodecs.VAR_INT, BoostSyncPayload::cooldownTicks,
            BoostSyncPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
