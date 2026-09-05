package dev.maicra.pickclimber.rules;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;

import org.junit.jupiter.api.Test;

import dev.maicra.pickclimber.rules.network.RuleBookNetworkLimits;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleBookNetworkLimitsTest {
    @Test
    void smallCompoundIsAccepted() {
        CompoundTag tag = new CompoundTag();
        tag.putString("book_name", "Route A");

        assertTrue(RuleBookNetworkLimits.accepts(tag));
    }

    @Test
    void oversizedCompoundIsRejectedBeforeSend() {
        CompoundTag tag = oversizedTag();

        assertFalse(RuleBookNetworkLimits.accepts(tag));
    }

    @Test
    void boundedCodecRejectsOversizedInboundNbt() {
        CompoundTag tag = oversizedTag();
        ByteBuf buffer = Unpooled.buffer();
        try {
            ByteBufCodecs.COMPOUND_TAG.encode(buffer, tag);
            assertThrows(RuntimeException.class, () -> RuleBookNetworkLimits.BOUNDED_COMPOUND_TAG.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    private static CompoundTag oversizedTag() {
        CompoundTag tag = new CompoundTag();
        tag.putByteArray("payload", new byte[ClimbingRuleBookDefinition.MAX_SERIALIZED_BYTES]);
        return tag;
    }
}
