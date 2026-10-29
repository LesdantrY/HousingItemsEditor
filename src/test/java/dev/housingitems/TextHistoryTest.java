package dev.housingitems;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TextHistoryTest {
    @Test void undoRedoRetainsFormattingAndLineBreaks() {
        var history = new TextHistory("&aFirst");
        history.record("&aFirst\n&#ABCDEFSecond");
        assertEquals("&aFirst", history.undo());
        assertEquals("&aFirst\n&#ABCDEFSecond", history.redo());
    }
    @Test void editAfterUndoDiscardsRedoAndRepeatedValuesDoNotPolluteHistory() {
        var history = new TextHistory("a");
        history.record("ab"); history.record("ab");
        assertEquals("a", history.undo());
        history.record("ac"); assertEquals("ac", history.redo());
        assertEquals("a", history.undo());
    }
}

