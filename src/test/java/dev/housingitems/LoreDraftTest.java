package dev.housingitems;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoreDraftTest {
    @BeforeAll static void bootstrap() { TestBootstrap.initialize(); }

    @Test void unchangedLoreEditorPreservesExactComponentPatch() {
        for (boolean explicit : new boolean[]{false, true}) {
            var original = new ItemStack(Items.DIAMOND_SWORD);
            if (explicit) {
                original.set(DataComponents.LORE, ItemLore.EMPTY);
                original.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.UNBREAKABLE, true));
            }
            var result = LoreDraft.apply(original, FormattedText.editable(original.getHoverName()), "",
                    original.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT));
            assertTrue(ItemStack.matches(original, result));
            assertEquals(original.getComponentsPatch(), result.getComponentsPatch());
        }
    }

    @Test void editingLoreDoesNotIntroduceTooltipFlags() {
        var original = new ItemStack(Items.STONE);
        var result = LoreDraft.apply(original, FormattedText.editable(original.getHoverName()), "&7One\n\n&aTwo", TooltipDisplay.DEFAULT);
        assertEquals(3, result.get(DataComponents.LORE).lines().size());
        assertEquals(original.get(DataComponents.TOOLTIP_DISPLAY), result.get(DataComponents.TOOLTIP_DISPLAY));
    }

    @Test void resetModelRestoresTheActualItemDefault() {
        var stack = new ItemStack(Items.DIAMOND_SWORD);
        stack.set(DataComponents.ITEM_MODEL, net.minecraft.resources.Identifier.parse("minecraft:apple"));
        ItemProperties.resetModel(stack);
        assertEquals(Items.DIAMOND_SWORD.getDefaultInstance().get(DataComponents.ITEM_MODEL), stack.get(DataComponents.ITEM_MODEL));
        assertTrue(stack.is(Items.DIAMOND_SWORD));
    }

    @Test void changingTypeUsesDestinationDefaultsAndKeepsCustomModelNameAndLore() {
        var original = new ItemStack(Items.DIAMOND_SWORD);
        original.setDamageValue(20);
        original.set(DataComponents.ITEM_MODEL, net.minecraft.resources.Identifier.parse("minecraft:apple"));
        original.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Custom"));
        original.set(DataComponents.LORE, new ItemLore(java.util.List.of(net.minecraft.network.chat.Component.literal("Lore"))));
        var result = ItemProperties.type(original, "minecraft:stone", true);
        assertTrue(result.is(Items.STONE));
        assertEquals(original.get(DataComponents.ITEM_MODEL), result.get(DataComponents.ITEM_MODEL));
        assertFalse(result.has(DataComponents.DAMAGE));
        assertEquals(original.get(DataComponents.CUSTOM_NAME), result.get(DataComponents.CUSTOM_NAME));
        assertEquals(original.get(DataComponents.LORE), result.get(DataComponents.LORE));
        assertEquals(Items.STONE.getDefaultInstance().get(DataComponents.ATTRIBUTE_MODIFIERS), result.get(DataComponents.ATTRIBUTE_MODIFIERS));
    }

    @Test void housingTypeRejectsModernItemsAndConvertsLegacyAliases() {
        var original = new ItemStack(Items.STONE);
        assertThrows(IllegalArgumentException.class, () -> ItemProperties.type(original, "minecraft:netherite_sword", true));
        var converted = ItemProperties.type(original, "minecraft:white_dye", true);
        assertTrue(converted.is(Items.BONE_MEAL));
        assertTrue(ItemProperties.type(original, "minecraft:netherite_sword", false).is(Items.NETHERITE_SWORD));
    }
}

