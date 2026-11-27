package com.thefolk.client;

import com.thefolk.TheFolkMod;
import com.thefolk.entity.UknowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class UknowRenderer extends MobRenderer<UknowEntity, UknowModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TheFolkMod.MOD_ID, "textures/entity/the_uknow.png");

    public UknowRenderer(EntityRendererProvider.Context context) {
        super(context, new UknowModel(context.bakeLayer(UknowModel.LAYER)), 0.9F);
        this.addLayer(new UknowEyesLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(UknowEntity entity) {
        return TEXTURE;
    }
}
