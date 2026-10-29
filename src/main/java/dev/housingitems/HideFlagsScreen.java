package dev.housingitems;
import fr.atesab.act.gui.modifier.GuiModifier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.*;
import java.util.function.Consumer;

public final class HideFlagsScreen extends GuiModifier<ItemStack> {
    private final ItemStack stack;
    private TooltipDisplay display;
    static final List<DataComponentType<?>> ENCHANTMENTS = List.of(DataComponents.ENCHANTMENTS, DataComponents.STORED_ENCHANTMENTS);
    static final List<DataComponentType<?>> MODIFIERS = List.of(DataComponents.ATTRIBUTE_MODIFIERS);
    static final List<DataComponentType<?>> ADDITIONAL = List.of(
        DataComponents.TROPICAL_FISH_PATTERN, DataComponents.INSTRUMENT, DataComponents.MAP_ID,
        DataComponents.BEES, DataComponents.CONTAINER_LOOT, DataComponents.CONTAINER,
        DataComponents.BANNER_PATTERNS, DataComponents.POT_DECORATIONS, DataComponents.CHARGED_PROJECTILES,
        DataComponents.FIREWORKS, DataComponents.FIREWORK_EXPLOSION, DataComponents.POTION_CONTENTS,
        DataComponents.JUKEBOX_PLAYABLE, DataComponents.TRIM, DataComponents.DYED_COLOR,
        DataComponents.PROFILE, DataComponents.UNBREAKABLE, DataComponents.CAN_BREAK, DataComponents.CAN_PLACE_ON);
    public HideFlagsScreen(Screen parent, ItemStack stack, Consumer<ItemStack> setter) {
        super(parent, Component.literal("Hide flags"), setter); this.stack = stack.copy();
        display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
    }
    private void toggleButton(int y, String label, List<DataComponentType<?>> types) {
        boolean hidden = types.stream().allMatch(display.hiddenComponents()::contains);
        button(width / 2 - 120, y, 240, label + ": " + (hidden ? "HIDDEN" : "SHOWN"), () -> {
            for (var type : types) display = display.withHidden(type, !hidden); rebuildWidgets();
        });
    }
    @Override protected void init() {
        int y = height / 2 - 55;
        toggleButton(y, "Enchantments", ENCHANTMENTS);
        toggleButton(y + 24, "Modifiers", MODIFIERS);
        toggleButton(y + 48, "Additional information", ADDITIONAL);
        button(width / 2 - 120, y + 82, 118, "Done", () -> { stack.set(DataComponents.TOOLTIP_DISPLAY, display); set(stack); onClose(); });
        button(width / 2 + 2, y + 82, 118, "Cancel", this::onClose);
    }
}


