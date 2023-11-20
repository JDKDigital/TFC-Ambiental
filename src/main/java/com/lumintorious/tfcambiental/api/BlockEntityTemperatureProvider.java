package com.lumintorious.tfcambiental.api;

import com.lumintorious.tfcambiental.TFCAmbientalConfig;
import com.lumintorious.tfcambiental.item.TFCAmbientalItems;
import com.lumintorious.tfcambiental.modifier.EnvironmentalModifier;
import com.lumintorious.tfcambiental.modifier.TempModifier;
import com.lumintorious.tfcambiental.modifier.TempModifierStorage;
import net.dries007.tfc.common.blockentities.BloomeryBlockEntity;
import net.dries007.tfc.common.blockentities.CharcoalForgeBlockEntity;
import net.dries007.tfc.common.blockentities.FirepitBlockEntity;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Locale;
import java.util.Optional;

@FunctionalInterface
public interface BlockEntityTemperatureProvider
{
    Optional<TempModifier> getModifier(Player player, BlockEntity entity);

    static void evaluateAll(Player player, TempModifierStorage storage) {
        BlockTemperatureProvider.evaluateAll(player, storage);
    }

    private static boolean hasProtection(Player player) {
        var item = CuriosApi.getCuriosHelper().findCurios(player, TFCAmbientalItems.LEATHER_APRON.get());
        if (item.isEmpty()) return false;
        float environmentTemperature = EnvironmentalModifier.getEnvironmentTemperatureWithTimeOfDay(player);
        float AVERAGE = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
        return environmentTemperature > AVERAGE;
    }

    public static Optional<TempModifier> handleCharcoalForge(Player player, BlockEntity entity) {
        if (entity instanceof CharcoalForgeBlockEntity forge) {

            float temp = forge.getTemperature();
            float change = temp / 140f;
            if (hasProtection(player)) {
                change = change * 0.3f;
            }
            return TempModifier.defined("charcoal_forge", change, 0);
        } else {
            return TempModifier.none();
        }
    }

    public static Optional<TempModifier> handleFirePit(Player player, BlockEntity entity) {
        if (entity instanceof FirepitBlockEntity pit) {
            float temp = pit.getTemperature();
            float change = temp / 100f;
            if (hasProtection(player)) {
                change = change * 0.3f;
            }
            return TempModifier.defined("fire_pit", Math.min(6f, change), 0);
        } else {
            return TempModifier.none();
        }
    }

    public static Optional<TempModifier> handleBloomery(Player player, BlockEntity entity) {
        if (entity instanceof BloomeryBlockEntity bloomery) {
            float change = bloomery.getRemainingTicks() > 0 ? 4f : 0f;
            if (hasProtection(player)) {
                change = change * 0.3f;
            }
            return TempModifier.defined("bloomery", change, 0);
        } else {
            return TempModifier.none();
        }
    }

    public static Optional<TempModifier> handleIHeatBlock(Player player, BlockEntity entity) {
        return entity.getCapability(HeatCapability.BLOCK_CAPABILITY).map(cap -> {
            return new TempModifier(
                    entity.getClass().getName().toLowerCase(Locale.ROOT),
                    cap.getTemperature() / 140f,
                    0
            );
        });
    }

//    public static Optional<TempModifier> handleLamps(Player player, BlockEntity entity) {
//        if(entity instanceof LampBlockEntity lamp) {
//            if(EnvironmentalModifier.getEnvironmentTemperature(player) < TemperatureCapability.AVERAGE) {
//                float change = (lamp.get) ? 1f : 0f;
//                float potency = 0f;
//                return new TileEntityModifier("lamp", change, potency, false);
//            }
//        }
//        return null;
//    }
}
