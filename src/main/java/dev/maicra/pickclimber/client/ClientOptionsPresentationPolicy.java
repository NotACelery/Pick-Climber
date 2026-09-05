package dev.maicra.pickclimber.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import dev.maicra.pickclimber.climb.AnchorIndicatorStatus;
import dev.maicra.pickclimber.climb.ClimbPresentationPolicy;

final class ClientOptionsPresentationPolicy implements ClimbPresentationPolicy {
    static final ClientOptionsPresentationPolicy INSTANCE = new ClientOptionsPresentationPolicy();

    private ClientOptionsPresentationPolicy() {
    }

    @Override
    public AnchorIndicatorStatus filterIndicator(Player player, AnchorIndicatorStatus status) {
        PickClimberClientOptions options = PickClimberClientOptionsStore.current();
        if (options.indicatorMode() == IndicatorMode.OFF) {
            return AnchorIndicatorStatus.NONE;
        }
        if (options.indicatorMode() == IndicatorMode.CONTEXTUAL
                && status == AnchorIndicatorStatus.UNCLIMBABLE
                && !options.showUnclimbableIndicator()) {
            return AnchorIndicatorStatus.NONE;
        }
        return status;
    }

    @Override
    public boolean showFailureText(Player player) {
        return !(Minecraft.getInstance().screen instanceof PickClimberOptionsScreen)
                && PickClimberClientOptionsStore.current().showFailureText();
    }
}
