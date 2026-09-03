package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.PickClimber;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateRuleBookPayload(
        BlockPos position,
        int sessionToken,
        CompoundTag definitionTag
) implements CustomPacketPayload {
    public static final Type<UpdateRuleBookPayload> TYPE = new Type<>(PickClimber.id("update_rule_book"));
    public static final StreamCodec<ByteBuf, UpdateRuleBookPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateRuleBookPayload::position,
            ByteBufCodecs.VAR_INT,
            UpdateRuleBookPayload::sessionToken,
            RuleBookNetworkLimits.BOUNDED_COMPOUND_TAG,
            UpdateRuleBookPayload::definitionTag,
            UpdateRuleBookPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
