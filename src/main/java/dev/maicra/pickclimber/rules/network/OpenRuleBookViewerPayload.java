package dev.maicra.pickclimber.rules.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.maicra.pickclimber.PickClimber;

public record OpenRuleBookViewerPayload(CompoundTag definitionTag) implements CustomPacketPayload {
    public static final Type<OpenRuleBookViewerPayload> TYPE = new Type<>(PickClimber.id("open_rule_book_viewer"));
    public static final StreamCodec<ByteBuf, OpenRuleBookViewerPayload> STREAM_CODEC =
            RuleBookNetworkLimits.BOUNDED_COMPOUND_TAG.map(
            OpenRuleBookViewerPayload::new, OpenRuleBookViewerPayload::definitionTag
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
