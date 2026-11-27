package com.thefolk;

import com.thefolk.entity.UknowEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Eventos generales: entrega de la guía y agarre de Uknow. */
@Mod.EventBusSubscriber(modid = TheFolkMod.MOD_ID)
public class FolkEvents {

    /** Al entrar al mundo: guía equipada (si no tiene ya una). */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FolkGuide.giveIfMissing(player);
        }
    }

    /** Al reaparecer tras morir: guía equipada (si no tiene ya una). */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FolkGuide.giveIfMissing(player);
        }
    }

    /** Mientras Uknow te sujeta no puedes bajarte (ni con la tecla de agacharse). */
    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (event.isDismounting()
                && event.getEntityBeingMounted() instanceof UknowEntity uknow
                && uknow.isHolding(event.getEntityMounting())) {
            event.setCanceled(true);
        }
    }
}
