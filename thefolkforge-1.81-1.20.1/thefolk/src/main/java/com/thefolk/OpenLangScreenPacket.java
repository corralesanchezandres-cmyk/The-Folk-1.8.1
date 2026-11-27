package com.thefolk;

import com.thefolk.client.FolkClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** El servidor le pide al cliente que abra el panel de idiomas (lo dispara /folklang). */
public class OpenLangScreenPacket {

    public static void encode(OpenLangScreenPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenLangScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenLangScreenPacket();
    }

    public static void handle(OpenLangScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> FolkClientHooks::openLangScreen));
        c.setPacketHandled(true);
    }
}
