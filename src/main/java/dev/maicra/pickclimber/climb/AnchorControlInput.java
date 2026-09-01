package dev.maicra.pickclimber.climb;

record AnchorControlInput(float forward, float strafe) {
    static final AnchorControlInput ZERO = new AnchorControlInput(0.0F, 0.0F);
}
