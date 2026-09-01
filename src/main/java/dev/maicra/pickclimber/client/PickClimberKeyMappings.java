package dev.maicra.pickclimber.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

final class PickClimberKeyMappings {
    static final KeyMapping OPEN_OPTIONS = new KeyMapping(
            "key.pickclimber.open_options",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.pickclimber"
    );

    private PickClimberKeyMappings() {
    }
}
