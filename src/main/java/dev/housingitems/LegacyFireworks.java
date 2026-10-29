package dev.housingitems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Keeps old firework NBT in translator-backed custom data consistent with edited components. */
final class LegacyFireworks {
    static boolean needed(ItemStack stack) {
        var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return HousingFilter.enabled || data.contains("Fireworks") || data.contains("Explosion")
            || data.keySet().stream().anyMatch(key -> key.startsWith("VV|") || key.startsWith("VB|"));
    }

    static ItemStack sync(ItemStack stack, boolean star, int flight, ListTag explosions) {
        var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (star) {
            if (explosions.isEmpty()) data.remove("Explosion");
            else data.put("Explosion", explosion((CompoundTag) explosions.get(0)));
        } else {
            if (flight < 0 || flight > 127) throw new IllegalArgumentException("Legacy firework flight must be 0–127.");
            CompoundTag fireworks = new CompoundTag();
            fireworks.putByte("Flight", (byte) flight);
            ListTag legacy = new ListTag();
            for (var value : explosions) legacy.add(explosion((CompoundTag) value));
            fireworks.put("Explosions", legacy);
            data.put("Fireworks", fireworks);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    static CompoundTag explosion(CompoundTag modern) {
        int shape = switch (modern.getString("shape").orElse("small_ball")) {
            case "small_ball" -> 0;
            case "large_ball" -> 1;
            case "star" -> 2;
            case "creeper" -> 3;
            case "burst" -> 4;
            default -> throw new IllegalArgumentException("Unknown firework shape.");
        };
        CompoundTag legacy = new CompoundTag();
        legacy.putByte("Type", (byte) shape);
        legacy.putIntArray("Colors", modern.getIntArray("colors").orElse(new int[0]));
        legacy.putIntArray("FadeColors", modern.getIntArray("fade_colors").orElse(new int[0]));
        legacy.putBoolean("Trail", modern.getBoolean("has_trail").orElse(false));
        legacy.putBoolean("Flicker", modern.getBoolean("has_twinkle").orElse(false));
        return legacy;
    }
}
