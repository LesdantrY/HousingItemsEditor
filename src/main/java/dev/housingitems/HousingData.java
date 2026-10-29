package dev.housingitems;
import com.google.gson.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Factual legacy IDs from PrismarineJS/minecraft-data (MIT); independent filter logic. */
public final class HousingData {
    private static final Set<String> ALLOWED = new HashSet<>();
    private static final Map<String, String> IDS = new HashMap<>();
    private static final Map<String, Variant> VARIANTS = new HashMap<>();
    private record Variant(String base, int data) {}
    static {
        try (var stream = Objects.requireNonNull(HousingData.class.getResourceAsStream("/assets/housing_items_editor/legacy-housing.json"));
             var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            root.getAsJsonArray("allowed").forEach(v -> ALLOWED.add(v.getAsString()));
            root.getAsJsonObject("blockIds").entrySet().forEach(e -> IDS.put(e.getKey(), e.getValue().getAsString()));
            root.getAsJsonObject("variants").entrySet().forEach(e -> { var a = e.getValue().getAsJsonArray(); VARIANTS.put("minecraft:" + e.getKey(), new Variant("minecraft:" + a.get(0).getAsString(), a.get(1).getAsInt())); });
        } catch (Exception e) { throw new IllegalStateException("Cannot read bundled legacy item data", e); }
    }
    private HousingData() {}
    private static final Set<String> HOUSING_EXCLUDED = Set.of("minecraft:spawner", "minecraft:command_block", "minecraft:command_block_minecart");
    public static boolean allowed(String id) { return ALLOWED.contains(id) && !HOUSING_EXCLUDED.contains(id); }
    private static final Set<String> LEGACY_ENCHANTMENTS = Set.of("protection", "fire_protection", "feather_falling",
            "blast_protection", "projectile_protection", "respiration", "aqua_affinity", "thorns", "depth_strider",
            "sharpness", "smite", "bane_of_arthropods", "knockback", "fire_aspect", "looting", "efficiency",
            "silk_touch", "unbreaking", "fortune", "power", "punch", "flame", "infinity", "luck_of_the_sea", "lure");
    private static final Set<String> LEGACY_POTIONS = Set.of("water", "mundane", "thick", "awkward", "night_vision",
            "invisibility", "leaping", "fire_resistance", "swiftness", "slowness", "water_breathing", "healing",
            "harming", "poison", "regeneration", "strength", "weakness");
    static boolean legacyPotion(String id) {
        if (!id.startsWith("minecraft:")) return false;
        if (id.equals("minecraft:strong_slowness")) return false;
        return LEGACY_POTIONS.contains(id.substring(10).replaceFirst("^(long_|strong_)", ""));
    }
    static boolean legacyEnchantment(String id) {
        return id.startsWith("minecraft:") && LEGACY_ENCHANTMENTS.contains(id.substring(10));
    }
    public static boolean allowed(ItemStack stack) {
        if (!allowed(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) return false;
        var potion = stack.get(DataComponents.POTION_CONTENTS);
        if (potion != null && potion.potion().isPresent()) {
            var key = potion.potion().get().unwrapKey();
            if (key.isEmpty() || !legacyPotion(key.get().identifier().toString())) return false;
        }
        var enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchantments != null) for (var enchantment : enchantments.keySet()) {
            var key = enchantment.unwrapKey();
            if (key.isEmpty() || !legacyEnchantment(key.get().identifier().toString())) return false;
        }
        return true;
    }
    public static String legacyId(String id) { return IDS.get(id); }
    public static ItemStack housingStack(ItemStack original) {
        String id = BuiltInRegistries.ITEM.getKey(original.getItem()).toString();
        Variant variant = VARIANTS.get(id); if (variant == null) return original;
        ItemStack next = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(variant.base())).builtInRegistryHolder(), original.getCount(), original.getComponentsPatch());
        next.set(DataComponents.ITEM_MODEL, Identifier.parse(id));
        next.set(DataComponents.ITEM_NAME, original.getItem().getName());
        if (variant.data() >= 0) {
            var lore = new ArrayList<>(original.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines());
            lore.add(Component.literal("Data Value: " + variant.data()).withStyle(ChatFormatting.GRAY));
            next.set(DataComponents.LORE, new ItemLore(lore));
        }
        return next;
    }
    public static void tooltip(ItemStack stack, List<Component> lines) {
        if (!HousingFilter.enabled || lines.isEmpty()) return;
        var model = stack.get(DataComponents.ITEM_MODEL);
        String name = model != null && VARIANTS.containsKey(model.toString()) ? model.toString() : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        String id = legacyId(name);
        if (id != null) lines.set(0, lines.getFirst().copy().append(Component.literal(" [" + id + "]").withStyle(ChatFormatting.DARK_GRAY)));
    }
}
