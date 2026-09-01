package dev.maicra.pickclimber.climb;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

final class AnchorVisualService {
    private AnchorVisualService() {
    }

    static int createCrackId(ServerPlayer player) {
        return -1_000_000 - (player.getUUID().hashCode() & 0x3FFFFFFF);
    }

    static void showCracks(ServerLevel level, ServerClimbState state) {
        level.destroyBlockProgress(state.crackId(), state.anchorBlock(), ClimbTuning.CRACK_STAGE);
    }

    static void clearAnchorVisuals(ServerPlayer player, ServerClimbState state) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        ServerLevel anchorLevel = server.getLevel(state.anchorDimension());
        if (anchorLevel != null) {
            anchorLevel.destroyBlockProgress(state.crackId(), state.anchorBlock(), -1);
        }
    }

    static void clearClientCracks(Player player, ClientClimbState state) {
        player.level().destroyBlockProgress(
                state.crackId(),
                state.anchorBlock(),
                -1
        );
    }

    static void playAnchorSound(
            ServerLevel level,
            ServerPlayer player,
            BlockState blockState,
            BlockPos blockPos
    ) {
        SoundType soundType = blockState.getSoundType();
        float volume = Mth.clamp((float) ((soundType.getVolume() + ClimbTuning.ANCHOR_SOUND_VOLUME_OFFSET)
                        * ClimbTuning.ANCHOR_SOUND_VOLUME_SCALE),
                ClimbTuning.ANCHOR_SOUND_MIN_VOLUME,
                ClimbTuning.ANCHOR_SOUND_MAX_VOLUME);
        float pitch = soundType.getPitch() * (ClimbTuning.ANCHOR_SOUND_PITCH_BASE
                + level.getRandom().nextFloat() * ClimbTuning.ANCHOR_SOUND_PITCH_VARIATION);

        level.playSound(
                null,
                blockPos,
                soundType.getBreakSound(),
                SoundSource.PLAYERS,
                volume,
                pitch
        );
    }
}
