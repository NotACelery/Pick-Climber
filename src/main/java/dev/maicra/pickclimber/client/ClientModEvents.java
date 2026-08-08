package dev.maicra.pickclimber.client;

import dev.maicra.pickclimber.PickClimber;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;

@EventBusSubscriber(
        modid = PickClimber.MOD_ID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD
)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        // Tags may not be bound yet when this event fires. Register the decorator
        // for every item and let the climbing-tool tags filter while rendering.
        // This preserves compatibility with modded tools without a fixed list.
        for (Item item : BuiltInRegistries.ITEM) {
            event.register(item, PickClimberItemDecorator.INSTANCE);
        }
    }
}
