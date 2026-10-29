package dev.housingitems;

import fr.atesab.act.gui.GuiACT;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Arrays;

/** Labeled property editor; invalid entries remain editable instead of closing. */
public final class FormScreen extends GuiACT {
    private final String[] labels, values;
    private final Apply apply;
    private final Runnable remove;
    private int top;
    @FunctionalInterface public interface Apply { void accept(String[] values) throws Exception; }
    public FormScreen(Screen parent, String title, String[] labels, String[] values, Apply apply) { this(parent, title, labels, values, apply, null); }
    public FormScreen(Screen parent, String title, String[] labels, String[] values, Apply apply, Runnable remove) {
        super(parent, Component.literal(title));
        this.labels = labels.clone(); this.values = values.clone(); this.apply = apply; this.remove = remove;
    }
    @Override protected void init() {
        top = Math.max(38, (height - (labels.length * 33 + 53)) / 2);
        int w = Math.min(340, width - 36), left = (width - w) / 2;
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            var box = field(left, top + i * 33 + 11, w, labels[i], values[i]);
            box.setResponder(text -> values[index] = text);
        }
        int y = top + labels.length * 33 + 7;
        button(left, y, w / 2 - 2, "Done", () -> { apply.accept(values.clone()); onClose(); });
        button(left + w / 2 + 2, y, w / 2 - 2, "Cancel", this::onClose);
        if (remove != null) button(left, y + 24, w, "Remove / reset", () -> { remove.run(); onClose(); });
    }
    @Override public void render(GuiGraphics g, int x, int y, float delta) {
        super.render(g, x, y, delta);
        int left = (width - Math.min(340, width - 36)) / 2;
        for (int i = 0; i < labels.length; i++) g.drawString(font, labels[i], left, top + i * 33, 0xFFAAAAAA);
    }
}
