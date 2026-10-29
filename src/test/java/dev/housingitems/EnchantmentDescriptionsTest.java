package dev.housingitems;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnchantmentDescriptionsTest {
    @Test void everyVanillaEnchantmentHasAnEffectDescription() {
        TestBootstrap.initialize();
        var registry = TestBootstrap.registries().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        registry.listElements().forEach(holder -> {
            String id = holder.key().identifier().toString();
            assertFalse(EnchantmentDescriptions.describe(id).contains("not described here"), id);
        });
    }
    @Test void customNamespaceDoesNotBorrowVanillaEffectText() {
        assertTrue(EnchantmentDescriptions.describe("custom:sharpness").contains("not described here"));
        assertTrue(EnchantmentDescriptions.describe("minecraft:future_enchantment").contains("not described here"));
    }
}

