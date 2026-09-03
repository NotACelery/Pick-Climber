package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.PickClimber;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DuplicateRuleBookPayload(BlockPos position, int copies) implements CustomPacketPayload {
    public static final Type<DuplicateRuleBookPayload> TYPE = new Type<>(PickClimber.id("duplicate_rule_book"));
    public static final StreamCodec<ByteBuf, DuplicateRuleBookPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            DuplicateRuleBookPayload::position,
            ByteBufCodecs.VAR_INT,
            DuplicateRuleBookPayload::copies,
            DuplicateRuleBookPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
