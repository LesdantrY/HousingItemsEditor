package dev.housingitems;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CreativeCatalogTest {
    @BeforeAll static void bootstrap() { TestBootstrap.initialize(); }
    private static ItemStack potion(Item item, net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> effect) {
        var stack = new ItemStack(item); stack.set(DataComponents.POTION_CONTENTS, new PotionContents(effect)); return stack;
    }
    @Test void catalogKeepsPotionVariantsAndAllRegisteredTypes() {
        var healing = potion(Items.POTION, Potions.HEALING);
        var strength = potion(Items.POTION, Potions.STRENGTH);
        var splash = potion(Items.SPLASH_POTION, Potions.HEALING);
        var catalog = CreativeCatalog.includeMissingTypes(List.of(healing, strength, splash));
        assertEquals(2, catalog.stream().filter(s -> s.is(Items.POTION)).count());
        assertTrue(catalog.stream().anyMatch(s -> ItemStack.matches(s, splash)));
        for (Item item : BuiltInRegistries.ITEM) if (item != Items.AIR) assertTrue(catalog.stream().anyMatch(s -> s.is(item)), item.toString());
        assertFalse(catalog.stream().anyMatch(ItemStack::isEmpty));
    }
    @Test void housingFiltersVariantsWithoutMutatingUnfilteredCatalog() {
        var healing = potion(Items.POTION, Potions.HEALING);
        var splash = potion(Items.SPLASH_POTION, Potions.STRENGTH);
        var slowFalling = potion(Items.POTION, Potions.SLOW_FALLING);
        var lingering = potion(Items.LINGERING_POTION, Potions.HEALING);
        var catalog = List.of(healing, splash, slowFalling, lingering, new ItemStack(Items.NETHERITE_SWORD),
                new ItemStack(Items.SPAWNER), new ItemStack(Items.COMMAND_BLOCK), new ItemStack(Items.COMMAND_BLOCK_MINECART));
        assertEquals(List.of(healing, splash), catalog.stream().filter(HousingData::allowed).toList());
        assertEquals(8, catalog.size());
        var selected = ItemProperties.type(new ItemStack(Items.STONE), splash, true);
        assertTrue(selected.is(Items.SPLASH_POTION));
        assertEquals(splash.get(DataComponents.POTION_CONTENTS), selected.get(DataComponents.POTION_CONTENTS));
    }
    @Test void updatingIndexesAfterCatalogBuildRestoresNameAndIdSearch() {
        var trees = new SessionSearchTrees();
        assertTrue(trees.creativeNameSearch().search("stone").isEmpty());
        var list = List.of(new ItemStack(Items.STONE), potion(Items.POTION, Potions.HEALING), new ItemStack(Items.NETHERITE_SWORD));
        // An empty, bound enchantment registry is enough for these unenchanted test items.
        var registries = new ArrayList<net.minecraft.core.Registry<?>>();
        RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).registries().forEach(entry -> registries.add(entry.value()));
        var enchantments = new net.minecraft.core.MappedRegistry<net.minecraft.world.item.enchantment.Enchantment>(net.minecraft.core.registries.Registries.ENCHANTMENT, com.mojang.serialization.Lifecycle.stable());
        enchantments.freeze(); registries.add(enchantments);
        CreativeCatalog.refreshSearches(trees, new RegistryAccess.ImmutableRegistryAccess(registries), list);
        assertTrue(trees.creativeNameSearch().search("minecraft:stone").stream().anyMatch(s -> s.is(Items.STONE)));
        assertTrue(trees.creativeNameSearch().search("potion").stream().anyMatch(s -> s.is(Items.POTION)));
        assertTrue(trees.creativeNameSearch().search("netherite").stream().anyMatch(s -> s.is(Items.NETHERITE_SWORD)));
        assertDoesNotThrow(() -> trees.creativeTagSearch().search("planks"));
    }
    @Test void typePreviewKeepsPotionVariantWithoutUsingCustomModelOrMutatingDraft() {
        var draft = potion(Items.SPLASH_POTION, Potions.STRENGTH);
        var customModel = net.minecraft.resources.Identifier.parse("minecraft:diamond");
        draft.set(DataComponents.ITEM_MODEL, customModel);
        var preview = ItemProperties.typePreview(draft);
        assertTrue(preview.is(Items.SPLASH_POTION));
        assertEquals(draft.get(DataComponents.POTION_CONTENTS), preview.get(DataComponents.POTION_CONTENTS));
        assertEquals(new ItemStack(Items.SPLASH_POTION).get(DataComponents.ITEM_MODEL), preview.get(DataComponents.ITEM_MODEL));
        assertEquals(customModel, draft.get(DataComponents.ITEM_MODEL));
    }
}



