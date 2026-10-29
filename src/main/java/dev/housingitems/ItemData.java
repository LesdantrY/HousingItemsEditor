package dev.housingitems;

import com.mojang.serialization.DynamicOps;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;

/** Uses the game's registry-aware codec, including every supported component. */
public final class ItemData {
    private ItemData() {}

    private static DynamicOps<Tag> ops() {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) throw new IllegalStateException("Join a world first.");
        return connection.registryAccess().createSerializationContext(NbtOps.INSTANCE);
    }

    public static String encode(ItemStack stack) {
        return ItemStack.CODEC.encodeStart(ops(), stack).getOrThrow().toString();
    }

    public static ItemStack decode(String text) throws Exception {
        if (text.length() > 131072) throw new IllegalArgumentException("Item data is too large (128 KiB maximum).");
        ItemStack stack = ItemStack.CODEC.parse(ops(), TagParser.parseCompoundFully(text)).getOrThrow();
        if (stack.isEmpty()) throw new IllegalArgumentException("Choose a non-air item.");
        if (stack.getCount() < 1 || stack.getCount() > stack.getMaxStackSize())
            throw new IllegalArgumentException("Count must be 1–" + stack.getMaxStackSize() + " for this item.");
        return stack;
    }

    public static void requireCreative() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) throw new IllegalStateException("Join a world first.");
        if (!mc.player.getAbilities().instabuild) {
            CreativeAccess.request();
            throw new IllegalStateException("Creative mode requested; waiting for the server to allow it.");
        }
    }

    public static void give(ItemStack stack) {
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.getAbilities().instabuild) {
            ItemStack copy = stack.copy(); CreativeAccess.run(() -> give(copy)); return;
        }
        requireCreative();
        Minecraft mc = Minecraft.getInstance();
        for (int slot = 0; slot < 36; slot++) {
            if (mc.player.getInventory().getItem(slot).isEmpty()) {
                mc.player.getInventory().setItem(slot, stack.copy());
                mc.gameMode.handleCreativeModeItemAdd(stack.copy(), slot < 9 ? 36 + slot : slot);
                return;
            }
        }
        throw new IllegalStateException("Inventory is full. Free a slot and try again.");
    }

    public static void replaceHeld(ItemStack stack, int selected, ItemStack original) {
        requireCreative();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player.getInventory().getSelectedSlot() != selected
                || !ItemStack.matches(mc.player.getMainHandItem(), original))
            throw new IllegalStateException("Your held item changed. Reopen the editor before replacing it.");
        mc.player.getInventory().setItem(selected, stack.copy());
        mc.gameMode.handleCreativeModeItemAdd(stack.copy(), 36 + selected);
    }
}
