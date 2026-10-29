package dev.housingitems;
public final class AttributeNumbers {
    private AttributeNumbers() {}
    public static String format(double value) {
        if (!Double.isFinite(value)) return Double.toString(value);
        return java.math.BigDecimal.valueOf(value).setScale(6, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}

