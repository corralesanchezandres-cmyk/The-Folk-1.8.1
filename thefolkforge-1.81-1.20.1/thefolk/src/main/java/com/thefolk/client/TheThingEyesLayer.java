package com.thefolk.client;

import com.thefolk.TheFolkMod;
import com.thefolk.entity.TheThingEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** Ojos rojos que brillan. */
public class TheThingEyesLayer extends EyesLayer<TheThingEntity, TheThingModel> {
    private static final RenderType EYES = RenderType.eyes(
            new ResourceLocation(TheFolkMod.MOD_ID, "textures/entity/the_thing_eyes.png"));

    public TheThingEyesLayer(RenderLayerParent<TheThingEntity, TheThingModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}
