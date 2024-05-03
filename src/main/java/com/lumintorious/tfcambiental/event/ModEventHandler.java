package com.lumintorious.tfcambiental.event;

import com.lumintorious.tfcambiental.TFCAmbiental;
import com.lumintorious.tfcambiental.TFCAmbientalGuiRenderer;
import com.lumintorious.tfcambiental.item.TFCAmbientalItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = TFCAmbiental.MOD_ID)
public class ModEventHandler
{
    @SubscribeEvent
    public static void registerGui(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id(), "temperature", TFCAmbientalGuiRenderer::render);
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.COMBAT)) {
            for (RegistryObject<Item> item : TFCAmbientalItems.ITEMS.getEntries()) {
                if (!item.getId().getPath().equals("snowshoes")) {
                    event.accept(item);
                }
            }
        }
    }
}
