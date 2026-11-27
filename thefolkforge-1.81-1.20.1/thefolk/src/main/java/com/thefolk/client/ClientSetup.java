package com.thefolk.client;

import com.thefolk.ModEntities;
import com.thefolk.TheFolkMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TheFolkMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TheFolkModel.LAYER, TheFolkModel::createBodyLayer);
        event.registerLayerDefinition(TheThingModel.LAYER, TheThingModel::createBodyLayer);
        event.registerLayerDefinition(UknowModel.LAYER, UknowModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.THE_FOLK.get(), TheFolkRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_THING.get(), TheThingRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_MAN.get(), TheManRenderer::new);
        event.registerEntityRenderer(ModEntities.UKNOW.get(), UknowRenderer::new);
    }
}
