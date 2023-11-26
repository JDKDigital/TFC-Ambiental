package com.lumintorious.tfcambiental;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

public class TFCAmbientalConfig
{
    public static CommonImpl COMMON = register(ModConfig.Type.COMMON, CommonImpl::new);
    public static ClientImpl CLIENT = register(ModConfig.Type.CLIENT, ClientImpl::new);

    public static void init() {
    }

    private static <C> C register(ModConfig.Type type, Function<ForgeConfigSpec.Builder, C> factory) {
        Pair<C, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(factory);
        ModLoadingContext.get().registerConfig(type, specPair.getRight());
        return specPair.getLeft();
    }

    public static class CommonImpl
    {
        public final ForgeConfigSpec.DoubleValue averageTemperature;
        public final ForgeConfigSpec.DoubleValue hotThreshold;
        public final ForgeConfigSpec.DoubleValue coolThreshold;
        public final ForgeConfigSpec.DoubleValue burnThreshold;
        public final ForgeConfigSpec.DoubleValue freezeThreshold;

        public final ForgeConfigSpec.DoubleValue wetnessChangeSpeed;
        public final ForgeConfigSpec.DoubleValue temperatureChangeSpeed;
        public final ForgeConfigSpec.DoubleValue goodTemperatureChangeSpeed;
        public final ForgeConfigSpec.DoubleValue badTemperatureChangeSpeed;

        public final ForgeConfigSpec.DoubleValue hotIngotTemperature;

        CommonImpl(ForgeConfigSpec.Builder builder) {
            averageTemperature = builder
                    .comment("The average point for temperature, the not too warm and not too cool point")
                    .defineInRange("averageTemperature", 15F, 0F, 30F);

            hotThreshold = builder
                    .comment("The point where warmth starts to affect the screen, but only mildly")
                    .defineInRange("hotThreshold", 25F, 5F, 35F);

            coolThreshold = builder
                    .comment("The point where cold starts to affect the screen, but only mildly")
                    .defineInRange("coolThreshold", 5F, -15F, 25F);

            burnThreshold = builder
                    .comment("The point where warmth starts to hurt the player")
                    .defineInRange("burnThreshold", 30F, 15F, 45F);

            freezeThreshold = builder
                    .comment("The point where cold starts to hurt the player")
                    .defineInRange("freezeThreshold", 0F, -15F, 15F);

            wetnessChangeSpeed = builder
                    .comment("How quickly player wetness changes towards the target environment wetness")
                    .defineInRange("wetnessChangeSpeed", 1F, 0F, 50F);

            temperatureChangeSpeed = builder
                    .comment("How quickly player temperature changes towards the target environment temperature")
                    .defineInRange("temperatureChangeSpeed", 1F, 0F, 50F);

            goodTemperatureChangeSpeed = builder
                    .comment("How quickly player temperature changes towards the target environment temperature when it's beneficial to do so")
                    .defineInRange("goodTemperatureChangeSpeed", 4F, 0F, 50F);

            badTemperatureChangeSpeed = builder
                    .comment("How quickly player temperature changes towards the target environment temperature when it's not beneficial")
                    .defineInRange("badTemperatureChangeSpeed", 1F, 0F, 50F);

            hotIngotTemperature = builder
                    .comment("How much do items in the forge:hot_ingots tag modify the temperature of the player")
                    .defineInRange("hotIngotTemperature", 1F, 0F, Float.MAX_VALUE);

        }
    }

    public static class ClientImpl
    {
        public final ForgeConfigSpec.DoubleValue noiseDarkness;
        public final ForgeConfigSpec.IntValue noiseLevels;
        public final ForgeConfigSpec.IntValue noiseArea;
        public final ForgeConfigSpec.IntValue drippiness;

        public final ForgeConfigSpec.ConfigValue<String> seasonColorSummer;
        public final ForgeConfigSpec.ConfigValue<String> seasonColorAutumn;
        public final ForgeConfigSpec.ConfigValue<String> seasonColorWinter;
        public final ForgeConfigSpec.ConfigValue<String> seasonColorSpring;

        ClientImpl(ForgeConfigSpec.Builder builder) {
            builder.comment("For all ARGB values, set to 00000000 to disable the feature in that season");

            noiseDarkness = builder
                    .comment("How dark should the noise be at most? Set to 0 to disable noise entirely")
                    .defineInRange("noiseDarkness", 0.18d, 0, 0.5d);

            noiseLevels = builder
                    .comment("How many darkness levels should there be?")
                    .defineInRange("noiseLevels", 1, 5, 30);

            noiseArea = builder
                    .comment("How big should noise areas be?")
                    .defineInRange("noiseArea", 10, 3, 50);

            drippiness = builder
                    .comment("How much to drip when wet. 0 to turn off")
                    .defineInRange("drippiness", 30, 0, 100);

            seasonColorSummer = builder
                    .comment("ARGB code for summer coloring in hexadecimal. Default: 1222FF11")
                    .define("seasonColorSummer", "1233FF11");

            seasonColorAutumn = builder
                    .comment("ARGB code for autumn coloring in hexadecimal. Default: EAFFDD55")
                    .define("seasonColorAutumn", "EAFFDD55");

            seasonColorWinter = builder
                    .comment("ARGB code for winter coloring in hexadecimal. Default: 6AFFEEEE")
                    .define("seasonColorWinter", "6AFFEEEE");

            seasonColorSpring = builder
                    .comment("ARGB code for spring coloring in hexadecimal. Default: 3311CFD1")
                    .define("seasonColorSpring", "3311CAD7");
        }
    }
}
