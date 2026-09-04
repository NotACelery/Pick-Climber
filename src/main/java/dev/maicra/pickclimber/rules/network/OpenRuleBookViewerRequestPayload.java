package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.PickClimber;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenRuleBookViewerRequestPayload(int handOrdinal) implements CustomPacketPayload {
    public static final Type<OpenRuleBookViewerRequestPayload> TYPE = new Type<>(
            PickClimber.id("open_rule_book_viewer_request")
    );
    public static final StreamCodec<ByteBuf, OpenRuleBookViewerRequestPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(
            OpenRuleBookViewerRequestPayload::new, OpenRuleBookViewerRequestPayload::handOrdinal
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
