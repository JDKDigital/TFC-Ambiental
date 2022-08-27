package com.lumintorious.tfcambiental;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

public class TFCAmbientalConfig {
    public static CommonImpl COMMON = register(ModConfig.Type.COMMON, CommonImpl::new);

    public static void init() {}

    private static <C> C register(ModConfig.Type type, Function<ForgeConfigSpec.Builder, C> factory)
    {
        Pair<C, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(factory);
        ModLoadingContext.get().registerConfig(type, specPair.getRight());
        return specPair.getLeft();
    }

    public static class CommonImpl {
        public final ForgeConfigSpec.DoubleValue averageTemperature;
        public final ForgeConfigSpec.DoubleValue hotThreshold;
        public final ForgeConfigSpec.DoubleValue coolThreshold;
        public final ForgeConfigSpec.DoubleValue burnThreshold;
        public final ForgeConfigSpec.DoubleValue freezeThreshold;

        public final ForgeConfigSpec.DoubleValue temperatureChangeSpeed;
        public final ForgeConfigSpec.DoubleValue goodTemperatureChangeSpeed;
        public final ForgeConfigSpec.DoubleValue badTemperatureChangeSpeed;

        CommonImpl(ForgeConfigSpec.Builder builder) {
            averageTemperature = builder
                .comment("The average point for temperature, the not too warm and not too cool point")
                .defineInRange("averageTemperature", 15F, 0F, 30F);

            hotThreshold = builder
                    .comment("The point where warmth starts to affect the screen, but only mildly")
                    .defineInRange("hotThreshold", 25F, 5F, 35F);

            coolThreshold = builder
                    .comment("The point where cold starts to affect the screen, but only mildly")
                    .defineInRange("coolThreshold",  5F, -15F, 25F);

            burnThreshold = builder
                    .comment("The point where warmth starts to hurt the player")
                    .defineInRange("burnThreshold", 30F, 15F, 45F);

            freezeThreshold = builder
                    .comment("The point where cold starts to hurt the player")
                    .defineInRange("freezeThreshold",  0F, -15F, 15F);


            temperatureChangeSpeed = builder
                    .comment("How quickly player temperature changes towards the target environment temperature")
                    .defineInRange("temperatureChangeSpeed",  1F, 0F, 50F);

            goodTemperatureChangeSpeed = builder
                    .comment("How quickly player temperature changes towards the target environment temperature when it's beneficial to do so")
                    .defineInRange("goodTemperatureChangeSpeed",  4F, 0F, 50F);

            badTemperatureChangeSpeed = builder
                    .comment("How quickly player temperature changes towards the target environment temperature when it's not beneficial")
                    .defineInRange("badTemperatureChangeSpeed",  1F, 0F, 50F);

        }
    }
}