package com.lumintorious.tfcambiental.api;

import com.lumintorious.tfcambiental.TFCAmbientalConfig;
import com.lumintorious.tfcambiental.item.ClothesItem;
import com.lumintorious.tfcambiental.item.TemperatureAlteringMaterial;
import com.lumintorious.tfcambiental.modifier.EnvironmentalModifier;
import com.lumintorious.tfcambiental.modifier.TempModifier;
import com.lumintorious.tfcambiental.modifier.TempModifierStorage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@FunctionalInterface
public interface EquipmentTemperatureProvider
{
    Optional<TempModifier> getModifier(Player player, ItemStack stack);

    static Optional<TempModifier> handleClothes(Player player, ItemStack stack) {
        if (stack.getItem() instanceof ClothesItem clothesItem) {
            if (clothesItem.getMaterial() instanceof TemperatureAlteringMaterial tempMaterial) {
                return Optional.of(tempMaterial.getTempModifier(stack));
            }
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleSunlightCap(Player player, ItemStack stack) {
        float AVERAGE = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
        if (stack.getItem() instanceof ClothesItem clothesItem) {
            if (clothesItem.getEquivalentSlot().equals(ArmorItem.Type.HELMET)) {
                if (player.level().getBrightness(LightLayer.SKY, player.getOnPos().above()) > 14) {
                    float envTemp = EnvironmentalModifier.getEnvironmentTemperatureWithTimeOfDay(player);
                    if (envTemp > AVERAGE) {
                        float diff = envTemp - AVERAGE;
                        Optional<TempModifier> helmetMod = handleClothes(player, stack);
                        if (helmetMod.isPresent()) {
                            diff = diff - helmetMod.get().getChange();
                        }
                        return TempModifier.defined("sunlight_protection", diff * -0.2f, -0.5f);
                    }
                }
            }
        }
        return TempModifier.none();
    }

    static void evaluateAll(Player player, TempModifierStorage storage) {
        CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(c -> {
            for (int i = 0; i < c.getSlots(); i++) {
                ItemStack stack = c.getStackInSlot(i);
                for (var fn : AmbientalRegistry.EQUIPMENT) {
                    storage.add(fn.getModifier(player, stack));
                }
            }
        });
    }
}
