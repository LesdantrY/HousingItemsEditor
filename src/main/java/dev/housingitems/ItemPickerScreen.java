package dev.housingitems;
import fr.atesab.act.gui.GuiACT;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import java.util.*;
import java.util.function.Consumer;

public final class ItemPickerScreen extends GuiACT {
    private record Preferences(String query, boolean housing, int sort, int category, int page) {}
    private static final Map<String, Preferences> remembered = new HashMap<>();
    private final String preferenceKey;
    private final Consumer<String> select;
    private java.util.function.BiConsumer<ItemStack, Boolean> selectType;
    private boolean housing;
    private String query = "";
    private int page, columns, rows, left, category, tabWidth;
    private int sort = 2;
    private Runnable reset;
    private Consumer<ItemStack> selectModelVariant;
    private java.util.function.Supplier<ItemStack> modelVariant;
    public ItemPickerScreen withModelVariant(Consumer<ItemStack> select, java.util.function.Supplier<ItemStack> current) {
        selectModelVariant = select; modelVariant = current; return this;
    }
    private java.util.function.Supplier<ItemStack> current;
    public ItemPickerScreen withCurrent(java.util.function.Supplier<ItemStack> current) { this.current = current; return this; }
    private boolean selected(ItemStack candidate) {
        if (current == null) return false;
        ItemStack draft = current.get();
        if (reset != null) {
            if (!Objects.equals(draft.get(DataComponents.ITEM_MODEL), BuiltInRegistries.ITEM.getKey(candidate.getItem()))) return false;
            ItemStack chosen = modelVariant == null ? ItemStack.EMPTY : modelVariant.get();
            if (!chosen.isEmpty()) return ItemStack.isSameItemSameComponents(chosen, candidate);
            return ItemStack.isSameItemSameComponents(new ItemStack(candidate.getItem()), candidate);
        }
        return draft.is(candidate.getItem())
            && Objects.equals(draft.get(DataComponents.POTION_CONTENTS), candidate.get(DataComponents.POTION_CONTENTS))
            && Objects.equals(draft.get(DataComponents.STORED_ENCHANTMENTS), candidate.get(DataComponents.STORED_ENCHANTMENTS));
    }
    private java.util.function.Supplier<ItemStack> advancedStack;
    private Consumer<ItemStack> advancedSetter;
    private List<ItemStack> matches = List.of();
    private static final String[] SORTS = {"Alphabetical", "Item type", "Registry order"};
    private List<CreativeModeTab> tabs = List.of();
    public ItemPickerScreen(Screen parent, Consumer<String> select, boolean housing) { this(parent, select, housing, "Select item type"); }
    public ItemPickerScreen(Screen parent, java.util.function.BiConsumer<ItemStack, Boolean> select, boolean housing) {
        this(parent, id -> {}, housing, "Select item type"); selectType = select;
    }
    public ItemPickerScreen(Screen parent, Consumer<String> select, boolean housing, String title) {
        super(parent, Component.literal(title)); this.select = select; this.housing = housing;
        preferenceKey = title;
        Preferences saved = remembered.get(preferenceKey);
        if (saved != null) {
            query = saved.query(); this.housing = saved.housing(); sort = saved.sort(); category = saved.category(); page = saved.page();
        }
    }
    @Override public void removed() {
        remembered.put(preferenceKey, new Preferences(query, housing, sort, category, page));
        super.removed();
    }
    public ItemPickerScreen withReset(Runnable reset) { this.reset = reset; return this; }
    public ItemPickerScreen withAdvancedData(java.util.function.Supplier<ItemStack> stack, Consumer<ItemStack> setter) {
        advancedStack = stack; advancedSetter = setter; return this;
    }
    private static Item item(String id) { return BuiltInRegistries.ITEM.getValue(Identifier.parse(id)); }
    private static String id(ItemStack stack) { return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(); }
    private static String name(ItemStack stack) { return stack.getHoverName().getString(); }
    private String category(ItemStack selected) {
        Item target = selected.getItem();
        for (var tab : tabs) if (tab.getDisplayItems().stream().anyMatch(stack -> stack.getItem() == target)) return tab.getDisplayName().getString();
        return "Other";
    }
    @Override protected void init() {
        initEditorTabs(reset == null ? "Item Type" : "Item Model");
        CreativeCatalog.rebuild();
        tabs = CreativeModeTabs.tabs().stream().filter(t -> t.getType() == CreativeModeTab.Type.CATEGORY).toList();
        category = Math.min(category, tabs.size());
        columns = Math.min(16, Math.max(7, (editorWidth() - 16) / 24));
        rows = Math.max(1, (height - 166) / 24); int w = columns * 24; left = editorLeft() + (editorWidth() - w) / 2;
        var search = field(left, 33, w - 64, "Search item names or IDs", query);
        search.setResponder(text -> query = text);
        button(left + w - 60, 33, 60, "Search", () -> { page = 0; rebuildWidgets(); });
        int advancedWidth = advancedStack == null && reset == null ? 0 : (w >= 336 ? 84 : 24);
        tabWidth = Math.min(24, (w - advancedWidth) / (tabs.size() + 1));
        button(left,58,tabWidth-1,"*",()-> {category=0;page=0;rebuildWidgets();}).setTooltip(Tooltip.create(Component.literal("All items")));
        for (int i=0;i<tabs.size();i++) {
            int choice=i+1;
            button(left+choice*tabWidth,58,tabWidth-1,"",()-> {category=choice;page=0;rebuildWidgets();}).setTooltip(Tooltip.create(tabs.get(i).getDisplayName()));
        }
        if (advancedStack != null) button(left + w - advancedWidth, 58, advancedWidth, advancedWidth >= 84 ? "Advanced Data" : "{ }", () -> open(new DataScreen(this, "Advanced component data", ItemData.encode(advancedStack.get()), value -> advancedSetter.accept(ItemData.decode(value)))))
                .setTooltip(Tooltip.create(Component.literal("Advanced component data")));
        if (reset != null) button(left + w - advancedWidth, 58, advancedWidth, advancedWidth >= 84 ? "Reset Model" : "R", () -> { reset.run(); status = "Default model selected. Press Done to apply."; })
                .setTooltip(Tooltip.create(Component.literal("Reset model to the default item appearance")));
        Comparator<ItemStack> order = switch (sort) {
            case 1 -> Comparator.comparing((ItemStack stack) -> category(stack)).thenComparing(ItemPickerScreen::name, String.CASE_INSENSITIVE_ORDER).thenComparing(ItemPickerScreen::id);
            case 2 -> Comparator.comparingInt(stack -> BuiltInRegistries.ITEM.getId(stack.getItem()));
            default -> Comparator.comparing(ItemPickerScreen::name, String.CASE_INSENSITIVE_ORDER).thenComparing(ItemPickerScreen::id);
        };
        String term = query.toLowerCase(Locale.ROOT);
        matches = CreativeCatalog.all().stream()
                .filter(stack -> !housing || HousingData.allowed(stack))
                .filter(selected -> category == 0 || tabs.get(category - 1).getDisplayItems().stream().anyMatch(stack -> stack.getItem() == selected.getItem()))
                .filter(stack -> (id(stack) + " " + name(stack)).toLowerCase(Locale.ROOT).contains(term)).sorted(order).toList();
        page = Math.max(0, Math.min(page, Math.max(0, (matches.size() - 1) / (columns * rows))));
        for (int i = page * columns * rows; i < Math.min(matches.size(), (page + 1) * columns * rows); i++) {
            ItemStack selected = matches.get(i); int cell = i % (columns * rows);
            var choice = button(left + cell % columns * 24, 86 + cell / columns * 24, 22, "", () -> { if (selectType != null) selectType.accept(selected.copy(), housing); else { select.accept(id(selected)); if (selectModelVariant != null) selectModelVariant.accept(selected.copy()); } status = "Selected: " + name(selected) + ". Press Done to apply."; });
            choice.setOverrideRenderHighlightedSprite(() -> selected(selected) || choice.isHovered());
        }
        button(left, height - 67, 98, housing ? "Housing: ON" : "Housing: OFF", () -> { housing = !housing; page = 0; rebuildWidgets(); });
        button(left + 102, height - 67, 26, "<", () -> { page--; rebuildWidgets(); }).active = page > 0;
        button(left + 132, height - 67, 26, ">", () -> { page++; rebuildWidgets(); }).active = (page + 1) * columns * rows < matches.size();
        button(left + 162, height - 67, w - 162, font.plainSubstrByWidth(SORTS[sort], Math.max(1, w - 170)), () -> { sort = (sort + 1) % SORTS.length; page = 0; rebuildWidgets(); });
        int firstWidth = Math.min(98, w - 116);
        button(left + firstWidth + 4, height - 42, 52, "Done", () -> {});
        button(left + w - 56, height - 42, 56, "Cancel", this::onClose);
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        for (int i = page * columns * rows; i < Math.min(matches.size(), (page + 1) * columns * rows); i++) {
            int cell = i % (columns * rows);
            item(g, matches.get(i), left + cell % columns * 24 + 3, 88 + cell / columns * 24, mouseX, mouseY);
            int x = left + cell % columns * 24, y = 86 + cell / columns * 24;
            if (mouseX >= x && mouseX < x + 22 && mouseY >= y && mouseY < y + 20)
                g.setTooltipForNextFrame(font, matches.get(i), mouseX, mouseY);
        }
        g.fill(left + category * tabWidth, 78, left + (category + 1) * tabWidth - 1, 80, 0xFFFFCC44);
        for(int i=0;i<tabs.size();i++) g.renderItem(tabs.get(i).getIconItem(),left+(i+1)*tabWidth+(tabWidth-16)/2,60);
    }
}











