package dev.housingitems;
import fr.atesab.act.gui.GuiACT;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;

public final class ChoiceScreen extends GuiACT {
    public record Choice(String id, Component label) {}
    private final List<Choice> choices;
    private final Consumer<String> select;
    private String search = "";
    private int page;
    public ChoiceScreen(Screen parent, String title, List<Choice> choices, Consumer<String> select) {
        super(parent, Component.literal(title)); this.choices = choices; this.select = select;
    }
    @Override protected void init() {
        int w = Math.min(400, width - 24), x = (width - w) / 2;
        var input = field(x, 33, w - 64, "Search", search); input.setResponder(v -> search = v);
        button(x + w - 60, 33, 60, "Search", () -> { page = 0; rebuildWidgets(); });
        var matches = choices.stream().filter(c -> (c.id() + " " + c.label().getString()).toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))).toList();
        int rows = Math.max(1, (height - 115) / 23), perPage = rows * 2;
        page = Math.max(0, Math.min(page, Math.max(0, (matches.size() - 1) / perPage)));
        for (int i = page * perPage; i < Math.min(matches.size(), (page + 1) * perPage); i++) {
            Choice choice = matches.get(i); int cell = i % perPage;
            button(x + cell % 2 * (w / 2), 61 + cell / 2 * 23, w / 2 - 2, font.plainSubstrByWidth(choice.label().getString(), w / 2 - 10), () -> { select.accept(choice.id()); onClose(); })
                .setTooltip(Tooltip.create(choice.label().copy().append("\n" + choice.id())));
        }
        button(x, height - 42, 28, "<", () -> { page--; rebuildWidgets(); }).active = page > 0;
        button(x + 32, height - 42, 28, ">", () -> { page++; rebuildWidgets(); }).active = (page + 1) * perPage < matches.size();
        button(width / 2 - 40, height - 42, 80, "Done", () -> {});
        button(x + w - 90, height - 42, 90, "Cancel", this::onClose);
    }
}


