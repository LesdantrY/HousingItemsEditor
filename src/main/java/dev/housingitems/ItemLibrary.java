package dev.housingitems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ItemLibrary {
    public record Entry(String id, String name, String data) {}
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path path() {
        String version = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow()
                .getMetadata().getVersion().getFriendlyString();
        return FabricLoader.getInstance().getConfigDir().resolve("housing-items-editor").resolve("items-" + version + ".json");
    }

    public static List<Entry> load() throws IOException {
        return load(path());
    }

    static List<Entry> load(Path target) throws IOException {
        if (!Files.exists(target)) return new ArrayList<>();
        try {
            if (Files.size(target) > 16 * 1024 * 1024) throw new IOException("Library exceeds 16 MiB.");
            List<Entry> entries = GSON.fromJson(Files.readString(target, StandardCharsets.UTF_8),
                    new TypeToken<List<Entry>>() {}.getType());
            if (entries == null || entries.stream().anyMatch(e -> e == null || e.id() == null || e.name() == null || e.data() == null))
                throw new IOException("Invalid library file; original file has been preserved.");
            return new ArrayList<>(entries);
        } catch (RuntimeException e) {
            throw new IOException("Cannot read saved items; original file has been preserved.", e);
        }
    }

    public static void save(String name, String data) throws IOException {
        save(path(), name, data);
    }

    static void save(Path target, String name, String data) throws IOException {
        List<Entry> entries = load(target);
        entries.add(new Entry(UUID.randomUUID().toString(), name, data));
        write(target, entries);
    }

    public static void delete(String id) throws IOException {
        delete(path(), id);
    }

    static void delete(Path target, String id) throws IOException {
        deleteAll(target, java.util.Set.of(id));
    }

    public static void deleteAll(java.util.Collection<String> ids) throws IOException {
        deleteAll(path(), ids);
    }

    static void deleteAll(Path target, java.util.Collection<String> ids) throws IOException {
        List<Entry> entries = load(target);
        entries.removeIf(entry -> ids.contains(entry.id()));
        write(target, entries);
    }

    private static void write(Path target, List<Entry> entries) throws IOException {
        Files.createDirectories(target.getParent());
        Path temp = Files.createTempFile(target.getParent(), "items-", ".tmp");
        try {
            Files.writeString(temp, GSON.toJson(entries), StandardCharsets.UTF_8);
            if (Files.exists(target)) Files.copy(target, target.resolveSibling(target.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
