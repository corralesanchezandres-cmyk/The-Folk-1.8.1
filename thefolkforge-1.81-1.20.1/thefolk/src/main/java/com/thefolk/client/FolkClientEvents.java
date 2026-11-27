package com.thefolk.client;

import com.thefolk.ModNetwork;
import com.thefolk.StrugglePacket;
import com.thefolk.TheFolkMod;
import com.thefolk.entity.UknowEntity;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Avisa al servidor cada vez que pulsas ESPACIO mientras Uknow te tiene agarrado. */
@Mod.EventBusSubscriber(modid = TheFolkMod.MOD_ID, value = Dist.CLIENT)
public class FolkClientEvents {

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (!mc.options.keyJump.matches(event.getKey(), event.getScanCode())) return;
        if (mc.player.getVehicle() instanceof UknowEntity) {
            ModNetwork.CHANNEL.sendToServer(new StrugglePacket());
        }
    }
}
