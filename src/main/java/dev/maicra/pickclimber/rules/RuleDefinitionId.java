package dev.maicra.pickclimber.rules;

import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;

public final class RuleDefinitionId {
    private RuleDefinitionId() {
    }

    public static String of(ClimbingRulesProfile profile) {
        StringBuilder canonical = new StringBuilder(4096);
        canonical.append("format=").append(profile.formatVersion()).append('\n');
        appendSet(canonical, "stable", profile.stableBlocks());
        appendSet(canonical, "unstable", profile.unstableBlocks());
        appendSet(canonical, "unclimbable", profile.unclimbableBlocks());
        canonical.append("unlisted=").append(profile.unlistedPolicy().serializedName()).append('\n');
        canonical.append("wear=").append(profile.pickaxeWear()).append('\n');
        canonical.append("mining=").append(profile.playerMiningEnabled()).append('\n');
        canonical.append("terminals=").append(profile.unmineableTerminals()).append('\n');
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte value : digest) {
                hex.append(String.format("%02x", value & 0xFF));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    public static boolean isValid(String value) {
        return value != null && value.matches("[0-9a-f]{64}");
    }

    private static void appendSet(StringBuilder out, String label, java.util.Set<ResourceLocation> values) {
        out.append(label).append('=');
        values.stream().sorted(Comparator.comparing(ResourceLocation::toString))
                .forEach(id -> out.append(id).append(';'));
        out.append('\n');
    }
}
