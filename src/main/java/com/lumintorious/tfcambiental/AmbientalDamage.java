package com.lumintorious.tfcambiental;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public abstract class AmbientalDamage
{
    public static final ResourceKey<DamageType> HOT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(TFCAmbiental.MOD_ID, "heatstroke"));
    public static final ResourceKey<DamageType> FREEZE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(TFCAmbiental.MOD_ID, "frostbite"));
}
