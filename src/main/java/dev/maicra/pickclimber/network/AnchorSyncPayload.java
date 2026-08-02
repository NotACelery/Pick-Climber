package dev.maicra.pickclimber.network;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.climb.ServerClimbState;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AnchorSyncPayload(
        boolean attached,
        double targetX,
        double targetY,
        double targetZ,
        int handOrdinal,
        int flags
) implements CustomPacketPayload {
    private static final int FLAG_RESTORE_NO_GRAVITY = 1;
    private static final int FLAG_RESTORE_FLYING = 1 << 1;
    private static final int FLAG_JUMP_DETACH = 1 << 2;
    private static final int FLAG_NEW_ANCHOR = 1 << 3;
    private static final int FLAG_REFUND_COOLDOWN = 1 << 4;

    public static final CustomPacketPayload.Type<AnchorSyncPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "anchor_sync")
    );

    public static final StreamCodec<ByteBuf, AnchorSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, AnchorSyncPayload::attached,
            ByteBufCodecs.DOUBLE, AnchorSyncPayload::targetX,
            ByteBufCodecs.DOUBLE, AnchorSyncPayload::targetY,
            ByteBufCodecs.DOUBLE, AnchorSyncPayload::targetZ,
            ByteBufCodecs.VAR_INT, AnchorSyncPayload::handOrdinal,
            ByteBufCodecs.VAR_INT, AnchorSyncPayload::flags,
            AnchorSyncPayload::new
    );

    public static AnchorSyncPayload attached(ServerClimbState state, boolean newAnchor) {
        int flags = makeFlags(state.restoreNoGravity(), state.restoreFlying(), false);
        if (newAnchor) {
            flags |= FLAG_NEW_ANCHOR;
        }
        return new AnchorSyncPayload(
                true,
                state.targetPosition().x,
                state.targetPosition().y,
                state.targetPosition().z,
                state.activeHand().ordinal(),
                flags
        );
    }

    public static AnchorSyncPayload detached(
            boolean restoreNoGravity,
            boolean restoreFlying,
            boolean jumpDetach,
            boolean refundCooldown
    ) {
        int flags = makeFlags(restoreNoGravity, restoreFlying, jumpDetach);
        if (refundCooldown) {
            flags |= FLAG_REFUND_COOLDOWN;
        }
        return new AnchorSyncPayload(
                false,
                0.0D,
                0.0D,
                0.0D,
                0,
                flags
        );
    }

    public boolean restoreNoGravity() {
        return (flags & FLAG_RESTORE_NO_GRAVITY) != 0;
    }

    public boolean restoreFlying() {
        return (flags & FLAG_RESTORE_FLYING) != 0;
    }

    public boolean jumpDetach() {
        return (flags & FLAG_JUMP_DETACH) != 0;
    }

    public boolean newAnchor() {
        return (flags & FLAG_NEW_ANCHOR) != 0;
    }

    public boolean refundCooldown() {
        return (flags & FLAG_REFUND_COOLDOWN) != 0;
    }

    private static int makeFlags(boolean restoreNoGravity, boolean restoreFlying, boolean jumpDetach) {
        int flags = 0;
        if (restoreNoGravity) {
            flags |= FLAG_RESTORE_NO_GRAVITY;
        }
        if (restoreFlying) {
            flags |= FLAG_RESTORE_FLYING;
        }
        if (jumpDetach) {
            flags |= FLAG_JUMP_DETACH;
        }
        return flags;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
