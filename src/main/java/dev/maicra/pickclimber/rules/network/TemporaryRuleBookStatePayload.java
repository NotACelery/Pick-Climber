package dev.maicra.pickclimber.rules.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.maicra.pickclimber.PickClimber;

public record TemporaryRuleBookStatePayload(long expiresAtGameTime) implements CustomPacketPayload {
    public static final Type<TemporaryRuleBookStatePayload> TYPE = new Type<>(
            PickClimber.id("temporary_rule_book_state")
    );
    public static final StreamCodec<ByteBuf, TemporaryRuleBookStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            TemporaryRuleBookStatePayload::expiresAtGameTime,
            TemporaryRuleBookStatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
