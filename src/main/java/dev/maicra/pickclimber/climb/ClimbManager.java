package dev.maicra.pickclimber.climb;

import com.mojang.logging.LogUtils;
import dev.maicra.pickclimber.network.AnchorSyncPayload;
import dev.maicra.pickclimber.network.BoostSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
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
    public static final int CRACK_STAGE = 5;
    public static final int ANCHOR_COOLDOWN_TICKS = 20;

    /** La velocidad sigue siendo una comprobación secundaria, nunca la autorización del impulso. */
    public static final double RISING_VELOCITY_THRESHOLD = 0.08D;
    private static final int JUMP_BOOST_WINDOW_TICKS = 8;
    private static final double BASE_BOOST_HEIGHT = 1.0D;
    private static final double BOOST_HEIGHT_PER_ENCHANTMENT_LEVEL = 0.5D;
    private static final double WALL_JUMP_HEIGHT_PER_ENCHANTMENT_LEVEL = 0.5D;

    /** Punto estable de la animación vanilla donde el pico está más adelantado. */
    private static final float PINNED_SWING_PROGRESS = 0.5F;
    private static final float PINNED_POSE_RAMP_TICKS = 4.0F;

    private static final double MAX_HIT_DISTANCE_SQR = 25.0D;
    private static final double MAX_ANCHOR_MOVE_SQR = 2.25D; // 1,5 bloques reales.
    private static final double MAX_DRIFT_DISTANCE_SQR = 16.0D;
    private static final double ATTACHMENT_COHERENCE_DISTANCE_SQR = 1.0D;
    private static final int CRACK_REFRESH_INTERVAL = 10;
    private static final int SERVER_SYNC_INTERVAL = 5;
    private static final int CLIENT_SYNC_TIMEOUT_TICKS = 40;
    private static final int FAILED_ATTACH_GRACE_TICKS = 5;

    private static final Map<UUID, ServerClimbState> SERVER_STATES = new HashMap<>();
    private static final Map<UUID, ClientClimbState> CLIENT_STATES = new HashMap<>();
    private static final Map<UUID, Long> LAST_REAL_JUMP = new HashMap<>();
    private static final Map<UUID, Long> CONSUMED_JUMP = new HashMap<>();

    private ClimbManager() {
    }

    /** Registra exclusivamente saltos reales disparados por LivingJumpEvent en el servidor. */
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

    public static boolean isPickaxe(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ItemTags.PICKAXES);
    }

    public static boolean isAttached(Player player) {
        return player.level().isClientSide()
                ? CLIENT_STATES.containsKey(player.getUUID())
                : SERVER_STATES.containsKey(player.getUUID());
    }

    public static InteractionHand activeHand(Player player) {
        if (player.level().isClientSide()) {
            ClientClimbState state = CLIENT_STATES.get(player.getUUID());
            return state == null ? null : state.activeHand();
        }

        ServerClimbState state = SERVER_STATES.get(player.getUUID());
        return state == null ? null : state.activeHand();
    }

    /** Comprueba el UUID del pico, no solo el tipo de ítem o la mano. */
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
     * Fracción visual del cooldown individual persistido en el ItemStack.
     *
     * El temporizador comienza al confirmar el enganche y continúa bajando aunque
     * el pico siga siendo el ancla activa. El estado "enganchado" no congela ni
     * reemplaza el cooldown: su indicador dedicado se implementará por separado.
     */
    public static float visualCooldownFraction(Player player, ItemStack stack) {
        return ToolIdentity.cooldownFraction(
                stack,
                player.level().getGameTime(),
                ANCHOR_COOLDOWN_TICKS
        );
    }

    /**
     * Devuelve el progreso de golpe fijo usado para representar el pico clavado.
     * Un valor negativo indica que este stack no es el pico activo del cliente.
     * La entrada se anima durante unos pocos ticks y luego queda completamente
     * congelada, sin depender de la animación vanilla que continúa avanzando.
     */
    public static float pinnedPoseProgress(Player player, ItemStack stack, float partialTick) {
        if (!player.level().isClientSide()) {
            return -1.0F;
        }

        ClientClimbState state = CLIENT_STATES.get(player.getUUID());
        if (state == null || !ToolIdentity.matches(stack, state.toolId())) {
            return -1.0F;
        }

        float age = (float) (player.level().getGameTime() - state.poseStartedGameTime()) + partialTick;
        float blend = Mth.clamp(age / PINNED_POSE_RAMP_TICKS, 0.0F, 1.0F);
        // Curva suave: entra rápido, pero evita un salto visual seco desde idle.
        blend = 1.0F - (1.0F - blend) * (1.0F - blend);
        return PINNED_SWING_PROGRESS * blend;
    }

    /**
     * Comprueba que el estado lógico del anclaje coincida con la posición física
     * del jugador. Se usa para recuperar estados transitorios donde el cliente
     * todavía cree estar enganchado mientras ya está cayendo.
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
            // El cliente no recupera ni altera estados físicos de forma autónoma.
            // Espera la decisión autoritativa del servidor para evitar carreras.
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
        // Volar en creativo no invalida el enganche: la mecánica también debe servir
        // como salvavidas cuando el jugador está cayendo.
        if (!player.isAlive() || player.isSpectator() || player.isFallFlying()) {
            return false;
        }

        Direction face = hit.getDirection();
        if (face.getAxis() == Direction.Axis.Y) {
            return false;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!isPickaxe(stack)) {
            return false;
        }

        // Un pico enganchado está ocupado aunque su cooldown interno pueda
        // haber terminado mientras permanecía clavado en la pared.
        if (isActiveTool(player, stack)) {
            return false;
        }

        if (ToolIdentity.isCoolingDown(stack, player.level().getGameTime())) {
            return false;
        }

        if (player.getEyePosition().distanceToSqr(hit.getLocation()) > MAX_HIT_DISTANCE_SQR) {
            return false;
        }

        BlockState state = player.level().getBlockState(hit.getBlockPos());
        if (state.isAir() || !state.isFaceSturdy(player.level(), hit.getBlockPos(), face)) {
            return false;
        }

        // El indicador y el clic comparten exactamente la misma regla de 1,5
        // bloques. Antes el icono dependía solo del alcance vanilla de interacción.
        Vec3 target = calculateTargetPosition(player, hit);
        if (player.position().distanceToSqr(target) > MAX_ANCHOR_MOVE_SQR) {
            return false;
        }

        Vec3 displacement = target.subtract(player.position());
        return player.level().noCollision(player, player.getBoundingBox().move(displacement));
    }

    /**
     * Decide de forma excluyente entre impulso y enganche.
     *
     * La velocidad positiva por sí sola nunca autoriza el impulso: debe existir
     * un LivingJumpEvent reciente y aún no consumido. Esto evita que correcciones
     * de red, escalones, teletransportes o estados desincronizados se interpreten
     * como un salto y lancen al jugador cuando esperaba quedar sujeto.
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
                && realJumpAuthorized;

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

        // Esta ruta nunca crea un estado de anclaje. El jugador conserva su
        // inercia horizontal y recibe únicamente el impulso vertical calculado.
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
        Vec3 target = calculateTargetPosition(player, hit);
        ServerLevel level = player.serverLevel();
        BlockState anchorState = level.getBlockState(hit.getBlockPos());
        ItemStack stack = player.getItemInHand(hand);
        UUID toolId = ToolIdentity.ensure(stack);

        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND
                : EquipmentSlot.OFFHAND;

        stack.hurtAndBreak(
                DURABILITY_COST,
                level,
                player,
                brokenItem -> player.onEquippedItemBroken(brokenItem, slot)
        );

        // Si la herramienta se rompe al intentar crear el nuevo punto, no se
        // reemplaza un anclaje anterior ni se genera movimiento artificial.
        if (stack.isEmpty()) {
            return false;
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
            clearCracks(level, previous.crackId(), previous.anchorBlock());
            // El pico anterior ya inició su cooldown al engancharse. Cambiar de
            // herramienta no reinicia ni prolonga ese temporizador.
        }

        ServerClimbState next = new ServerClimbState(
                hit.getBlockPos().immutable(),
                hit.getDirection(),
                target,
                hand,
                toolId,
                crackId,
                restoreNoGravity,
                restoreFlying,
                player.level().getGameTime()
        );

        SERVER_STATES.put(player.getUUID(), next);
        LAST_REAL_JUMP.remove(player.getUUID());
        CONSUMED_JUMP.remove(player.getUUID());

        // Transición atómica y autoritativa. Para ServerPlayer no basta setPos:
        // connection.teleport registra una posición pendiente de confirmación y
        // evita que el siguiente paquete de movimiento del cliente restaure la
        // posición anterior, que era el origen histórico del tirón/impulso.
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

        // El cooldown comienza inmediatamente al confirmar el enganche y su
        // overlay baja en tiempo real aunque el pico continúe clavado. Soltarlo
        // no inicia ni reinicia el temporizador.
        startCooldown(player, stack);
        syncAttached(player, next, true);
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

        if (!isServerStateValid(player, state)) {
            boolean failedImmediately = player.level().getGameTime() - state.attachedAtGameTime()
                    <= FAILED_ATTACH_GRACE_TICKS;
            detachServerInternal(player, false, failedImmediately);
            return;
        }

        // Si el jugador intenta reactivar el vuelo creativo mientras está
        // sujeto, el anclaje conserva el control hasta que se desenganche.
        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        if (player.tickCount % CRACK_REFRESH_INTERVAL == 0) {
            showCracks(player.serverLevel(), state);
        }
        if (player.tickCount % SERVER_SYNC_INTERVAL == 0) {
            syncAttached(player, state, false);
        }

        holdPlayer(player, state.targetPosition());
    }

    private static void tickClient(Player player) {
        ClientClimbState state = CLIENT_STATES.get(player.getUUID());
        if (state == null) {
            return;
        }

        long elapsed = player.level().getGameTime() - state.lastSyncGameTime();
        if (elapsed > CLIENT_SYNC_TIMEOUT_TICKS) {
            // El estado cliente es solamente visual. Nunca corrige posición,
            // velocidad, gravedad ni vuelo; esas propiedades pertenecen al servidor.
            CLIENT_STATES.remove(player.getUUID());
        }
    }

    private static boolean isServerStateValid(ServerPlayer player, ServerClimbState state) {
        if (!player.isAlive() || player.isSpectator() || player.isFallFlying()) {
            return false;
        }

        ItemStack held = player.getItemInHand(state.activeHand());
        if (!isPickaxe(held) || !ToolIdentity.matchesOrRepair(held, state.toolId())) {
            return false;
        }

        if (player.position().distanceToSqr(state.targetPosition()) > MAX_DRIFT_DISTANCE_SQR) {
            return false;
        }

        BlockState blockState = player.level().getBlockState(state.anchorBlock());
        return !blockState.isAir()
                && blockState.isFaceSturdy(player.level(), state.anchorBlock(), state.anchorFace());
    }

    private static void holdPlayer(ServerPlayer player, Vec3 target) {
        player.setNoGravity(true);
        player.fallDistance = 0.0F;
        player.setOnGround(false);
        player.setDeltaMovement(Vec3.ZERO);

        // Solo se corrige si realmente hubo deriva. La corrección usa el canal de
        // teletransporte del jugador, no setPos ni una velocidad hacia el ancla.
        if (player.position().distanceToSqr(target) > 2.5E-3D) {
            player.connection.teleport(
                    target.x,
                    target.y,
                    target.z,
                    player.getYRot(),
                    player.getXRot()
            );
            player.setDeltaMovement(Vec3.ZERO);
        }
    }

    public static void detachServer(ServerPlayer player, boolean jump) {
        detachServerInternal(player, jump, false);
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

        clearCracks(player.serverLevel(), state.crackId(), state.anchorBlock());

        ItemStack activeTool = findToolById(player, state.toolId());
        int remainingCooldownTicks = 0;
        if (refundCooldown) {
            if (!activeTool.isEmpty()) {
                ToolIdentity.clearCooldown(activeTool);
                player.getInventory().setChanged();
            }
        } else if (!activeTool.isEmpty()) {
            // El temporizador ya comenzó al crear el anclaje. Al soltar el pico
            // no se reinicia: solo se sincroniza el tiempo que realmente queda.
            remainingCooldownTicks = cooldownTicksRemaining(
                    activeTool,
                    player.level().getGameTime()
            );
        }

        Vec3 detachVelocity = jump
                ? calculateJumpVelocity(player, activeTool)
                : Vec3.ZERO;

        restoreAbilities(player, state.restoreNoGravity(), state.restoreFlying());
        player.fallDistance = 0.0F;
        player.setDeltaMovement(detachVelocity);

        if (jump) {
            // El wall jump es también un ascenso explícitamente solicitado por el
            // jugador y puede encadenar un impulso con el otro pico.
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
    }

    public static void detachClient(Player player, boolean jump) {
        ClientClimbState state = CLIENT_STATES.remove(player.getUUID());
        if (state == null) {
            return;
        }

        // Solo se predice localmente el salto solicitado por el usuario. La
        // gravedad, el vuelo y los desenganches pasivos los sincroniza el servidor.
        if (jump) {
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

        clearCracks(player.serverLevel(), state.crackId(), state.anchorBlock());
        restoreAbilities(player, state.restoreNoGravity(), state.restoreFlying());
        player.setDeltaMovement(Vec3.ZERO);
    }

    public static void applyClientSync(Player player, AnchorSyncPayload payload) {
        ClientClimbState previousState = CLIENT_STATES.remove(player.getUUID());

        if (!payload.attached()) {
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
                    // Si el cooldown terminó mientras el pico seguía enganchado,
                    // se elimina cualquier desfase visual residual del cliente.
                    ToolIdentity.clearCooldown(releasedTool);
                }
            }

            // El payload propio solo sincroniza estado visual. La posición,
            // gravedad, vuelo y velocidad llegan por los paquetes vanilla del servidor.
            if (!payload.jumpDetach()) {
                player.setDeltaMovement(Vec3.ZERO);
            }
            return;
        }

        InteractionHand hand = payload.handOrdinal() == InteractionHand.OFF_HAND.ordinal()
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        Vec3 target = new Vec3(payload.targetX(), payload.targetY(), payload.targetZ());

        ItemStack localTool = player.getItemInHand(hand);
        if (isPickaxe(localTool)) {
            // El paquete de anclaje es la fuente de verdad del UUID. Esto evita
            // que una actualización tardía de inventario haga marcar otro pico.
            if (!ToolIdentity.matches(localTool, payload.toolId())) {
                ToolIdentity.assign(localTool, payload.toolId());
            }
            if (payload.newAnchor()) {
                ToolIdentity.startCooldown(
                        localTool,
                        player.level().getGameTime() + ANCHOR_COOLDOWN_TICKS
                );
            }
        }

        long now = player.level().getGameTime();
        long poseStartedGameTime = payload.newAnchor()
                || previousState == null
                || !previousState.toolId().equals(payload.toolId())
                ? now
                : previousState.poseStartedGameTime();

        ClientClimbState next = new ClientClimbState(
                target,
                hand,
                payload.toolId(),
                payload.restoreNoGravity(),
                payload.restoreFlying(),
                now,
                poseStartedGameTime
        );

        CLIENT_STATES.put(player.getUUID(), next);
        // No se toca la física local. connection.teleport y los datos de entidad
        // vanilla son la única fuente de verdad para el movimiento del jugador.
    }

    public static void applyClientBoost(Player player, BoostSyncPayload payload) {
        // Un paquete de impulso atrasado jamás puede desmontar un anclaje ya
        // confirmado. Se prioriza siempre el estado seguro de estar sujeto.
        if (CLIENT_STATES.containsKey(player.getUUID())) {
            return;
        }

        InteractionHand hand = payload.handOrdinal() == InteractionHand.OFF_HAND.ordinal()
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack localTool = player.getItemInHand(hand);
        if (isPickaxe(localTool) && payload.cooldownTicks() > 0) {
            ToolIdentity.startCooldown(
                    localTool,
                    player.level().getGameTime() + payload.cooldownTicks()
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

    public static void clearAllClientStates(Player localPlayer) {
        if (localPlayer != null) {
            CLIENT_STATES.remove(localPlayer.getUUID());
        } else {
            CLIENT_STATES.clear();
        }
    }

    private static Vec3 calculateTargetPosition(Player player, BlockHitResult hit) {
        BlockPos block = hit.getBlockPos();
        Direction face = hit.getDirection();
        Vec3 location = hit.getLocation();
        double wallOffset = player.getBbWidth() * 0.5D + 0.08D;

        // Se conserva la coordenada exacta pulsada sobre la cara para permitir
        // desplazamiento horizontal y diagonal, no solo vertical.
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
            default -> {
                // Las caras superior e inferior ya fueron rechazadas.
            }
        }

        double targetY = location.y - player.getBbHeight() * 0.62D;
        return new Vec3(targetX, targetY, targetZ);
    }

    private static Vec3 calculateJumpVelocity(Player player, ItemStack activeTool) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);

        if (horizontal.lengthSqr() < 1.0E-5D) {
            horizontal = Vec3.directionFromRotation(0.0F, player.getYRot());
            horizontal = new Vec3(horizontal.x, 0.0D, horizontal.z);
        }

        horizontal = horizontal.normalize().scale(0.65D);
        double baseVertical = Mth.clamp(0.28D + look.y * 0.32D, 0.12D, 0.52D);
        int enchantmentLevel = ModEnchantments.getPickClimberLevel(player.level(), activeTool);
        double extraHeight = enchantmentLevel * WALL_JUMP_HEIGHT_PER_ENCHANTMENT_LEVEL;
        double vertical = extraHeight <= 0.0D
                ? baseVertical
                : velocityForAdditionalRise(baseVertical, extraHeight);
        return new Vec3(horizontal.x, vertical, horizontal.z);
    }

    /**
     * Calcula una velocidad inicial que añade una cantidad aproximada de altura
     * a la trayectoria vertical restante de Minecraft. Evita sumar bloques como
     * velocidad bruta, lo que produciría lanzamientos descontrolados.
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
            if (isPickaxe(stack) && ToolIdentity.matches(stack, toolId)) {
                return stack;
            }
        }

        ItemStack offhand = player.getOffhandItem();
        if (isPickaxe(offhand) && ToolIdentity.matches(offhand, toolId)) {
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
        ToolIdentity.startCooldown(
                stack,
                player.level().getGameTime() + ANCHOR_COOLDOWN_TICKS
        );
        player.getInventory().setChanged();
    }

    private static int createCrackId(ServerPlayer player) {
        // ID sintético: evita que Minecraft excluya al propio jugador del
        // paquete de grietas por confundirlo con el breakerId vanilla.
        return -1_000_000 - (player.getUUID().hashCode() & 0x3FFFFFFF);
    }

    private static void showCracks(ServerLevel level, ServerClimbState state) {
        level.destroyBlockProgress(state.crackId(), state.anchorBlock(), CRACK_STAGE);
    }

    private static void clearCracks(ServerLevel level, int crackId, BlockPos blockPos) {
        level.destroyBlockProgress(crackId, blockPos, -1);
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
}
