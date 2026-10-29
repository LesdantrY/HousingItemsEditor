package dev.housingitems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class FormattedText {
    private static final java.util.Map<String, String> ORIGINAL_TEXT = new java.util.LinkedHashMap<>(256, .75f, true) {
        @Override protected boolean removeEldestEntry(java.util.Map.Entry<String,String> entry) { return size() > 2048; }
    };
    private FormattedText() {}
    public static Component parse(String value) {
        MutableComponent result = Component.empty();
        Style style = Style.EMPTY.withItalic(false);
        StringBuilder segment = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '&' && i + 7 < value.length() && value.charAt(i + 1) == '#'
                    && value.substring(i + 2, i + 8).matches("[0-9a-fA-F]{6}")) {
                result.append(Component.literal(segment.toString()).setStyle(style)); segment.setLength(0);
                style = Style.EMPTY.withItalic(false).withColor(Integer.parseInt(value.substring(i + 2, i + 8), 16));
                i += 7; continue;
            }
            ChatFormatting format = c == '&' && i + 1 < value.length() ? ChatFormatting.getByCode(value.charAt(i + 1)) : null;
            if (format != null) {
                result.append(Component.literal(segment.toString()).setStyle(style));
                segment.setLength(0);
                style = format == ChatFormatting.RESET ? Style.EMPTY.withItalic(false) : style.applyFormat(format);
                i++;
            } else segment.append(c);
        }
        result.append(Component.literal(segment.toString()).setStyle(style));
        ORIGINAL_TEXT.put(canonical(result), value);
        return result;
    }
    public static String editable(Component component) {
        String canonical = canonical(component);
        return ORIGINAL_TEXT.getOrDefault(canonical, canonical);
    }
    private static String canonical(Component component) {
        StringBuilder out = new StringBuilder();
        component.visit((style, text) -> {
            if (text.isEmpty()) return java.util.Optional.empty();
            if (!out.isEmpty()) out.append("&r");
            if (style.getColor() != null) {
                ChatFormatting legacy = java.util.Arrays.stream(ChatFormatting.values())
                        .filter(f -> java.util.Objects.equals(LegacyColors.rgb(f), style.getColor().getValue())).findFirst().orElse(null);
                if (legacy != null) out.append('&').append(LegacyColors.code(legacy));
                else out.append("&#").append(String.format("%06X", style.getColor().getValue()));
            }
            if (style.isBold()) out.append("&l");
            if (style.isItalic()) out.append("&o");
            if (style.isUnderlined()) out.append("&n");
            if (style.isStrikethrough()) out.append("&m");
            if (style.isObfuscated()) out.append("&k");
            out.append(text);
            return java.util.Optional.empty();
        }, Style.EMPTY);
        return out.toString();
    }
}
