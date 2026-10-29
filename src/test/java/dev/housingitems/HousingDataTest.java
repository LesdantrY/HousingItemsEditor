package dev.housingitems;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HousingDataTest {
    @Test void housingAllowsLegacyVariantsButNotModernBuildingBlocks() {
        assertTrue(HousingData.allowed("minecraft:smooth_stone"));
        assertTrue(HousingData.allowed("minecraft:oak_wood"));
        assertTrue(HousingData.allowed("minecraft:nether_bricks"));
        assertTrue(HousingData.allowed("minecraft:short_grass"));
        assertFalse(HousingData.allowed("minecraft:bamboo_block"));
        assertFalse(HousingData.allowed("minecraft:cherry_planks"));
        assertFalse(HousingData.allowed("minecraft:command_block"));
        assertFalse(HousingData.allowed("minecraft:command_block_minecart"));
        assertFalse(HousingData.allowed("minecraft:spawner"));
    }
    @Test void legacySpawnEggsAreCompleteWithoutNewerMobs() {
        for (String mob : new String[]{"creeper","skeleton","spider","zombie","slime","ghast","zombified_piglin",
                "enderman","cave_spider","silverfish","blaze","magma_cube","bat","witch","endermite","guardian",
                "pig","sheep","cow","chicken","squid","wolf","mooshroom","ocelot","horse","rabbit","villager"})
            assertTrue(HousingData.allowed("minecraft:" + mob + "_spawn_egg"), mob);
        for (String mob : new String[]{"shulker","polar_bear","allay","warden","sniffer"})
            assertFalse(HousingData.allowed("minecraft:" + mob + "_spawn_egg"), mob);
        assertTrue(HousingData.allowed("minecraft:red_bed"));
        assertFalse(HousingData.allowed("minecraft:white_bed"));
    }
    @Test void slabAndDoubleSlabHaveDifferentProtoolIds() {
        assertEquals("44:0", HousingData.legacyId("minecraft:smooth_stone_slab"));
        assertEquals("43:8", HousingData.legacyId("minecraft:smooth_stone"));
        assertEquals("17:12", HousingData.legacyId("minecraft:oak_wood"));
        assertEquals("35:14", HousingData.legacyId("minecraft:red_wool"));
    }
    @Test void newerPotionAndBookVariantsAreNotLegacy() {
        assertTrue(HousingData.legacyPotion("minecraft:long_swiftness"));
        assertTrue(HousingData.legacyPotion("minecraft:strong_healing"));
        assertFalse(HousingData.legacyPotion("minecraft:slow_falling"));
        assertFalse(HousingData.legacyPotion("minecraft:strong_slowness"));
        assertFalse(HousingData.legacyPotion("minecraft:wind_charged"));
        assertTrue(HousingData.legacyEnchantment("minecraft:depth_strider"));
        assertFalse(HousingData.legacyEnchantment("minecraft:mending"));
    }
}

