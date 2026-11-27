package com.thefolk.client;

import com.thefolk.TheFolkMod;
import com.thefolk.entity.TheFolkEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** Ojos que brillan en la oscuridad. */
public class TheFolkEyesLayer extends EyesLayer<TheFolkEntity, TheFolkModel> {
    private static final RenderType EYES = RenderType.eyes(
            new ResourceLocation(TheFolkMod.MOD_ID, "textures/entity/the_folk_eyes.png"));

    public TheFolkEyesLayer(RenderLayerParent<TheFolkEntity, TheFolkModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}
