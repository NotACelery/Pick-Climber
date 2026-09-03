package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.PlayerRulesSnapshot;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlayerRulesStatePayload(
        boolean active,
        CompoundTag definitionTag,
        long expiresAtGameTime,
        long policyRevision
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerRulesStatePayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(PickClimber.MOD_ID, "player_rules_state")
    );
    public static final StreamCodec<ByteBuf, PlayerRulesStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PlayerRulesStatePayload::active,
            RuleBookNetworkLimits.BOUNDED_COMPOUND_TAG,
            PlayerRulesStatePayload::definitionTag,
            ByteBufCodecs.VAR_LONG,
            PlayerRulesStatePayload::expiresAtGameTime,
            ByteBufCodecs.VAR_LONG,
            PlayerRulesStatePayload::policyRevision,
            PlayerRulesStatePayload::new
    );

    public static PlayerRulesStatePayload from(PlayerRulesSnapshot snapshot) {
        if (!snapshot.active()) {
            return new PlayerRulesStatePayload(false, new CompoundTag(), 0L, 0L);
        }
        CompoundTag encoded = ClimbingRuleBookCodec.encodeToNbt(snapshot.definition().orElseThrow()).getOrThrow();
        return new PlayerRulesStatePayload(
                true,
                encoded,
                snapshot.expiresAtGameTime(),
                snapshot.policyRevision()
        );
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
