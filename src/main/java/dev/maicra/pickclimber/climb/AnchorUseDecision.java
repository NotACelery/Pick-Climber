package dev.maicra.pickclimber.climb;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

public record AnchorUseDecision(
        boolean consume,
        InteractionHand hand,
        Component feedback
) {
    public static AnchorUseDecision pass() {
        return new AnchorUseDecision(false, null, null);
    }

    static AnchorUseDecision consume(InteractionHand hand) {
        return new AnchorUseDecision(true, hand, null);
    }

    static AnchorUseDecision feedback(Component message) {
        return new AnchorUseDecision(false, null, message);
    }
}
