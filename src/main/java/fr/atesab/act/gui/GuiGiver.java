/* Derived from ate47/AdvancedCreativeTab, LGPL-3.0-only. */
package fr.atesab.act.gui;
import dev.housingitems.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;

/** Shared draft and commit target for the tabbed editors. */
public class GuiGiver extends GuiACT {
    private static String lastTab = "Lore";
    private ItemStack currentItemStack;
    private Identifier selectedModel;
    private ItemStack selectedModelVariant = ItemStack.EMPTY;
    private final ItemStack original;
    private final int originalSlot;
    public GuiGiver(Screen parent, ItemStack itemStack) {
        super(parent, Component.literal("Housing Items Editor"));
        currentItemStack = itemStack.isEmpty() ? new ItemStack(Items.STONE) : itemStack.copy();
        original = mc.player.getMainHandItem().copy();
        originalSlot = mc.player.getInventory().getSelectedSlot();
    }
    public void setCurrent(ItemStack stack) { currentItemStack = stack.copy(); }
    public ItemStack currentItem() { return currentItemStack.copy(); }
    private void selectType(ItemStack selection, boolean housing) {
        ItemStack next = ItemProperties.type(currentItemStack, selection, housing);
        if (selectedModel != null) next.set(DataComponents.ITEM_MODEL, selectedModel);
        setCurrent(next);
    }
    @Override public void init() {
        mc.execute(() -> { if (mc.screen == this) reopenLastMenu(); });
    }
    public void openTab(String tab) {
        if (!tab.equals("Library")) lastTab = tab;
        switch (tab) {
            case "Enchantments" -> open(new EnchantmentScreen(this,currentItemStack,this::setCurrent,currentItemStack.is(Items.ENCHANTED_BOOK)));
            case "Item Model" -> open(new ItemPickerScreen(this,id -> { selectedModel = Identifier.parse(id); currentItemStack.set(DataComponents.ITEM_MODEL,selectedModel); },false,"Select item model").withCurrent(() -> currentItemStack).withModelVariant(stack -> selectedModelVariant = stack, () -> selectedModelVariant).withReset(() -> { selectedModel = null; selectedModelVariant = ItemStack.EMPTY; ItemProperties.resetModel(currentItemStack); }));
            case "Item Type" -> open(new ItemPickerScreen(this,this::selectType,true).withCurrent(() -> currentItemStack).withAdvancedData(() -> currentItemStack,this::setCurrent));
            case "Item Color" -> open(new ColorScreen(this,currentItemStack,this::setCurrent));
            case "Library" -> open(new LibraryScreen(this));
            default -> open(new LoreEditorScreen(this,currentItemStack,this::setCurrent));
        }
    }
    public void saveToLibrary() throws Exception { ItemLibrary.save(currentItemStack.getHoverName().getString(),ItemData.encode(currentItemStack)); }
    public void reopenLastMenu() { openTab(lastTab); }
    public static void rememberMainMenu() {}
    @Override public void complete() throws Exception {
        if (ItemStack.matches(currentItemStack,original)) { mc.setScreen(null); return; }
        ItemStack draft=ItemData.decode(ItemData.encode(currentItemStack));
        CreativeAccess.run(() -> { ItemData.replaceHeld(draft,originalSlot,original); mc.setScreen(null); });
    }
    @Override public void onClose() { CreativeAccess.cancel(); mc.setScreen(null); }
}
