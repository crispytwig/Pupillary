package com.crispytwig.pupillary;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Pupillary.MODID, dist = Dist.CLIENT)
public class PupillaryClient {
    private static final int LOOK_STEPS = 12;
    private static final int SMOOTHING = 10;
    private static final int DECREASE_TICKS = 20;
    private static final int INCREASE_TICKS = 60;
    private static final double ALPHA_UP = 1.0 / (double) INCREASE_TICKS;
    private static final double ALPHA_DOWN = 1.0 / (double) DECREASE_TICKS;
    private static final double TOLERANCE = 1e-4;

    private static double smoothing = 1.0;
    private static double currentBrightness = 1.0;
    private static double lastBrightness = -1.0;
    public PupillaryClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.addListener(PupillaryClient::onClientTick);
    }

    static void onClientTick(ClientTickEvent.Post event) {

        final Minecraft minecraft = Minecraft.getInstance();

        if (!Config.ENABLED.get()) {
            defaultBrightness(minecraft);
            return;
        }

        if (minecraft.level == null || minecraft.player == null) {
            defaultBrightness(minecraft);
            return;
        }

        double brightnessSlider = minecraft.options.gamma().get();
        if (lastBrightness < 0.0) {
            currentBrightness = lastBrightness = brightnessSlider;
        }

        double lookBrightness = lookBrightness(minecraft.player);
        double brightnessAlpha = 1.0 / Math.max(1, SMOOTHING);
        smoothing = smoothing + (lookBrightness - smoothing) * brightnessAlpha;

        boolean playerBrightness = lastBrightness >= 0.0 && Math.abs(brightnessSlider - lastBrightness) > TOLERANCE;
        if (playerBrightness) {
            currentBrightness = brightnessSlider;
            lastBrightness = brightnessSlider;
        }
        double min = Math.min(Config.MIN_BRIGHTNESS.get(), Config.MAX_BRIGHTNESS.get());
        double max = Math.max(Config.MIN_BRIGHTNESS.get(), Config.MAX_BRIGHTNESS.get());
        double wantedBrightness = clamp(1.0 - smoothing, min, max);
        double delta = wantedBrightness - currentBrightness;
        if (delta > 0) {
            currentBrightness = currentBrightness + Math.min(delta, ALPHA_UP);
        } else if (delta < 0) {
            currentBrightness = currentBrightness + Math.max(delta, -ALPHA_DOWN);
        }
        double targetBrightness = clamp(currentBrightness, min, max);
        if (Math.abs(targetBrightness - brightnessSlider) > TOLERANCE) {
            minecraft.options.gamma().set(targetBrightness);
            lastBrightness = targetBrightness;
        }
    }

    private static void defaultBrightness(Minecraft minecraft) {
        lastBrightness = minecraft.options.gamma().get();
        currentBrightness = lastBrightness;
        smoothing = 1.0;
    }

    private static double lookBrightness(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        double maxBrightness = 0.0;
        for (int i = 1; i <= LOOK_STEPS; i++) {
            Vec3 sample = eye.add(look.scale(i));
            BlockPos pos = BlockPos.containing(sample);
            int raw = player.level().getRawBrightness(pos, player.level().getSkyDarken());
            maxBrightness = Math.max(maxBrightness, raw / 15.0);
            if (maxBrightness >= 1.0) {
                break;
            }
        }
        return clamp(maxBrightness);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
