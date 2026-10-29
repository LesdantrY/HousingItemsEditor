package dev.housingitems;

/** One-page packing; short labels retain full-name hover tooltips on small windows. */
record EnchantmentLayout(int rows, int columns, int cellWidth, int rowHeight) {
    static EnchantmentLayout fit(int width, int height, int count) {
        int availableHeight = Math.max(12, height - 81);
        int maxRows = Math.max(1, availableHeight / 13);
        int columns = Math.max(1, (count + maxRows - 1) / maxRows);
        int rows = Math.max(1, (count + columns - 1) / columns);
        return new EnchantmentLayout(rows, columns, Math.min(220, Math.max(1, (width - 16) / columns)), Math.min(24, availableHeight / rows));
    }
}



