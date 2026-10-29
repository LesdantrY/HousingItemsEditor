package dev.housingitems;
import fr.atesab.act.gui.modifier.GuiModifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import java.util.*;
import java.util.function.Consumer;

public final class FireworkColorsScreen extends GuiModifier<int[]> {
    private final List<Integer> colors = new ArrayList<>();
    private int page, rows, left;
    public FireworkColorsScreen(Screen parent, String title, int[] values, Consumer<int[]> setter) {
        super(parent, Component.literal(title), setter); for (int color : values) colors.add(color);
    }
    private void pick(int index) {
        ItemStack sample = new ItemStack(Items.LEATHER_CHESTPLATE);
        sample.set(DataComponents.DYED_COLOR, new DyedItemColor(index < 0 ? 0xFF0000 : colors.get(index)));
        open(new ColorScreen(this, sample, value -> {
            var color = value.get(DataComponents.DYED_COLOR);
            if (color == null) { if (index >= 0) colors.remove(index); }
            else if (index < 0) colors.add(color.rgb()); else colors.set(index, color.rgb());
        }));
    }
    @Override protected void init() {
        left = width / 2 - 110; rows = Math.max(1, (height - 120) / 24);
        page = Math.max(0, Math.min(page, Math.max(0, (colors.size() - 1) / rows)));
        for (int r = 0; r < rows && page * rows + r < colors.size(); r++) {
            int i = page * rows + r;
            button(left + 25, 38 + r * 24, 165, String.format("#%06X", colors.get(i)), () -> pick(i));
            button(left + 194, 38 + r * 24, 26, "−", () -> { colors.remove(i); rebuildWidgets(); });
        }
        button(left, height - 69, 150, "+ Add color", () -> { if (colors.size() >= 256) throw new IllegalArgumentException("Maximum 256 colors."); pick(-1); });
        button(left + 156, height - 69, 28, "<", () -> { page--; rebuildWidgets(); }).active = page > 0;
        button(left + 190, height - 69, 30, ">", () -> { page++; rebuildWidgets(); }).active = (page + 1) * rows < colors.size();
        button(left, height - 44, 108, "Done", () -> { set(colors.stream().mapToInt(Integer::intValue).toArray()); onClose(); });
        button(left + 112, height - 44, 108, "Cancel", this::onClose);
    }
    @Override public void render(GuiGraphics g, int x, int y, float delta) {
        super.render(g, x, y, delta);
        for (int r = 0; r < rows && page * rows + r < colors.size(); r++)
            g.fill(left + 2, 40 + r * 24, left + 20, 56 + r * 24, 0xFF000000 | colors.get(page * rows + r));
    }
}

