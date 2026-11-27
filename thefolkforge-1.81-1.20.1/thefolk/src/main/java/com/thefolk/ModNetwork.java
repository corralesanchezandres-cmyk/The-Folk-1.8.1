package com.thefolk;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TheFolkMod.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        CHANNEL.registerMessage(0, SetModePacket.class,
                SetModePacket::encode, SetModePacket::decode, SetModePacket::handle);
        CHANNEL.registerMessage(1, SetLangPacket.class,
                SetLangPacket::encode, SetLangPacket::decode, SetLangPacket::handle);
        CHANNEL.registerMessage(2, OpenLangScreenPacket.class,
                OpenLangScreenPacket::encode, OpenLangScreenPacket::decode, OpenLangScreenPacket::handle);
        CHANNEL.registerMessage(3, StrugglePacket.class,
                StrugglePacket::encode, StrugglePacket::decode, StrugglePacket::handle);
    }
}
