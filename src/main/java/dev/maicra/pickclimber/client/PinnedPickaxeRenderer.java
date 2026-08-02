package dev.maicra.pickclimber.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.climb.ClimbManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/**
 * Render dedicado de primera persona para el pico que mantiene el anclaje.
 *
 * En lugar de transformar la animación vanilla y dejar que siga avanzando,
 * cancela únicamente esa mano y vuelve a dibujar el ItemStack una sola vez en
 * una pose estable equivalente al tramo adelantado del golpe vanilla.
 */
@EventBusSubscriber(modid = PickClimber.MOD_ID, value = Dist.CLIENT)
public final class PinnedPickaxeRenderer {
    private PinnedPickaxeRenderer() {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ItemStack stack = event.getItemStack();

        if (player == null
                || stack.isEmpty()
                || !ClimbManager.isPickaxe(stack)
                || ClimbManager.activeHand(player) != event.getHand()) {
            return;
        }

        float pinnedProgress = ClimbManager.pinnedPoseProgress(
                player,
                stack,
                event.getPartialTick()
        );
        if (pinnedProgress < 0.0F) {
            return;
        }

        event.setCanceled(true);
        renderPinnedTool(minecraft, player, event, stack, pinnedProgress);
    }

    private static void renderPinnedTool(
            Minecraft minecraft,
            LocalPlayer player,
            RenderHandEvent event,
            ItemStack stack,
            float swingProgress
    ) {
        InteractionHand hand = event.getHand();
        boolean mainHand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm arm = mainHand
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        boolean rightArm = arm == HumanoidArm.RIGHT;
        int side = rightArm ? 1 : -1;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        // Réplica del camino vanilla para herramientas normales, pero con un
        // swingProgress controlado por el estado del anclaje y equipProgress=0.
        float rootSwing = Mth.sqrt(swingProgress);
        float translateX = -0.4F * Mth.sin(rootSwing * (float) Math.PI);
        float translateY = 0.2F * Mth.sin(rootSwing * (float) (Math.PI * 2.0D));
        float translateZ = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
        poseStack.translate(side * translateX, translateY, translateZ);

        // Transformación base de la mano completamente equipada.
        poseStack.translate(side * 0.56F, -0.52F, -0.72F);

        float attackCurve = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float forwardCurve = Mth.sin(rootSwing * (float) Math.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * (45.0F - attackCurve * 20.0F)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * forwardCurve * -20.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(forwardCurve * -80.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * -45.0F));

        minecraft.gameRenderer.itemInHandRenderer.renderItem(
                player,
                stack,
                rightArm
                        ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                        : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                !rightArm,
                poseStack,
                event.getMultiBufferSource(),
                event.getPackedLight()
        );

        poseStack.popPose();
    }
}
