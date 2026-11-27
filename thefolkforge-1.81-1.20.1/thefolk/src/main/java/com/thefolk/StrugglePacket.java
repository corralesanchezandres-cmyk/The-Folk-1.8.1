package com.thefolk;

import com.thefolk.entity.UknowEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** El jugador atrapado por Uknow pulsó ESPACIO. */
public class StrugglePacket {

    public static void encode(StrugglePacket msg, FriendlyByteBuf buf) {
    }

    public static StrugglePacket decode(FriendlyByteBuf buf) {
        return new StrugglePacket();
    }

    public static void handle(StrugglePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer player = c.getSender();
            if (player != null && player.getVehicle() instanceof UknowEntity uknow) {
                uknow.struggle(player);
            }
        });
        c.setPacketHandled(true);
    }
}
