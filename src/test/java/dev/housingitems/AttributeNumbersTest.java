package dev.housingitems;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AttributeNumbersTest {
    @Test void swordModifierAndBaseProduceExpectedSpeed() {
        assertEquals("-2.4", AttributeNumbers.format(-2.4000000953674316));
        assertEquals("1.6", AttributeNumbers.format(4 - 2.4000000953674316));
    }
    @Test void cleanNumbersDoNotAcquireUnnecessaryDecimals() {
        assertEquals("0", AttributeNumbers.format(0));
        assertEquals("10", AttributeNumbers.format(10));
        assertEquals("0.15", AttributeNumbers.format(.15));
    }
}

