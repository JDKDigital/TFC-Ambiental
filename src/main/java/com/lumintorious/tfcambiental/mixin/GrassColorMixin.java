package com.lumintorious.tfcambiental.mixin;

import com.lumintorious.tfcambiental.TFCAmbientalConfig;
import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.common.entities.livestock.horse.TFCDonkey;
import net.dries007.tfc.common.entities.livestock.horse.TFCHorse;
import net.dries007.tfc.common.entities.livestock.horse.TFCMule;
import net.dries007.tfc.util.calendar.Calendar;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Month;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.lwjgl.system.MathUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Random;

@Mixin(TFCColors.class)
public class GrassColorMixin {
    private static PerlinNoise noiseGenerator =
        PerlinNoise.create(new XoroshiroRandomSource(1122), List.of(2));

    private static Color[] monthlyColors = new Color[12];

    static {
        resetColors();
    }

    @Inject(method = "getGrassColor", at = @At("RETURN"), cancellable = true, remap = false)
    private static void onGetGrassColor(
        @Nullable BlockPos pos,
        int tintIndex,
        CallbackInfoReturnable<Integer> callbackInfo
        ) {
        if(pos == null) return;
        int originalColor = callbackInfo.getReturnValueI();
        callbackInfo.setReturnValue(mapColor(originalColor, pos));
    }

    @Inject(method = "getTallGrassColor", at = @At("RETURN"), cancellable = true, remap = false)
    private static void onGetTallGrassColor(
            @Nullable BlockPos pos,
            int tintIndex,
            CallbackInfoReturnable<Integer> callbackInfo
    ) {
        if(pos == null) return;
        int originalColor = callbackInfo.getReturnValueI();
        callbackInfo.setReturnValue(mapColor(originalColor, pos));
    }

    @Inject(method = "getFoliageColor", at = @At("RETURN"), cancellable = true, remap = false)
    private static void onGetFoliageColor(
            @Nullable BlockPos pos,
            int tintIndex,
            CallbackInfoReturnable<Integer> callbackInfo
    ) {
        if(pos == null) return;
        int originalColor = TFCColors.getGrassColor(pos, tintIndex);
        callbackInfo.setReturnValue(mapColor(originalColor, pos));
    }

//    @Inject(method = "getSeasonalFoliageColor", at = @At("RETURN"), cancellable = true, remap = false)
//    private static void onGetSeasonalFoliageColor(
//            @Nullable BlockPos pos,
//            int tintIndex,
//            CallbackInfoReturnable<Integer> callbackInfo
//    ) {
//        if(pos == null) return;
//        int originalColorInt = callbackInfo.getReturnValueI();
//        Color originalColor = new Color(originalColorInt);
//        Color color = new Color(mapColor(originalColorInt, pos));
//        callbackInfo.setReturnValue(color.getRGB());
//    }

    private static int mapColor(int originalColor, BlockPos pos) {
        Color col = new Color(originalColor, true);
        int levels = TFCAmbientalConfig.CLIENT.noiseLevels.get();
        float scale = TFCAmbientalConfig.CLIENT.noiseArea.get();
        double darkness = TFCAmbientalConfig.CLIENT.noiseDarkness.get();
        double value = noiseGenerator.getValue(pos.getX() / scale, 50, pos.getZ() / scale);
        value = curve(0, 1, remap(value, -((1 << levels) - 1), (1 << levels) - 1, 0, 1), 1) * darkness;
        Color seasonalColor = getSeasonalColor();
        col = blendByAlpha(col, seasonalColor);
        col = blendByWeight(Color.BLACK, col, value);

        return col.getRGB();
    }

    private static double clamp(double num, double min, double max) {
        if(num > max) {
            return max;
        } else if(num < min) {
            return min;
        } else {
            return num;
        }
    }

    private static Color getSeasonalColor()
    {
        return monthlyColors[Calendars.CLIENT.getCalendarMonthOfYear().ordinal()];
    }

    private static void resetColors()
    {
        int julyCode = 0x00;
        int octoberCode = 0x00;
        int januaryCode = 0x00;
        int aprilCode = 0x00;
        try
        {
            julyCode = Integer.parseUnsignedInt(TFCAmbientalConfig.CLIENT.seasonColorSummer.get(), 16);
            octoberCode = Integer.parseUnsignedInt(TFCAmbientalConfig.CLIENT.seasonColorAutumn.get(), 16);
            januaryCode = Integer.parseUnsignedInt(TFCAmbientalConfig.CLIENT.seasonColorWinter.get(), 16);
            aprilCode = Integer.parseUnsignedInt(TFCAmbientalConfig.CLIENT.seasonColorSpring.get(), 16);
        }
        finally
        {
            monthlyColors[Month.JULY.ordinal()] = new Color(julyCode, true);
            monthlyColors[Month.OCTOBER.ordinal()] = new Color(octoberCode, true);
            monthlyColors[Month.JANUARY.ordinal()] = new Color(januaryCode, true);
            monthlyColors[Month.APRIL.ordinal()] = new Color(aprilCode, true);

            for (int i = 0; i < 12; i += 3)
            {
                monthlyColors[i + 1] = blendWithAlphas(monthlyColors[i], monthlyColors[(i + 3) % 12], 0.7);
                monthlyColors[i + 2] = blendWithAlphas(monthlyColors[i], monthlyColors[(i + 3) % 12], 0.3);
            }
        }
    }

    private static double remap(double value, double currentLow, double currentHigh, double newLow, double newHigh)
    {
        return newLow + (value - currentLow) * (newHigh - newLow) / (currentHigh - currentLow);
    }

    private static double curve(double start, double end, double amount, double waves)
    {
        amount = clamp(amount, 0, 1);
        amount = clamp((amount - start) / (end - start), 0, 1);


        return clamp(0.5 + 0.5 * Math.sin(Math.cos((float) (Math.PI * Math.tan(90 * amount)))) * Math.cos(Math.sin((float) Math.tan(amount))), 0, 1);
    }

    private static Color blendByWeight(Color c0, Color c1, double weight0)
    {
        double weight1 = 1.0d - weight0;
        double r = weight0 * c0.getRed() + weight1 * c1.getRed();
        double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
        double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
        double a = Math.max(c0.getAlpha(), c1.getAlpha());

        return new Color((int) clamp(r, 0, 255), (int) clamp(g, 0, 255), (int) clamp(b, 0, 255), (int) a);
    }

    private static Color blendWithAlphas(Color c0, Color c1, double weight0)
    {
        double weight1 = 1.0d - weight0;
        double r = weight0 * c0.getRed() + weight1 * c1.getRed();
        double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
        double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
        double a = weight0 * c0.getAlpha() + weight1 * c1.getAlpha();

        return new Color((int) r, (int) g, (int) b, (int) a);
    }

    private static Color blendByAlpha(Color c0, Color c1)
    {
        return blendByWeight(c0, c1, 1d - (double) c1.getAlpha() / 255d);
    }
//
//    // Default TFC grass coloring
//    private static int computeInitialGrassColor(BlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
//    {
//        if (pos != null)
//        {
//            double temp = MathHelper.clamp((ClimateTFC.getMonthlyTemp(pos) + 30) / 60, 0, 1);
//            double rain = MathHelper.clamp((ClimateTFC.getRainfall(pos) - 50) / 400, 0, 1);
//            return ColorizerGrass.getGrassColor(temp, rain);
//        }
//
//        return ColorizerGrass.getGrassColor(0.5, 0.5);
//    }
}
