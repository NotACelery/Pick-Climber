package dev.maicra.pickclimber.client;

import dev.maicra.pickclimber.climb.ClimbPresentationGate;

final class ClientPickClimberBootstrap {
    private static boolean installed;

    private ClientPickClimberBootstrap() {
    }

    static void ensureInstalled() {
        if (installed) {
            return;
        }
        PickClimberClientOptionsStore.current();
        ClimbPresentationGate.installPolicy(ClientOptionsPresentationPolicy.INSTANCE);
        installed = true;
    }
}
