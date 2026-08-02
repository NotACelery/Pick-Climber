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
        // Los tags aún pueden no estar enlazados cuando se dispara este evento.
        // Se registra el decorador en todos los ítems y él mismo filtra
        // #minecraft:pickaxes al renderizar. Esto conserva compatibilidad con
        // picos de otros mods sin depender de una lista fija.
        for (Item item : BuiltInRegistries.ITEM) {
            event.register(item, PickClimberItemDecorator.INSTANCE);
        }
    }
}
