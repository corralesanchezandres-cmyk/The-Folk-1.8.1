package com.thefolk.client;

import com.thefolk.TheFolkMod;
import com.thefolk.entity.TheFolkEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TheFolkRenderer extends MobRenderer<TheFolkEntity, TheFolkModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TheFolkMod.MOD_ID, "textures/entity/the_folk.png");

    public TheFolkRenderer(EntityRendererProvider.Context context) {
        super(context, new TheFolkModel(context.bakeLayer(TheFolkModel.LAYER)), 0.0F);
        this.addLayer(new TheFolkEyesLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(TheFolkEntity entity) {
        return TEXTURE;
    }
}
