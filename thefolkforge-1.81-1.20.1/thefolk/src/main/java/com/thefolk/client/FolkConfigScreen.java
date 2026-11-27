package com.thefolk.client;

import com.thefolk.FolkDirector;
import com.thefolk.ModNetwork;
import com.thefolk.SetModePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Menú del FolkConfig con los 3 botones. */
public class FolkConfigScreen extends Screen {

    public FolkConfigScreen() {
        super(Component.literal("FolkConfig"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 36;

        this.addRenderableWidget(Button.builder(Component.literal("Normal"),
                        b -> choose(FolkDirector.MODE_NORMAL))
                .bounds(cx - 100, y, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("ERROR").withStyle(ChatFormatting.GOLD),
                        b -> choose(FolkDirector.MODE_ERROR))
                .bounds(cx - 100, y + 28, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("THE FOLK IS COMING").withStyle(ChatFormatting.RED),
                        b -> choose(FolkDirector.MODE_COMING))
                .bounds(cx - 100, y + 56, 200, 20).build());
    }

    private void choose(int mode) {
        ModNetwork.CHANNEL.sendToServer(new SetModePacket(mode));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 62, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
