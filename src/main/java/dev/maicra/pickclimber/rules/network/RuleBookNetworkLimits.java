package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class RuleBookNetworkLimits {
    public static final int MAX_NETWORK_BYTES = ClimbingRuleBookDefinition.MAX_SERIALIZED_BYTES;
    public static final StreamCodec<ByteBuf, CompoundTag> BOUNDED_COMPOUND_TAG =
            ByteBufCodecs.compoundTagCodec(() -> NbtAccounter.create(MAX_NETWORK_BYTES));

    private RuleBookNetworkLimits() {
    }

    public static boolean accepts(CompoundTag tag) {
        return tag != null && tag.sizeInBytes() <= MAX_NETWORK_BYTES;
    }
}
