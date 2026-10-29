package dev.housingitems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import java.util.function.Consumer;

public final class AttributeScreen extends StructuredListScreen {
    public AttributeScreen(Screen parent, ItemStack stack, Consumer<ItemStack> setter) { super(parent, "Attributes", stack, setter, effectiveModifiers(stack)); }
    private static ListTag effectiveModifiers(ItemStack stack) {
        var modifiers = stack.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
        var ops = net.minecraft.client.Minecraft.getInstance().getConnection().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        Tag value = net.minecraft.world.item.component.ItemAttributeModifiers.CODEC.encodeStart(ops, modifiers).getOrThrow();
        if (!(value instanceof ListTag list)) throw new IllegalStateException("Unexpected attribute data format.");
        return list;
    }
    protected String label(CompoundTag e) {
        return AttributeModifierScreen.name(e.getString("type").orElse("Attribute")) + " " + AttributeNumbers.format(e.getDouble("amount").orElse(0d)) + " (" + e.getString("slot").orElse("any") + ")";
    }
    protected ItemStack result(ListTag entries) { return ItemProperties.set(stack, "attribute_modifiers", entries); }
    protected void edit(int index, CompoundTag e) { open(new AttributeModifierScreen(this, e, value -> replace(index, value))); }
}


