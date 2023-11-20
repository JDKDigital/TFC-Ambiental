package com.lumintorious.tfcambiental.api;

import com.lumintorious.tfcambiental.modifier.EnvironmentalModifier;
import com.lumintorious.tfcambiental.modifier.TempModifier;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

public class AmbientalRegistry<Type> implements Iterable<Type>
{
    public static final AmbientalRegistry<ItemTemperatureProvider> ITEMS = new AmbientalRegistry<>();
    public static final AmbientalRegistry<BlockTemperatureProvider> BLOCKS = new AmbientalRegistry<>();
    public static final AmbientalRegistry<BlockEntityTemperatureProvider> BLOCK_ENTITIES = new AmbientalRegistry<>();
    public static final AmbientalRegistry<EnvironmentalTemperatureProvider> ENVIRONMENT = new AmbientalRegistry<>();
    public static final AmbientalRegistry<EquipmentTemperatureProvider> EQUIPMENT = new AmbientalRegistry<>();

    static {
        EQUIPMENT.register(EquipmentTemperatureProvider::handleSunlightCap);
        EQUIPMENT.register(EquipmentTemperatureProvider::handleClothes);

        ITEMS.register(ItemTemperatureProvider::handleTemperatureCapability);

        BLOCK_ENTITIES.register(BlockEntityTemperatureProvider::handleCharcoalForge);
        BLOCK_ENTITIES.register(BlockEntityTemperatureProvider::handleFirePit);
        BLOCK_ENTITIES.register(BlockEntityTemperatureProvider::handleBloomery);
        BLOCK_ENTITIES.register(BlockEntityTemperatureProvider::handleIHeatBlock);
//        BLOCK_ENTITIES.register(BlockEntityTemperatureProvider::handleLamps);

        BLOCKS.register((player, pos, state) -> Optional.of(new TempModifier("fire", 3f, 0f)).filter((mod) -> state.getBlock() == Blocks.FIRE));
        BLOCKS.register((player, pos, state) -> Optional.of(new TempModifier("lava", 3f, 0f)).filter((mod) -> state.getBlock() == Blocks.LAVA));
        BLOCKS.register((player, pos, state) -> Optional.of(new TempModifier("snow", -0.5f, 0.2f)).filter((mod) -> state.getBlock() == Blocks.SNOW && player.level().getBrightness(LightLayer.SKY, pos) == 15));

        ENVIRONMENT.register(EnvironmentalModifier::handleGeneralTemperature);
        ENVIRONMENT.register(EnvironmentalModifier::handleTimeOfDay);
        ENVIRONMENT.register(EnvironmentalModifier::handleShade);
        ENVIRONMENT.register(EnvironmentalModifier::handleCozy);
        ENVIRONMENT.register(EnvironmentalModifier::handleThirst);
        ENVIRONMENT.register(EnvironmentalModifier::handleFood);
        ENVIRONMENT.register(EnvironmentalModifier::handleDiet);
        ENVIRONMENT.register(EnvironmentalModifier::handleFire);
        ENVIRONMENT.register(EnvironmentalModifier::handleWater);
        ENVIRONMENT.register(EnvironmentalModifier::handleRain);
        ENVIRONMENT.register(EnvironmentalModifier::handleSprinting);
        ENVIRONMENT.register(EnvironmentalModifier::handleUnderground);
//        ENVIRONMENT.register(EnvironmentalModifier::handlePotionEffects);
    }

    private final ArrayList<Type> list = new ArrayList<>();

    private AmbientalRegistry() {
    }

    public void register(Type type) {
        list.add(type);
    }

    @Override
    public Iterator<Type> iterator() {
        return list.iterator();
    }
}
