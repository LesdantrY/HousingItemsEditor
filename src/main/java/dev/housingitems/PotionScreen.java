package dev.housingitems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import java.util.function.Consumer;

public final class PotionScreen extends StructuredListScreen {
    private final CompoundTag contents;
    public PotionScreen(Screen parent, ItemStack stack, Consumer<ItemStack> setter) {
        super(parent, "Potion effects", stack, setter, ItemProperties.compound(stack, "potion_contents").getList("custom_effects").orElseGet(ListTag::new));
        contents = ItemProperties.compound(stack, "potion_contents");
    }
    protected String label(CompoundTag e) { return e.getString("id").orElse("Effect") + " " + (e.getInt("amplifier").orElse(0) + 1) + " · " + e.getInt("duration").orElse(0) + " ticks"; }
    protected ItemStack result(ListTag entries) { CompoundTag c = contents.copy(); c.put("custom_effects", entries.copy()); return ItemProperties.set(stack, "potion_contents", c); }
    protected void edit(int index, CompoundTag e) {
        open(new FormScreen(this, "Potion effect", new String[]{"Effect ID", "Level (1–256)", "Duration in ticks (20 = 1 second; -1 = infinite)", "Particles: true / false"},
                new String[]{e.getString("id").orElse("minecraft:speed"), "" + (e.getInt("amplifier").orElse(0) + 1), "" + e.getInt("duration").orElse(600), "" + e.getBoolean("show_particles").orElse(true)}, v -> {
                    e.putString("id", v[0]); e.putInt("amplifier", ItemProperties.number(v[1], 1, 256) - 1);
                    e.putInt("duration", ItemProperties.number(v[2], -1, Integer.MAX_VALUE));
                    if (!v[3].equals("true") && !v[3].equals("false")) throw new IllegalArgumentException("Particles must be true or false.");
                    e.putBoolean("show_particles", Boolean.parseBoolean(v[3])); replace(index, e);
                }));
    }
}
