package dev.housingitems;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

abstract class PortableScreen extends Screen {
    protected PortableScreen(Component title) { super(title); }
    protected abstract void draw(Canvas canvas, int mouseX, int mouseY, float delta);
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        draw(new Canvas() {
            public void drawString(Font font, String text, int x, int y, int color) {
                graphics.drawString(font, text, x, y, color);
            }
            public void drawCenteredString(Font font, Component text, int x, int y, int color) {
                graphics.drawCenteredString(font, text, x, y, color);
            }
        }, mouseX, mouseY, delta);
    }
}
