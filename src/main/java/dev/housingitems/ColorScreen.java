package dev.housingitems;
import fr.atesab.act.gui.modifier.GuiModifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import java.util.function.Consumer;

public final class ColorScreen extends GuiModifier<ItemStack> {
    private final ItemStack stack;
    private int color, left, top, panelWidth, boxHeight;
    private final float[] hsv = new float[3];
    private static boolean rememberedGradient = true;
    private boolean gradient = rememberedGradient, useDefault;
    private EditBox hex;
    public ColorScreen(Screen parent, ItemStack stack, Consumer<ItemStack> setter) {
        super(parent, Component.literal("Item Color"), setter); this.stack = stack.copy();
        var dye = stack.get(DataComponents.DYED_COLOR); setColor(dye == null ? 0xA06540 : dye.rgb()); useDefault = dye == null;
    }
    private void colorChanged(int rgb) { useDefault = false; color = rgb; if (hex != null) hex.setValue(String.format("%06X", color)); }
    private void setColor(int rgb) {
        java.awt.Color.RGBtoHSB((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, hsv);
        colorChanged(rgb);
    }
    @Override protected void init() {
        initEditorTabs("Item Color");
        panelWidth = Math.min(340, editorWidth() - 24); left = editorLeft() + (editorWidth()-panelWidth)/2;
        boxHeight = Math.max(66, Math.min(140, height-174));
        top = Math.max(28, (height-boxHeight-124)/2);
        button(left, top, panelWidth/2, gradient ? "Picker: Gradient" : "Picker: RGB", () -> {
            setColor(ItemProperties.color(hex.getValue())); gradient = !gradient; rememberedGradient = gradient; rebuildWidgets();
        });
        if (gradient) {
            addRenderableWidget(new GradientColorWidget(left, top+26, panelWidth, boxHeight, hsv, this::colorChanged));
            addRenderableWidget(new AbstractSliderButton(left, top+boxHeight+32, panelWidth, 20, Component.literal("Hue: " + Math.round(hsv[0]*360) + "°"), hsv[0]) {
                protected void updateMessage() { setMessage(Component.literal("Hue: " + Math.round(value*360) + "°")); }
                protected void applyValue() { hsv[0] = (float)value; colorChanged(java.awt.Color.HSBtoRGB(hsv[0],hsv[1],hsv[2]) & 0xFFFFFF); }
            });
        } else {
            for (int i=0;i<3;i++) {
                final int shift=(2-i)*8; final String label=new String[]{"Red","Green","Blue"}[i];
                addRenderableWidget(new AbstractSliderButton(left, top+26+i*23, panelWidth, 20, Component.literal(label+": "+((color>>shift)&255)), ((color>>shift)&255)/255d) {
                    protected void updateMessage() { setMessage(Component.literal(label+": "+Math.round(value*255))); }
                    protected void applyValue() { setColor((color & ~(255<<shift)) | ((int)Math.round(value*255)<<shift)); }
                });
            }
        }
        hex = field(left, top+boxHeight+58, panelWidth-90, "RGB hex color", String.format("%06X",color));
        button(left+panelWidth-86, top+boxHeight+58, 86, "Set hex", () -> { setColor(ItemProperties.color(hex.getValue())); rebuildWidgets(); });
        for (int i=0;i<16;i++) {
            final DyeColor dye=DyeColor.values()[i];
            button(left+i*panelWidth/16, top+boxHeight+83, panelWidth/16, "", () -> { setColor(dye.getFireworkColor()); rebuildWidgets(); });
        }
        int third=panelWidth/3;
        button(left, top+boxHeight+107, third-2, "Done", () -> {
            color=ItemProperties.color(hex.getValue()); ItemStack next=stack.copy(); if (useDefault && color == 0xA06540) next.remove(DataComponents.DYED_COLOR); else next.set(DataComponents.DYED_COLOR,new DyedItemColor(color)); set(next); onClose();
        });
        button(left+third, top+boxHeight+107, third-2, "Default", () -> { setColor(0xA06540); useDefault = true; rebuildWidgets(); });
        button(left+2*third, top+boxHeight+107, panelWidth-2*third, "Cancel", this::onClose);
    }
    @Override protected int titleY() { return top-22; }
    @Override protected ItemStack editorPreview() {
        ItemStack preview = stack.copy();
        if (useDefault) preview.remove(DataComponents.DYED_COLOR);
        else preview.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        return preview;
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt) {
        super.render(g,mx,my,dt);
        g.fill(left+panelWidth/2+6,top,left+panelWidth-24,top+20,0xFF000000|color);
        ItemStack preview=stack.copy(); preview.set(DataComponents.DYED_COLOR,new DyedItemColor(color));
        item(g,preview,left+panelWidth-20,top+2,mx,my);
        for(int i=0;i<16;i++) g.fill(left+i*panelWidth/16+2,top+boxHeight+86,left+(i+1)*panelWidth/16-2,top+boxHeight+100,0xFF000000|DyeColor.values()[i].getFireworkColor());
    }
}


