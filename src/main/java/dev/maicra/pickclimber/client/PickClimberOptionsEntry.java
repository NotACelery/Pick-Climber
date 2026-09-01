package dev.maicra.pickclimber.client;

import dev.maicra.pickclimber.PickClimber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = PickClimber.MOD_ID, value = Dist.CLIENT)
public final class PickClimberOptionsEntry {
    private PickClimberOptionsEntry() {
    }

    @SubscribeEvent
    public static void onScreenInitialized(ScreenEvent.Init.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(event.getScreen() instanceof OptionsScreen optionsScreen)
                || minecraft.level == null
                || minecraft.player == null) {
            return;
        }

        ClientPickClimberBootstrap.ensureInstalled();
        int buttonWidth = Math.min(150, Math.max(110, optionsScreen.width / 3));
        event.addListener(Button.builder(
                net.minecraft.network.chat.Component.translatable("options.pickclimber.open"),
                button -> minecraft.setScreen(new PickClimberOptionsScreen(optionsScreen))
        ).bounds(6, 6, buttonWidth, 20).build());
    }
}
