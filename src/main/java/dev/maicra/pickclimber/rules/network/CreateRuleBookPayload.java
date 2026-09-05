package dev.maicra.pickclimber.rules.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.maicra.pickclimber.PickClimber;

public record CreateRuleBookPayload(BlockPos position, String profileName) implements CustomPacketPayload {
    public static final Type<CreateRuleBookPayload> TYPE = new Type<>(PickClimber.id("create_rule_book"));
    public static final StreamCodec<ByteBuf, CreateRuleBookPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            CreateRuleBookPayload::position,
            ByteBufCodecs.stringUtf8(64),
            CreateRuleBookPayload::profileName,
            CreateRuleBookPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
