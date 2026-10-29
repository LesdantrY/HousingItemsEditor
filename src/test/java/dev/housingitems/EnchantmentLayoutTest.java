package dev.housingitems;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EnchantmentLayoutTest {
    @Test void allVanillaEnchantmentsFitOnePageIncludingSmallGui() {
        for (int[] size : new int[][]{{320,240},{427,240},{640,360},{960,540}}) {
            var layout = EnchantmentLayout.fit(size[0], size[1], 42);
            assertTrue(layout.rows() * layout.columns() >= 42);
            assertTrue(33 + layout.rows() * layout.rowHeight() <= size[1] - 48);
            assertTrue(8 + layout.columns() * layout.cellWidth() <= size[0] - 8);
            assertTrue(layout.rowHeight() >= 13);
            assertTrue(layout.cellWidth() >= 65);
        }
    }
    @Test void emptyRegistryStillHasValidGridDimensions() {
        var layout = EnchantmentLayout.fit(320,240,0);
        assertTrue(layout.rows() > 0 && layout.columns() > 0);
    }
    @Test void largeScreensDoNotStretchIndividualRows() {
        for (int count : new int[]{13,46}) {
            var layout = EnchantmentLayout.fit(676,360,count);
            assertTrue(layout.cellWidth() <= 220);
            assertTrue(layout.columns() * layout.cellWidth() <= 660);
            assertTrue(layout.rows() * layout.columns() >= count);
        }
    }
}


