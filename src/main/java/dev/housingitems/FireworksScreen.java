/* Interactive ACT-style explosion list; LGPL-3.0-only.
 * Modified 2026-10-26 for 1.21.11. */
package dev.housingitems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.function.Consumer;

public final class FireworksScreen extends StructuredListScreen {
    private final boolean star;
    private final boolean legacy;
    private String flight;
    private int page, rows, left, w;
    public FireworksScreen(Screen parent, ItemStack stack, Consumer<ItemStack> setter) {
        super(parent, "Fireworks", stack, setter, explosions(stack));
        star = stack.is(Items.FIREWORK_STAR);
        legacy = LegacyFireworks.needed(stack) || (mc.level != null && mc.level.getScoreboard().getObjectiveNames().stream().anyMatch(name -> name.equalsIgnoreCase("housing")));
        flight = "" + ItemProperties.compound(stack, "fireworks").getInt("flight_duration").orElse(1);
    }
    private static ListTag explosions(ItemStack stack) {
        if (!stack.is(Items.FIREWORK_STAR)) return ItemProperties.compound(stack, "fireworks").getList("explosions").orElseGet(ListTag::new);
        ListTag list = new ListTag(); var e = ItemProperties.get(stack, "firework_explosion"); if (e instanceof CompoundTag) list.add(e); return list;
    }
    @Override protected void init() {
        w = Math.min(380, width - 32); left = (width - w) / 2;
        if (!star) { var f = field(left + 104, 33, 64, legacy ? "Flight duration (0–127)" : "Flight duration (0–255)", flight); f.setMaxLength(3); f.setResponder(v -> flight = v); }
        rows = Math.max(1, (height - 145) / 25);
        page = Math.max(0, Math.min(page, Math.max(0, (entries.size() - 1) / rows)));
        for (int r = 0; r < rows && page * rows + r < entries.size(); r++) {
            int i = page * rows + r; CompoundTag e = (CompoundTag) entries.get(i);
            button(left, 64 + r * 25, w - 92, "Explosion " + (i + 1) + ": " + label(e), () -> edit(i, e.copy()));
            button(left + w - 88, 64 + r * 25, 58, "Copy", () -> { replace(-1, e.copy()); rebuildWidgets(); }).active = !star && entries.size() < 256;
            button(left + w - 26, 64 + r * 25, 26, "−", () -> { entries.remove(i); rebuildWidgets(); });
        }
        button(left, height - 68, w - 66, "+ Add explosion", () -> edit(-1, new CompoundTag())).active = entries.size() < (star ? 1 : 256);
        button(left + w - 62, height - 68, 28, "<", () -> { page--; rebuildWidgets(); }).active = page > 0;
        button(left + w - 30, height - 68, 30, ">", () -> { page++; rebuildWidgets(); }).active = (page + 1) * rows < entries.size();
        button(left, height - 43, w / 2 - 2, "Done", () -> { set(result(entries)); onClose(); });
        button(left + w / 2, height - 43, w / 2, "Cancel", this::onClose);
    }
    protected String label(CompoundTag e) { return e.getString("shape").orElse("small_ball").replace('_', ' '); }
    protected ItemStack result(ListTag values) {
        if (star) {
            if (values.size() > 1) throw new IllegalArgumentException("A firework star can contain one explosion.");
            var result = ItemProperties.set(stack, "firework_explosion", values.isEmpty() ? null : values.get(0));
            return legacy ? LegacyFireworks.sync(result, true, 0, values) : result;
        }
        CompoundTag c = ItemProperties.compound(stack, "fireworks");
        int duration = ItemProperties.number(flight, 0, legacy ? 127 : 255);
        c.putInt("flight_duration", duration); c.put("explosions", values.copy());
        var result = ItemProperties.set(stack, "fireworks", c);
        return legacy ? LegacyFireworks.sync(result, false, duration, values) : result;
    }
    protected void edit(int index, CompoundTag e) {
        open(new FireworkExplosionScreen(this, e, next -> replace(index, next)));
    }
    @Override public void render(GuiGraphics g, int x, int y, float delta) {
        super.render(g, x, y, delta);
        if (!star) g.drawString(font, legacy ? "Flight (0–127)" : "Flight (0–255)", left, 39, 0xFFFFFFFF);
    }
}


