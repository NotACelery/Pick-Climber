package dev.maicra.pickclimber.climb;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

final class AnchorImpulseCalculator {
    private AnchorImpulseCalculator() {
    }

    static Vec3 climbingBoostVelocity(Player player, ItemStack activeTool) {
        Vec3 currentVelocity = player.getDeltaMovement();
        int enchantmentLevel = ModEnchantments.getPickClimberLevel(player.level(), activeTool);
        double additionalHeight = ClimbTuning.BASE_BOOST_HEIGHT
                + ClimbTuning.BOOST_HEIGHT_PER_ENCHANTMENT_LEVEL * enchantmentLevel;
        double boostedY = velocityForAdditionalRise(currentVelocity.y, additionalHeight);
        return new Vec3(currentVelocity.x, boostedY, currentVelocity.z);
    }

    static Vec3 wallJumpVelocity(Player player, ItemStack activeTool) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);

        if (horizontal.lengthSqr() < ClimbTuning.DIRECTION_EPSILON_SQR) {
            horizontal = Vec3.directionFromRotation(0.0F, player.getYRot());
            horizontal = new Vec3(horizontal.x, 0.0D, horizontal.z);
        }

        horizontal = horizontal.normalize().scale(ClimbTuning.WALL_JUMP_HORIZONTAL_IMPULSE);
        double baseVertical = Mth.clamp(ClimbTuning.WALL_JUMP_BASE_VERTICAL
                + look.y * ClimbTuning.WALL_JUMP_LOOK_VERTICAL_FACTOR,
                ClimbTuning.WALL_JUMP_MIN_VERTICAL,
                ClimbTuning.WALL_JUMP_MAX_VERTICAL);
        int enchantmentLevel = ModEnchantments.getPickClimberLevel(player.level(), activeTool);
        double extraHeight = enchantmentLevel * ClimbTuning.WALL_JUMP_HEIGHT_PER_ENCHANTMENT_LEVEL;
        double vertical = extraHeight <= 0.0D
                ? baseVertical
                : velocityForAdditionalRise(baseVertical, extraHeight);
        return new Vec3(horizontal.x, vertical, horizontal.z);
    }

    static Vec3 ceilingReleaseVelocity(ServerPlayer player, ServerClimbState state) {
        Vec3 requestedDirection = CeilingAnchorMotion.inputDirection(player, state);
        Vec3 releaseMomentum = new Vec3(
                state.swingReleaseMomentum().x,
                0.0D,
                state.swingReleaseMomentum().z
        );
        Vec3 release = releaseMomentum.add(
                requestedDirection.scale(ClimbTuning.WALL_JUMP_HORIZONTAL_IMPULSE)
        );
        if (release.length() > ClimbTuning.CEILING_RELEASE_MAX_SPEED) {
            release = release.normalize().scale(ClimbTuning.CEILING_RELEASE_MAX_SPEED);
        }
        return release;
    }

    private static double velocityForAdditionalRise(double currentVelocity, double additionalHeight) {
        double safeCurrent = Math.max(0.0D, currentVelocity);
        double desiredRise = predictedVerticalRise(safeCurrent) + Math.max(0.0D, additionalHeight);
        double low = safeCurrent;
        double high = Math.max(
                ClimbTuning.RISE_SEARCH_INITIAL_MARGIN,
                safeCurrent + ClimbTuning.RISE_SEARCH_INITIAL_MARGIN
        );

        while (predictedVerticalRise(high) < desiredRise && high < ClimbTuning.RISE_SEARCH_MAX_VELOCITY) {
            high *= ClimbTuning.RISE_SEARCH_GROWTH;
        }

        for (int i = 0; i < ClimbTuning.RISE_SEARCH_ITERATIONS; i++) {
            double middle = (low + high) * 0.5D;
            if (predictedVerticalRise(middle) < desiredRise) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return high;
    }

    private static double predictedVerticalRise(double initialVelocity) {
        double velocity = initialVelocity;
        double height = 0.0D;

        for (int tick = 0; tick < ClimbTuning.VERTICAL_RISE_PREDICTION_TICKS && velocity > 0.0D; tick++) {
            height += velocity;
            velocity = (velocity - ClimbTuning.VANILLA_GRAVITY_PER_TICK)
                    * ClimbTuning.VANILLA_VERTICAL_DRAG;
        }
        return height;
    }
}
