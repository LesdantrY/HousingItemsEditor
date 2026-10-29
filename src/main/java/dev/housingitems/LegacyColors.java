package dev.housingitems;

import net.minecraft.ChatFormatting;

/** Stable legacy chat palette, also used after ChatFormatting's color accessors were removed. */
final class LegacyColors {
    private static final int[] RGB = {0x000000,0x0000AA,0x00AA00,0x00AAAA,0xAA0000,0xAA00AA,0xFFAA00,0xAAAAAA,
        0x555555,0x5555FF,0x55FF55,0x55FFFF,0xFF5555,0xFF55FF,0xFFFF55,0xFFFFFF};
    static char code(ChatFormatting format) { return format.toString().charAt(1); }
    static Integer rgb(ChatFormatting format) {
        int index = "0123456789abcdef".indexOf(code(format));
        return index < 0 ? null : RGB[index];
    }
}
