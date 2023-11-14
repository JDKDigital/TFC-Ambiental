package com.lumintorious.tfcambiental;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public abstract class AmbientalDamage {
    static DamageType HOT = new DamageType("heatstroke",0.2f);
    static DamageType FREEZE = new DamageType("frostbite",0.4f)
    public static final DamageSource HEAT = new DamageSource(HOT).bypassArmor();
    public static final DamageSource COLD = new DamageSource(FREEZE).bypassArmor();
}
