/* Ported from ate47/AdvancedCreativeTab GuiMetaModifier, LGPL-3.0-only.
 * Modified 2026-10-26: component data, count/damage/model/trim controls and draft isolation. */
package fr.atesab.act.gui.modifier;
import dev.housingitems.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import java.util.function.Consumer;

public class GuiMetaModifier extends GuiModifier<ItemStack> {
    private ItemStack stack;
    public GuiMetaModifier(Screen parent, Consumer<ItemStack> setter, ItemStack stack) {
        super(parent, Component.literal("Meta"), setter); this.stack = stack.copy();
    }
    @Override public void init() {
        int x = width / 2 - 100, y = height / 2 - 73;
        button(x, y, 200, "Unbreakable: " + stack.has(DataComponents.UNBREAKABLE), () -> {
            if (stack.has(DataComponents.UNBREAKABLE)) stack.remove(DataComponents.UNBREAKABLE); else stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE); rebuildWidgets();
        });
        button(x, y + 21, 200, "Count / damage / repair cost", () -> open(new FormScreen(this, "Item properties", new String[]{"Count", "Damage", "Repair cost"},
                new String[]{"" + stack.getCount(), "" + stack.getDamageValue(), "" + stack.getOrDefault(DataComponents.REPAIR_COST, 0)}, values -> {
                    ItemStack next = stack.copy(); next.setCount(ItemProperties.number(values[0], 1, next.getMaxStackSize()));
                    int damage = ItemProperties.number(values[1], 0, next.getMaxDamage());
                    if (next.isDamageableItem()) next.setDamageValue(damage);
                    next.set(DataComponents.REPAIR_COST, ItemProperties.number(values[2], 0, Integer.MAX_VALUE)); stack = next;
                })));
        button(x, y + 42, 200, "Item model / armor trim", () -> SpecialEditors.appearance(this, stack, value -> stack = value));
        button(x, y + 63, 200, "Color", () -> open(new ColorScreen(this, stack, value -> stack = value)));
        button(x, y + 84, 200, "Advanced component data", () -> open(new DataScreen(this, "Item data (advanced)", ItemData.encode(stack), value -> stack = ItemData.decode(value))));
        button(x, y + 112, 100, "Done", () -> { set(stack); onClose(); });
        button(x + 101, y + 112, 99, "Cancel", this::onClose);
    }
}
