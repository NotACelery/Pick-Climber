package dev.maicra.pickclimber.climb;

/** Clasificación data-driven de la superficie que recibe un anclaje. */
public enum AnchorSurface {
    UNCLIMBABLE,
    UNSTABLE,
    STABLE,
    /** Conserva la compatibilidad actual para los bloques que ningún tag clasifica. */
    FALLBACK
}
