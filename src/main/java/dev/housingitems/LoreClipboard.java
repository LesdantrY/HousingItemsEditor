package dev.housingitems;

import net.minecraft.network.chat.Component;
import java.util.List;

/** A typed in-session clipboard, preserving formatting and literal line breaks. */
public final class LoreClipboard {
    private static List<Component> contents;
    private LoreClipboard() {}
    public static void copy(List<Component> lines) { contents = lines.stream().map(c -> (Component)c.copy()).toList(); }
    public static List<Component> read() {
        if (contents == null) throw new IllegalStateException("Copy lore from an item first.");
        return contents.stream().map(c -> (Component)c.copy()).toList();
    }
}
