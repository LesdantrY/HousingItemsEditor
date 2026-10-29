package dev.housingitems;
import fr.atesab.act.gui.modifier.GuiModifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import java.util.function.Consumer;

public final class AppearanceScreen extends GuiModifier<ItemStack> {
    private ItemStack stack;
    public AppearanceScreen(Screen parent, ItemStack stack, Consumer<ItemStack> setter) {
        super(parent, Component.literal("Item appearance"), setter); this.stack = stack.copy();
    }
    @Override protected void init() {
        int x = width / 2 - 100, y = height / 2 - 35;
        button(x, y, 200, "Select item model", () -> open(new ItemPickerScreen(this, id -> stack.set(DataComponents.ITEM_MODEL, Identifier.parse(id)), HousingFilter.enabled, "Select item model")));
        button(x, y + 23, 200, "Reset item model", () -> { stack.remove(DataComponents.ITEM_MODEL); rebuildWidgets(); });
        if (stack.is(net.minecraft.tags.ItemTags.TRIMMABLE_ARMOR)) button(x, y + 46, 200, "Armor trim", () -> {
            CompoundTag trim = ItemProperties.compound(stack, "trim");
            open(new FormScreen(this, "Armor trim", new String[]{"Pattern (blank removes trim)", "Material"}, new String[]{trim.getString("pattern").orElse(""), trim.getString("material").orElse("minecraft:quartz")}, v -> {
                if (v[0].isBlank()) stack = ItemProperties.set(stack, "trim", null);
                else { CompoundTag t = new CompoundTag(); t.putString("pattern", v[0]); t.putString("material", v[1]); stack = ItemProperties.set(stack, "trim", t); }
            }));
        });
        button(x, y + 72, 99, "Done", () -> { set(stack); onClose(); });
        button(x + 101, y + 72, 99, "Cancel", this::onClose);
    }
    @Override public void render(GuiGraphics g, int x, int y, float delta) {
        super.render(g, x, y, delta); item(g, stack, width / 2 - 8, height / 2 - 62, x, y);
    }
}

