package dev.housingitems;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import java.util.*;

/** Unfiltered creative stacks, including component variants and registry-only item types. */
public final class CreativeCatalog {
    private CreativeCatalog() {}
    public static void rebuild() {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getConnection() == null) return;
        CreativeModeTabs.tryRebuildTabContents(mc.level.enabledFeatures(), mc.player.canUseGameMasterBlocks() && mc.options.operatorItemsTab().get(), mc.level.registryAccess());
        // Rebuilding tabs outside the vanilla creative screen must also refresh both indexes.
        var items = all();
        refreshSearches(mc.getConnection().searchTrees(), mc.level.registryAccess(), items);
    }
    static void refreshSearches(net.minecraft.client.multiplayer.SessionSearchTrees trees, net.minecraft.core.HolderLookup.Provider registries, List<ItemStack> items) {
        trees.updateCreativeTooltips(registries, items);
        trees.updateCreativeTags(items);
    }
    public static List<ItemStack> all() { return includeMissingTypes(CreativeModeTabs.searchTab().getDisplayItems()); }
    static List<ItemStack> includeMissingTypes(Collection<ItemStack> creative) {
        List<ItemStack> result = new ArrayList<>(creative);
        Set<Item> present = new HashSet<>();
        for (var stack : creative) present.add(stack.getItem());
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR && !present.contains(item)) result.add(item.getDefaultInstance());
        }
        return List.copyOf(result);
    }
}

