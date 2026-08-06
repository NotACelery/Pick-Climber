package dev.maicra.pickclimber.climb;

/** Data-driven classification of the surface receiving an anchor. */
public enum AnchorSurface {
    UNCLIMBABLE,
    UNSTABLE,
    STABLE,
    /** Preserves current compatibility for blocks not classified by any tag. */
    FALLBACK
}
