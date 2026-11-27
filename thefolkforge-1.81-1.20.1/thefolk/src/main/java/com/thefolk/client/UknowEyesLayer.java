package com.thefolk.client;

import com.thefolk.TheFolkMod;
import com.thefolk.entity.UknowEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** Ojos rojos que brillan. */
public class UknowEyesLayer extends EyesLayer<UknowEntity, UknowModel> {
    private static final RenderType EYES = RenderType.eyes(
            new ResourceLocation(TheFolkMod.MOD_ID, "textures/entity/the_uknow_eyes.png"));

    public UknowEyesLayer(RenderLayerParent<UknowEntity, UknowModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}
