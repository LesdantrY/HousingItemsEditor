package dev.housingitems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;

/** Only writes components whose editor values actually changed. */
final class LoreDraft {
    static ItemStack apply(ItemStack original, String name, String text, TooltipDisplay display) {
        ItemStack next = original.copy();
        if (!name.equals(FormattedText.editable(original.getHoverName()))) {
            if (name.isBlank()) next.remove(DataComponents.CUSTOM_NAME);
            else next.set(DataComponents.CUSTOM_NAME, FormattedText.parse(name));
        }
        var originalLines = original.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines();
        if (!text.equals(LoreText.text(originalLines))) {
            var lines = LoreText.apply(originalLines, text);
            if (lines.isEmpty()) next.remove(DataComponents.LORE);
            else next.set(DataComponents.LORE, new ItemLore(lines));
        }
        if (!display.equals(original.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT)))
            next.set(DataComponents.TOOLTIP_DISPLAY, display);
        return next;
    }
}
