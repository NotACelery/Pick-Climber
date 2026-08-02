package dev.maicra.pickclimber.climb;

import com.mojang.logging.LogUtils;
import dev.maicra.pickclimber.network.AnchorSyncPayload;
import dev.maicra.pickclimber.network.BoostSyncPayload;
import dev.maicra.pickclimber.network.SlideInputPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
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
    private static final int BRAKING_DURABILITY_PER_BLOCK = 10;
    public static final int CRACK_STAGE = 5;
    public static final int ANCHOR_COOLDOWN_TICKS = 20;
    public static final int UNSTABLE_ANCHOR_COOLDOWN_TICKS = 40;

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
    private static final double BRAKING_START_SPEED = -0.40D;
    private static final float BRAKING_MIN_FALL_DISTANCE = 5.0F;
    private static final double BRAKING_STOP_SPEED = -0.08D;
    private static final double BRAKING_DRAG = 0.75D;
    private static final double BRAKING_RECOVERY = 0.035D;
    private static final double UNSTABLE_SLIDE_SPEED = -0.128D;
    /** Evita atravesar o saltarse bloques al absorber una caída extrema. */
    private static final double MAX_BRAKING_MOVE_PER_TICK = 0.60D;
    private static final double CONTACT_BLOCK_EPSILON = 1.0E-3D;

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
        return ToolIdentity.cooldownFraction(stack, player.level().getGameTime());
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
        if (face == Direction.UP) {
            return false;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!isPickaxe(stack)) {
            return false;
        }

        boolean ceilingAttempt = face == Direction.DOWN;
        if (ceilingAttempt && !ModEnchantments.hasStrongGrip(player.level(), stack)) {
            return false;
        }

        boolean duplicatedActiveIdentity = hasDuplicatedActiveIdentity(player, hand, stack);
        if (duplicatedActiveIdentity && !player.level().isClientSide()) {
            // La identidad pertenece al ItemStack, no a la mano. Dos stacks
            // simultáneos con el mismo UUID son una copia y el candidato debe
            // recuperar identidad/cooldown individuales antes de validarse.
            ToolIdentity.assign(stack, UUID.randomUUID());
            ToolIdentity.clearCooldown(stack);
        }

        // Un pico enganchado está ocupado aunque su cooldown interno pueda
        // haber terminado mientras permanecía clavado en la pared.
        // Solo la mano que sostiene el ancla está ocupada. Si otra picota
        // heredó por copia el mismo UUID, sigue siendo una herramienta distinta
        // y debe poder crear el siguiente punto.
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

        // El indicador y el clic comparten exactamente la misma regla de 1,5
        // bloques. Antes el icono dependía solo del alcance vanilla de interacción.
        Vec3 target = calculateTargetPosition(player, hit);
        Vec3 movementOrigin = currentAttachmentTarget(player);
        if (movementOrigin.distanceToSqr(target) > MAX_ANCHOR_MOVE_SQR) {
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
        if (previous != null
                && previous.activeHand() != hand
                && previous.toolId().equals(toolId)) {
            // Dos ItemStack distintos nunca comparten identidad de ancla. Esto
            // puede ocurrir al duplicar una picota con NBT; sin separarlos, el
            // segundo pico parece el activo y recibe el desgaste equivocado.
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

        // Si la herramienta se rompe al intentar crear el nuevo punto, no se
        // reemplaza un anclaje anterior ni se genera movimiento artificial.
        if (stack.isEmpty()) {
            return false;
        }

        UUID brakingSupportToolId = null;
        if (initialMotion == AnchorMotion.BRAKING) {
            InteractionHand supportHand = hand == InteractionHand.MAIN_HAND
                    ? InteractionHand.OFF_HAND
                    : InteractionHand.MAIN_HAND;
            ItemStack supportStack = player.getItemInHand(supportHand);
            if (isPickaxe(supportStack)) {
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
            // El pico anterior ya inició su cooldown al engancharse. Cambiar de
            // herramienta no reinicia ni prolonga ese temporizador.
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
                // La cara golpeada vive justo en un borde entero. Se desplaza
                // apenas hacia el interior del bloque para que BlockPos no
                // resuelva el bloque de aire del lado del jugador.
                hit.getLocation().subtract(target).subtract(
                        hit.getDirection().getStepX() * CONTACT_BLOCK_EPSILON,
                        hit.getDirection().getStepY() * CONTACT_BLOCK_EPSILON,
                        hit.getDirection().getStepZ() * CONTACT_BLOCK_EPSILON
                ),
                Vec3.ZERO,
                reinforcedLatch,
                brakingSupportToolId,
                0.0D,
                0
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
        startCooldown(player, stack, next.cooldownTicks());
        if (initialMotion == AnchorMotion.BRAKING) {
            startEquippedEffortCooldown(player, hand);
        }
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

        // Si el jugador intenta reactivar el vuelo creativo mientras está
        // sujeto, el anclaje conserva el control hasta que se desenganche.
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
        if (state.motion() != AnchorMotion.FIXED || player.tickCount % SERVER_SYNC_INTERVAL == 0) {
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
            // Si se perdió el paquete de desenganche, también se limpia localmente
            // el overlay de grietas para que no quede huérfano varios segundos.
            CLIENT_STATES.remove(player.getUUID());
            clearClientCracks(player, state);
        }
    }

    /**
     * Reconcilia el intercambio vanilla de manos (`F`) por UUID del ItemStack.
     * No intercepta la tecla ni vuelve a crear el anclaje: cuando el mismo pico
     * aparece en la mano contraria, solo se traslada el estado activo y se
     * sincroniza la pose. Durabilidad, cooldown, sonido y grietas no cambian.
     */
    private static ServerClimbState reconcileActiveHand(
            ServerPlayer player,
            ServerClimbState state
    ) {
        ItemStack current = player.getItemInHand(state.activeHand());
        if (isPickaxe(current) && ToolIdentity.matches(current, state.toolId())) {
            return state;
        }

        InteractionHand otherHand = state.activeHand() == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack other = player.getItemInHand(otherHand);

        if (!isPickaxe(other) || !ToolIdentity.matches(other, state.toolId())) {
            // No se repara un UUID ausente sobre otro pico: cambiar de slot debe
            // desenganchar, no convertir accidentalmente una herramienta distinta
            // en el ancla activa.
            return state;
        }

        ServerClimbState transferred = state.withActiveHand(otherHand);
        SERVER_STATES.put(player.getUUID(), transferred);
        syncAttached(player, transferred, false);
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
        if (!isPickaxe(held) || !ToolIdentity.matches(held, state.toolId())) {
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

        // Las superficies inestables incluyen nieve en capa y nieve en polvo,
        // cuya geometría vanilla no siempre declara una cara lateral sturdy.
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

    /** Actualiza exclusivamente en el servidor el frenado o descenso del ancla. */
    private static ServerClimbState advanceAnchorMotion(ServerPlayer player, ServerClimbState state) {
        if (state.motion() == AnchorMotion.FIXED) {
            return state;
        }
        if (player.level().getGameTime() <= state.attachedAtGameTime()) {
            return state;
        }
        if (state.motion() == AnchorMotion.BRAKING
                && state.brakingSupportToolId() != null
                && !hasBrakingSupport(player, state)) {
            // El segundo pico dejó de estar equipado o se rompió: el ancla
            // principal continúa de forma segura, pero ya no recibe el doble
            // frenado ni desgaste sobre una herramienta ajena.
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

        // La velocidad interna puede ser muy alta tras una caída larga, pero el
        // recorrido físico se limita para inspeccionar cada bloque de la pared.
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
            // Una colisión durante el deslizamiento nunca atraviesa bloques ni
            // fuerza una caída artificial. Se estabiliza en la última posición segura.
            return state.withMotion(state.targetPosition(), AnchorMotion.FIXED, 0.0D);
        }

        BlockPos nextAnchorBlock = anchorBlockAt(player, state, nextTarget);
        BlockState nextBlockState = player.level().getBlockState(nextAnchorBlock);
        if (!hasValidAnchorFace(player, nextBlockState, nextAnchorBlock, state.anchorFace())) {
            // Al acabar la pared inestable no queda un soporte válido: el
            // servidor termina el anclaje en vez de seguir usando el bloque inicial.
            return null;
        }

        AnchorSurface nextSurface = AnchorSurfaceClassifier.classify(nextBlockState);
        // Una pared firme no cancela su propio frenado. Solo una transición
        // desde material inestable a uno firme debe terminar el descenso.
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

    /** Dos picos reducen a la mitad aproximada el tiempo y recorrido de frenado. */
    private static boolean hasBrakingSupport(ServerPlayer player, ServerClimbState state) {
        UUID supportToolId = state.brakingSupportToolId();
        return supportToolId != null && findEquippedToolById(player, supportToolId) != ItemStack.EMPTY;
    }

    /** Cobra el desgaste adicional solo por bloques realmente recorridos al frenar. */
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

    /** Usa el punto anclado como posición real mientras el servidor lo mantiene fijo. */
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

        // La componente normal a la pared queda anulada: se puede mirar y
        // girar libremente, pero el ancla no se separa ni atraviesa la pared.
        return switch (state.anchorFace().getAxis()) {
            case X -> new Vec3(0.0D, 0.0D, input.z);
            case Z -> new Vec3(input.x, 0.0D, 0.0D);
            case Y -> Vec3.ZERO;
        };
    }

    private static BlockPos anchorBlockAt(Player player, ServerClimbState state, Vec3 target) {
        return BlockPos.containing(target.add(state.contactOffset()));
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
                    player.getXRot(),
                    RelativeMovement.ROTATION
            );
            player.setDeltaMovement(Vec3.ZERO);
        }
    }

    public static void detachServer(ServerPlayer player, boolean jump) {
        detachServerInternal(player, jump, false);
    }

    /** Recibe intención cliente; el servidor decide velocidad, plano y colisiones. */
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

        clearClientCracks(player, state);

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

        clearAnchorVisuals(player, state);
        restoreAbilities(player, state.restoreNoGravity(), state.restoreFlying());
        player.setDeltaMovement(Vec3.ZERO);
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
        if (isPickaxe(localTool) && payload.newAnchor()) {
            // Solo un anclaje realmente nuevo puede asignar identidad. Durante
            // una transferencia con F, el paquete vanilla de inventario puede
            // llegar un instante después; asignar aquí marcaría el pico equivocado.
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

    /** Limpia el estado visual y el overlay sintético antes de abandonar el nivel. */
    public static void clearAllClientStates(Player localPlayer) {
        if (localPlayer != null) {
            ClientClimbState state = CLIENT_STATES.remove(localPlayer.getUUID());
            if (state != null) {
                clearClientCracks(localPlayer, state);
            }
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

    /** Una caída fuerte compromete ambas manos equipadas y evita encadenar otro pico. */
    private static void startEquippedEffortCooldown(ServerPlayer player, InteractionHand activeHand) {
        long until = player.level().getGameTime() + ANCHOR_COOLDOWN_TICKS;
        for (InteractionHand hand : InteractionHand.values()) {
            if (hand == activeHand) {
                continue;
            }
            ItemStack equipped = player.getItemInHand(hand);
            if (isPickaxe(equipped)) {
                ToolIdentity.startCooldown(equipped, until, ANCHOR_COOLDOWN_TICKS);
            }
        }
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

    /**
     * Limpia siempre en la dimensión donde nació el anclaje. Esto importa al
     * cambiar de dimensión: para cuando llega PlayerChangedDimensionEvent, el
     * ServerPlayer ya pertenece al nivel nuevo.
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
     * El cliente conserva los overlays de destrucción en una tabla local. Al
     * desconectarse, el paquete servidor puede llegar demasiado tarde; por eso
     * se elimina también directamente antes de destruir el ClientLevel.
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
