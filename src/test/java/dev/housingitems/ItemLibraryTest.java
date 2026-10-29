package dev.housingitems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ItemLibraryTest {
    @TempDir Path directory;

    @Test void persistsUnicodeAndDeletesByIdentityRatherThanName() throws Exception {
        Path file = directory.resolve("items.json");
        ItemLibrary.save(file, "同名 ✨", "{id:\"minecraft:stone\",count:1}");
        ItemLibrary.save(file, "同名 ✨", "{id:\"minecraft:diamond\",count:1}");
        var saved = ItemLibrary.load(file);
        assertEquals(2, saved.size());
        assertEquals("同名 ✨", saved.getFirst().name());
        ItemLibrary.delete(file, saved.getFirst().id());
        assertEquals(saved.get(1), ItemLibrary.load(file).getFirst());
        assertEquals(2, ItemLibrary.load(file.resolveSibling("items.json.bak")).size());
    }

    @Test void malformedLibraryIsNeverOverwritten() throws Exception {
        Path file = directory.resolve("items.json");
        String corrupt = "[{\"name\":\"valuable item\", BROKEN";
        Files.writeString(file, corrupt);
        assertThrows(IOException.class, () -> ItemLibrary.save(file, "new item", "{}"));
        assertEquals(corrupt, Files.readString(file));
        assertFalse(Files.exists(directory.resolve("items.json.bak")));
    }

    @Test void bulkDeleteKeepsUnselectedItemsAndBacksUpTheWholeOriginalLibrary() throws Exception {
        Path file = directory.resolve("items.json");
        for (int i = 0; i < 3; i++) ItemLibrary.save(file, "Same name", "{count:" + i + "}");
        var original = ItemLibrary.load(file);
        ItemLibrary.deleteAll(file, java.util.Set.of(original.get(0).id(), original.get(2).id()));
        assertEquals(java.util.List.of(original.get(1)), ItemLibrary.load(file));
        assertEquals(original, ItemLibrary.load(file.resolveSibling("items.json.bak")));
    }

    @Test void rejectsNullEntriesWithoutDestroyingFile() throws Exception {
        Path file = directory.resolve("items.json");
        Files.writeString(file, "[null]");
        assertThrows(IOException.class, () -> ItemLibrary.delete(file, "missing"));
        assertEquals("[null]", Files.readString(file));
    }
}

