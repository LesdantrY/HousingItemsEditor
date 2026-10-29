package dev.housingitems;
import fr.atesab.act.gui.modifier.GuiModifier;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.*;
import java.util.function.Consumer;

public final class EnchantmentScreen extends GuiModifier<ItemStack> {
    private int panelTop;
    private static final String[] GROUPS = {"All", "Weapons", "Armor", "Tools", "Overall"};
    private ItemStack stack;
    private final String key;
    private final Map<String,String> levels = new LinkedHashMap<>();
    private final Map<String,EditBox> levelFields = new LinkedHashMap<>();
    private final Map<String,Component> names = new HashMap<>();
    public EnchantmentScreen(Screen parent, ItemStack stack, Consumer<ItemStack> setter, boolean stored) {
        super(parent, Component.literal(stored ? "Stored enchantments" : "Enchantments"), setter);
        this.stack = stack.copy(); key = stored ? "stored_enchantments" : "enchantments";
        CompoundTag entries = ItemProperties.compound(stack, key);
        var registry = mc.getConnection().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        registry.keySet().stream().sorted(Comparator.comparing(id -> registry.getValue(id).description().getString(), String.CASE_INSENSITIVE_ORDER)).forEach(id -> {
            levels.put(id.toString(), "" + Math.min(10, entries.getInt(id.toString()).orElse(0)));
            names.put(id.toString(), registry.getValue(id).description());
        });
        entries.keySet().forEach(id -> levels.putIfAbsent(id, "" + Math.min(10, entries.getInt(id).orElse(0))));
    }
    @Override protected void init() {
        initEditorTabs("Enchantments");
        levelFields.clear();
        var ids = new ArrayList<String>();
        for (int n = 1; n < GROUPS.length; n++) {
            final String category = GROUPS[n];
            var entries = levels.keySet().stream().filter(id -> category(id).equals(category)).toList();
            if (!entries.isEmpty()) { ids.add("@" + category); ids.addAll(entries); }
        }
        EnchantmentLayout layout = EnchantmentLayout.fit(Math.min(editorWidth(), 676), Math.min(height - 26, 360), ids.size());
        int contentWidth = layout.columns() * layout.cellWidth();
        int contentLeft = editorLeft() + (editorWidth() - contentWidth) / 2;
        panelTop = Math.max(32, (height - (layout.rows() * layout.rowHeight() + 80)) / 2 + 22);
        int barWidth = Math.min(440, editorWidth() - 16), barLeft = editorCenter() - barWidth / 2;
        int maxAllWidth = 50;
        int toggleWidth = Math.min(118, (barWidth - maxAllWidth - 8) / 2);
        button(barLeft, panelTop, toggleWidth, "Unbreakable: " + (stack.has(net.minecraft.core.component.DataComponents.UNBREAKABLE) ? "ON" : "OFF"), () -> {
            if (stack.has(net.minecraft.core.component.DataComponents.UNBREAKABLE)) stack.remove(net.minecraft.core.component.DataComponents.UNBREAKABLE);
            else stack.set(net.minecraft.core.component.DataComponents.UNBREAKABLE, net.minecraft.util.Unit.INSTANCE);
            rebuildWidgets();
        });
        int maximum = stack.getMaxDamage();
        int durabilityLeft = barLeft + toggleWidth + 4;
        int durabilityWidth = barWidth - toggleWidth - maxAllWidth - 8;
        var durability = addRenderableWidget(new net.minecraft.client.gui.components.AbstractSliderButton(durabilityLeft + 14, panelTop, Math.max(12, durabilityWidth - 28), 20,
                Component.literal(maximum > 0 ? "Durability: " + (maximum - stack.getDamageValue()) + " / " + maximum : "No durability"),
                maximum > 0 ? (maximum - stack.getDamageValue()) / (double)maximum : 0) {
            protected void updateMessage() { setMessage(Component.literal("Durability: " + (int)Math.round(value * maximum) + " / " + maximum)); }
            protected void applyValue() { if (maximum > 0) stack.setDamageValue(maximum - (int)Math.round(value * maximum)); }
        });
        durability.active = maximum > 0 && !stack.has(net.minecraft.core.component.DataComponents.UNBREAKABLE);
        var lessDurability = button(durabilityLeft, panelTop, 12, "<", () -> stepDurability(-1));
        var moreDurability = button(durabilityLeft + durabilityWidth - 12, panelTop, 12, ">", () -> stepDurability(1));
        lessDurability.active = durability.active;
        moreDurability.active = durability.active;
        lessDurability.setTooltip(Tooltip.create(Component.literal("Decrease remaining durability by 1")));
        moreDurability.setTooltip(Tooltip.create(Component.literal("Increase remaining durability by 1")));
        button(barLeft + barWidth - maxAllWidth, panelTop, maxAllWidth, "Max All", () -> {
            levels.replaceAll((id, value) -> "10");
            levelFields.values().forEach(field -> { field.setValue("10"); field.moveCursorToStart(false); });
            status = "All enchantments set to 10. Press Done to apply.";
        });
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            int x = contentLeft + (i / layout.rows()) * layout.cellWidth();
            int y = panelTop + 27 + (i % layout.rows()) * layout.rowHeight();
            if (id.startsWith("@")) {
                var heading = button(x, y, layout.cellWidth() - 4, id.substring(1), () -> {}); heading.setHeight(layout.rowHeight() - 1); heading.active = false; continue;
            }
            boolean compact = layout.cellWidth() < 100;
            int fieldWidth = compact ? 12 : 26;
            int maxWidth = compact ? 8 : 26;
            int arrowWidth = compact ? 5 : 12;
            int labelWidth = Math.max(1, layout.cellWidth() - fieldWidth - maxWidth - arrowWidth * 2 - 5);
            Component label = names.getOrDefault(id, Component.literal(id));
            EditBox level = new EditBox(font, x + labelWidth + arrowWidth + 1, y, fieldWidth, layout.rowHeight() - 1, label.copy().append(" level (0–10)"));
            level.setBordered(!compact);
            level.setMaxLength(2); level.setValue(levels.get(id)); level.setResponder(v -> levels.put(id, v));
            level.moveCursorToStart(false);
            levelFields.put(id, level);
            Component explanation = label.copy().append("\n" + EnchantmentDescriptions.describe(id));
            level.setTooltip(Tooltip.create(explanation.copy().append("\n0 removes; 1–10 applies.")));
            addRenderableWidget(level);
            var less = button(x + labelWidth, y, arrowWidth, "<", () -> stepLevel(level, -1));
            var more = button(x + labelWidth + arrowWidth + fieldWidth + 2, y, arrowWidth, ">", () -> stepLevel(level, 1));
            less.setHeight(layout.rowHeight() - 1); more.setHeight(layout.rowHeight() - 1);
            less.setTooltip(Tooltip.create(explanation.copy().append("\nDecrease level by 1 (minimum 0)")));
            more.setTooltip(Tooltip.create(explanation.copy().append("\nIncrease level by 1 (maximum 10)")));
            var max = button(x + labelWidth + fieldWidth + arrowWidth * 2 + 3, y, maxWidth, maxWidth < 24 ? "M" : "Max", () -> {
                levels.put(id, "10"); level.setValue("10"); level.moveCursorToStart(false);
                status = label.getString() + " set to 10. Press Done to apply.";
            });
            max.setHeight(layout.rowHeight() - 1);
            max.setTooltip(Tooltip.create(explanation.copy().append("\nSet to level 10")));
            var b = button(x, y, Math.max(1, labelWidth - 2), font.plainSubstrByWidth(label.getString(), Math.max(1, labelWidth - 8)), () -> setFocused(level));
            b.setHeight(layout.rowHeight() - 1); b.setTooltip(Tooltip.create(explanation.copy().append("\n" + id)));
        }
        button(barLeft, panelTop + 39 + layout.rows() * layout.rowHeight(), barWidth / 3 - 3, "Done", () -> {
            CompoundTag next = new CompoundTag();
            for (var entry : levels.entrySet()) {
                int level;
                try { level = ItemProperties.number(entry.getValue().isBlank() ? "0" : entry.getValue(), 0, 10); }
                catch (Exception ex) { throw new IllegalArgumentException(entry.getKey() + ": enter a level from 0 to 10."); }
                if (level > 0) next.putInt(entry.getKey(), level);
            }
            set(ItemProperties.set(stack, key, next)); onClose();
        });
        button(barLeft + 2 * (barWidth / 3), panelTop + 39 + layout.rows() * layout.rowHeight(), barWidth / 3 - 3, "Cancel", this::onClose);
    }
    @Override protected int titleY() { return panelTop - 22; }
    private void stepDurability(int step) {
        if (stack.getMaxDamage() <= 0 || stack.has(net.minecraft.core.component.DataComponents.UNBREAKABLE)) return;
        stack.setDamageValue(Math.clamp(stack.getDamageValue() - step, 0, stack.getMaxDamage()));
        rebuildWidgets();
    }
    private void stepLevel(EditBox field, int step) {
        int value;
        try { value = Integer.parseInt(field.getValue()); } catch (NumberFormatException ignored) { value = 0; }
        field.setValue(Integer.toString(Math.clamp(value + step, 0, 10)));
        field.moveCursorToStart(false);
    }
    @Override protected ItemStack editorPreview() {
        ItemStack preview = stack.copy();
        var values = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        var registry = mc.getConnection().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for (var entry : levels.entrySet()) {
            int level;
            try { level = ItemProperties.number(entry.getValue().isBlank() ? "0" : entry.getValue(), 0, 10); }
            catch (RuntimeException invalidLevel) { continue; }
            final int amount = level;
            registry.get(net.minecraft.resources.Identifier.parse(entry.getKey())).ifPresent(holder -> values.set(holder, amount));
        }
        preview.set(key.equals("stored_enchantments") ? net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS : net.minecraft.core.component.DataComponents.ENCHANTMENTS, values.toImmutable());
        return preview;
    }
    private static String category(String id) {
        String name = id.substring(id.indexOf(':') + 1);
        if (Set.of("mending", "unbreaking", "vanishing_curse", "binding_curse").contains(name)) return "Overall";
        if (Set.of("efficiency", "fortune", "silk_touch", "luck_of_the_sea", "lure").contains(name)) return "Tools";
        if (Set.of("protection", "fire_protection", "blast_protection", "projectile_protection", "feather_falling", "respiration", "aqua_affinity", "thorns", "depth_strider", "frost_walker", "soul_speed", "swift_sneak").contains(name)) return "Armor";
        return "Weapons";
    }}














