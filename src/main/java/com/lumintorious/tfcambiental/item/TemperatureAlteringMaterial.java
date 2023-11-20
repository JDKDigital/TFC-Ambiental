package com.lumintorious.tfcambiental.item;

import com.lumintorious.tfcambiental.modifier.TempModifier;
import net.minecraft.world.item.ItemStack;

public interface TemperatureAlteringMaterial
{
    TempModifier getTempModifier(ItemStack stack);
}
