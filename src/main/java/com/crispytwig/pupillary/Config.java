package com.crispytwig.pupillary;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .translation("config.pupillary.enabled")
            .define("client.enabled", true);
    public static final ModConfigSpec.DoubleValue MIN_BRIGHTNESS = BUILDER
            .translation("config.pupillary.min_brightness")
            .defineInRange("client.minBrightness", 0.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue MAX_BRIGHTNESS = BUILDER
            .translation("config.pupillary.max_brightness")
            .defineInRange("client.maxBrightness", 1.0, 0.0, 1.0);

    static final ModConfigSpec SPEC = BUILDER.build();
}
