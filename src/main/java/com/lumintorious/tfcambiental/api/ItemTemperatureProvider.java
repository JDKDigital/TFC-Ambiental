package com.lumintorious.tfcambiental.api;

import com.lumintorious.tfcambiental.TFCAmbiental;
import com.lumintorious.tfcambiental.TFCAmbientalConfig;
import com.lumintorious.tfcambiental.modifier.TempModifier;
import com.lumintorious.tfcambiental.modifier.TempModifierStorage;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

@FunctionalInterface
public interface ItemTemperatureProvider
{
    Optional<TempModifier> getModifier(Player player, ItemStack stack);

    static void evaluateAll(Player player, TempModifierStorage modifiers) {
        for (ItemStack stack : player.getInventory().items) {
            for (ItemTemperatureProvider provider : AmbientalRegistry.ITEMS) {
                modifiers.add(provider.getModifier(player, stack));
            }
        }
    }

    static Optional<TempModifier> handleTemperatureCapability(Player player, ItemStack stack) {
        return stack.getCapability(HeatCapability.CAPABILITY).map(cap -> {
            float temp = cap.getTemperature() / 800;
            return new TempModifier("heat_item", temp, 0.1f * stack.getCount());
        });
    }

    static Optional<TempModifier> handleHotIngots(Player player, ItemStack stack) {
        return stack.is(TFCAmbiental.HOT_INGOTS) ? Optional.of(new TempModifier("heat_item", TFCAmbientalConfig.COMMON.hotIngotTemperature.get().floatValue(), 0.1f * stack.getCount())) : TempModifier.none();
    }
}
