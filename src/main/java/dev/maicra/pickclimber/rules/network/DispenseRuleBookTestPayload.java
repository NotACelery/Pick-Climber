package dev.maicra.pickclimber.rules.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.maicra.pickclimber.PickClimber;

public record DispenseRuleBookTestPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<DispenseRuleBookTestPayload> TYPE = new Type<>(PickClimber.id("dispense_rule_book_test"));
    public static final StreamCodec<ByteBuf, DispenseRuleBookTestPayload> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
            DispenseRuleBookTestPayload::new,
            DispenseRuleBookTestPayload::position
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
