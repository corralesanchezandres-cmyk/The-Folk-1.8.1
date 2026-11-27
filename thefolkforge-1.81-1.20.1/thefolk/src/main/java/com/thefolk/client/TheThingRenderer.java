package com.thefolk.client;

import com.thefolk.TheFolkMod;
import com.thefolk.entity.TheThingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TheThingRenderer extends MobRenderer<TheThingEntity, TheThingModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TheFolkMod.MOD_ID, "textures/entity/the_thing.png");

    public TheThingRenderer(EntityRendererProvider.Context context) {
        super(context, new TheThingModel(context.bakeLayer(TheThingModel.LAYER)), 0.0F);
        this.addLayer(new TheThingEyesLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(TheThingEntity entity) {
        return TEXTURE;
    }
}
