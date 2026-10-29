package dev.housingitems;
import fr.atesab.act.gui.GuiACT;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.*;

public final class LibraryScreen extends GuiACT {
    private String filter = "";
    private final Set<String> selectedIds = new LinkedHashSet<>();
    private Set<String> confirmDelete = Set.of();
    private List<ItemLibrary.Entry> entries = List.of();
    private int page, columns, rows, left;
    private List<ItemLibrary.Entry> matches = List.of();
    private final Map<String,ItemStack> previews = new HashMap<>();
    private final Screen returnScreen;
    public LibraryScreen(Screen parent) { super(null, Component.literal("Library")); returnScreen = parent; }
    private ItemLibrary.Entry selected() {
        if (selectedIds.size() != 1) throw new IllegalArgumentException("Select exactly one item to edit.");
        return entries.stream().filter(e -> selectedIds.contains(e.id())).findFirst().orElseThrow(() -> new IllegalArgumentException("Select an item first."));
    }
    @Override protected void init() {
        int w = Math.min(384, editorWidth() - 16); left = editorLeft() + (editorWidth() - w) / 2;
        columns = Math.max(1, w / 24); rows = Math.max(1, (height - 157) / 24);
        var search = field(left, 32, w - 64, "Search saved items", filter);
        search.setResponder(text -> filter = text);
        button(left + w - 60, 32, 60, "Search", () -> { page = 0; rebuildWidgets(); });
        try {
            entries = ItemLibrary.load();
            selectedIds.retainAll(entries.stream().map(ItemLibrary.Entry::id).toList());
            matches = entries.stream().filter(e -> e.name().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))).toList();
            previews.clear();
            for (var entry : matches) {
                try { previews.put(entry.id(), ItemData.decode(entry.data())); }
                catch (Exception e) { status = "Some saved items cannot be decoded; select them to inspect/delete."; }
            }
        } catch (Exception e) { status = e.getMessage(); }
        page = Math.max(0, Math.min(page, Math.max(0, (matches.size() - 1) / (columns * rows))));
        for (int i = page * columns * rows; i < Math.min(matches.size(), (page + 1) * columns * rows); i++) {
            var entry = matches.get(i); int cell = i % (columns * rows);
            var b = button(left + cell % columns * 24, 62 + cell / columns * 24, 22, "", () -> {
                if (!selectedIds.remove(entry.id())) selectedIds.add(entry.id());
                confirmDelete = Set.of(); status = selectedIds.size() + " selected (click again to deselect)."; rebuildWidgets();
            });
            ItemStack preview = previews.get(entry.id());
            if (preview == null) b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(entry.name())));
        }
        int y = height - 89, quarter = w / 3;
        button(left, y, quarter - 2, "Edit", () -> open(new EditorScreen(this, ItemData.decode(selected().data())))).active = selectedIds.size() == 1;
        button(left + quarter, y, quarter - 2, "Give (" + selectedIds.size() + ")", () -> {
            List<ItemStack> stacks = new ArrayList<>();
            for (var entry : entries) if (selectedIds.contains(entry.id())) stacks.add(ItemData.decode(entry.data()));
            CreativeAccess.run(() -> {
                int empty = 0;
                for (int slot = 0; slot < 36; slot++) if (mc.player.getInventory().getItem(slot).isEmpty()) empty++;
                if (empty < stacks.size()) throw new IllegalStateException("Free " + stacks.size() + " inventory slots to give all selected items.");
                stacks.forEach(ItemData::give);
                status = "Sent " + stacks.size() + " items to inventory.";
            });
        }).active = !selectedIds.isEmpty();

        button(left + 2 * quarter, y, w - 2 * quarter, !selectedIds.isEmpty() && selectedIds.equals(confirmDelete) ? "Confirm delete?" : "Delete (" + selectedIds.size() + ")", () -> {
            if (!selectedIds.equals(confirmDelete)) { confirmDelete = Set.copyOf(selectedIds); rebuildWidgets(); return; }
            ItemLibrary.deleteAll(selectedIds); selectedIds.clear(); confirmDelete = Set.of(); status = "Selected items deleted."; rebuildWidgets();
        }).active = !selectedIds.isEmpty();
        button(left, height - 64, 28, "<", () -> { page--; rebuildWidgets(); }).active = page > 0;
        button(left + 32, height - 64, 28, ">", () -> { page++; rebuildWidgets(); }).active = (page + 1) * columns * rows < matches.size();
        button(left + 66, height - 64, Math.min(100, w - 202), "Import SNBT", () -> open(new DataScreen(this, "Import item SNBT", "", value -> {
            ItemStack stack = ItemData.decode(value); ItemLibrary.save(stack.getHoverName().getString(), ItemData.encode(stack));
        })));
        button(left + w - 132, height - 64, 64, "Clear", () -> { selectedIds.clear(); confirmDelete = Set.of(); status = "Selection cleared."; rebuildWidgets(); }).active = !selectedIds.isEmpty();
        button(left + w - 64, height - 64, 64, returnScreen == null ? "Close" : "Back", this::onClose);
    }
    @Override public void complete() throws Exception {
        CreativeAccess.cancel(); mc.setScreen(null);
    }
    @Override public void onClose() {
        CreativeAccess.cancel();
        if (returnScreen instanceof fr.atesab.act.gui.GuiGiver root) root.reopenLastMenu();
        else if (returnScreen != null) mc.setScreen(returnScreen);
        else mc.setScreen(null);
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        for (int i = page * columns * rows; i < Math.min(matches.size(), (page + 1) * columns * rows); i++) {
            var entry = matches.get(i); int cell = i % (columns * rows);
            int x = left + cell % columns * 24, y = 62 + cell / columns * 24;
            if (selectedIds.contains(entry.id())) g.fill(x + 1, y + 1, x + 21, y + 21, 0x88FFCC44);
            ItemStack preview = previews.get(entry.id());
            if (preview != null) {
                item(g, preview, x + 3, y + 2, mouseX, mouseY);
                if (mouseX >= x && mouseX < x+22 && mouseY >= y && mouseY < y+20) g.setTooltipForNextFrame(font, preview, mouseX, mouseY);
            }
            else g.drawString(font, "?", x + 8, y + 6, 0xFFFF5555);
        }
        if (matches.isEmpty()) g.drawCenteredString(font, "No matching saved items.", editorCenter(), 70, 0xFFAAAAAA);
    }
}






