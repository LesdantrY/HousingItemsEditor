package dev.housingitems;

/** Only the first logical newline separates the name; remaining newlines belong to lore. */
record NameLoreText(String name, String lore) {
    static NameLoreText parse(String value) {
        int newline = value.indexOf('\n');
        return newline < 0 ? new NameLoreText(value, "") : new NameLoreText(value.substring(0, newline), value.substring(newline + 1));
    }
    String text() { return name + "\n" + lore; }
}
