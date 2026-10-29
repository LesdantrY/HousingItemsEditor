package dev.housingitems;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/** Small UI adapter for the rendering API transition in Minecraft 26.1. */
interface Canvas {
    void drawString(Font font, String text, int x, int y, int color);
    void drawCenteredString(Font font, Component text, int x, int y, int color);
    default void text(Font font, String text, int x, int y, int color) { drawString(font, text, x, y, color); }
    default void centeredText(Font font, Component text, int x, int y, int color) { drawCenteredString(font, text, x, y, color); }
    default void drawCenteredString(Font font, String text, int x, int y, int color) {
        drawCenteredString(font, Component.literal(text), x, y, color);
    }
}
