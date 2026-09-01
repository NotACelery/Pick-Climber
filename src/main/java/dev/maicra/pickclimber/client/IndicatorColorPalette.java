package dev.maicra.pickclimber.client;

final class IndicatorColorPalette {
    private IndicatorColorPalette() {
    }

    static int resolve(int color, IndicatorColorIntensity intensity) {
        return switch (intensity) {
            case MUTED -> rgb(
                    muted(channel(color, 16)),
                    muted(channel(color, 8)),
                    muted(channel(color, 0))
            );
            case NORMAL -> color & 0x00FFFFFF;
            case NEON -> neonColor(color);
        };
    }

    private static int neonColor(int color) {
        int red = channel(color, 16);
        int green = channel(color, 8);
        int blue = channel(color, 0);
        int max = Math.max(red, Math.max(green, blue));
        int min = Math.min(red, Math.min(green, blue));
        if (max - min < 24) {
            int bright = clamp(max + 42);
            return rgb(bright, bright, bright);
        }
        return rgb(neon(red, max), neon(green, max), neon(blue, max));
    }

    private static int muted(int value) {
        return clamp((value * 3 + 128 * 2) / 5);
    }

    private static int neon(int value, int max) {
        return value >= max - 16 ? 255 : clamp(value - 24);
    }

    private static int channel(int color, int shift) {
        return (color >> shift) & 0xFF;
    }

    private static int rgb(int red, int green, int blue) {
        return (red << 16) | (green << 8) | blue;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
