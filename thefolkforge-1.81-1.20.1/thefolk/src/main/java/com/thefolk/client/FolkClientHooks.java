package com.thefolk.client;

import net.minecraft.client.Minecraft;

public class FolkClientHooks {
    public static void openConfigScreen() {
        Minecraft.getInstance().setScreen(new FolkConfigScreen());
    }

    public static void openLangScreen() {
        Minecraft.getInstance().setScreen(new FolkLangScreen());
    }
}
