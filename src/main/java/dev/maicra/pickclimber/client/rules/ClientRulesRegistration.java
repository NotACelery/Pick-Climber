package dev.maicra.pickclimber.client.rules;

import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.ModMenus;
import dev.maicra.pickclimber.PickClimber;
import dev.maicra.pickclimber.rules.ClimbingRulesClientUi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = PickClimber.MOD_ID, value = Dist.CLIENT)
public final class ClientRulesRegistration {
    private ClientRulesRegistration() {
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                RuleBookItemColors::color,
                ModItems.CLIMBING_RULE_BOOK.get(),
                ModItems.TEMPORARY_RULE_BOOK.get()
        );
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.CLIMBING_RULES_TABLE.get(), ClimbingRulesTableScreen::new);
        event.register(ModMenus.CLIMBING_RULE_DISPENSER.get(), ClimbingRuleDispenserScreen::new);
        ClimbingRulesClientUi.installViewer(definition -> {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(new ClimbingRuleBookViewerScreen(minecraft.screen, definition));
        });
        ClimbingRulesClientUi.install((position, sessionToken, profile) -> {
            Minecraft minecraft = Minecraft.getInstance();
            Screen current = minecraft.screen;
            Screen parent;
            if (current instanceof ClimbingRulesEditorScreen editor) {
                parent = editor.parentScreen();
            } else if (current instanceof ClimbingRulesImportScreen importScreen) {
                parent = importScreen.parentScreen();
            } else {
                parent = current;
            }
            minecraft.setScreen(new ClimbingRulesEditorScreen(parent, position, sessionToken, profile));
        });
    }
}
