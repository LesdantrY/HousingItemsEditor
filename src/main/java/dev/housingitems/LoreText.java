package dev.housingitems;
import net.minecraft.network.chat.Component;
import java.util.*;

/** One logical line per lore component; soft wrapping never creates extra lore. */
public final class LoreText {
    private LoreText() {}
    public static String text(List<Component> lines) {
        return String.join("\n", lines.stream().map(FormattedText::editable).toList());
    }
    public static List<Component> apply(List<Component> original, String text) {
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        if (normalized.isEmpty()) return List.of();
        String[] lines = normalized.split("\n", -1);
        if (lines.length > 256) throw new IllegalArgumentException("Lore supports at most 256 lines.");
        Map<String, ArrayDeque<Component>> unchanged = new HashMap<>();
        for (Component line : original) unchanged.computeIfAbsent(FormattedText.editable(line), k -> new ArrayDeque<>()).add(line);
        List<Component> result = new ArrayList<>();
        for (String line : lines) {
            var existing = unchanged.get(line);
            result.add(existing != null && !existing.isEmpty() ? existing.removeFirst() : FormattedText.parse(line));
        }
        return List.copyOf(result);
    }
}

