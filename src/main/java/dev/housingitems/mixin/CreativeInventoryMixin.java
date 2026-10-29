package dev.housingitems.mixin;
import dev.housingitems.HousingFilter;
import dev.housingitems.CreativeCatalog;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Shadow private static CreativeModeTab selectedTab;
    @Shadow private EditBox searchBox;
    @Shadow private float scrollOffs;
    @Shadow protected abstract void selectTab(CreativeModeTab tab);
    @Shadow protected abstract void refreshSearchResults();
    protected CreativeInventoryMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); }
    @Inject(method = "init", at = @At("HEAD"))
    private void hie$initialize(CallbackInfo ci) { HousingFilter.initialize(); CreativeCatalog.rebuild(); }
    @Inject(method = "init", at = @At("TAIL"))
    private void hie$buttons(CallbackInfo ci) {
        int x = Math.max(4, leftPos), y = Math.min(height - 21, topPos + imageHeight + 32);
        var housingButton = addRenderableWidget(Button.builder(Component.literal("Housing: " + (HousingFilter.enabled ? "ON" : "OFF")), b -> {
            HousingFilter.enabled = !HousingFilter.enabled;
            b.setMessage(Component.literal("Housing: " + (HousingFilter.enabled ? "ON" : "OFF")));
            String query = searchBox.getValue(); selectTab(selectedTab);
            if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) { searchBox.setValue(query); refreshSearchResults(); }
        }).bounds(x, y, 98, 20).build());
        housingButton.setOverrideRenderHighlightedSprite(housingButton::isHovered);
    }
    @Inject(method = {"selectTab", "refreshSearchResults", "refreshCurrentTabContents"}, at = @At("RETURN"))
    private void hie$filter(CallbackInfo ci) {
        if (selectedTab.getType() == CreativeModeTab.Type.SEARCH && searchBox != null && searchBox.getValue().isEmpty()) {
            menu.items.clear(); menu.items.addAll(CreativeCatalog.all());
            scrollOffs = 0; menu.scrollTo(0);
        }
        if (!HousingFilter.enabled || selectedTab.getType() == CreativeModeTab.Type.INVENTORY || selectedTab.getType() == CreativeModeTab.Type.HOTBAR) return;
        HousingFilter.filter(menu.items); scrollOffs = 0; menu.scrollTo(0);
    }
}
