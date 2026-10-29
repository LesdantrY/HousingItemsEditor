package dev.housingitems;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/** Bounded text history. Editing after undo discards the abandoned redo branch. */
public final class TextHistory {
    private final Deque<String> undo = new ArrayDeque<>(), redo = new ArrayDeque<>();
    private String current;
    private static final int LIMIT = 100;
    public TextHistory(String initial) { current = initial; }
    public void record(String next) {
        if (Objects.equals(current, next)) return;
        undo.addLast(current); while (undo.size() > LIMIT) undo.removeFirst();
        current = next; redo.clear();
    }
    public String undo() {
        if (!undo.isEmpty()) { redo.addLast(current); current = undo.removeLast(); }
        return current;
    }
    public String redo() {
        if (!redo.isEmpty()) { undo.addLast(current); current = redo.removeLast(); }
        return current;
    }
}

