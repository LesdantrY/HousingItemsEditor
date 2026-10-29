package dev.housingitems;
import fr.atesab.act.gui.GuiACT;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import java.util.function.Consumer;

public final class SpecialEditors {
    private SpecialEditors() {}
    public static void head(GuiACT parent, ItemStack stack, Consumer<ItemStack> setter) {
        CompoundTag profile = ItemProperties.compound(stack, "profile");
        parent.open(new FormScreen(parent, "Player head", new String[]{"Player username"}, new String[]{profile.getString("name").orElse("")}, v -> {
            if (!v[0].matches("[A-Za-z0-9_]{1,16}")) throw new IllegalArgumentException("Use a valid Minecraft player name.");
            CompoundTag next = new CompoundTag(); next.putString("name", v[0]); setter.accept(ItemProperties.set(stack, "profile", next));
        }));
    }
    public static void command(GuiACT parent, ItemStack stack, Consumer<ItemStack> setter) {
        boolean minecart = stack.is(net.minecraft.world.item.Items.COMMAND_BLOCK_MINECART);
        String component = minecart ? "entity_data" : "block_entity_data";
        CompoundTag data = ItemProperties.compound(stack, component);
        parent.open(new FormScreen(parent, "Command block", new String[]{"Command (without leading /)"}, new String[]{data.getString("Command").orElse("")}, v -> {
            data.putString("id", minecart ? "minecraft:command_block_minecart" : "minecraft:command_block"); data.putString("Command", v[0]); setter.accept(ItemProperties.set(stack, component, data));
        }));
    }
    public static void light(GuiACT parent, ItemStack stack, Consumer<ItemStack> setter) {
        CompoundTag state = ItemProperties.compound(stack, "block_state");
        parent.open(new FormScreen(parent, "Light level", new String[]{"Level (0–15)"}, new String[]{state.getString("level").orElse("15")}, v -> {
            state.putString("level", "" + ItemProperties.number(v[0], 0, 15)); setter.accept(ItemProperties.set(stack, "block_state", state));
        }));
    }
    public static void appearance(GuiACT parent, ItemStack stack, Consumer<ItemStack> setter) {
        parent.open(new AppearanceScreen(parent, stack, setter));
    }
    public static void trim(GuiACT parent, ItemStack stack, Consumer<ItemStack> setter) {
        CompoundTag trim = ItemProperties.compound(stack, "trim");
        parent.open(new FormScreen(parent, "Armor trim", new String[]{"Pattern (blank removes trim)", "Material"},
                new String[]{trim.getString("pattern").orElse(""), trim.getString("material").orElse("minecraft:quartz")}, v -> {
                    if (v[0].isBlank()) setter.accept(ItemProperties.set(stack, "trim", null));
                    else { CompoundTag t = new CompoundTag(); t.putString("pattern", v[0]); t.putString("material", v[1]); setter.accept(ItemProperties.set(stack, "trim", t)); }
                }));
    }
}

