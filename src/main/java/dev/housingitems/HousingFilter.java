package dev.housingitems;
import net.minecraft.client.Minecraft;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.ItemStack;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.List;

public final class HousingFilter {
    public static boolean enabled;
    private static boolean showButton = true;
    private static boolean initialized;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("Housing Items Editor");
    private HousingFilter() {}
    private static Path settingsPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("housing-items-editor").resolve("settings.properties");
    }
    public static boolean showButton() { return showButton; }
    public static void setShowButton(boolean show) {
        showButton = show;
        Path settings = settingsPath();
        Properties properties = new Properties();
        properties.setProperty("showHousingButton", Boolean.toString(show));
        try {
            Files.createDirectories(settings.getParent());
            try (var writer = Files.newBufferedWriter(settings)) { properties.store(writer, "Housing Items Editor settings"); }
        } catch (IOException e) {
            LOGGER.error("Cannot save Housing button visibility setting", e);
        }
    }
    public static void loadSettings() {
        Path settings = settingsPath();
        if (!Files.exists(settings)) return;
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(settings)) {
            properties.load(reader);
            String value = properties.getProperty("showHousingButton");
            if (value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")))
                showButton = Boolean.parseBoolean(value);
            else if (value != null) LOGGER.warn("Ignoring invalid showHousingButton setting: {}", value);
        } catch (IOException e) {
            LOGGER.error("Cannot read Housing Items Editor settings", e);
        }
    }
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
