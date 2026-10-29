/* Derived from ate47/AdvancedCreativeTab GuiItemStackModifier, LGPL-3.0-only.
 * Modified 2026-10-26: 1.21.11 components and registries, contextual modern editors;
 * retained two-column layout and setter/Done/Cancel flow. */
package fr.atesab.act.gui.modifier;
import dev.housingitems.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemLore;
import java.util.function.Consumer;

public class GuiItemStackModifier extends GuiModifier<ItemStack> {
    private ItemStack currentItemStack;
    public GuiItemStackModifier(Screen parent, ItemStack currentItemStack, Consumer<ItemStack> setter) {
        super(parent, Component.literal("Item Editor"), setter);
        this.currentItemStack = currentItemStack == null || currentItemStack.isEmpty() ? new ItemStack(Items.STONE) : currentItemStack.copy();
    }
    public void update(ItemStack stack) { currentItemStack = stack.copy(); }
    @Override public void init() {
        int x = width / 2 - 100, y = height / 2 - 42;
        button(x, y, 100, "Name", () -> {
            String old = FormattedText.editable(currentItemStack.getHoverName());
            open(new GuiStringModifier(this, Component.literal("Name"), old, value -> {
                if (value.equals(old)) return;
                if (value.isBlank()) currentItemStack.remove(DataComponents.CUSTOM_NAME);
                else currentItemStack.set(DataComponents.CUSTOM_NAME, FormattedText.parse(value));
            }));
        });
        button(x + 101, y, 99, "Lore", () -> open(new LoreEditorScreen(this, currentItemStack, this::update)));
        button(x, y + 21, 100, "Enchantments", () -> open(new EnchantmentScreen(this, currentItemStack, this::update, false)));
        button(x + 101, y + 21, 99, "Attributes", () -> open(new AttributeScreen(this, currentItemStack, this::update)));
        button(x, y + 42, 100, "Type", () -> open(new ItemPickerScreen(this, id -> update(ItemProperties.type(currentItemStack, id)), false)));
        button(x + 101, y + 42, 99, "Meta", () -> open(new GuiMetaModifier(this, this::update, currentItemStack)));
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(currentItemStack.getItem()).getPath();
        int row = y + 63;
        if (id.startsWith("leather_")) button(x, row, 200, "Set color", () -> open(new ColorScreen(this, currentItemStack, this::update)));
        else if (currentItemStack.is(Items.ENCHANTED_BOOK)) button(x, row, 200, "Stored enchantments", () -> open(new EnchantmentScreen(this, currentItemStack, this::update, true)));
        else if (id.contains("potion") || currentItemStack.is(Items.TIPPED_ARROW)) button(x, row, 200, "Potion effects", () -> open(new PotionScreen(this, currentItemStack, this::update)));
        else if (currentItemStack.is(Items.PLAYER_HEAD)) button(x, row, 200, "Player head", () -> SpecialEditors.head(this, currentItemStack, this::update));
        else if (id.contains("command_block")) button(x, row, 200, "Command block", () -> SpecialEditors.command(this, currentItemStack, this::update));
        else if (id.startsWith("firework")) button(x, row, 200, "Fireworks", () -> open(new FireworksScreen(this, currentItemStack, this::update)));
        else if (id.contains("shulker_box") || id.equals("chest") || id.equals("barrel") || currentItemStack.has(DataComponents.CONTAINER)) button(x, row, 200, "Inventory", () -> open(new ContainerScreen(this, currentItemStack, this::update)));
        else if (currentItemStack.is(Items.LIGHT)) button(x, row, 200, "Light level", () -> SpecialEditors.light(this, currentItemStack, this::update));
        else button(x, row, 200, "Item model / armor trim", () -> SpecialEditors.appearance(this, currentItemStack, this::update));
        button(x, row + 25, 100, "Done", () -> { set(ItemData.decode(ItemData.encode(currentItemStack))); onClose(); });
        button(x + 101, row + 25, 99, "Cancel", this::onClose);
    }
    @Override public void render(GuiGraphics g, int x, int y, float delta) {
        super.render(g, x, y, delta); item(g, currentItemStack, width / 2 - 8, height / 2 - 65, x, y);
    }
}

