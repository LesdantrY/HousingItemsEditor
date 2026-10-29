package dev.housingitems;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

abstract class PortableScreen extends Screen {
    protected PortableScreen(Component title) { super(title); }
    protected abstract void draw(Canvas canvas, int mouseX, int mouseY, float delta);
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        draw(new Canvas() {
            public void drawString(Font font, String text, int x, int y, int color) {
                graphics.text(font, text, x, y, color, true);
            }
            public void drawCenteredString(Font font, Component text, int x, int y, int color) {
                graphics.text(font, text, x - font.width(text) / 2, y, color, true);
            }
        }, mouseX, mouseY, delta);
    }
}
