package dev.housingitems;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

/** Saturation horizontally, brightness vertically; the hue comes from the hue slider. */
final class GradientColorWidget extends AbstractWidget {
    private final float[] hsv;
    private final Consumer<Integer> changed;
    GradientColorWidget(int x, int y, int width, int height, float[] hsv, Consumer<Integer> changed) {
        super(x, y, width, height, Component.literal("Color gradient: left to right saturation, top to bottom brightness"));
        this.hsv = hsv; this.changed = changed;
    }
    private void select(double x, double y) {
        hsv[1] = (float)Math.clamp((x - getX()) / Math.max(1, width - 1), 0, 1);
        hsv[2] = 1 - (float)Math.clamp((y - getY()) / Math.max(1, height - 1), 0, 1);
        changed.accept(java.awt.Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) & 0xFFFFFF);
    }
    @Override public void onClick(MouseButtonEvent event, boolean doubleClick) { select(event.x(), event.y()); }
    @Override protected void onDrag(MouseButtonEvent event, double dx, double dy) { select(event.x(), event.y()); }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (!isFocused()) return false;
        float step = (event.modifiers() & org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT) != 0 ? .1f : .01f;
        switch (event.key()) {
            case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT -> hsv[1] = Math.max(0, hsv[1] - step);
            case org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT -> hsv[1] = Math.min(1, hsv[1] + step);
            case org.lwjgl.glfw.GLFW.GLFW_KEY_UP -> hsv[2] = Math.min(1, hsv[2] + step);
            case org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN -> hsv[2] = Math.max(0, hsv[2] - step);
            default -> { return false; }
        }
        changed.accept(java.awt.Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) & 0xFFFFFF);
        return true;
    }
    @Override protected void renderWidget(GuiGraphics g, int mx, int my, float dt) {
        for (int x = 0; x < width; x++) {
            int top = java.awt.Color.HSBtoRGB(hsv[0], x / (float)Math.max(1, width - 1), 1);
            g.fillGradient(getX()+x, getY(), getX()+x+1, getY()+height, top, 0xFF000000);
        }
        int x = getX() + Math.round(hsv[1] * (width-1));
        int y = getY() + Math.round((1-hsv[2]) * (height-1));
        g.fill(x-3,y-3,x+4,y-2,0xFFFFFFFF); g.fill(x-3,y+3,x+4,y+4,0xFFFFFFFF);
        g.fill(x-3,y-2,x-2,y+3,0xFFFFFFFF); g.fill(x+3,y-2,x+4,y+3,0xFFFFFFFF);
        g.fill(x-1,y-1,x+2,y+2,0xFF000000);
    }
    @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
}
