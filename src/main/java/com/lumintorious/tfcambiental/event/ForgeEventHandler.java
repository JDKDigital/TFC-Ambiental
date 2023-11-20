package com.lumintorious.tfcambiental.event;

import com.lumintorious.tfcambiental.TFCAmbiental;
import com.lumintorious.tfcambiental.capability.TemperatureCapability;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TFCAmbiental.MOD_ID)
public class ForgeEventHandler
{
    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            TemperatureCapability capability = new TemperatureCapability();
            capability.setPlayer(player);
            event.addCapability(TemperatureCapability.KEY, capability);
        }
    }

    @SubscribeEvent
    public static void onPlayerUpdate(TickEvent.PlayerTickEvent event) {
        if (!event.player.level().isClientSide()) {
            event.player.getCapability(TemperatureCapability.CAPABILITY).ifPresent(TemperatureCapability::update);
        }
    }
}
