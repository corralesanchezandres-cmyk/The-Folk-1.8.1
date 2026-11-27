package com.thefolk;

import com.thefolk.entity.TheFolkEntity;
import com.thefolk.entity.TheManEntity;
import com.thefolk.entity.TheThingEntity;
import com.thefolk.entity.UknowEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TheFolkMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.THE_FOLK.get(), TheFolkEntity.createAttributes().build());
        event.put(ModEntities.THE_THING.get(), TheThingEntity.createAttributes().build());
        event.put(ModEntities.THE_MAN.get(), TheManEntity.createAttributes().build());
        event.put(ModEntities.UKNOW.get(), UknowEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.FOLK_CONFIG);
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.REAPER);
            event.accept(ModItems.PARALYZER);
        }
    }
}
