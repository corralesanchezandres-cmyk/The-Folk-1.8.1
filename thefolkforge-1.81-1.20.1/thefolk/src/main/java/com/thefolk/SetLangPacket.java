package com.thefolk;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** El cliente elige idioma en el panel; el servidor lo guarda. Cualquier jugador puede usarlo. */
public class SetLangPacket {
    private final String lang;

    public SetLangPacket(String lang) {
        this.lang = lang;
    }

    public static void encode(SetLangPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.lang, 8);
    }

    public static SetLangPacket decode(FriendlyByteBuf buf) {
        return new SetLangPacket(buf.readUtf(8));
    }

    public static void handle(SetLangPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer player = c.getSender();
            if (player == null) return;
            if (!FolkLang.ES.equals(msg.lang) && !FolkLang.EN.equals(msg.lang)) return;
            applyLanguage(player, msg.lang);
        });
        c.setPacketHandled(true);
    }

    /** Guarda el idioma y avisa al jugador en el idioma nuevo. */
    public static void applyLanguage(ServerPlayer player, String lang) {
        FolkLangData.get(player.serverLevel()).setLang(player.getUUID(), lang);
        player.displayClientMessage(Component.literal(FolkLang.get(player, "lang.set")), true);
    }
}
