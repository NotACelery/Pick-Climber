package dev.maicra.pickclimber.climb;

/** Server-authoritative physical state of a wall anchor. */
public enum AnchorMotion {
    FIXED,
    BRAKING,
    UNSTABLE_SLIDING
}
