package dev.housingitems;
import fr.atesab.act.gui.modifier.GuiModifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import java.util.*;
import java.util.function.Consumer;
public final class LoreEditorScreen extends GuiModifier<ItemStack> {
    private final ItemStack stack;
    private final List<Component> original;
    private String name, text;
    private final String originalName;
    private MultiLineEditBox editor;
    private final TextHistory history;
    private String document;
    private boolean restoring;
    private net.minecraft.world.item.component.TooltipDisplay display;
    private List<net.minecraft.util.FormattedCharSequence> previewLines;
    private int previewScroll;
    private int left, top, panelWidth, panelHeight;
    public LoreEditorScreen(Screen parent, ItemStack stack, Consumer<ItemStack> setter) {
        super(parent, Component.literal("Name and Lore"), setter); this.stack = stack.copy();
        original = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines();
        name = originalName = FormattedText.editable(stack.getHoverName()); text = LoreText.text(original);
        document = new NameLoreText(name, text).text(); history = new TextHistory(document);
        display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, net.minecraft.world.item.component.TooltipDisplay.DEFAULT);
    }
    @Override protected void init() {
        initEditorTabs("Lore");
        panelWidth = (Math.min(720, editorWidth() - 32) - 12) / 2;
        int w = panelWidth * 2 + 12;
        panelHeight = Math.max(40, Math.min(210, height - 194));
        top = Math.max(62, (height - (panelHeight + 95) + 40) / 2);
        previewLines = null;
        left = editorLeft() + (editorWidth() - w) / 2;
        editor = MultiLineEditBox.builder().setX(left + panelWidth + 12).setY(top).setShowDecorations(false).setPlaceholder(Component.literal("First line: name. Remaining lines: lore."))
            .build(font,panelWidth,panelHeight + 27,Component.literal("Name and lore editor: first line is the name"));
        editor.setCharacterLimit(131072); editor.setValue(document); editor.setValueListener(v -> {
            document = v; var parts = NameLoreText.parse(v); name = parts.name(); text = parts.lore();
            previewLines = null; if (!restoring) history.record(v);
        });
        addRenderableWidget(editor); setInitialFocus(editor);
        int toggleWidth = (w - 8) / 3;
        flagButton(left, toggleWidth, "Enchants", HideFlagsScreen.ENCHANTMENTS);
        flagButton(left + toggleWidth + 4, toggleWidth, "Modifiers", HideFlagsScreen.MODIFIERS);
        flagButton(left + 2 * (toggleWidth + 4), w - 2 * (toggleWidth + 4), "Additional", HideFlagsScreen.ADDITIONAL);
        button(left,top+panelHeight+75, panelWidth,"Done",()-> {
            set(LoreDraft.apply(stack, name, text, display)); onClose();
        });
        button(left+panelWidth+12,top+panelHeight+75,panelWidth,"Cancel",this::onClose);
    }
    private void flagButton(int x, int w, String label, List<net.minecraft.core.component.DataComponentType<?>> types) {
        long hidden = types.stream().filter(display.hiddenComponents()::contains).count();
        String state = hidden == types.size() ? "Hidden" : hidden == 0 ? "Shown" : "Mixed";
        String caption = font.plainSubstrByWidth(label, Math.max(10, w - font.width(": " + state) - 8)) + ": " + state;
        button(x, top+panelHeight+51, w, caption, () -> {
            for (var type : types) display = display.withHidden(type, hidden != types.size());
            rebuildWidgets();
        }).setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(label + ": " + state + "\n" + switch (label) {
            case "Enchants" -> "Shows or hides enchantment names and levels.";
            case "Modifiers" -> "Shows or hides attribute values such as attack damage and speed.";
            default -> "Shows or hides extra details such as Unbreakable, dyes, trims, potion effects and fireworks.";
        } + "\nThis changes the tooltip only; item effects stay active.\nMixed means some details in this group are hidden and others are shown.\nClick to " + (hidden == types.size() ? "show" : "hide") + " all details in this group.")));
    }
    @Override protected ItemStack editorPreview() { return LoreDraft.apply(stack, name, text, display); }
    private void updatePreview() {
        if (previewLines != null) return;
        ItemStack preview = stack.copy();
        if (name.isBlank()) preview.remove(DataComponents.CUSTOM_NAME);
        else if (!name.equals(originalName)) preview.set(DataComponents.CUSTOM_NAME, FormattedText.parse(name));
        preview.set(DataComponents.TOOLTIP_DISPLAY, display);
        var lore = Arrays.stream(text.split("\n", -1)).limit(256).map(FormattedText::parse).toList();
        if (text.isEmpty()) preview.remove(DataComponents.LORE); else preview.set(DataComponents.LORE, new ItemLore(lore));
        var tooltip = preview.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(mc.level), mc.player, net.minecraft.world.item.TooltipFlag.NORMAL);
        previewLines = new ArrayList<>();
        for (int i = 0; i < tooltip.size(); i++) {
            var wrapped = font.split(tooltip.get(i), panelWidth - 8);
            if (wrapped.isEmpty()) previewLines.add(Component.literal(" ").getVisualOrderText()); else previewLines.addAll(wrapped);
        }
        previewScroll = Math.min(previewScroll, Math.max(0, previewLines.size() * 10 - panelHeight - 19));
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x >= left && x < left + panelWidth && y >= top && y < top+27+panelHeight) {
            updatePreview();
            previewScroll = Math.max(0, Math.min(Math.max(0, previewLines.size()*10-panelHeight-19), previewScroll-(int)(vertical*20)));
            return true;
        }
        return super.mouseScrolled(x,y,horizontal,vertical);
    }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        boolean control = (event.modifiers() & org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (event.modifiers() & org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT) != 0;
        if (control && (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_Z || event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_Y)
                && editor.isFocused()) {
            boolean redo = shift || event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_Y;
            restoring = true;
            try {
                String value = redo ? history.redo() : history.undo();
                editor.setValue(value);
            } finally { restoring = false; }
            return true;
        }
        return super.keyPressed(event);
    }
    @Override protected int titleY() { return top - 58; }
    private void drawFormattingHelp(GuiGraphics g, int mx, int my) {
        int w = Math.min(260, panelWidth * 2 + 12);
        int helpLeft = editorCenter() - w / 2;
        int colorStep = Math.min(22, w / 8);
        int colorLeft = editorCenter() - colorStep * 4;
        g.fill(helpLeft - 4, top - 47, helpLeft + w + 4, top - 2, 0xEF181820);
        String codes = "0123456789abcdef";
        String[] colors = {"Black", "Dark blue", "Dark green", "Dark aqua", "Dark red", "Dark purple", "Gold", "Gray", "Dark gray", "Blue", "Green", "Aqua", "Red", "Light purple", "Yellow", "White"};
        for (int i = 0; i < codes.length(); i++) {
            int x = colorLeft + (i % 8) * colorStep, y = top - 44 + (i / 8) * 11;
            int rgb = LegacyColors.rgb(net.minecraft.ChatFormatting.getByCode(codes.charAt(i)));
            g.fill(x - 1, y - 1, x + 14, y + 9, i < 6 || i == 8 ? 0xFFAAAAAA : 0xFF303038);
            g.drawString(font, "&" + codes.charAt(i), x, y, 0xFF000000 | rgb, false);
            if (mx >= x && mx < x + colorStep && my >= y && my < y + 10)
                g.setTooltipForNextFrame(font, Component.literal("&" + codes.charAt(i) + " — " + colors[i]), mx, my);
        }
        String[] formats = {"&k Random", "&l Bold", "&m Strike", "&n Underline", "&o Italic", "&r Reset"};
        for (int i = 0; i < formats.length; i++) {
            int x = helpLeft + (i % 3) * w / 3, y = top - 23 + (i / 3) * 10;
            String label = font.plainSubstrByWidth(formats[i], w / 3 - 3);
            g.drawString(font, label, x + (w / 3 - font.width(label)) / 2, y, 0xFFCCCCCC);
            if (mx >= x && mx < x + w / 3 && my >= y && my < y + 10)
                g.setTooltipForNextFrame(font, Component.literal(formats[i]), mx, my);
        }
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt) {
        super.render(g,mx,my,dt);
        drawFormattingHelp(g, mx, my);
        g.fill(left,top,left+panelWidth,top+panelHeight+27,0xE0181820);
        int y = top+4;
        g.enableScissor(left+4,top+4,left+panelWidth-4,top+panelHeight+23);
        updatePreview();
        int loreY=y-previewScroll;
        for (var line : previewLines) {
            if(loreY+font.lineHeight>y && loreY<top+panelHeight+23) g.drawString(font,line,left+4,loreY,0xFFAAAAAA);
            loreY+=10;
        }
        g.disableScissor();
    }
}





