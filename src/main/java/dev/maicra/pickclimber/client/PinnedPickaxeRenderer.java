package dev.maicra.pickclimber.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.climb.ClimbManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
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
 * Dedicated first-person renderer for the pickaxe maintaining the anchor.
 *
 * Instead of transforming the vanilla animation and letting it keep advancing,
 * only that hand is cancelled and its ItemStack is redrawn once in a stable pose
 * equivalent to the forward portion of the vanilla swing.
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
        float ceilingProgress = ClimbManager.ceilingPoseProgress(
                player,
                stack,
                event.getPartialTick()
        );
        if (ceilingProgress >= 0.0F) {
            renderCeilingTool(minecraft, player, event, stack, ceilingProgress);
        } else {
            renderPinnedTool(minecraft, player, event, stack, pinnedProgress);
        }
    }

    /**
     * Strong Grip pose: arm and tool rise together above the camera. The
     * transformation mirrors through the actual arm, so it works equally for
     * main hand, off hand, and left-handed players.
     */
    private static void renderCeilingTool(
            Minecraft minecraft,
            LocalPlayer player,
            RenderHandEvent event,
            ItemStack stack,
            float poseProgress
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
        applyCeilingLift(poseStack, side, poseProgress);

        if (!player.isInvisible()) {
            renderRaisedArm(minecraft, player, event, arm);
        }

        // Base position of an equipped tool. The raised frame above rotates the
        // handle and head toward the ceiling block without stacking vanilla swing.
        poseStack.translate(side * 0.56F, -0.52F, -0.72F);

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

    private static void applyCeilingLift(PoseStack poseStack, int side, float progress) {
        poseStack.translate(
                side * Mth.lerp(progress, 0.0F, -0.10F),
                Mth.lerp(progress, 0.0F, 0.86F),
                Mth.lerp(progress, 0.0F, 0.08F)
        );
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * Mth.lerp(progress, 0.0F, -12.0F)));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(progress, 0.0F, -78.0F)));
    }

    /** Reproduces vanilla equipped-arm geometry inside the raised frame. */
    private static void renderRaisedArm(
            Minecraft minecraft,
            LocalPlayer player,
            RenderHandEvent event,
            HumanoidArm arm
    ) {
        boolean rightArm = arm == HumanoidArm.RIGHT;
        int side = rightArm ? 1 : -1;
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        poseStack.translate(side * 0.64000005F, -0.6F, -0.71999997F);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * 45.0F));
        poseStack.translate(side * -1.0F, 3.6F, 3.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * 120.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * -135.0F));
        poseStack.translate(side * 5.6F, 0.0F, 0.0F);

        PlayerRenderer renderer = (PlayerRenderer) minecraft.getEntityRenderDispatcher().getRenderer(player);
        if (rightArm) {
            renderer.renderRightHand(
                    poseStack,
                    event.getMultiBufferSource(),
                    event.getPackedLight(),
                    player
            );
        } else {
            renderer.renderLeftHand(
                    poseStack,
                    event.getMultiBufferSource(),
                    event.getPackedLight(),
                    player
            );
        }
        poseStack.popPose();
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

        // Reproduces the vanilla path for normal tools, but with swingProgress
        // controlled by anchor state and equipProgress fixed at zero.
        float rootSwing = Mth.sqrt(swingProgress);
        float translateX = -0.4F * Mth.sin(rootSwing * (float) Math.PI);
        float translateY = 0.2F * Mth.sin(rootSwing * (float) (Math.PI * 2.0D));
        float translateZ = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
        poseStack.translate(side * translateX, translateY, translateZ);

        // Base transform for a fully equipped hand.
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
