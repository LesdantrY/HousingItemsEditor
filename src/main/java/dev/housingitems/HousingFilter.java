package dev.housingitems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public final class HousingFilter {
    public static boolean enabled;
    private static boolean initialized;
    private HousingFilter() {}
    public static void initialize() {
        if (initialized) return;
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        // Hypixel exposes a scoreboard objective named housing. No network probing.
        enabled = level.getScoreboard().getObjectiveNames().stream().anyMatch(name -> name.equalsIgnoreCase("housing"));
        initialized = true;
    }
    public static void filter(List<ItemStack> items) {
        if (!enabled) return;
        items.removeIf(stack -> !HousingData.allowed(stack));
        items.replaceAll(HousingData::housingStack);
    }
}
