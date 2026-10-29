package dev.housingitems;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.CreativeModeTab;
final class FabricCompat {
    private FabricCompat() {}
    static KeyMapping key(KeyMapping key) { return KeyMappingHelper.registerKeyMapping(key); }
    static CreativeModeTab.Builder tab() { return FabricCreativeModeTab.builder(); }
}
