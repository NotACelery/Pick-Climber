package dev.maicra.pickclimber.rules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public enum UnlistedPolicy {
    UNCLIMBABLE("unclimbable"),
    USE_PICK_CLIMBER_DEFAULTS("use_pick_climber_defaults");

    public static final Codec<UnlistedPolicy> CODEC = Codec.STRING.comapFlatMap(
            UnlistedPolicy::decode,
            UnlistedPolicy::serializedName
    );

    private final String serializedName;

    UnlistedPolicy(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    private static DataResult<UnlistedPolicy> decode(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
        for (UnlistedPolicy policy : values()) {
            if (policy.serializedName.equals(normalized)) {
                return DataResult.success(policy);
            }
        }
        return DataResult.error(() -> "Unknown unlisted policy: " + value);
    }
}
