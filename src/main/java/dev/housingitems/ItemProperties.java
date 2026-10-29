package dev.housingitems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class ItemProperties {
    private ItemProperties() {}
    public static Tag get(ItemStack stack, String key) {
        try { return TagParser.parseCompoundFully(ItemData.encode(stack)).getCompound("components").orElseGet(CompoundTag::new).get("minecraft:" + key); }
        catch (Exception e) { throw new IllegalArgumentException(e.getMessage(), e); }
    }
    public static ItemStack set(ItemStack stack, String key, Tag value) {
        try {
            CompoundTag root = TagParser.parseCompoundFully(ItemData.encode(stack));
            CompoundTag parts = root.getCompound("components").orElseGet(CompoundTag::new);
            if (value == null) parts.remove("minecraft:" + key); else parts.put("minecraft:" + key, value);
            root.put("components", parts); return ItemData.decode(root.toString());
        } catch (Exception e) { throw new IllegalArgumentException(e.getMessage(), e); }
    }
    public static ItemStack type(ItemStack stack, String id) {
        return type(stack, id, HousingFilter.enabled);
    }
    public static ItemStack type(ItemStack stack, String id, boolean housing) {
        Identifier identifier = Identifier.parse(id);
        if (!BuiltInRegistries.ITEM.containsKey(identifier) || identifier.getPath().equals("air")) throw new IllegalArgumentException("Unknown item: " + id);
        if (housing && !HousingData.allowed(id)) throw new IllegalArgumentException("This item did not exist in 1.8.9. Choose a Housing-compatible item.");
        if (stack.is(BuiltInRegistries.ITEM.getValue(identifier))) return stack.copy();
        // Start with the destination defaults, not the previous item's component patch.
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.getValue(identifier), stack.getCount());
        copyComponent(stack, result, DataComponents.CUSTOM_NAME);
        copyComponent(stack, result, DataComponents.LORE);
        copyComponent(stack, result, DataComponents.ENCHANTMENTS);
        copyComponent(stack, result, DataComponents.UNBREAKABLE);
        copyComponent(stack, result, DataComponents.TOOLTIP_DISPLAY);
        copyComponent(stack, result, DataComponents.CUSTOM_DATA);
        result.setCount(Math.min(result.getCount(), result.getMaxStackSize()));
        if (result.getMaxDamage() <= 0 && result.has(DataComponents.UNBREAKABLE)) result.remove(DataComponents.UNBREAKABLE);
        result = housing ? HousingData.housingStack(result) : result;
        // Preserve a custom appearance, but let ordinary items acquire their new type's default model.
        var model = stack.get(DataComponents.ITEM_MODEL);
        var defaultModel = stack.getItem().getDefaultInstance().get(DataComponents.ITEM_MODEL);
        if (model != null && !java.util.Objects.equals(model, defaultModel)) result.set(DataComponents.ITEM_MODEL, model);
        return result;
    }
    public static ItemStack type(ItemStack stack, ItemStack selection, boolean housing) {
        if (housing && !HousingData.allowed(selection)) throw new IllegalArgumentException("Choose a 1.8.9 item variant.");
        ItemStack result = type(stack, BuiltInRegistries.ITEM.getKey(selection.getItem()).toString(), housing);
        copyComponent(selection, result, DataComponents.POTION_CONTENTS);
        copyComponent(selection, result, DataComponents.STORED_ENCHANTMENTS);
        copyComponent(selection, result, DataComponents.INSTRUMENT);
        copyComponent(selection, result, DataComponents.SUSPICIOUS_STEW_EFFECTS);
        return result;
    }
    private static <T> void copyComponent(ItemStack source, ItemStack target, net.minecraft.core.component.DataComponentType<T> type) {
        T value = source.get(type);
        if (value != null) target.set(type, value);
    }
    public static void resetModel(ItemStack stack) {
        var model = stack.getItem().getDefaultInstance().get(DataComponents.ITEM_MODEL);
        if (model != null) stack.set(DataComponents.ITEM_MODEL, model);
        else stack.set(DataComponents.ITEM_MODEL, BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
    public static ItemStack typePreview(ItemStack stack) {
        ItemStack preview = stack.copy();
        resetModel(preview);
        return preview;
    }
    public static int number(String text, int min, int max) {
        int n = Integer.parseInt(text.trim());
        if (n < min || n > max) throw new IllegalArgumentException("Value must be " + min + "–" + max);
        return n;
    }
    public static int color(String text) {
        String hex = text.replaceFirst("^#", "").trim();
        if (!hex.matches("[0-9a-fA-F]{6}")) throw new IllegalArgumentException("Use six RGB hex digits, e.g. FF8800.");
        return Integer.parseInt(hex, 16);
    }
    public static CompoundTag compound(ItemStack stack, String key) {
        Tag tag = get(stack, key); return tag instanceof CompoundTag c ? c.copy() : new CompoundTag();
    }
}

