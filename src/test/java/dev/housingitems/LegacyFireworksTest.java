package dev.housingitems;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LegacyFireworksTest {
    @BeforeAll static void bootstrap() { TestBootstrap.initialize(); }
    @Test void allShapesAndEffectsConvertToLegacyFields() {
        String[] shapes = {"small_ball", "large_ball", "star", "creeper", "burst"};
        for (int i = 0; i < shapes.length; i++) {
            CompoundTag modern = new CompoundTag();
            modern.putString("shape", shapes[i]);
            modern.putIntArray("colors", new int[]{0xFF0000, 0x00FF00});
            modern.putIntArray("fade_colors", new int[]{0x0000FF});
            modern.putBoolean("has_trail", true); modern.putBoolean("has_twinkle", true);
            CompoundTag legacy = LegacyFireworks.explosion(modern);
            assertEquals(i, legacy.getByte("Type").orElseThrow().intValue());
            assertArrayEquals(new int[]{0xFF0000, 0x00FF00}, legacy.getIntArray("Colors").orElseThrow());
            assertArrayEquals(new int[]{0x0000FF}, legacy.getIntArray("FadeColors").orElseThrow());
            assertTrue(legacy.getBoolean("Trail").orElseThrow());
            assertTrue(legacy.getBoolean("Flicker").orElseThrow());
        }
    }
    @Test void staleRocketBackupIsReplacedAndOtherCustomDataSurvives() {
        var rocket = Items.FIREWORK_ROCKET.getDefaultInstance();
        CompoundTag custom = new CompoundTag();
        custom.putString("server_data", "keep"); custom.put("Fireworks", new CompoundTag());
        rocket.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
        assertTrue(LegacyFireworks.needed(rocket));
        ListTag explosions = new ListTag(); explosions.add(new CompoundTag());
        var updated = LegacyFireworks.sync(rocket, false, 3, explosions).get(DataComponents.CUSTOM_DATA).copyTag();
        assertEquals("keep", updated.getString("server_data").orElseThrow());
        var fireworks = updated.getCompound("Fireworks").orElseThrow();
        assertEquals(3, fireworks.getByte("Flight").orElseThrow().intValue());
        assertEquals(1, fireworks.getList("Explosions").orElseThrow().size());
        assertThrows(IllegalArgumentException.class, () -> LegacyFireworks.sync(rocket, false, 128, explosions));
    }
    @Test void removingStarExplosionAlsoRemovesLegacyBackup() {
        var star = Items.FIREWORK_STAR.getDefaultInstance();
        CompoundTag custom = new CompoundTag(); custom.put("Explosion", new CompoundTag());
        star.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
        LegacyFireworks.sync(star, true, 0, new ListTag());
        assertFalse(star.get(DataComponents.CUSTOM_DATA).copyTag().contains("Explosion"));
    }
}


