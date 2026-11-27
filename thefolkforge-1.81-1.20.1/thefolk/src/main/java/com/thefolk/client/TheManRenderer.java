package com.thefolk.client;

import com.thefolk.TheFolkMod;
import com.thefolk.entity.TheManEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Usa el modelo de jugador de vanilla (formato de skin estándar 64x64). */
public class TheManRenderer extends HumanoidMobRenderer<TheManEntity, TheManModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TheFolkMod.MOD_ID, "textures/entity/the_man.png");

    public TheManRenderer(EntityRendererProvider.Context context) {
        super(context, new TheManModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new TheManEyesLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(TheManEntity entity) {
        return TEXTURE;
    }
}
