/* ACT-style explosion controls, adapted to 1.21.11 components.
 * LGPL-3.0-only; modified 2026-10-26. */
package dev.housingitems;
import fr.atesab.act.gui.modifier.GuiModifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

public final class FireworkExplosionScreen extends GuiModifier<CompoundTag> {
    private final CompoundTag explosion;
    private static final String[] SHAPES = {"small_ball", "large_ball", "star", "creeper", "burst"};
    private static final String[] LABELS = {"Small ball", "Large ball", "Star", "Creeper", "Burst"};
    private int left, top, w;
    public FireworkExplosionScreen(Screen parent, CompoundTag value, Consumer<CompoundTag> setter) {
        super(parent, Component.literal("Firework explosion"), setter); explosion = value.copy();
        if (explosion.isEmpty()) { explosion.putString("shape", "small_ball"); explosion.putIntArray("colors", new int[]{0xFF0000}); }
    }
    @Override protected void init() {
        w = Math.min(380, width - 32); left = (width - w) / 2; top = Math.max(38, height / 2 - 65);
        for (int i = 0; i < SHAPES.length; i++) {
            int n = i;
            var b = button(left + i * w / 5, top, w / 5 - 2, LABELS[i], () -> { explosion.putString("shape", SHAPES[n]); rebuildWidgets(); });
            b.active = !explosion.getString("shape").orElse("small_ball").equals(SHAPES[i]);
        }
        button(left, top + 25, w / 2 - 2, "Trail: " + (explosion.getBoolean("has_trail").orElse(false) ? "ON" : "OFF"), () -> {
            explosion.putBoolean("has_trail", !explosion.getBoolean("has_trail").orElse(false)); rebuildWidgets();
        });
        button(left + w / 2, top + 25, w / 2, "Twinkle: " + (explosion.getBoolean("has_twinkle").orElse(false) ? "ON" : "OFF"), () -> {
            explosion.putBoolean("has_twinkle", !explosion.getBoolean("has_twinkle").orElse(false)); rebuildWidgets();
        });
        button(left, top + 51, w / 2 - 2, "Colors", () -> colors("colors", "Explosion colors"));
        button(left + w / 2, top + 51, w / 2, "Fade colors", () -> colors("fade_colors", "Fade colors"));
        button(left, top + 104, w / 2 - 2, "Done", () -> { set(explosion.copy()); onClose(); });
        button(left + w / 2, top + 104, w / 2, "Cancel", this::onClose);
    }
    private void colors(String key, String title) {
        open(new FireworkColorsScreen(this, title, explosion.getIntArray(key).orElse(new int[0]), values -> explosion.putIntArray(key, values)));
    }
    @Override public void render(GuiGraphics g, int x, int y, float delta) {
        super.render(g, x, y, delta);
        for (int side = 0; side < 2; side++) {
            int[] colors = explosion.getIntArray(side == 0 ? "colors" : "fade_colors").orElse(new int[0]);
            int start = left + side * w / 2;
            for (int i = 0; i < Math.min(colors.length, (w / 2 - 8) / 14); i++)
                g.fill(start + i * 14 + 3, top + 78, start + i * 14 + 15, top + 94, 0xFF000000 | colors[i]);
            if (colors.length == 0) g.drawString(font, "None", start + 4, top + 81, 0xFFAAAAAA);
        }
    }
}

