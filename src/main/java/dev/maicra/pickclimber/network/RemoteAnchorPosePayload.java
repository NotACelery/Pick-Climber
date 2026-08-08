package dev.maicra.pickclimber.network;

import dev.maicra.pickclimber.PickClimber;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import java.util.UUID;

/**
 * Minimal observer-only state for third-person ceiling poses. It never carries
 * position, velocity, durability, or any other server-authoritative physics.
 */
public record RemoteAnchorPosePayload(
        UUID playerId,
        boolean ceilingAnchor,
        int handOrdinal
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RemoteAnchorPosePayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "remote_anchor_pose")
    );

    public static final StreamCodec<ByteBuf, RemoteAnchorPosePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                UUIDUtil.STREAM_CODEC.encode(buffer, payload.playerId());
                ByteBufCodecs.BOOL.encode(buffer, payload.ceilingAnchor());
                ByteBufCodecs.VAR_INT.encode(buffer, payload.handOrdinal());
            },
            buffer -> new RemoteAnchorPosePayload(
                    UUIDUtil.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer)
            )
    );

    public static RemoteAnchorPosePayload attached(UUID playerId, InteractionHand hand) {
        return new RemoteAnchorPosePayload(playerId, true, hand.ordinal());
    }

    public static RemoteAnchorPosePayload detached(UUID playerId) {
        return new RemoteAnchorPosePayload(playerId, false, InteractionHand.MAIN_HAND.ordinal());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
