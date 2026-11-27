package com.thefolk;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** El cliente le dice al servidor qué botón del FolkConfig se pulsó. */
public class SetModePacket {
    private final int mode;

    public SetModePacket(int mode) {
        this.mode = mode;
    }

    public static void encode(SetModePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.mode);
    }

    public static SetModePacket decode(FriendlyByteBuf buf) {
        return new SetModePacket(buf.readVarInt());
    }

    public static void handle(SetModePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer player = c.getSender();
            if (player == null) return;
            if (msg.mode < FolkDirector.MODE_NORMAL || msg.mode > FolkDirector.MODE_COMING) return;

            boolean holding = player.getMainHandItem().is(ModItems.FOLK_CONFIG.get())
                    || player.getOffhandItem().is(ModItems.FOLK_CONFIG.get());
            if (!holding) return;

            FolkDirector.setMode(player.serverLevel(), msg.mode);
            ChatFormatting color = msg.mode == FolkDirector.MODE_NORMAL ? ChatFormatting.GREEN
                    : msg.mode == FolkDirector.MODE_ERROR ? ChatFormatting.GOLD : ChatFormatting.RED;
            player.displayClientMessage(
                    Component.literal("FolkConfig: " + FolkDirector.modeName(msg.mode)).withStyle(color), true);
        });
        c.setPacketHandled(true);
    }
}
