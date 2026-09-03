package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.WorldRulesSnapshot;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WorldRulesStatePayload(
        boolean active,
        boolean temporary,
        CompoundTag definitionTag,
        long expiresAtGameTime,
        long policyRevision
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WorldRulesStatePayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "world_rules_state")
    );
    public static final StreamCodec<ByteBuf, WorldRulesStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            WorldRulesStatePayload::active,
            ByteBufCodecs.BOOL,
            WorldRulesStatePayload::temporary,
            RuleBookNetworkLimits.BOUNDED_COMPOUND_TAG,
            WorldRulesStatePayload::definitionTag,
            ByteBufCodecs.VAR_LONG,
            WorldRulesStatePayload::expiresAtGameTime,
            ByteBufCodecs.VAR_LONG,
            WorldRulesStatePayload::policyRevision,
            WorldRulesStatePayload::new
    );

    public static WorldRulesStatePayload from(WorldRulesSnapshot snapshot) {
        if (!snapshot.active()) {
            return new WorldRulesStatePayload(false, false, new CompoundTag(), 0L, snapshot.policyRevision());
        }
        CompoundTag encoded = ClimbingRuleBookCodec.encodeToNbt(snapshot.effectiveDefinition().orElseThrow())
                .getOrThrow();
        return new WorldRulesStatePayload(
                true,
                snapshot.temporaryActive(),
                encoded,
                snapshot.temporaryExpiresAtGameTime(),
                snapshot.policyRevision()
        );
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
