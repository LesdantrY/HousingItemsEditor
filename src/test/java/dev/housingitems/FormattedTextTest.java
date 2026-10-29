package dev.housingitems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FormattedTextTest {
    @Test void retainsOriginalLegacyAndHexInputWhenReopened() {
        for (String raw : new String[]{"&aGreen &lBold", "&#00FF00Hex chosen deliberately", "&#Ab12CdMixed case", "Plain text"}) {
            assertEquals(raw, FormattedText.editable(FormattedText.parse(raw).copy()));
        }
    }
    @Test void existingVanillaColorsUseAmpersandInsteadOfHex() {
        assertEquals("&6Existing gold item", FormattedText.editable(Component.literal("Existing gold item").withStyle(ChatFormatting.GOLD)));
    }
    @Test void editingPreservesRenderedStyles() {
        Component original = Component.literal("Header").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                .append(Component.literal(" plain").withStyle(ChatFormatting.RED));
        Component result = FormattedText.parse(FormattedText.editable(original));
        assertEquals(original.getString(), result.getString());
        var expected = new java.util.ArrayList<String>();
        var actual = new java.util.ArrayList<String>();
        original.visit((style, text) -> { if (!text.isEmpty()) expected.add(text + ":" + style.getColor() + ":" + style.isBold()); return java.util.Optional.empty(); }, net.minecraft.network.chat.Style.EMPTY);
        result.visit((style, text) -> { if (!text.isEmpty()) actual.add(text + ":" + style.getColor() + ":" + style.isBold()); return java.util.Optional.empty(); }, net.minecraft.network.chat.Style.EMPTY);
        assertEquals(expected, actual);
    }
}

