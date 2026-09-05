package dev.maicra.pickclimber.rules.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.maicra.pickclimber.PickClimber;

public record OpenRulesEditorPayload(
        BlockPos position,
        int sessionToken,
        CompoundTag definitionTag
) implements CustomPacketPayload {
    public static final Type<OpenRulesEditorPayload> TYPE = new Type<>(PickClimber.id("open_rules_editor"));
    public static final StreamCodec<ByteBuf, OpenRulesEditorPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenRulesEditorPayload::position,
            ByteBufCodecs.VAR_INT,
            OpenRulesEditorPayload::sessionToken,
            RuleBookNetworkLimits.BOUNDED_COMPOUND_TAG,
            OpenRulesEditorPayload::definitionTag,
            OpenRulesEditorPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
