package com.lumintorious.tfcambiental.item;

import net.minecraft.world.item.*;

public class ClothesItem extends Item implements Vanishable
{
    private final ArmorMaterial material;
    private final ArmorItem.Type slot;

    public ClothesItem(ArmorMaterial material, ArmorItem.Type slot, Properties pProperties) {
        super(pProperties);
        this.material = material;
        this.slot = slot;
    }

    public ArmorMaterial getMaterial() {
        return material;
    }

    public ArmorItem.Type getEquivalentSlot() {
        return this.slot;
    }

    @Override
    public int getDamage(ItemStack stack) {
        return super.getDamage(stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return material.getDurabilityForType(getEquivalentSlot());
    }
}
