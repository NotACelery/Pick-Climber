package dev.maicra.pickclimber.rules.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.maicra.pickclimber.PickClimber;

public record ImportRuleBookPayload(BlockPos position, CompoundTag profileTag) implements CustomPacketPayload {
    public static final Type<ImportRuleBookPayload> TYPE = new Type<>(PickClimber.id("import_rule_book"));
    public static final StreamCodec<ByteBuf, ImportRuleBookPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ImportRuleBookPayload::position,
            RuleBookNetworkLimits.BOUNDED_COMPOUND_TAG,
            ImportRuleBookPayload::profileTag,
            ImportRuleBookPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
