package com.thefolk.client;

import com.thefolk.FolkLang;
import com.thefolk.ModNetwork;
import com.thefolk.SetLangPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Panel de idiomas (se abre con /folklang). */
public class FolkLangScreen extends Screen {

    public FolkLangScreen() {
        super(Component.literal("Idioma / Language"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 24;

        this.addRenderableWidget(Button.builder(Component.literal("Español"),
                        b -> choose(FolkLang.ES))
                .bounds(cx - 100, y, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("English"),
                        b -> choose(FolkLang.EN))
                .bounds(cx - 100, y + 28, 200, 20).build());
    }

    private void choose(String lang) {
        ModNetwork.CHANNEL.sendToServer(new SetLangPacket(lang));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
