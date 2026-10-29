package dev.housingitems;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemModelPreservationTest {
    @BeforeAll static void bootstrap() { TestBootstrap.initialize(); }
    @Test void customModelSurvivesTypeChangeWithEitherHousingSetting() {
        for (boolean housing : new boolean[]{false, true}) {
            var original = new ItemStack(Items.STONE);
            var model = Identifier.parse("minecraft:diamond");
            original.set(DataComponents.ITEM_MODEL, model);
            var result = ItemProperties.type(original, new ItemStack(Items.DIAMOND_SWORD), housing);
            assertTrue(result.is(Items.DIAMOND_SWORD));
            assertEquals(model, result.get(DataComponents.ITEM_MODEL));
            assertTrue(original.is(Items.STONE));
        }
    }
    @Test void ordinaryTypeChangeUsesDestinationDefaultModel() {
        var result = ItemProperties.type(new ItemStack(Items.STONE), "minecraft:diamond_sword", false);
        assertEquals(new ItemStack(Items.DIAMOND_SWORD).get(DataComponents.ITEM_MODEL), result.get(DataComponents.ITEM_MODEL));
    }
}

