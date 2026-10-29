package dev.housingitems;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.CreativeModeTab;
final class FabricCompat {
    private FabricCompat() {}
    static KeyMapping key(KeyMapping key) { return KeyBindingHelper.registerKeyBinding(key); }
    static CreativeModeTab.Builder tab() { return FabricItemGroup.builder(); }
}
