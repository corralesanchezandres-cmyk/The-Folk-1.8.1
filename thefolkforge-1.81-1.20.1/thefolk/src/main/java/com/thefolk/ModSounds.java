package com.thefolk;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Sonidos propios. Sus archivos se definen en assets/thefolk/sounds.json. */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TheFolkMod.MOD_ID);

    public static final RegistryObject<SoundEvent> WHISTLE = register("whistle");
    public static final RegistryObject<SoundEvent> VOICE_BEHIND_ES = register("voice.behind_es");
    public static final RegistryObject<SoundEvent> VOICE_BEHIND_EN = register("voice.behind_en");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TheFolkMod.MOD_ID, name)));
    }
}
