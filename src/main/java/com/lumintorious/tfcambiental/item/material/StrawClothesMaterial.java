package com.lumintorious.tfcambiental.item.material;

import com.lumintorious.tfcambiental.modifier.TempModifier;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class StrawClothesMaterial implements ArmorMaterial, TemperatureAlteringMaterial
{
    public static final StrawClothesMaterial MATERIAL = new StrawClothesMaterial();

    @Override
    public int getDurabilityForType(ArmorItem.Type pType) {
        return 100;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type pType) {
        return 0;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public @NotNull SoundEvent getEquipSound() {
        return SoundEvents.GRASS_BREAK;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.of(new ItemStack(TFCItems.STRAW.get(), 1));
    }

    @Override
    public @NotNull String getName() {
        return "tfcambiental:straw";
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }

    @Override
    public TempModifier getTempModifier(ItemStack stack) {
        return new TempModifier(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString(), 0, -0.1f);
    }
}
