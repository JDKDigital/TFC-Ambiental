package com.lumintorious.tfcambiental.modifier;

import com.lumintorious.tfcambiental.TFCAmbientalConfig;
import com.lumintorious.tfcambiental.api.AmbientalRegistry;
import com.lumintorious.tfcambiental.api.EnvironmentalTemperatureProvider;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class EnvironmentalModifier
{
    public static float getEnvironmentTemperatureWithTimeOfDay(Player player) {
        return getEnvironmentTemperature(player) + handleTimeOfDay(player).get().getChange();
    }

    public static float getEnvironmentTemperature(Player player) {
        float avg = Climate.getAverageTemperature(player.level(), player.getOnPos());
        float actual = Climate.getTemperature(player.level(), player.getOnPos());
//        if(TFCAmbientalConfig.GENERAL.harsherTemperateAreas) {
        float diff = actual - 15; //TemperatureCapability.AVERAGE;
        float sign = Math.signum(diff);
        float generalDiff = Math.abs(avg - 15);//TemperatureCapability.AVERAGE);
        float mult0 = Math.max(0f, 1f); //TFCAmbientalConfig.GENERAL.harsherMultiplier - 1f);
//            float multiplier = 1 + Math.max(0, 1 - generalDiff / 55) * mult0;
        actual = 20 + (diff + 0.5f * sign);
//        }
        return actual;
    }

    public static float getEnvironmentHumidity(Player player) {
        return Climate.getRainfall(player.level(), player.getOnPos()) / 3000;
    }

    public static Optional<TempModifier> handleFire(Player player) {
        return player.isOnFire() ? TempModifier.defined("on_fire", 4f, 4f) : TempModifier.none();
    }

    public static Optional<TempModifier> handleGeneralTemperature(Player player) {
        return Optional.of(new TempModifier("environment", getEnvironmentTemperature(player), getEnvironmentHumidity(player)));
    }

    public static Optional<TempModifier> handleTimeOfDay(Player player) {
        int dayTicks = (int) (player.level().dayTime() % 24000);
        if (dayTicks < 6000) return TempModifier.defined("morning", 2f, 0);
        else if (dayTicks < 12000) return TempModifier.defined("afternoon", 4f, 0);
        else if (dayTicks < 18000) return TempModifier.defined("evening", 1f, 0);
        else return TempModifier.defined("night", 1f, 0);
    }

    public static boolean isSpringWater(Block block) {
        return (
                block == TFCFluids.SPRING_WATER.getFlowing().defaultFluidState().createLegacyBlock().getBlock() ||
                        block == TFCFluids.SPRING_WATER.getSource().defaultFluidState().createLegacyBlock().getBlock()
        );
    }

    public static Optional<TempModifier> handleWater(Player player) {
        if (player.isInWater()) {
            BlockPos pos = player.getOnPos().above();
            BlockState state = player.level().getBlockState(pos);
            if (isSpringWater(state.getBlock())) {
                return TempModifier.defined("in_hot_water", 5f, 6f);
            } else if (state.getBlock() == Blocks.LAVA) {
                return TempModifier.defined("in_lava", 10f, 5f);
//            }else if(state.getBlock() == FluidsTFC.SALT_WATER.get().getBlock() && player.level.getBiome(pos).get() == Biome.TempCategory.OCEAN ){
//                return new TempModifier("in_ocean_water", -8f, 6f);
            } else {
                return TempModifier.defined("in_water", -5f, 6f);
            }
        } else {
            return TempModifier.none();
        }
    }

    public static Optional<TempModifier> handleRain(Player player) {
        if (player.level().isRaining()) {
            if (getSkylight(player) < 15) {
                return TempModifier.defined("weather", -2f, 0.1f);
            } else {
                return TempModifier.defined("weather", -4f, 0.3f);
            }
        } else {
            return TempModifier.none();
        }
    }

    public static Optional<TempModifier> handleSprinting(Player player) {
        if (player.isSprinting()) {
            return TempModifier.defined("sprint", 2f, 0.3f);
        } else {
            return TempModifier.none();
        }
    }

    public static Optional<TempModifier> handleUnderground(Player player) {
        if (getSkylight(player) < 2) {
            return TempModifier.defined("underground", -6f, 0.2f);
        } else {
            return TempModifier.none();
        }
    }

    public static Optional<TempModifier> handleShade(Player player) {
        int light = getSkylight(player);
        light = Math.max(12, light);
        float temp = getEnvironmentTemperatureWithTimeOfDay(player);
        float avg = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
        if (light < 15 && temp > avg) {
            return TempModifier.defined("shade", -Math.abs(avg - temp) * 0.6f, 0f);
        } else {
            return TempModifier.none();
        }
    }

    public static Optional<TempModifier> handleCozy(Player player) {
        float temp = getEnvironmentTemperatureWithTimeOfDay(player);
        float avg = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
        if (EnvironmentalTemperatureProvider.calculateEnclosure(player, 30) && temp < avg - 1) {
            return TempModifier.defined("cozy", Math.abs(avg - 1 - temp) * 0.6f, 0f);
        } else {
            return TempModifier.none();
        }
    }

    public static Optional<TempModifier> handleThirst(Player player) {
        if (player.getFoodData() instanceof TFCFoodData stats) {
            if (getEnvironmentTemperatureWithTimeOfDay(player) > TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue() + 3 && stats.getThirst() > 80f) {
                return TempModifier.defined("well_hydrated", -2.5f, 0f);
            }
        }
        return TempModifier.none();
    }

    public static Optional<TempModifier> handleFood(Player player) {
        if (getEnvironmentTemperatureWithTimeOfDay(player) < TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue() - 3 && player.getFoodData().getFoodLevel() > 14) {
            return TempModifier.defined("well_fed", 2.5f, 0f);
        }
        return TempModifier.none();
    }

    public static Optional<TempModifier> handleDiet(Player player) {
        if (player.getFoodData() instanceof TFCFoodData stats) {
            if (getEnvironmentTemperatureWithTimeOfDay(player) < TFCAmbientalConfig.COMMON.coolThreshold.get().floatValue()) {
                float grainLevel = stats.getNutrition().getNutrient(Nutrient.GRAIN);
                float meatLevel = stats.getNutrition().getNutrient(Nutrient.PROTEIN);
                return TempModifier.defined("nutrients", 4f * grainLevel * meatLevel, 0f);
            }
            if (getEnvironmentTemperatureWithTimeOfDay(player) > TFCAmbientalConfig.COMMON.hotThreshold.get().floatValue()) {
                float fruitLevel = stats.getNutrition().getNutrient(Nutrient.FRUIT);
                float veggieLevel = stats.getNutrition().getNutrient(Nutrient.VEGETABLES);
                return TempModifier.defined("nutrients", -4f * fruitLevel * veggieLevel, 0f);
            }
        }
        return TempModifier.none();
    }

    public static int getSkylight(Player player) {
        BlockPos pos = new BlockPos(player.getOnPos());
        BlockPos pos2 = pos.above(1);
        return player.level().getBrightness(LightLayer.SKY, pos2);
    }

    public static int getBlockLight(Player player) {
        BlockPos pos = new BlockPos(player.getOnPos());
        BlockPos pos2 = pos.above(1);
        return player.level().getBrightness(LightLayer.BLOCK, pos2);
    }

//    public static EnvironmentalModifier handlePotionEffects(Player player) {
//        if(player.isPotionActive(TempEffect.COOL)){
//            return new EnvironmentalModifier("cooling_effect", -10F, 0);
//        }
//        if(player.isPotionActive(TempEffect.WARM)){
//            return new EnvironmentalModifier("heating_effect", 10F, 0);
//        }
//            return null;
//    }

    public static void evaluateAll(Player player, TempModifierStorage storage) {
        for (var fn : AmbientalRegistry.ENVIRONMENT) {
            storage.add(fn.getModifier(player));
        }
    }
}
