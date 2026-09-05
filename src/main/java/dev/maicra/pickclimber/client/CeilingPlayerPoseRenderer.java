package dev.maicra.pickclimber.client;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.climb.ClimbManager;

@EventBusSubscriber(modid = PickClimber.MOD_ID, value = Dist.CLIENT)
public final class CeilingPlayerPoseRenderer {
    private static final Map<PlayerRenderer, ArmPoseSnapshot> PREVIOUS_POSES = new IdentityHashMap<>();

    private CeilingPlayerPoseRenderer() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void beforePlayerRender(RenderPlayerEvent.Pre event) {
        if (!ClimbManager.isCeilingAnchor(event.getEntity())) {
            return;
        }

        InteractionHand activeHand = ClimbManager.activeHand(event.getEntity());
        if (activeHand == null) {
            return;
        }

        PlayerRenderer renderer = event.getRenderer();
        PlayerModel<?> model = renderer.getModel();
        PREVIOUS_POSES.put(
                renderer,
                new ArmPoseSnapshot(model.leftArmPose, model.rightArmPose)
        );

        HumanoidArm activeArm = activeHand == InteractionHand.MAIN_HAND
                ? event.getEntity().getMainArm()
                : event.getEntity().getMainArm().getOpposite();
        if (activeArm == HumanoidArm.RIGHT) {
            model.rightArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
        } else {
            model.leftArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
        }
    }

    @SubscribeEvent
    public static void afterPlayerRender(RenderPlayerEvent.Post event) {
        ArmPoseSnapshot previous = PREVIOUS_POSES.remove(event.getRenderer());
        if (previous == null) {
            return;
        }

        PlayerModel<?> model = event.getRenderer().getModel();
        model.leftArmPose = previous.left();
        model.rightArmPose = previous.right();
    }

    private record ArmPoseSnapshot(
            HumanoidModel.ArmPose left,
            HumanoidModel.ArmPose right
    ) {
    }
}
