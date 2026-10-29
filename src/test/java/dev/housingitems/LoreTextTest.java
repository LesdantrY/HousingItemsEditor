package dev.housingitems;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class LoreTextTest {
    @Test void newlinesBecomeLoreLinesAndFormattingStaysTyped() {
        String text = "&aFirst\n&#aBcDeFSecond\n\n";
        var lines = LoreText.apply(List.of(), text);
        assertEquals(4, lines.size());
        assertEquals(text, LoreText.text(lines));
    }
    @Test void unchangedComponentsSurviveInsertionWithoutFlattening() {
        Component original = Component.translatable("item.minecraft.diamond");
        var lines = LoreText.apply(List.of(original), "&6Added\n" + LoreText.text(List.of(original)));
        assertSame(original, lines.get(1));
    }
    @Test void validatesLineLimitAndNormalizesWindowsNewlines() {
        assertEquals(2, LoreText.apply(List.of(), "one\r\ntwo").size());
        assertThrows(IllegalArgumentException.class, () -> LoreText.apply(List.of(), "x\n".repeat(256)));
        assertTrue(LoreText.apply(List.of(Component.literal("old")), "").isEmpty());
    }
}

