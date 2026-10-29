package dev.housingitems;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class NameLoreTextTest {
    @BeforeAll static void bootstrap() { TestBootstrap.initialize(); }
    @Test void firstNewlineSeparatesNameWithoutLosingLoreBlankLinesOrFormatting() {
        var original = new NameLoreText("&6Name", "\n&aLine\n\n");
        assertEquals(original, NameLoreText.parse(original.text()));
        assertEquals(new NameLoreText("Name", ""), NameLoreText.parse("Name"));
        assertEquals(new NameLoreText("", "Lore"), NameLoreText.parse("\nLore"));
    }
    @Test void untouchedCombinedEditorPreservesExactItemComponents() {
        for (boolean custom : new boolean[]{false, true}) {
            var item = new ItemStack(Items.DIAMOND_SWORD);
            if (custom) {
                item.set(DataComponents.CUSTOM_NAME, Component.literal("Custom"));
                item.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(""), Component.literal("Lore"), Component.literal(""))));
            }
            var document = new NameLoreText(FormattedText.editable(item.getHoverName()), LoreText.text(item.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines()));
            var parsed = NameLoreText.parse(document.text());
            var result = LoreDraft.apply(item, parsed.name(), parsed.lore(), item.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT));
            assertEquals(item.getComponentsPatch(), result.getComponentsPatch());
        }
    }
}

