package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.PickClimber;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RulesActionResultPayload(boolean success, String messageKey) implements CustomPacketPayload {
    public static final Type<RulesActionResultPayload> TYPE = new Type<>(PickClimber.id("rules_action_result"));
    public static final StreamCodec<ByteBuf, RulesActionResultPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            RulesActionResultPayload::success,
            ByteBufCodecs.stringUtf8(128),
            RulesActionResultPayload::messageKey,
            RulesActionResultPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
