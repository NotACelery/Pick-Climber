package dev.maicra.pickclimber.climb;

import com.mojang.logging.LogUtils;
import dev.maicra.pickclimber.network.AnchorSyncPayload;
import dev.maicra.pickclimber.network.BoostSyncPayload;
import dev.maicra.pickclimber.network.RemoteAnchorPosePayload;
import dev.maicra.pickclimber.network.SlideInputPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;

public final class ClimbManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int DURABILITY_COST = 15;
    private static final int CEILING_DURABILITY_COST = 20;
    private static final int CEILING_DURABILITY_INTERVAL_TICKS = 20;
    private static final double CEILING_SWING_RADIUS = 0.665D;
    private static final double CEILING_SWING_ACCELERATION = 0.025D;
    private static final double CEILING_SWING_RETURN = 0.012D;
    private static final double CEILING_SWING_DAMPING = 0.96D;
    private static final double CEILING_SWING_MAX_SPEED = 0.18D;
    private static final double WALL_JUMP_HORIZONTAL_IMPULSE = 0.65D;
    private static final double CEILING_RELEASE_MOMENTUM_MAX_SPEED = 0.38D;
    private static final double CEILING_RELEASE_MAX_SPEED = WALL_JUMP_HORIZONTAL_IMPULSE
            + CEILING_RELEASE_MOMENTUM_MAX_SPEED;
    private static final int BRAKING_DURABILITY_PER_BLOCK = 10;
    public static final int CRACK_STAGE = 5;
    public static final int ANCHOR_COOLDOWN_TICKS = 20;
    public static final int UNSTABLE_ANCHOR_COOLDOWN_TICKS = 40;

    /** Velocity remains a secondary check and never authorizes a boost by itself. */
    public static final double RISING_VELOCITY_THRESHOLD = 0.08D;
    private static final int JUMP_BOOST_WINDOW_TICKS = 8;
    private static final double BASE_BOOST_HEIGHT = 1.0D;
    private static final double BOOST_HEIGHT_PER_ENCHANTMENT_LEVEL = 0.5D;
    private static final double WALL_JUMP_HEIGHT_PER_ENCHANTMENT_LEVEL = 0.5D;

    /** Stable vanilla animation point where the pickaxe is furthest forward. */
    private static final float PINNED_SWING_PROGRESS = 0.5F;
    private static final float PINNED_POSE_RAMP_TICKS = 4.0F;

    private static final double MAX_HIT_DISTANCE_SQR = 25.0D;
    private static final double MAX_INDICATOR_DISTANCE_SQR = 9.0D;
    private static final double MAX_ANCHOR_MOVE_SQR = 2.25D; // 1.5 actual blocks.
    private static final double MAX_DRIFT_DISTANCE_SQR = 16.0D;
    private static final double ATTACHMENT_COHERENCE_DISTANCE_SQR = 1.0D;
    private static final int WALL_TARGET_COLLISION_STEPS = 18;
    private static final int CRACK_REFRESH_INTERVAL = 10;
    private static final int SERVER_SYNC_INTERVAL = 5;
    private static final int CLIENT_SYNC_TIMEOUT_TICKS = 40;
    private static final int REMOTE_POSE_REFRESH_INTERVAL_TICKS = 20;
    private static final int FAILED_ATTACH_GRACE_TICKS = 5;
    private static final double BRAKING_START_SPEED = -0.40D;
    private static final float BRAKING_MIN_FALL_DISTANCE = 5.0F;
    private static final double BRAKING_STOP_SPEED = -0.08D;
    private static final double BRAKING_DRAG = 0.75D;
    private static final double BRAKING_RECOVERY = 0.035D;
    private static final double UNSTABLE_SLIDE_SPEED = -0.136D;
    /** Prevents crossing or skipping blocks while absorbing an extreme fall. */
    private static final double MAX_BRAKING_MOVE_PER_TICK = 0.60D;
    private static final double CONTACT_BLOCK_EPSILON = 1.0E-3D;

    private static final Map<UUID, ServerClimbState> SERVER_STATES = new HashMap<>();
    private static final Map<UUID, ClientClimbState> CLIENT_STATES = new HashMap<>();
    private static final Map<UUID, RemoteAnchorPoseState> REMOTE_ANCHOR_POSES = new HashMap<>();
    private static final Map<UUID, Long> LAST_REAL_JUMP = new HashMap<>();
    private static final Map<UUID, Long> CONSUMED_JUMP = new HashMap<>();

    private ClimbManager() {
    }

    /** Records only real jumps fired by LivingJumpEvent on the server. */
    public static void recordRealJump(ServerPlayer player) {
        LAST_REAL_JUMP.put(player.getUUID(), player.level().getGameTime());
    }

    private static boolean hasFreshUnconsumedJump(ServerPlayer player) {
        long jumpTime = LAST_REAL_JUMP.getOrDefault(player.getUUID(), Long.MIN_VALUE);
        long consumedTime = CONSUMED_JUMP.getOrDefault(player.getUUID(), Long.MIN_VALUE);
        long age = player.level().getGameTime() - jumpTime;
        return jumpTime != Long.MIN_VALUE
                && jumpTime != consumedTime
                && age >= 0L
                && age <= JUMP_BOOST_WINDOW_TICKS;
    }

    private static void consumeJump(ServerPlayer player) {
        Long jumpTime = LAST_REAL_JUMP.get(player.getUUID());
        if (jumpTime != null) {
            CONSUMED_JUMP.put(player.getUUID(), jumpTime);
        }
    }

    public static boolean isClimbingTool(ItemStack stack) {
        return ClimbingToolClassifier.isClimbingTool(stack);
    }

    public static boolean isAttached(Player player) {
        return player.level().isClientSide()
                ? CLIENT_STATES.containsKey(player.getUUID())
                : SERVER_STATES.containsKey(player.getUUID());
    }

    public static InteractionHand activeHand(Player player) {
        if (player.level().isClientSide()) {
            ClientClimbState state = CLIENT_STATES.get(player.getUUID());
            if (state != null) {
                return state.activeHand();
            }
            RemoteAnchorPoseState remotePose = remoteAnchorPose(player);
            return remotePose == null ? null : remotePose.activeHand();
        }

        ServerClimbState state = SERVER_STATES.get(player.getUUID());
        return state == null ? null : state.activeHand();
    }

    /** Reports whether the current synchronized state belongs to a block's underside. */
    public static boolean isCeilingAnchor(Player player) {
        if (player.level().isClientSide()) {
            ClientClimbState state = CLIENT_STATES.get(player.getUUID());
            return state != null && state.ceilingAnchor() || remoteAnchorPose(player) != null;
        }

        ServerClimbState state = SERVER_STATES.get(player.getUUID());
        return state != null && state.anchorFace() == Direction.DOWN;
    }

    /** Checks the pickaxe UUID rather than only its item type or hand. */
    public static boolean isActiveTool(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        UUID activeId;
        if (player.level().isClientSide()) {
            ClientClimbState state = CLIENT_STATES.get(player.getUUID());
            activeId = state == null ? null : state.toolId();
        } else {
            ServerClimbState state = SERVER_STATES.get(player.getUUID());
            activeId = state == null ? null : state.toolId();
        }

        return activeId != null && ToolIdentity.matches(stack, activeId);
    }

    /**
     * Visual fraction of the individual cooldown persisted on the ItemStack.
     *
     * The timer starts when attachment is confirmed and continues decreasing
     * while the pickaxe remains the active anchor. The attached state neither
     * freezes nor replaces the cooldown; it uses a separate dedicated indicator.
     */
    public static float visualCooldownFraction(Player player, ItemStack stack) {
        return ToolIdentity.cooldownFraction(stack, player.level().getGameTime());
    }

    /**
     * Returns the fixed swing progress used to represent the pinned pickaxe.
     * A negative value means this stack is not the client's active pickaxe.
     * Entry animates for a few ticks and then freezes completely, independently
     * of the vanilla animation that keeps advancing.
     */
    public static float pinnedPoseProgress(Player player, ItemStack stack, float partialTick) {
        float blend = pinnedPoseBlend(player, stack, partialTick, false);
        if (blend < 0.0F) {
            return -1.0F;
        }

        return PINNED_SWING_PROGRESS * blend;
    }

    /**
     * Returns normalized entry progress for the raised pose while the active
     * pickaxe maintains a ceiling anchor. A negative value selects the wall pose
     * or vanilla rendering instead.
     */
    public static float ceilingPoseProgress(Player player, ItemStack stack, float partialTick) {
        return pinnedPoseBlend(player, stack, partialTick, true);
    }

    private static float pinnedPoseBlend(
            Player player,
            ItemStack stack,
            float partialTick,
            boolean requireCeilingAnchor
    ) {
        if (!player.level().isClientSide()) {
            return -1.0F;
        }

        ClientClimbState state = CLIENT_STATES.get(player.getUUID());
        if (state == null
                || !ToolIdentity.matches(stack, state.toolId())
                || requireCeilingAnchor && !state.ceilingAnchor()) {
            return -1.0F;
        }

        float age = (float) (player.level().getGameTime() - state.poseStartedGameTime()) + partialTick;
        float blend = Mth.clamp(age / PINNED_POSE_RAMP_TICKS, 0.0F, 1.0F);
        // Smooth curve: enters quickly while avoiding a harsh visual snap from idle.
        blend = 1.0F - (1.0F - blend) * (1.0F - blend);
        return blend;
    }

    /**
     * Checks that the logical anchor state matches the player's physical
     * position. Used to recover transitional states where the client still
     * believes it is attached while it is already falling.
     */
    public static boolean isAttachmentCoherent(Player player) {
        if (player.level().isClientSide()) {
            ClientClimbState state = CLIENT_STATES.get(player.getUUID());
            return state != null
                    && player.position().distanceToSqr(state.targetPosition()) <= ATTACHMENT_COHERENCE_DISTANCE_SQR
                    && player.isNoGravity();
        }

        ServerClimbState state = SERVER_STATES.get(player.getUUID());
        return state != null
                && player.position().distanceToSqr(state.targetPosition()) <= ATTACHMENT_COHERENCE_DISTANCE_SQR
                && player.isNoGravity();
    }

    public static void recoverStaleAttachment(Player player) {
        if (player.level().isClientSide()) {
            // The client never recovers or alters physical state autonomously.
            // Wait for the authoritative server decision to avoid races.
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            ServerClimbState state = SERVER_STATES.get(player.getUUID());
            boolean refundCooldown = state != null
                    && player.level().getGameTime() - state.attachedAtGameTime() <= FAILED_ATTACH_GRACE_TICKS;
            detachServerInternal(serverPlayer, false, refundCooldown);
        }
    }

    public static boolean canAttemptAnchor(Player player, InteractionHand hand, BlockHitResult hit) {
        // Creative flight does not invalidate attachment: the mechanic must also
        // work as a safety catch while the player is falling.
        if (!player.isAlive() || player.isSpectator() || player.isFallFlying()) {
            return false;
        }

        Direction face = hit.getDirection();
        if (face == Direction.UP) {
            return false;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!isClimbingTool(stack)) {
            return false;
        }

        boolean ceilingAttempt = face == Direction.DOWN;
        if (ceilingAttempt && !ModEnchantments.hasStrongGrip(player.level(), stack)) {
            return false;
        }

        boolean duplicatedActiveIdentity = hasDuplicatedActiveIdentity(player, hand, stack);
        if (duplicatedActiveIdentity && !player.level().isClientSide()) {
            // Identity belongs to the ItemStack, not the hand. Two simultaneous
            // stacks with the same UUID are copies, so the candidate must regain
            // an individual identity and cooldown before validation.
            ToolIdentity.assign(stack, UUID.randomUUID());
            ToolIdentity.clearCooldown(stack);
        }

        // An attached pickaxe remains occupied even if its internal cooldown
        // finishes while pinned to the wall. Only the hand holding the anchor is
        // occupied. If another pickaxe inherited the same UUID through copying,
        // it is still a separate tool and must be able to create the next point.
        if (isActiveTool(player, stack) && activeHand(player) == hand) {
            return false;
        }

        if (!duplicatedActiveIdentity
                && ToolIdentity.isCoolingDown(stack, player.level().getGameTime())) {
            return false;
        }

        if (player.getEyePosition().distanceToSqr(hit.getLocation()) > MAX_HIT_DISTANCE_SQR) {
            return false;
        }

        BlockState state = player.level().getBlockState(hit.getBlockPos());
        if (!hasValidAnchorFace(player, state, hit.getBlockPos(), face)) {
            return false;
        }
        if (ceilingAttempt && AnchorSurfaceClassifier.classify(state) == AnchorSurface.UNSTABLE
                && !ModEnchantments.hasSturdyLatch(player.level(), stack)) {
            return false;
        }

        // The indicator and click share the exact same 1.5-block rule. Previously,
        // the icon depended only on vanilla interaction reach.
        Vec3 target = resolveCollisionSafeTargetPosition(player, hit);
        if (target == null) {
            return false;
        }
        Vec3 movementOrigin = currentAttachmentTarget(player);
        if (movementOrigin.distanceToSqr(target) > MAX_ANCHOR_MOVE_SQR) {
            return false;
        }
        return true;
    }

    /**
     * Mirrors anchor validation into a read-only HUD result. This method never
     * creates identity, consumes cooldown, damages tools, or changes physics.
     */
    public static AnchorIndicatorStatus anchorIndicatorStatus(Player player, BlockHitResult hit) {
        if (!player.level().isClientSide()
                || !player.isAlive()
                || player.isSpectator()
                || player.isFallFlying()
                || hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK
                || hit.getDirection() == Direction.UP
                || ClimbingHandSelector.preservesVanillaMenuUse(player, hit)) {
            return AnchorIndicatorStatus.NONE;
        }

        boolean hasClimbingTool = false;
        for (InteractionHand hand : InteractionHand.values()) {
            if (isClimbingTool(player.getItemInHand(hand))) {
                hasClimbingTool = true;
                break;
            }
        }
        if (!hasClimbingTool) {
            return AnchorIndicatorStatus.NONE;
        }

        // Minecraft represents a crosshair miss as a BlockHitResult too. Check
        // the exact hit point before classifying its synthetic position so the
        // HUD never follows the crosshair into the distance.
        if (player.getEyePosition().distanceToSqr(hit.getLocation()) > MAX_INDICATOR_DISTANCE_SQR) {
            return AnchorIndicatorStatus.NONE;
        }

        BlockState state = player.level().getBlockState(hit.getBlockPos());
        AnchorSurface surface = AnchorSurfaceClassifier.classify(state);
        if (surface == AnchorSurface.UNCLIMBABLE) {
            return AnchorIndicatorStatus.UNCLIMBABLE;
        }

        InteractionHand preferredHand = ClimbingHandSelector.preferred(player, hit);
        if (preferredHand != null) {
            ItemStack preferredTool = player.getItemInHand(preferredHand);
            return surface == AnchorSurface.UNSTABLE
                    && !ModEnchantments.hasSturdyLatch(player.level(), preferredTool)
                    ? AnchorIndicatorStatus.UNSTABLE
                    : AnchorIndicatorStatus.READY;
        }

        // Range is measured from the current anchor target before collision
        // correction. A valid block two blocks away must report range, not an
        // obstruction caused by trying to resolve an unreachable final hitbox.
        Vec3 idealTarget = calculateTargetPosition(player, hit);
        if (currentAttachmentTarget(player).distanceToSqr(idealTarget) > MAX_ANCHOR_MOVE_SQR) {
            return AnchorIndicatorStatus.OUT_OF_RANGE;
        }
        if (!hasValidAnchorFace(player, state, hit.getBlockPos(), hit.getDirection())) {
            return AnchorIndicatorStatus.OBSTRUCTED;
        }

        Vec3 target = resolveCollisionSafeTargetPosition(player, hit);
        if (target == null) {
            return AnchorIndicatorStatus.OBSTRUCTED;
        }
        if (currentAttachmentTarget(player).distanceToSqr(target) > MAX_ANCHOR_MOVE_SQR) {
            return AnchorIndicatorStatus.OUT_OF_RANGE;
        }

        boolean ceilingAttempt = hit.getDirection() == Direction.DOWN;
        if (ceilingAttempt) {
            boolean hasUnoccupiedClimbingTool = false;
            boolean hasStrongGrip = false;
            boolean hasRequiredEnchantments = false;
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);
                if (!isClimbingTool(stack)
                        || isActiveTool(player, stack) && activeHand(player) == hand) {
                    continue;
                }
                hasUnoccupiedClimbingTool = true;
                if (!ModEnchantments.hasStrongGrip(player.level(), stack)) {
                    continue;
                }
                hasStrongGrip = true;
                if (surface != AnchorSurface.UNSTABLE
                        || ModEnchantments.hasSturdyLatch(player.level(), stack)) {
                    hasRequiredEnchantments = true;
                }
            }
            if (hasUnoccupiedClimbingTool && !hasStrongGrip) {
                return AnchorIndicatorStatus.REQUIRES_STRONG_GRIP;
            }
            if (hasStrongGrip && !hasRequiredEnchantments) {
                return AnchorIndicatorStatus.REQUIRES_STURDY_LATCH;
            }
        }

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!isClimbingTool(stack)) {
                continue;
            }
            if (ceilingAttempt
                    && (!ModEnchantments.hasStrongGrip(player.level(), stack)
                    || surface == AnchorSurface.UNSTABLE
                    && !ModEnchantments.hasSturdyLatch(player.level(), stack))) {
                continue;
            }

            boolean duplicatedActiveIdentity = hasDuplicatedActiveIdentity(player, hand, stack);
            boolean occupied = isActiveTool(player, stack) && activeHand(player) == hand;
            boolean coolingDown = !duplicatedActiveIdentity
                    && ToolIdentity.isCoolingDown(stack, player.level().getGameTime());
            if (occupied || coolingDown) {
                return AnchorIndicatorStatus.COOLDOWN;
            }
        }

        return AnchorIndicatorStatus.OBSTRUCTED;
    }

    /**
     * Returns localized action-bar feedback for a rejected block interaction.
     * Unstable anchors intentionally return no warning: they remain valid and
     * merely use their controlled sliding behavior without Sturdy Latch.
     */
    public static Component anchorAttemptFailureMessage(Player player, BlockHitResult hit) {
        AnchorIndicatorStatus status = anchorIndicatorStatus(player, hit);
        return switch (status) {
            case REQUIRES_STRONG_GRIP -> Component.translatable(
                    "message.pickclimber.requires_strong_grip"
            );
            case COOLDOWN -> Component.translatable("message.pickclimber.anchor.cooldown");
            case OUT_OF_RANGE -> Component.translatable("message.pickclimber.anchor.out_of_range");
            case UNCLIMBABLE -> Component.translatable(
                    "message.pickclimber.anchor.blocked_by",
                    player.level().getBlockState(hit.getBlockPos()).getBlock().getName()
            );
            case OBSTRUCTED -> anchorObstructionMessage(player, hit);
            case NONE, READY, UNSTABLE, REQUIRES_STURDY_LATCH -> null;
        };
    }

    private static Component anchorObstructionMessage(Player player, BlockHitResult hit) {
        BlockState state = player.level().getBlockState(hit.getBlockPos());
        if (!hasValidAnchorFace(player, state, hit.getBlockPos(), hit.getDirection())) {
            return Component.translatable(
                    "message.pickclimber.anchor.blocked_by",
                    state.getBlock().getName()
            );
        }
        if (resolveCollisionSafeTargetPosition(player, hit) == null) {
            return Component.translatable("message.pickclimber.anchor.not_enough_space");
        }
        return Component.translatable("message.pickclimber.anchor.obstructed");
    }

    /**
     * Chooses exclusively between a boost and an attachment.
     *
     * Positive velocity alone never authorizes a boost: a recent, unconsumed
     * LivingJumpEvent must exist. This prevents network corrections, steps,
     * teleports, or desynchronized states from being interpreted as a jump and
     * launching a player who expected to remain attached.
     */
    public static boolean useClimbingTool(ServerPlayer player, InteractionHand hand, BlockHitResult hit) {
        if (!canAttemptAnchor(player, hand, hit)) {
            return false;
        }

        boolean activelyFlying = player.getAbilities().flying;
        boolean rising = player.getDeltaMovement().y > RISING_VELOCITY_THRESHOLD;
        boolean airborne = !player.onGround();
        boolean realJumpAuthorized = hasFreshUnconsumedJump(player);

        boolean chooseBoost = !activelyFlying
                && !isAttached(player)
                && airborne
                && rising
                && realJumpAuthorized
                && hit.getDirection().getAxis() != Direction.Axis.Y;

        LOGGER.info(
                "[PickClimber] action={} player={} hand={} onGround={} flying={} deltaY={} jumpAuthorized={} attached={} target={}",
                chooseBoost ? "BOOST" : "ATTACH",
                player.getScoreboardName(),
                hand,
                player.onGround(),
                activelyFlying,
                player.getDeltaMovement().y,
                realJumpAuthorized,
                isAttached(player),
                hit.getBlockPos()
        );

        if (chooseBoost) {
            boolean boosted = performClimbingBoost(player, hand, hit);
            if (boosted) {
                consumeJump(player);
            }
            return boosted;
        }

        return attach(player, hand, hit);
    }

    private static boolean performClimbingBoost(
            ServerPlayer player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!canAttemptAnchor(player, hand, hit)) {
            return false;
        }

        ItemStack stack = player.getItemInHand(hand);
        ServerLevel level = player.serverLevel();
        BlockState targetState = level.getBlockState(hit.getBlockPos());
        int enchantmentLevel = ModEnchantments.getPickClimberLevel(level, stack);
        Vec3 currentVelocity = player.getDeltaMovement();

        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND
                : EquipmentSlot.OFFHAND;

        stack.hurtAndBreak(
                DURABILITY_COST,
                level,
                player,
                brokenItem -> player.onEquippedItemBroken(brokenItem, slot)
        );

        double additionalHeight = BASE_BOOST_HEIGHT
                + BOOST_HEIGHT_PER_ENCHANTMENT_LEVEL * enchantmentLevel;
        double boostedY = velocityForAdditionalRise(currentVelocity.y, additionalHeight);
        Vec3 boostedVelocity = new Vec3(currentVelocity.x, boostedY, currentVelocity.z);

        // This path never creates anchor state. The player preserves horizontal
        // inertia and receives only the calculated vertical boost.
        player.setDeltaMovement(boostedVelocity);
        player.fallDistance = 0.0F;
        player.setOnGround(false);

        if (!stack.isEmpty()) {
            startCooldown(player, stack);
        }
        playAnchorSound(level, player, targetState, hit.getBlockPos());
        PacketDistributor.sendToPlayer(
                player,
                new BoostSyncPayload(
                        boostedVelocity.x,
                        boostedVelocity.y,
                        boostedVelocity.z,
                        hand.ordinal(),
                        ANCHOR_COOLDOWN_TICKS
                )
        );
        return true;
    }

    public static boolean attach(ServerPlayer player, InteractionHand hand, BlockHitResult hit) {
        if (!canAttemptAnchor(player, hand, hit)) {
            return false;
        }

        ServerClimbState previous = SERVER_STATES.get(player.getUUID());
        Vec3 target = resolveCollisionSafeTargetPosition(player, hit);
        if (target == null) {
            return false;
        }
        ServerLevel level = player.serverLevel();
        BlockState anchorState = level.getBlockState(hit.getBlockPos());
        ItemStack stack = player.getItemInHand(hand);
        UUID toolId = ToolIdentity.ensure(stack);
        if (previous != null
                && previous.activeHand() != hand
                && previous.toolId().equals(toolId)) {
            // Distinct ItemStacks never share anchor identity. This can happen
            // when duplicating a pickaxe with NBT; without separating them, the
            // second pickaxe appears active and receives the wrong wear.
            toolId = UUID.randomUUID();
            ToolIdentity.assign(stack, toolId);
        }
        AnchorSurface surface = AnchorSurfaceClassifier.classify(anchorState);
        boolean reinforcedLatch = ModEnchantments.hasSturdyLatch(level, stack);
        boolean ceilingAnchor = hit.getDirection() == Direction.DOWN;
        AnchorMotion initialMotion = initialMotion(surface, player, reinforcedLatch, ceilingAnchor);

        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND
                : EquipmentSlot.OFFHAND;

        stack.hurtAndBreak(
                ceilingAnchor ? CEILING_DURABILITY_COST : DURABILITY_COST,
                level,
                player,
                brokenItem -> player.onEquippedItemBroken(brokenItem, slot)
        );

        // If the tool breaks while creating a new point, do not replace a
        // previous anchor or generate artificial movement.
        if (stack.isEmpty()) {
            return false;
        }

        UUID brakingSupportToolId = null;
        if (initialMotion == AnchorMotion.BRAKING) {
            InteractionHand supportHand = hand == InteractionHand.MAIN_HAND
                    ? InteractionHand.OFF_HAND
                    : InteractionHand.MAIN_HAND;
            ItemStack supportStack = player.getItemInHand(supportHand);
            if (isClimbingTool(supportStack)) {
                brakingSupportToolId = ToolIdentity.ensure(supportStack);
                if (brakingSupportToolId.equals(toolId)) {
                    brakingSupportToolId = UUID.randomUUID();
                    ToolIdentity.assign(supportStack, brakingSupportToolId);
                }
                EquipmentSlot supportSlot = supportHand == InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND
                        : EquipmentSlot.OFFHAND;
                supportStack.hurtAndBreak(
                        DURABILITY_COST,
                        level,
                        player,
                        brokenItem -> player.onEquippedItemBroken(brokenItem, supportSlot)
                );
                if (supportStack.isEmpty()) {
                    brakingSupportToolId = null;
                }
            }
        }

        boolean restoreNoGravity = previous == null
                ? player.isNoGravity()
                : previous.restoreNoGravity();
        boolean restoreFlying = previous == null
                ? player.getAbilities().flying
                : previous.restoreFlying();
        int crackId = previous == null
                ? createCrackId(player)
                : previous.crackId();

        if (previous != null) {
            clearAnchorVisuals(player, previous);
            // The previous pickaxe started its cooldown when it attached.
            // Switching tools neither restarts nor extends that timer.
        }

        ServerClimbState next = new ServerClimbState(
                level.dimension(),
                hit.getBlockPos().immutable(),
                hit.getDirection(),
                target,
                hand,
                toolId,
                crackId,
                restoreNoGravity,
                restoreFlying,
                player.level().getGameTime(),
                surface,
                initialMotion,
                initialSlideVelocity(surface, player, reinforcedLatch, ceilingAnchor),
                cooldownTicksFor(surface, reinforcedLatch),
                0.0F,
                0.0F,
                // The hit face lies exactly on an integer boundary. Move slightly
                // inside the block so BlockPos does not resolve the air block on
                // the player's side.
                hit.getLocation().subtract(target).subtract(
                        hit.getDirection().getStepX() * CONTACT_BLOCK_EPSILON,
                        hit.getDirection().getStepY() * CONTACT_BLOCK_EPSILON,
                        hit.getDirection().getStepZ() * CONTACT_BLOCK_EPSILON
                ),
                Vec3.ZERO,
                reinforcedLatch,
                brakingSupportToolId,
                0.0D,
                0,
                target,
                Vec3.ZERO,
                Vec3.ZERO
        );

        SERVER_STATES.put(player.getUUID(), next);
        LAST_REAL_JUMP.remove(player.getUUID());
        CONSUMED_JUMP.remove(player.getUUID());

        // Atomic, authoritative transition. setPos alone is insufficient for a
        // ServerPlayer: connection.teleport records a position pending confirmation
        // and prevents the next client movement packet from restoring the previous
        // position, the historical source of the snap or unintended boost.
        player.setDeltaMovement(Vec3.ZERO);
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        player.setNoGravity(true);
        player.connection.teleport(
                target.x,
                target.y,
                target.z,
                player.getYRot(),
                player.getXRot()
        );
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        player.setOnGround(false);

        showCracks(level, next);
        playAnchorSound(level, player, anchorState, next.anchorBlock());

        // Cooldown begins immediately when attachment is confirmed, and its overlay
        // decreases in real time while the pickaxe remains pinned. Releasing it
        // neither starts nor restarts the timer.
        startCooldown(player, stack, next.cooldownTicks());
        if (initialMotion == AnchorMotion.BRAKING) {
            startEquippedEffortCooldown(player, hand);
        }
        syncAttached(player, next, true);
        syncRemoteAnchorPose(player, next);
        return true;
    }

    public static void tick(Player player) {
        if (player.level().isClientSide()) {
            tickClient(player);
        } else if (player instanceof ServerPlayer serverPlayer) {
            tickServer(serverPlayer);
        }
    }

    private static void tickServer(ServerPlayer player) {
        if (player.onGround()) {
            LAST_REAL_JUMP.remove(player.getUUID());
            CONSUMED_JUMP.remove(player.getUUID());
        }

        ServerClimbState state = SERVER_STATES.get(player.getUUID());
        if (state == null) {
            return;
        }

        state = reconcileActiveHand(player, state);

        if (!isServerStateValid(player, state)) {
            boolean failedImmediately = player.level().getGameTime() - state.attachedAtGameTime()
                    <= FAILED_ATTACH_GRACE_TICKS;
            detachServerInternal(player, false, failedImmediately);
            return;
        }

        if (state.anchorFace() == Direction.DOWN
                && player.level().getGameTime() - state.attachedAtGameTime() > 0
                && (player.level().getGameTime() - state.attachedAtGameTime()) % CEILING_DURABILITY_INTERVAL_TICKS == 0
                && !damageEquippedTool(player, state.toolId(), 1)) {
            detachServerInternal(player, false, false);
            return;
        }

        // If the player attempts to reactivate creative flight while attached,
        // the anchor retains control until release.
        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        state = advanceAnchorMotion(player, state);
        if (state == null) {
            detachServerInternal(player, false, false);
            return;
        }
        SERVER_STATES.put(player.getUUID(), state);

        if (player.tickCount % CRACK_REFRESH_INTERVAL == 0) {
            showCracks(player.serverLevel(), state);
        }
        if (state.anchorFace() == Direction.DOWN
                || state.motion() != AnchorMotion.FIXED
                || player.tickCount % SERVER_SYNC_INTERVAL == 0) {
            syncAttached(player, state, false);
        }
        if (state.anchorFace() == Direction.DOWN
                && player.tickCount % REMOTE_POSE_REFRESH_INTERVAL_TICKS == 0) {
            syncRemoteAnchorPose(player, state);
        }

        holdPlayer(player, state);
    }

    private static void tickClient(Player player) {
        ClientClimbState state = CLIENT_STATES.get(player.getUUID());
        if (state == null) {
            return;
        }

        long elapsed = player.level().getGameTime() - state.lastSyncGameTime();
        if (elapsed > CLIENT_SYNC_TIMEOUT_TICKS) {
            // If the detach packet was lost, also clear the crack overlay locally
            // so it does not remain orphaned for several seconds.
            CLIENT_STATES.remove(player.getUUID());
            clearClientCracks(player, state);
        }
    }

    /**
     * Reconciles vanilla hand swapping (`F`) by ItemStack UUID. It neither
     * intercepts the key nor recreates the anchor: when the same pickaxe appears
     * in the opposite hand, only active state and pose move. Durability, cooldown,
     * sound, and cracks remain unchanged.
     */
    private static ServerClimbState reconcileActiveHand(
            ServerPlayer player,
            ServerClimbState state
    ) {
        ItemStack current = player.getItemInHand(state.activeHand());
        if (isClimbingTool(current) && ToolIdentity.matches(current, state.toolId())) {
            return state;
        }

        InteractionHand otherHand = state.activeHand() == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack other = player.getItemInHand(otherHand);

        if (!isClimbingTool(other) || !ToolIdentity.matches(other, state.toolId())) {
            // Do not repair a missing UUID onto another pickaxe: changing slots
            // must detach, not accidentally turn a different tool into the active
            // anchor.
            return state;
        }

        ServerClimbState transferred = state.withActiveHand(otherHand);
        SERVER_STATES.put(player.getUUID(), transferred);
        syncAttached(player, transferred, false);
        syncRemoteAnchorPose(player, transferred);
        LOGGER.info(
                "[PickClimber] action=TRANSFER_HAND player={} from={} to={} target={}",
                player.getScoreboardName(),
                state.activeHand(),
                otherHand,
                state.anchorBlock()
        );
        return transferred;
    }

    private static boolean isServerStateValid(ServerPlayer player, ServerClimbState state) {
        if (!player.isAlive() || player.isSpectator() || player.isFallFlying()) {
            return false;
        }

        if (!player.level().dimension().equals(state.anchorDimension())) {
            return false;
        }

        ItemStack held = player.getItemInHand(state.activeHand());
        if (!isClimbingTool(held) || !ToolIdentity.matches(held, state.toolId())) {
            return false;
        }

        if (player.position().distanceToSqr(state.targetPosition()) > MAX_DRIFT_DISTANCE_SQR) {
            return false;
        }

        BlockState blockState = player.level().getBlockState(state.anchorBlock());
        if (state.anchorFace() == Direction.DOWN) {
            return hasValidCeilingAnchor(player, blockState, state.anchorBlock(), held);
        }
        return hasValidAnchorFace(player, blockState, state.anchorBlock(), state.anchorFace());
    }

    private static boolean hasValidAnchorFace(
            Player player,
            BlockState state,
            BlockPos position,
            Direction face
    ) {
        if (state.isAir() || AnchorSurfaceClassifier.classify(state) == AnchorSurface.UNCLIMBABLE) {
            return false;
        }

        // Unstable surfaces include snow layers and powder snow, whose vanilla
        // geometry does not always declare a sturdy lateral face.
        return state.isFaceSturdy(player.level(), position, face)
                || AnchorSurfaceClassifier.classify(state) == AnchorSurface.UNSTABLE;
    }

    private static boolean hasValidCeilingAnchor(
            Player player,
            BlockState state,
            BlockPos position,
            ItemStack tool
    ) {
        AnchorSurface surface = AnchorSurfaceClassifier.classify(state);
        if (!ModEnchantments.hasStrongGrip(player.level(), tool)
                || surface == AnchorSurface.UNCLIMBABLE
                || (surface == AnchorSurface.UNSTABLE
                && !ModEnchantments.hasSturdyLatch(player.level(), tool))) {
            return false;
        }
        return state.isFaceSturdy(player.level(), position, Direction.DOWN)
                || surface == AnchorSurface.UNSTABLE;
    }

    private static AnchorMotion initialMotion(
            AnchorSurface surface,
            ServerPlayer player,
            boolean reinforcedLatch,
            boolean ceilingAnchor
    ) {
        if (ceilingAnchor) {
            return AnchorMotion.FIXED;
        }
        if (player.getDeltaMovement().y < BRAKING_START_SPEED
                && player.fallDistance > BRAKING_MIN_FALL_DISTANCE) {
            return AnchorMotion.BRAKING;
        }
        return surface == AnchorSurface.UNSTABLE && !reinforcedLatch
                ? AnchorMotion.UNSTABLE_SLIDING
                : AnchorMotion.FIXED;
    }

    private static double initialSlideVelocity(
            AnchorSurface surface,
            ServerPlayer player,
            boolean reinforcedLatch,
            boolean ceilingAnchor
    ) {
        return initialMotion(surface, player, reinforcedLatch, ceilingAnchor) == AnchorMotion.BRAKING
                ? player.getDeltaMovement().y
                : surface == AnchorSurface.UNSTABLE ? UNSTABLE_SLIDE_SPEED : 0.0D;
    }

    private static int cooldownTicksFor(AnchorSurface surface, boolean reinforcedLatch) {
        return surface == AnchorSurface.UNSTABLE && !reinforcedLatch
                ? UNSTABLE_ANCHOR_COOLDOWN_TICKS
                : ANCHOR_COOLDOWN_TICKS;
    }

    /** Updates anchor braking or descent exclusively on the server. */
    private static ServerClimbState advanceAnchorMotion(ServerPlayer player, ServerClimbState state) {
        if (player.level().getGameTime() <= state.attachedAtGameTime()) {
            return state;
        }
        if (state.anchorFace() == Direction.DOWN) {
            return advanceCeilingSwing(player, state);
        }
        if (state.motion() == AnchorMotion.FIXED) {
            return state;
        }
        if (state.motion() == AnchorMotion.BRAKING
                && state.brakingSupportToolId() != null
                && !hasBrakingSupport(player, state)) {
            // The second pickaxe is no longer equipped or has broken. The main
            // anchor continues safely, but no longer receives double braking or
            // applies wear to an unrelated tool.
            state = state.withoutBrakingSupport();
        }

        AnchorMotion nextMotion = state.motion();
        double nextVelocity = state.slideVelocity();
        if (state.motion() == AnchorMotion.BRAKING) {
            int brakingSteps = hasBrakingSupport(player, state) ? 2 : 1;
            for (int step = 0; step < brakingSteps; step++) {
                nextVelocity = Math.min(0.0D, nextVelocity * BRAKING_DRAG + BRAKING_RECOVERY);
            }
            if (nextVelocity > BRAKING_STOP_SPEED) {
                if (state.surface() == AnchorSurface.UNSTABLE && !state.reinforcedLatch()) {
                    nextMotion = AnchorMotion.UNSTABLE_SLIDING;
                    nextVelocity = UNSTABLE_SLIDE_SPEED;
                } else {
                    return state.withMotion(state.targetPosition(), AnchorMotion.FIXED, 0.0D);
                }
            }
        } else {
            nextVelocity = UNSTABLE_SLIDE_SPEED;
        }

        // Internal velocity may be very high after a long fall, but physical
        // travel is limited so every wall block can be inspected.
        double movementVelocity = Math.max(nextVelocity, -MAX_BRAKING_MOVE_PER_TICK);
        double lateralSpeed = Math.abs(movementVelocity) * 0.5D;
        if (state.motion() == AnchorMotion.BRAKING
                && state.committedBrakeDirection().lengthSqr() < 1.0E-5D) {
            Vec3 requestedDirection = lateralSlideDirection(player, state);
            if (requestedDirection.lengthSqr() >= 1.0E-5D) {
                state = state.withCommittedBrakeDirection(requestedDirection.normalize());
            }
        }
        Vec3 lateralMovement = state.committedBrakeDirection().lengthSqr() >= 1.0E-5D
                ? state.committedBrakeDirection().scale(lateralSpeed)
                : lateralSlideMovement(player, state, lateralSpeed);
        Vec3 nextTarget = state.targetPosition().add(lateralMovement).add(0.0D, movementVelocity, 0.0D);
        Vec3 displacement = nextTarget.subtract(player.position());
        if (!player.level().noCollision(player, player.getBoundingBox().move(displacement))) {
            if (!player.level().noCollision(
                    player,
                    player.getBoundingBox().move(0.0D, movementVelocity, 0.0D)
            )) {
                return null;
            }
            // A collision while sliding never crosses blocks or forces an
            // artificial fall. Stabilize at the last safe position.
            return state.withMotion(state.targetPosition(), AnchorMotion.FIXED, 0.0D);
        }

        BlockPos nextAnchorBlock = anchorBlockAt(player, state, nextTarget);
        BlockState nextBlockState = player.level().getBlockState(nextAnchorBlock);
        if (!hasValidAnchorFace(player, nextBlockState, nextAnchorBlock, state.anchorFace())) {
            // When the unstable wall ends, no valid support remains. The server
            // ends the anchor instead of continuing to use the initial block.
            return null;
        }

        AnchorSurface nextSurface = AnchorSurfaceClassifier.classify(nextBlockState);
        // A firm wall does not cancel its own braking. Only a transition from
        // unstable material to firm material should end descent.
        if (state.surface() == AnchorSurface.UNSTABLE
                && nextSurface != AnchorSurface.UNSTABLE) {
            nextMotion = AnchorMotion.FIXED;
            nextVelocity = 0.0D;
        }

        ServerClimbState next = state.withSlide(
                nextAnchorBlock,
                nextSurface,
                nextTarget,
                nextMotion,
                nextVelocity
        );
        if (state.motion() == AnchorMotion.BRAKING) {
            double brakingDistance = state.brakingDistance() + Math.abs(movementVelocity);
            int crossedBlocks = (int) Math.floor(brakingDistance) - state.chargedBrakingBlocks();
            next = next.withBrakingProgress(brakingDistance, state.chargedBrakingBlocks());
            if (crossedBlocks > 0) {
                next = applyBrakingWear(player, next, crossedBlocks * BRAKING_DURABILITY_PER_BLOCK);
                if (next == null) {
                    return null;
                }
                next = next.withBrakingProgress(brakingDistance, (int) Math.floor(brakingDistance));
            }
        }
        if (!state.anchorBlock().equals(nextAnchorBlock)) {
            clearAnchorVisuals(player, state);
            showCracks(player.serverLevel(), next);
        }
        return next;
    }

    /**
     * Restricted horizontal swing around the ceiling's center point. The server
     * integrates input, return force, damping, radius, and collisions.
     */
    private static ServerClimbState advanceCeilingSwing(ServerPlayer player, ServerClimbState state) {
        Vec3 center = state.ceilingCenter();
        Vec3 horizontalOffset = new Vec3(
                state.targetPosition().x - center.x,
                0.0D,
                state.targetPosition().z - center.z
        );
        Vec3 input = ceilingInputDirection(player, state);
        Vec3 acceleration = input.scale(CEILING_SWING_ACCELERATION)
                .add(horizontalOffset.scale(-CEILING_SWING_RETURN));
        Vec3 velocity = new Vec3(state.swingVelocity().x, 0.0D, state.swingVelocity().z)
                .add(acceleration)
                .scale(CEILING_SWING_DAMPING);
        Vec3 releaseMomentum = new Vec3(
                state.swingReleaseMomentum().x,
                0.0D,
                state.swingReleaseMomentum().z
        ).add(acceleration).scale(CEILING_SWING_DAMPING);

        if (velocity.length() > CEILING_SWING_MAX_SPEED) {
            velocity = velocity.normalize().scale(CEILING_SWING_MAX_SPEED);
        }
        if (releaseMomentum.length() > CEILING_RELEASE_MOMENTUM_MAX_SPEED) {
            releaseMomentum = releaseMomentum.normalize().scale(CEILING_RELEASE_MOMENTUM_MAX_SPEED);
        }

        Vec3 nextOffset = horizontalOffset.add(velocity);
        if (nextOffset.length() > CEILING_SWING_RADIUS) {
            nextOffset = nextOffset.normalize().scale(CEILING_SWING_RADIUS);
            double outwardSpeed = velocity.dot(nextOffset.normalize());
            if (outwardSpeed > 0.0D) {
                velocity = velocity.subtract(nextOffset.normalize().scale(outwardSpeed));
            }
        }

        // The vanilla hitbox stays vertical and cannot tilt like a pendulum body.
        // Raising it at the extremes would insert it into the ceiling, so the
        // pivot is simulated on a horizontal plane at a safe height.
        Vec3 nextTarget = new Vec3(
                center.x + nextOffset.x,
                center.y,
                center.z + nextOffset.z
        );
        Vec3 displacement = nextTarget.subtract(player.position());
        if (!player.level().noCollision(player, player.getBoundingBox().move(displacement))) {
            return state.withCeilingSwing(state.targetPosition(), Vec3.ZERO, Vec3.ZERO);
        }
        return state.withCeilingSwing(nextTarget, velocity, releaseMomentum);
    }

    private static Vec3 ceilingInputDirection(ServerPlayer player, ServerClimbState state) {
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (forward.lengthSqr() < 1.0E-5D) {
            return Vec3.ZERO;
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 input = forward.scale(state.lateralForward())
                .add(right.scale(-state.lateralStrafe()));
        return input.lengthSqr() > 1.0D ? input.normalize() : input;
    }

    /** Two pickaxes approximately halve braking time and travel. */
    private static boolean hasBrakingSupport(ServerPlayer player, ServerClimbState state) {
        UUID supportToolId = state.brakingSupportToolId();
        return supportToolId != null && findEquippedToolById(player, supportToolId) != ItemStack.EMPTY;
    }

    /** Charges additional wear only for blocks actually travelled while braking. */
    private static ServerClimbState applyBrakingWear(
            ServerPlayer player,
            ServerClimbState state,
            int amount
    ) {
        if (!damageEquippedTool(player, state.toolId(), amount)) {
            return null;
        }

        UUID supportToolId = state.brakingSupportToolId();
        if (supportToolId == null || damageEquippedTool(player, supportToolId, amount)) {
            return state;
        }
        return state.withoutBrakingSupport();
    }

    private static boolean damageEquippedTool(ServerPlayer player, UUID toolId, int amount) {
        ItemStack stack = findEquippedToolById(player, toolId);
        if (stack.isEmpty()) {
            return false;
        }
        InteractionHand hand = ToolIdentity.matches(player.getMainHandItem(), toolId)
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND
                : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(
                amount,
                player.serverLevel(),
                player,
                brokenItem -> player.onEquippedItemBroken(brokenItem, slot)
        );
        return !stack.isEmpty();
    }

    private static ItemStack findEquippedToolById(ServerPlayer player, UUID toolId) {
        if (ToolIdentity.matches(player.getMainHandItem(), toolId)) {
            return player.getMainHandItem();
        }
        if (ToolIdentity.matches(player.getOffhandItem(), toolId)) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private static boolean hasDuplicatedActiveIdentity(
            Player player,
            InteractionHand candidateHand,
            ItemStack candidate
    ) {
        InteractionHand currentActiveHand = activeHand(player);
        if (currentActiveHand == null
                || currentActiveHand == candidateHand
                || !isActiveTool(player, candidate)) {
            return false;
        }
        return ToolIdentity.get(candidate)
                .map(id -> ToolIdentity.matches(player.getItemInHand(currentActiveHand), id))
                .orElse(false);
    }

    /** Uses the anchor point as the real position while the server holds it fixed. */
    private static Vec3 currentAttachmentTarget(Player player) {
        if (player.level().isClientSide()) {
            ClientClimbState state = CLIENT_STATES.get(player.getUUID());
            return state == null ? player.position() : state.targetPosition();
        }
        ServerClimbState state = SERVER_STATES.get(player.getUUID());
        return state == null ? player.position() : state.targetPosition();
    }

    private static Vec3 lateralSlideMovement(ServerPlayer player, ServerClimbState state, double speed) {
        return lateralSlideDirection(player, state).scale(speed);
    }

    private static Vec3 lateralSlideDirection(ServerPlayer player, ServerClimbState state) {
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (forward.lengthSqr() < 1.0E-5D) {
            return Vec3.ZERO;
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 input = forward.scale(state.lateralForward()).add(right.scale(-state.lateralStrafe()));
        if (input.lengthSqr() > 1.0D) {
            input = input.normalize();
        }

        // Cancel the wall-normal component: the player may look and turn freely,
        // but the anchor neither separates from nor crosses the wall.
        return switch (state.anchorFace().getAxis()) {
            case X -> new Vec3(0.0D, 0.0D, input.z);
            case Z -> new Vec3(input.x, 0.0D, 0.0D);
            case Y -> Vec3.ZERO;
        };
    }

    private static BlockPos anchorBlockAt(Player player, ServerClimbState state, Vec3 target) {
        return BlockPos.containing(target.add(state.contactOffset()));
    }

    private static void holdPlayer(ServerPlayer player, ServerClimbState state) {
        Vec3 target = state.targetPosition();
        player.setNoGravity(true);
        player.fallDistance = 0.0F;
        player.setOnGround(false);
        player.setDeltaMovement(Vec3.ZERO);

        // Walls tolerate minimal drift. Swinging must confirm even small movements:
        // accumulating 0.05 blocks caused slow return motion to arrive in visible
        // multi-tick jumps.
        double correctionThresholdSqr = state.anchorFace() == Direction.DOWN
                ? 1.0E-8D
                : 2.5E-3D;
        if (player.position().distanceToSqr(target) > correctionThresholdSqr) {
            boolean smoothCeilingStep = state.anchorFace() == Direction.DOWN
                    && player.tickCount % SERVER_SYNC_INTERVAL != 0;
            if (smoothCeilingStep) {
                // The target was collision-validated by the server and is sent to
                // the client in the same tick. Keep periodic teleport confirmations
                // without issuing a hard correction for every sub-block step.
                player.setPos(target);
            } else {
                player.connection.teleport(
                        target.x,
                        target.y,
                        target.z,
                        player.getYRot(),
                        player.getXRot(),
                        RelativeMovement.ROTATION
                );
            }
            player.setDeltaMovement(Vec3.ZERO);
        }
    }

    public static void detachServer(ServerPlayer player, boolean jump) {
        detachServerInternal(player, jump, false);
    }

    /** Receives client intent; the server decides velocity, plane, and collisions. */
    public static void updateSlideInput(ServerPlayer player, SlideInputPayload payload) {
        ServerClimbState state = SERVER_STATES.get(player.getUUID());
        if (state == null
                || !Float.isFinite(payload.forward())
                || !Float.isFinite(payload.strafe())
                || !Float.isFinite(payload.yaw())
                || !Float.isFinite(payload.pitch())) {
            return;
        }

        float forward = Mth.clamp(payload.forward(), -1.0F, 1.0F);
        float strafe = Mth.clamp(payload.strafe(), -1.0F, 1.0F);
        player.setYRot(Mth.wrapDegrees(payload.yaw()));
        player.setXRot(Mth.clamp(payload.pitch(), -90.0F, 90.0F));
        SERVER_STATES.put(player.getUUID(), state.withSlideInput(forward, strafe));
    }

    private static void detachServerInternal(ServerPlayer player, boolean jump, boolean refundCooldown) {
        ServerClimbState state = SERVER_STATES.remove(player.getUUID());
        if (state == null) {
            return;
        }

        long attachmentAge = player.level().getGameTime() - state.attachedAtGameTime();
        LOGGER.info(
                "[PickClimber] action={} player={} hand={} attachmentAge={} target={}",
                jump ? "DETACH_JUMP" : "DETACH_PASSIVE",
                player.getScoreboardName(),
                state.activeHand(),
                attachmentAge,
                state.anchorBlock()
        );

        clearAnchorVisuals(player, state);

        ItemStack activeTool = findToolById(player, state.toolId());
        int remainingCooldownTicks = 0;
        if (refundCooldown) {
            if (!activeTool.isEmpty()) {
                ToolIdentity.clearCooldown(activeTool);
                player.getInventory().setChanged();
            }
        } else if (!activeTool.isEmpty()) {
            // The timer already started when the anchor was created. Releasing
            // the pickaxe does not restart it; only the true remaining time is synced.
            remainingCooldownTicks = cooldownTicksRemaining(
                    activeTool,
                    player.level().getGameTime()
            );
        }

        Vec3 detachVelocity = jump
                ? state.anchorFace() == Direction.DOWN
                ? calculateCeilingReleaseVelocity(player, state)
                : calculateJumpVelocity(player, activeTool)
                : Vec3.ZERO;

        restoreAbilities(player, state.restoreNoGravity(), state.restoreFlying());
        player.fallDistance = 0.0F;
        player.setDeltaMovement(detachVelocity);

        if (jump && state.anchorFace() != Direction.DOWN) {
            // A wall jump is also an explicit ascent requested by the player and
            // may chain into a boost with the other pickaxe.
            LAST_REAL_JUMP.put(player.getUUID(), player.level().getGameTime());
            CONSUMED_JUMP.remove(player.getUUID());
        }

        syncDetached(
                player,
                state.restoreNoGravity(),
                state.restoreFlying(),
                jump,
                state.toolId(),
                remainingCooldownTicks,
                refundCooldown
        );
        syncRemoteAnchorPoseDetached(player);
        if (jump && state.anchorFace() == Direction.DOWN) {
            // Unlike wall jumps, ceiling releases cannot be predicted from client
            // anchor state because swing velocity is server-authoritative. Send
            // the exact result after detach so local physics cannot discard it.
            PacketDistributor.sendToPlayer(player, new BoostSyncPayload(
                    detachVelocity.x,
                    detachVelocity.y,
                    detachVelocity.z,
                    state.activeHand().ordinal(),
                    0
            ));
        }
    }

    public static void detachClient(Player player, boolean jump) {
        ClientClimbState state = CLIENT_STATES.remove(player.getUUID());
        if (state == null) {
            return;
        }

        clearClientCracks(player, state);

        // Only the user-requested jump is predicted locally. Gravity, flight, and
        // passive detaches are synchronized by the server.
        if (jump && !state.ceilingAnchor()) {
            ItemStack activeTool = player.getItemInHand(state.activeHand());
            player.setDeltaMovement(calculateJumpVelocity(player, activeTool));
        }
    }

    public static void cleanupServer(ServerPlayer player) {
        LAST_REAL_JUMP.remove(player.getUUID());
        CONSUMED_JUMP.remove(player.getUUID());
        ServerClimbState state = SERVER_STATES.remove(player.getUUID());
        if (state == null) {
            return;
        }

        clearAnchorVisuals(player, state);
        restoreAbilities(player, state.restoreNoGravity(), state.restoreFlying());
        player.setDeltaMovement(Vec3.ZERO);
        syncRemoteAnchorPoseDetached(player);
    }

    public static void applyClientSync(Player player, AnchorSyncPayload payload) {
        ClientClimbState previousState = CLIENT_STATES.get(player.getUUID());

        if (!payload.attached()) {
            CLIENT_STATES.remove(player.getUUID());
            if (previousState != null) {
                clearClientCracks(player, previousState);
            }
            ItemStack releasedTool = findToolById(player, payload.toolId());
            if (!releasedTool.isEmpty()) {
                if (payload.refundCooldown()) {
                    ToolIdentity.clearCooldown(releasedTool);
                } else if (payload.cooldownTicks() > 0) {
                    ToolIdentity.startCooldown(
                            releasedTool,
                            player.level().getGameTime() + payload.cooldownTicks()
                    );
                } else {
                    // If cooldown ended while the pickaxe remained attached,
                    // remove any residual client-side visual drift.
                    ToolIdentity.clearCooldown(releasedTool);
                }
            }

            // This anchor-state payload synchronizes visual state only. Passive
            // movement still arrives through vanilla packets; a ceiling jump is
            // followed by an explicit server-authoritative velocity payload.
            if (!payload.jumpDetach()) {
                player.setDeltaMovement(Vec3.ZERO);
            }
            return;
        }

        InteractionHand hand = payload.handOrdinal() == InteractionHand.OFF_HAND.ordinal()
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        Vec3 target = new Vec3(payload.targetX(), payload.targetY(), payload.targetZ());
        BlockPos anchorBlock = new BlockPos(
                payload.anchorX(),
                payload.anchorY(),
                payload.anchorZ()
        );

        if (previousState != null
                && (previousState.crackId() != payload.crackId()
                || !previousState.anchorBlock().equals(anchorBlock))) {
            clearClientCracks(player, previousState);
        }

        ItemStack localTool = player.getItemInHand(hand);
        if (isClimbingTool(localTool) && payload.newAnchor()) {
            // Only a genuinely new anchor may assign identity. During an F transfer,
            // the vanilla inventory packet may arrive an instant later; assigning
            // here would mark the wrong pickaxe.
            if (!ToolIdentity.matches(localTool, payload.toolId())) {
                ToolIdentity.assign(localTool, payload.toolId());
            }
            ToolIdentity.startCooldown(
                    localTool,
                    player.level().getGameTime() + payload.cooldownTicks(),
                    payload.cooldownTicks()
            );
        }

        long now = player.level().getGameTime();
        long poseStartedGameTime = payload.newAnchor()
                || previousState == null
                || !previousState.toolId().equals(payload.toolId())
                ? now
                : previousState.poseStartedGameTime();

        ClientClimbState next = new ClientClimbState(
                target,
                anchorBlock,
                payload.crackId(),
                hand,
                payload.toolId(),
                payload.restoreNoGravity(),
                payload.restoreFlying(),
                now,
                poseStartedGameTime,
                payload.ceilingAnchor()
        );

        CLIENT_STATES.put(player.getUUID(), next);
        if (payload.ceilingAnchor()) {
            // This is the exact collision-validated server target, not local
            // prediction. setPos preserves normal render interpolation between
            // ticks while periodic teleports remain the hard authority check.
            player.setDeltaMovement(Vec3.ZERO);
            player.setPos(target);
        }
    }

    public static void applyClientBoost(Player player, BoostSyncPayload payload) {
        // A delayed boost packet must never remove an already confirmed anchor.
        // Always prioritize the safe attached state.
        if (CLIENT_STATES.containsKey(player.getUUID())) {
            return;
        }

        InteractionHand hand = payload.handOrdinal() == InteractionHand.OFF_HAND.ordinal()
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack localTool = player.getItemInHand(hand);
        if (isClimbingTool(localTool) && payload.cooldownTicks() > 0) {
            ToolIdentity.startCooldown(
                    localTool,
                    player.level().getGameTime() + payload.cooldownTicks(),
                    payload.cooldownTicks()
            );
        }

        player.setDeltaMovement(new Vec3(
                payload.velocityX(),
                payload.velocityY(),
                payload.velocityZ()
        ));
        player.fallDistance = 0.0F;
        player.setOnGround(false);
    }

    /** Applies observer-only pose state without touching local anchor physics. */
    public static void applyRemoteAnchorPose(Player receivingPlayer, RemoteAnchorPosePayload payload) {
        if (!payload.ceilingAnchor()) {
            REMOTE_ANCHOR_POSES.remove(payload.playerId());
            return;
        }

        InteractionHand hand = payload.handOrdinal() == InteractionHand.OFF_HAND.ordinal()
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        REMOTE_ANCHOR_POSES.put(
                payload.playerId(),
                new RemoteAnchorPoseState(hand, receivingPlayer.level().getGameTime())
        );
    }

    /** Clears visual state and the synthetic overlay before leaving the level. */
    public static void clearAllClientStates(Player localPlayer) {
        if (localPlayer != null) {
            ClientClimbState state = CLIENT_STATES.remove(localPlayer.getUUID());
            if (state != null) {
                clearClientCracks(localPlayer, state);
            }
        } else {
            CLIENT_STATES.clear();
        }
        REMOTE_ANCHOR_POSES.clear();
    }

    private static Vec3 calculateTargetPosition(Player player, BlockHitResult hit) {
        BlockPos block = hit.getBlockPos();
        Direction face = hit.getDirection();
        Vec3 location = hit.getLocation();
        double wallOffset = player.getBbWidth() * 0.5D + 0.08D;

        // Preserve the exact coordinate clicked on the face to allow horizontal
        // and diagonal movement rather than vertical movement only.
        double targetX = location.x;
        double targetZ = location.z;

        switch (face) {
            case EAST -> {
                targetX = block.getX() + 1.0D + wallOffset;
                targetZ = Mth.clamp(location.z, block.getZ() + 0.04D, block.getZ() + 0.96D);
            }
            case WEST -> {
                targetX = block.getX() - wallOffset;
                targetZ = Mth.clamp(location.z, block.getZ() + 0.04D, block.getZ() + 0.96D);
            }
            case SOUTH -> {
                targetX = Mth.clamp(location.x, block.getX() + 0.04D, block.getX() + 0.96D);
                targetZ = block.getZ() + 1.0D + wallOffset;
            }
            case NORTH -> {
                targetX = Mth.clamp(location.x, block.getX() + 0.04D, block.getX() + 0.96D);
                targetZ = block.getZ() - wallOffset;
            }
            case DOWN -> {
                targetX = Mth.clamp(location.x, block.getX() + 0.04D, block.getX() + 0.96D);
                targetZ = Mth.clamp(location.z, block.getZ() + 0.04D, block.getZ() + 0.96D);
                return new Vec3(
                        targetX,
                        block.getY() - player.getBbHeight() - 0.08D,
                        targetZ
                );
            }
            default -> {
                // Top and bottom faces were already rejected.
            }
        }

        double targetY = location.y - player.getBbHeight() * 0.62D;
        return new Vec3(targetX, targetY, targetZ);
    }

    /**
     * Adjusts only the height of a lateral target when the ideal position would
     * insert the hitbox into a nearby floor or ceiling. The common case is moving
     * from ceiling to wall: an eye-level hit places the feet half a block higher,
     * even though preserving the current height is completely safe.
     *
     * The search always starts at the ideal target and moves toward the current
     * height, preserving as much vertical displacement as collision allows. It
     * never corrects ceiling faces or more than half the player's height; the
     * 1.5-block reach is validated against the resulting position afterward.
     */
    private static Vec3 resolveCollisionSafeTargetPosition(Player player, BlockHitResult hit) {
        Vec3 idealTarget = calculateTargetPosition(player, hit);
        if (isTargetPositionFree(player, idealTarget)) {
            return idealTarget;
        }

        if (hit.getDirection().getAxis() == Direction.Axis.Y) {
            return null;
        }

        double safeReferenceY = currentAttachmentTarget(player).y;
        double verticalCorrection = safeReferenceY - idealTarget.y;
        if (Math.abs(verticalCorrection) > player.getBbHeight() * 0.5D) {
            return null;
        }

        for (int step = 1; step <= WALL_TARGET_COLLISION_STEPS; step++) {
            double progress = (double) step / WALL_TARGET_COLLISION_STEPS;
            Vec3 candidate = new Vec3(
                    idealTarget.x,
                    Mth.lerp(progress, idealTarget.y, safeReferenceY),
                    idealTarget.z
            );
            if (isTargetPositionFree(player, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isTargetPositionFree(Player player, Vec3 target) {
        Vec3 displacement = target.subtract(player.position());
        return player.level().noCollision(player, player.getBoundingBox().move(displacement));
    }

    private static Vec3 calculateJumpVelocity(Player player, ItemStack activeTool) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);

        if (horizontal.lengthSqr() < 1.0E-5D) {
            horizontal = Vec3.directionFromRotation(0.0F, player.getYRot());
            horizontal = new Vec3(horizontal.x, 0.0D, horizontal.z);
        }

        horizontal = horizontal.normalize().scale(WALL_JUMP_HORIZONTAL_IMPULSE);
        double baseVertical = Mth.clamp(0.28D + look.y * 0.32D, 0.12D, 0.52D);
        int enchantmentLevel = ModEnchantments.getPickClimberLevel(player.level(), activeTool);
        double extraHeight = enchantmentLevel * WALL_JUMP_HEIGHT_PER_ENCHANTMENT_LEVEL;
        double vertical = extraHeight <= 0.0D
                ? baseVertical
                : velocityForAdditionalRise(baseVertical, extraHeight);
        return new Vec3(horizontal.x, vertical, horizontal.z);
    }

    private static Vec3 calculateCeilingReleaseVelocity(
            ServerPlayer player,
            ServerClimbState state
    ) {
        Vec3 requestedDirection = ceilingInputDirection(player, state);
        Vec3 releaseMomentum = new Vec3(
                state.swingReleaseMomentum().x,
                0.0D,
                state.swingReleaseMomentum().z
        );
        Vec3 release = releaseMomentum
                // W/A/S/D apply exactly the horizontal impulse of a wall jump.
                // Accumulated swing momentum is added as a vector, so build-up in
                // any direction can reinforce or oppose the requested jump.
                .add(requestedDirection.scale(WALL_JUMP_HORIZONTAL_IMPULSE));
        if (release.length() > CEILING_RELEASE_MAX_SPEED) {
            release = release.normalize().scale(CEILING_RELEASE_MAX_SPEED);
        }
        return release;
    }

    /**
     * Calculates an initial velocity that adds an approximate amount of height
     * to Minecraft's remaining vertical trajectory. This avoids adding blocks as
     * raw velocity, which would produce uncontrolled launches.
     */
    private static double velocityForAdditionalRise(double currentVelocity, double additionalHeight) {
        double safeCurrent = Math.max(0.0D, currentVelocity);
        double desiredRise = predictedVerticalRise(safeCurrent) + Math.max(0.0D, additionalHeight);
        double low = safeCurrent;
        double high = Math.max(0.5D, safeCurrent + 0.5D);

        while (predictedVerticalRise(high) < desiredRise && high < 4.0D) {
            high *= 1.35D;
        }

        for (int i = 0; i < 24; i++) {
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

        for (int tick = 0; tick < 80 && velocity > 0.0D; tick++) {
            height += velocity;
            velocity = (velocity - 0.08D) * 0.98D;
        }
        return height;
    }

    private static ItemStack findToolById(Player player, UUID toolId) {
        for (ItemStack stack : player.getInventory().items) {
            if (isClimbingTool(stack) && ToolIdentity.matches(stack, toolId)) {
                return stack;
            }
        }

        ItemStack offhand = player.getOffhandItem();
        if (isClimbingTool(offhand) && ToolIdentity.matches(offhand, toolId)) {
            return offhand;
        }
        return ItemStack.EMPTY;
    }

    private static int cooldownTicksRemaining(ItemStack stack, long gameTime) {
        long until = ToolIdentity.cooldownUntil(stack);
        if (until <= gameTime) {
            return 0;
        }
        return (int) Math.min(ANCHOR_COOLDOWN_TICKS, until - gameTime);
    }

    private static void startCooldown(ServerPlayer player, ItemStack stack) {
        startCooldown(player, stack, ANCHOR_COOLDOWN_TICKS);
    }

    private static void startCooldown(ServerPlayer player, ItemStack stack, int cooldownTicks) {
        ToolIdentity.startCooldown(
                stack,
                player.level().getGameTime() + cooldownTicks,
                cooldownTicks
        );
        player.getInventory().setChanged();
    }

    /** A hard fall commits both equipped hands and prevents chaining another pickaxe. */
    private static void startEquippedEffortCooldown(ServerPlayer player, InteractionHand activeHand) {
        long until = player.level().getGameTime() + ANCHOR_COOLDOWN_TICKS;
        for (InteractionHand hand : InteractionHand.values()) {
            if (hand == activeHand) {
                continue;
            }
            ItemStack equipped = player.getItemInHand(hand);
            if (isClimbingTool(equipped)) {
                ToolIdentity.startCooldown(equipped, until, ANCHOR_COOLDOWN_TICKS);
            }
        }
        player.getInventory().setChanged();
    }

    private static int createCrackId(ServerPlayer player) {
        // Synthetic ID: prevents Minecraft from excluding the player from the
        // crack packet by confusing it with the vanilla breakerId.
        return -1_000_000 - (player.getUUID().hashCode() & 0x3FFFFFFF);
    }

    private static void showCracks(ServerLevel level, ServerClimbState state) {
        level.destroyBlockProgress(state.crackId(), state.anchorBlock(), CRACK_STAGE);
    }

    private static void clearCracks(ServerLevel level, int crackId, BlockPos blockPos) {
        level.destroyBlockProgress(crackId, blockPos, -1);
    }

    /**
     * Always cleans up in the dimension where the anchor was created. This matters
     * during dimension changes: by the time PlayerChangedDimensionEvent arrives,
     * the ServerPlayer already belongs to the new level.
     */
    private static void clearAnchorVisuals(ServerPlayer player, ServerClimbState state) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        ServerLevel anchorLevel = server.getLevel(state.anchorDimension());
        if (anchorLevel != null) {
            clearCracks(anchorLevel, state.crackId(), state.anchorBlock());
        }
    }

    /**
     * The client stores destruction overlays in a local table. On disconnect, the
     * server packet may arrive too late, so the overlay is also removed directly
     * before ClientLevel is destroyed.
     */
    private static void clearClientCracks(Player player, ClientClimbState state) {
        player.level().destroyBlockProgress(
                state.crackId(),
                state.anchorBlock(),
                -1
        );
    }

    private static void playAnchorSound(
            ServerLevel level,
            ServerPlayer player,
            BlockState blockState,
            BlockPos blockPos
    ) {
        SoundType soundType = blockState.getSoundType();
        float volume = Mth.clamp((soundType.getVolume() + 1.0F) * 0.22F, 0.25F, 0.65F);
        float pitch = soundType.getPitch() * (0.88F + level.getRandom().nextFloat() * 0.08F);

        level.playSound(
                null,
                blockPos,
                soundType.getBreakSound(),
                SoundSource.PLAYERS,
                volume,
                pitch
        );
    }

    private static void restoreAbilities(Player player, boolean noGravity, boolean flying) {
        player.setNoGravity(noGravity);
        player.getAbilities().flying = flying;
        player.onUpdateAbilities();
    }

    private static void syncAttached(ServerPlayer player, ServerClimbState state, boolean newAnchor) {
        PacketDistributor.sendToPlayer(player, AnchorSyncPayload.attached(state, newAnchor));
    }

    private static void syncRemoteAnchorPose(ServerPlayer player, ServerClimbState state) {
        RemoteAnchorPosePayload payload = state.anchorFace() == Direction.DOWN
                ? RemoteAnchorPosePayload.attached(player.getUUID(), state.activeHand())
                : RemoteAnchorPosePayload.detached(player.getUUID());
        PacketDistributor.sendToPlayersTrackingEntity(player, payload);
    }

    private static void syncRemoteAnchorPoseDetached(ServerPlayer player) {
        // A dimension change can move the entity between tracking sets before
        // cleanup runs. Broadcasting this tiny removal packet prevents a stale
        // raised arm in clients that tracked the previous dimension.
        PacketDistributor.sendToAllPlayers(RemoteAnchorPosePayload.detached(player.getUUID()));
    }

    private static void syncDetached(
            ServerPlayer player,
            boolean restoreNoGravity,
            boolean restoreFlying,
            boolean jump,
            UUID toolId,
            int cooldownTicks,
            boolean refundCooldown
    ) {
        PacketDistributor.sendToPlayer(
                player,
                AnchorSyncPayload.detached(
                        restoreNoGravity,
                        restoreFlying,
                        jump,
                        toolId,
                        cooldownTicks,
                        refundCooldown
                )
        );
    }

    private static RemoteAnchorPoseState remoteAnchorPose(Player player) {
        RemoteAnchorPoseState state = REMOTE_ANCHOR_POSES.get(player.getUUID());
        if (state == null) {
            return null;
        }
        if (player.level().getGameTime() - state.lastSyncGameTime() > CLIENT_SYNC_TIMEOUT_TICKS) {
            REMOTE_ANCHOR_POSES.remove(player.getUUID());
            return null;
        }
        return state;
    }

    private record RemoteAnchorPoseState(
            InteractionHand activeHand,
            long lastSyncGameTime
    ) {
    }
}
