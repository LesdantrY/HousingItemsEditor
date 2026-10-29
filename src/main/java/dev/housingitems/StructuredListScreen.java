package dev.housingitems;
import fr.atesab.act.gui.modifier.GuiModifier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.function.Consumer;

/** Structured, paginated component lists; editing a row preserves unrelated fields. */
public abstract class StructuredListScreen extends GuiModifier<ItemStack> {
    protected final ItemStack stack;
    protected final ListTag entries;
    private int page;
    protected StructuredListScreen(Screen parent, String title, ItemStack stack, Consumer<ItemStack> setter, ListTag entries) {
        super(parent, Component.literal(title), setter); this.stack = stack.copy(); this.entries = entries.copy();
    }
    protected abstract String label(CompoundTag entry);
    protected abstract void edit(int index, CompoundTag entry);
    protected abstract ItemStack result(ListTag list);
    protected void replace(int index, CompoundTag entry) {
        ListTag next = entries.copy(); if (index < 0) next.add(entry); else next.set(index, entry);
        result(next); // validate before mutating the draft
        if (index < 0) entries.add(entry); else entries.set(index, entry);
    }
    @Override protected void init() {
        int rows = Math.max(1, (height - 112) / 24), w = Math.min(380, width - 24), x = (width - w) / 2;
        page = Math.max(0, Math.min(page, Math.max(0, (entries.size() - 1) / rows)));
        for (int row = 0; row < rows && page * rows + row < entries.size(); row++) {
            int i = page * rows + row; CompoundTag entry = (CompoundTag)entries.get(i);
            button(x, 38 + row * 24, w - 28, font.plainSubstrByWidth(label(entry), w - 36), () -> edit(i, entry.copy()));
            button(x + w - 24, 38 + row * 24, 24, "−", () -> { entries.remove(i); rebuildWidgets(); });
        }
        button(x, height - 67, w - 64, "+ Add", () -> edit(-1, new CompoundTag()));
        button(x + w - 60, height - 67, 28, "<", () -> { page--; rebuildWidgets(); }).active = page > 0;
        button(x + w - 28, height - 67, 28, ">", () -> { page++; rebuildWidgets(); }).active = (page + 1) * rows < entries.size();
        button(x, height - 42, w / 2 - 1, "Done", () -> { set(result(entries)); onClose(); });
        button(x + w / 2 + 1, height - 42, w / 2 - 1, "Cancel", this::onClose);
    }
    protected static ListTag list(ItemStack stack, String key) {
        Tag value = ItemProperties.get(stack, key); return value instanceof ListTag list ? list : new ListTag();
    }
}
