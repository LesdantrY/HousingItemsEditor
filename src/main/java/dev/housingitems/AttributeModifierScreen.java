package dev.housingitems;
import fr.atesab.act.gui.modifier.GuiModifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import java.util.*;
import java.util.function.Consumer;

public final class AttributeModifierScreen extends GuiModifier<CompoundTag> {
    private final CompoundTag entry;
    private String type, amount, operation, slot;
    private final String originalAmount;
    private int left, top, w;
    private static final String[] OPS = {"add_value", "add_multiplied_base", "add_multiplied_total"};
    private static final String[] OP_NAMES = {"Add a fixed amount", "Add a fraction of base", "Multiply total"};
    private static final String[] SLOTS = {"any", "mainhand", "offhand", "hand", "armor", "head", "chest", "legs", "feet", "body", "saddle"};
    public AttributeModifierScreen(Screen parent, CompoundTag value, Consumer<CompoundTag> setter) {
        super(parent, Component.literal("Attribute modifier amount"), setter); entry = value.copy();
        type = entry.getString("type").orElse("minecraft:attack_damage");
        amount = AttributeNumbers.format(entry.getDouble("amount").orElse(1d));
        originalAmount = amount;
        operation = entry.getString("operation").orElse("add_value");
        slot = entry.getString("slot").orElse("mainhand");
    }
    static String name(String id) {
        var key = Identifier.tryParse(id);
        if (key == null || !BuiltInRegistries.ATTRIBUTE.containsKey(key)) return id;
        return Component.translatable(BuiltInRegistries.ATTRIBUTE.getValue(key).getDescriptionId()).getString();
    }
    @Override protected void init() {
        w = Math.min(360, width - 28); left = (width - w) / 2; top = Math.max(33, height / 2 - 75);
        button(left, top, w, name(type), () -> {
            var choices = BuiltInRegistries.ATTRIBUTE.keySet().stream().map(id -> new ChoiceScreen.Choice(id.toString(), Component.literal(name(id.toString()))))
                .sorted(Comparator.comparing(c -> c.label().getString(), String.CASE_INSENSITIVE_ORDER)).toList();
            open(new ChoiceScreen(this, "Choose attribute", choices, id -> type = id));
        });
        int op = Math.max(0, Arrays.asList(OPS).indexOf(operation));
        button(left, top + 24, w, "Operation: " + OP_NAMES[op], () -> { operation = OPS[(op + 1) % OPS.length]; rebuildWidgets(); });
        button(left, top + 48, w, "When equipped: " + slot, () -> {
            slot = SLOTS[(Arrays.asList(SLOTS).indexOf(slot) + 1) % SLOTS.length]; rebuildWidgets();
        });
        button(left, top + 75, 30, "−", () -> adjust(-1));
        var input = field(left + 35, top + 75, w - 70, "Modifier amount", amount); input.setResponder(v -> amount = v);
        button(left + w - 30, top + 75, 30, "+", () -> adjust(1));
        button(left, top + 123, w / 2 - 2, "Done", () -> {
            double value = amount.equals(originalAmount) ? entry.getDouble("amount").orElse(1d) : Double.parseDouble(amount); if (!Double.isFinite(value)) throw new IllegalArgumentException("Amount must be a finite number.");
            CompoundTag next = entry.copy(); next.putString("type", type); next.putDouble("amount", value);
            next.putString("operation", operation); next.putString("slot", slot);
            if (!next.contains("id")) next.putString("id", "housing_items_editor:" + UUID.randomUUID());
            set(next); onClose();
        });
        button(left + w / 2, top + 123, w / 2, "Cancel", this::onClose);
    }
    private void adjust(int direction) {
        double current = Double.parseDouble(amount), next = current + direction * (operation.equals("add_value") ? 1 : .1);
        if (!Double.isFinite(next)) throw new IllegalArgumentException("Amount must be finite.");
        amount = java.math.BigDecimal.valueOf(next).setScale(6, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(); rebuildWidgets();
    }
    @Override public void render(GuiGraphics g, int x, int y, float delta) {
        super.render(g, x, y, delta);
        String hint = "Modifier amount, not the final attribute value.";
        try {
            double base = BuiltInRegistries.ATTRIBUTE.getValue(Identifier.parse(type)).getDefaultValue();
            double change = Double.parseDouble(amount);
            double total = operation.equals("add_value") ? base + change : base * (1 + change);
            hint = "Default base " + AttributeNumbers.format(base) + (operation.equals("add_value") ? " + (" : " × (1 + ") + AttributeNumbers.format(change) + ") = " + AttributeNumbers.format(total);
        } catch (Exception ignored) {}
        g.drawCenteredString(font, hint, width / 2, top + 103, 0xFFAAAAAA);
        g.drawCenteredString(font, "Other equipped modifiers may also affect the total.", width / 2, top + 113, 0xFF888888);
    }
}



