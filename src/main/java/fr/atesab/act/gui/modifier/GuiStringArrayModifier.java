/* ACT-derived modifier flow, LGPL-3.0-only.
 * Modified 2026-10-27: single multiline lore document with synchronized line gutter. */
package fr.atesab.act.gui.modifier;
import dev.housingitems.LoreText;
import dev.housingitems.LoreClipboard;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.function.Consumer;

public class GuiStringArrayModifier extends GuiModifier<List<Component>> {
    private final List<Component> original;
    private String value;
    private MultiLineEditBox editor;
    private TextLayout layout;
    private static final class TextLayout extends MultilineTextField {
        TextLayout(net.minecraft.client.gui.Font font, int width) { super(font, width); }
        int[] starts() {
            java.util.List<Integer> starts = new java.util.ArrayList<>();
            for (var line : iterateLines()) starts.add(line.beginIndex());
            return starts.stream().mapToInt(Integer::intValue).toArray();
        }
    }
    private final java.util.List<Integer> lineNumbers = new java.util.ArrayList<>();
    private int left, top, editorHeight;
    public GuiStringArrayModifier(Screen parent, List<Component> values, Consumer<List<Component>> setter) {
        super(parent, Component.literal("Lore editor"), setter);
        original = List.copyOf(values); value = LoreText.text(values);
    }
    @Override public void init() {
        left = 12; top = 43; int w = width - 24, gutter = 30;
        editorHeight = height - 105;
        layout = new TextLayout(font, w - gutter - 8);
        editor = MultiLineEditBox.builder().setX(left + gutter).setY(top)
                .setPlaceholder(Component.literal("&7Write lore here. Enter starts a new lore line."))
                .build(font, w - gutter, editorHeight, Component.literal("Lore text"));
        editor.setCharacterLimit(131072);
        editor.setValue(value);
        updateLayout();
        editor.setValueListener(text -> { value = text; updateLayout(); });
        addRenderableWidget(editor);
        button(left, height - 42, 65, "Copy all", () -> {
            mc.keyboardHandler.setClipboard(value); LoreClipboard.copy(LoreText.apply(original, value)); status = "Lore copied.";
        });
        button(left + 69, height - 42, 65, "Paste", () -> editor.setValue(mc.keyboardHandler.getClipboard()));
        button(width - 146, height - 42, 64, "Done", () -> { set(LoreText.apply(original, value)); onClose(); });
        button(width - 78, height - 42, 66, "Cancel", this::onClose);
        setInitialFocus(editor);
    }
    private void updateLayout() {
        layout.setValue(value); lineNumbers.clear();
        int offset = 0, logical = 1;
        for (int begin : layout.starts()) {
            while (offset < begin) if (value.charAt(offset++) == '\n') logical++;
            lineNumbers.add(logical);
        }
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawString(font, "lore.txt  •  Enter = new line  •  & / &#RRGGBB", left, 29, 0xFFAAAAAA);
        g.fill(left, top, left + 29, top + editorHeight, 0xFF252526);
        g.enableScissor(left, top + 2, left + 29, top + editorHeight - 2);
        int first = Math.max(0, (int)editor.scrollAmount() / 9);
        int last = Math.min(lineNumbers.size(), first + editorHeight / 9 + 2);
        for (int visualRow = first; visualRow < last; visualRow++) {
            int logicalLine = lineNumbers.get(visualRow);
            int y = top + 4 + visualRow * 9 - (int)editor.scrollAmount();
            if (visualRow == 0 || logicalLine != lineNumbers.get(visualRow - 1)) {
                String number = "" + logicalLine;
                g.drawString(font, number, left + 25 - font.width(number), y, 0xFF858585);
            }
        }
        g.disableScissor();
        int count = value.isEmpty() ? 0 : 1 + (int)value.chars().filter(c -> c == '\n').count();
        g.drawString(font, count + "/256 lines", left, height - 58, count > 256 ? 0xFFFF5555 : 0xFFAAAAAA);
    }
}

