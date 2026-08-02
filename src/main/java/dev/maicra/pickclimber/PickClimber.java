package dev.maicra.pickclimber;

import dev.maicra.pickclimber.network.ModNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(PickClimber.MOD_ID)
public final class PickClimber {
    public static final String MOD_ID = "pickclimber";

    public PickClimber(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ModNetworking::registerPayloads);
    }
}
