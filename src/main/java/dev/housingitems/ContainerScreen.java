package dev.housingitems;
import fr.atesab.act.gui.modifier.GuiModifier;
import fr.atesab.act.gui.modifier.GuiItemStackModifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import java.util.*;
import java.util.function.Consumer;

public final class ContainerScreen extends GuiModifier<ItemStack> {
    private final ItemStack stack;
    private final List<ItemStack> items;
    private int page;
    private boolean clear;
    public ContainerScreen(Screen parent, ItemStack stack, Consumer<ItemStack> setter) {
        super(parent, Component.literal("Inventory"), setter); this.stack = stack.copy();
        items = new ArrayList<>(stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).stream().map(ItemStack::copy).toList());
        while (items.size() < 27 || items.size() % 27 != 0) items.add(ItemStack.EMPTY);
    }
    @Override protected void init() {
        int x = width / 2 - 108, y = height / 2 - 47;
        for (int i = 0; i < 27; i++) {
            final int slot = page * 27 + i;
            button(x + i % 9 * 24, y + i / 9 * 24, 22, "", () -> {
                if (clear) { items.set(slot, ItemStack.EMPTY); return; }
                if (items.get(slot).isEmpty()) open(new ItemPickerScreen(this, id -> items.set(slot, new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)))), false));
                else open(new GuiItemStackModifier(this, items.get(slot), value -> items.set(slot, value.copy())));
            });
        }
        button(x, y + 77, 216, clear ? "Clear slots: ON" : "Clear slots: OFF", () -> { clear = !clear; rebuildWidgets(); });
        button(x, y + 101, 25, "<", () -> { page--; rebuildWidgets(); }).active = page > 0;
        button(x + 29, y + 101, 25, ">", () -> { page++; rebuildWidgets(); }).active = (page + 1) * 27 < items.size();
        button(x + 58, y + 101, 76, "Done", () -> { ItemStack next = stack.copy(); next.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items)); set(ItemData.decode(ItemData.encode(next))); onClose(); });
        button(x + 138, y + 101, 78, "Cancel", this::onClose);
    }
    @Override public void render(GuiGraphics g, int x, int y, float delta) {
        super.render(g, x, y, delta);
        for (int i = 0; i < 27; i++) item(g, items.get(page * 27 + i), width / 2 - 105 + i % 9 * 24, height / 2 - 45 + i / 9 * 24, x, y);
    }
}
