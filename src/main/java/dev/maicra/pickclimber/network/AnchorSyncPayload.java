package dev.maicra.pickclimber.network;

import java.util.UUID;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.climb.ServerClimbState;

public record AnchorSyncPayload(
        boolean attached,
        double targetX,
        double targetY,
        double targetZ,
        int anchorX,
        int anchorY,
        int anchorZ,
        int crackId,
        int handOrdinal,
        UUID toolId,
        int flags
) implements CustomPacketPayload {
    private static final int FLAG_RESTORE_NO_GRAVITY = 1;
    private static final int FLAG_RESTORE_FLYING = 1 << 1;
    private static final int FLAG_JUMP_DETACH = 1 << 2;
    private static final int FLAG_NEW_ANCHOR = 1 << 3;
    private static final int FLAG_REFUND_COOLDOWN = 1 << 4;
    private static final int FLAG_CEILING_ANCHOR = 1 << 5;
    private static final int COOLDOWN_SHIFT = 8;
    private static final int COOLDOWN_MASK = 0xFFFF;

    public static final CustomPacketPayload.Type<AnchorSyncPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "anchor_sync")
    );

    public static final StreamCodec<ByteBuf, AnchorSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                ByteBufCodecs.BOOL.encode(buffer, payload.attached());
                ByteBufCodecs.DOUBLE.encode(buffer, payload.targetX());
                ByteBufCodecs.DOUBLE.encode(buffer, payload.targetY());
                ByteBufCodecs.DOUBLE.encode(buffer, payload.targetZ());
                ByteBufCodecs.VAR_INT.encode(buffer, payload.anchorX());
                ByteBufCodecs.VAR_INT.encode(buffer, payload.anchorY());
                ByteBufCodecs.VAR_INT.encode(buffer, payload.anchorZ());
                ByteBufCodecs.VAR_INT.encode(buffer, payload.crackId());
                ByteBufCodecs.VAR_INT.encode(buffer, payload.handOrdinal());
                UUIDUtil.STREAM_CODEC.encode(buffer, payload.toolId());
                ByteBufCodecs.VAR_INT.encode(buffer, payload.flags());
            },
            buffer -> new AnchorSyncPayload(
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.DOUBLE.decode(buffer),
                    ByteBufCodecs.DOUBLE.decode(buffer),
                    ByteBufCodecs.DOUBLE.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    UUIDUtil.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer)
            )
    );

    public static AnchorSyncPayload attached(ServerClimbState state, boolean newAnchor) {
        int flags = makeFlags(
                state.restoreNoGravity(),
                state.restoreFlying(),
                false,
                newAnchor ? state.cooldownTicks() : 0
        );
        if (newAnchor) {
            flags |= FLAG_NEW_ANCHOR;
        }
        if (state.anchorFace() == net.minecraft.core.Direction.DOWN) {
            flags |= FLAG_CEILING_ANCHOR;
        }
        return new AnchorSyncPayload(
                true,
                state.targetPosition().x,
                state.targetPosition().y,
                state.targetPosition().z,
                state.anchorBlock().getX(),
                state.anchorBlock().getY(),
                state.anchorBlock().getZ(),
                state.crackId(),
                state.activeHand().ordinal(),
                state.toolId(),
                flags
        );
    }

    public static AnchorSyncPayload detached(
            boolean restoreNoGravity,
            boolean restoreFlying,
            boolean jumpDetach,
            UUID toolId,
            int cooldownTicks,
            boolean refundCooldown
    ) {
        int flags = makeFlags(restoreNoGravity, restoreFlying, jumpDetach, cooldownTicks);
        if (refundCooldown) {
            flags |= FLAG_REFUND_COOLDOWN;
        }
        return new AnchorSyncPayload(
                false,
                0.0D,
                0.0D,
                0.0D,
                0,
                0,
                0,
                0,
                0,
                toolId,
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

    public boolean ceilingAnchor() {
        return (flags & FLAG_CEILING_ANCHOR) != 0;
    }

    public int cooldownTicks() {
        return (flags >>> COOLDOWN_SHIFT) & COOLDOWN_MASK;
    }

    private static int makeFlags(
            boolean restoreNoGravity,
            boolean restoreFlying,
            boolean jumpDetach,
            int cooldownTicks
    ) {
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
        flags |= (Math.max(0, Math.min(COOLDOWN_MASK, cooldownTicks)) << COOLDOWN_SHIFT);
        return flags;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
