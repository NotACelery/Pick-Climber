package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.PickClimber;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenRulesEditorRequestPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<OpenRulesEditorRequestPayload> TYPE = new Type<>(
            PickClimber.id("open_rules_editor_request")
    );
    public static final StreamCodec<ByteBuf, OpenRulesEditorRequestPayload> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
            OpenRulesEditorRequestPayload::new,
            OpenRulesEditorRequestPayload::position
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
