/* Ported from ate47/AdvancedCreativeTab GuiStringModifier, LGPL-3.0-only.
 * Modified 2026-10-26: modern widgets own input, preserve draft on resize, text preview. */
package fr.atesab.act.gui.modifier;

import dev.housingitems.FormattedText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

public class GuiStringModifier extends GuiModifier<String> {
    private EditBox field;
    private String value;
    public GuiStringModifier(Screen parent, Component name, String value, Consumer<String> setter) {
        super(parent, name, setter);
        this.value = value;
    }
    @Override public void init() {
        field = field(width / 2 - 99, height / 2 - 20, 198, "Text", value);
        field.setResponder(text -> value = text);
        setInitialFocus(field);
        button(width / 2 - 100, height / 2 + 12, 200, "Done", () -> { set(value); onClose(); });
        button(width / 2 - 100, height / 2 + 33, 200, "Cancel", this::onClose);
    }
    @Override public void render(GuiGraphics g, int x, int y, float delta) {
        super.render(g, x, y, delta);
        g.drawCenteredString(font, FormattedText.parse(value), width / 2, height / 2 - 42, 0xFFFFFFFF);
        g.drawCenteredString(font, "& color codes supported", width / 2, height / 2 + 65, 0xFFAAAAAA);
    }
}
