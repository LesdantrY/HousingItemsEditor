/* Derived from ate47/AdvancedCreativeTab, LGPL-3.0-only.
 * Modified 2026-10-26: Fabric 1.21.11 rendering/input and shared editor widgets. */
package fr.atesab.act.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GuiACT extends Screen {
    protected Screen parent;
    protected Minecraft mc;
    protected String status = "";
    private Action draftAction;
    private boolean suppressClose;
    private static boolean tabsVisible = true;
    private String activeTab;
    private int sidebarWidth() { return Math.min(136, Math.max(88, width / 5)); }
    private int frameLeft() { return Math.max(0, (width - sidebarWidth() - 36 - 752) / 2); }
    private int sidebarTop() { return Math.max(28, (height - 215) / 2); }
    protected int editorLeft() { return parent instanceof GuiGiver && tabsVisible ? frameLeft() + sidebarWidth() + 36 : 0; }
    protected int editorWidth() { return parent instanceof GuiGiver && tabsVisible ? width - editorLeft() - frameLeft() : width; }
    protected int editorCenter() { return editorLeft() + editorWidth() / 2; }
    protected void initEditorTabs(String selected) {
        if (!(parent instanceof GuiGiver root)) return;
        activeTab = selected;
        button(width - 174, height - 36, 80, "Save Item", () -> { applyDraft(); root.saveToLibrary(); status = "Saved to library."; })
                .setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Saved items go in Library. You can also use /hie saveItem.")));
        button(width - 90, height - 36, 80, "Library", () -> { applyDraft(); root.openTab("Library"); })
                .setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("View all saved items")));
        button(6, 6, 24, tabsVisible ? "‹" : "›", () -> { tabsVisible = !tabsVisible; rebuildWidgets(); }).setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(tabsVisible ? "Hide left-side tabs" : "Show left-side tabs")));
        if (!tabsVisible) {
            editorDoneButton(6, height - 36, 100);
            return;
        }
        int w = sidebarWidth(), x = frameLeft() + 6;
        String[] tabs = {"Lore", "Enchantments", "Item Model", "Item Type", "Item Color"};
        String[] tips = {"Edit item description and display", "Add or remove enchantments; edit Unbreakable and durability", "Change item appearance on clients that support item models. Not visible on legacy 1.8.9 clients.", "Change the base item", "Only works on leather armor"};
        int y = sidebarTop();
        for (int i = 0; i < tabs.length; i++) {
            String tab = tabs[i];
            String label = font.plainSubstrByWidth(tab, w - 8);
            var b = button(x, y + i * 25, w, label, () -> { dev.housingitems.CreativeAccess.cancel(); applyDraft(); root.openTab(tab); });
            b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(tips[i])));
            b.setOverrideRenderHighlightedSprite(() -> tab.equals(activeTab) || b.isHovered());
        }
        int libraryY = Math.min(height - 61, y + 170);
        editorDoneButton(x, libraryY + 25, w);
    }
    private void editorDoneButton(int x, int y, int w) {
        var done = addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            try { complete(); } catch (Exception e) { status = e.getMessage() == null ? "Invalid value" : e.getMessage(); }
        }).bounds(x,y,w,20).build());
        done.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Save changes and close")));
    }
    public GuiACT(Screen parent, Component name) {
        super(name);
        this.parent = parent;
        mc = Minecraft.getInstance();
    }
    public Screen getParent() { return parent; }
    public void setParent(Screen parent) { this.parent = parent; }
    public void open(Screen screen) { mc.setScreen(screen); }
    public Button button(int x, int y, int w, String label, Action action) {
        boolean done = label.equals("Done"), back = label.equals("Cancel") && !(this instanceof GuiGiver);
        if (done) draftAction = action;
        var widget = Button.builder(Component.literal(back ? backLabel() : label), b -> {
            try { if (done) complete(); else if (back) back(); else action.run(); } catch (Exception e) {
                String error = e.getMessage() == null ? "Invalid value" : e.getMessage();
                status = error; if (mc.screen instanceof GuiACT current) current.status = error;
            }
        }).bounds(x, y, w, 20).build();
        // Keep the tab's draft callback, but use the single sidebar completion button.
        if (parent instanceof GuiGiver && (done || back)) return widget;
        return addRenderableWidget(widget);
    }
    protected EditBox field(int x, int y, int w, String label, String value) {
        EditBox box = new EditBox(font, x, y, w, 20, Component.literal(label));
        box.setMaxLength(8192);
        box.setValue(value);
        return addRenderableWidget(box);
    }
    protected void item(GuiGraphics graphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack, x, y);
        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16)
            graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        if (parent instanceof GuiGiver && tabsVisible) {
            ItemStack preview;
            try { preview = editorPreview(); } catch (RuntimeException invalidDraft) { preview = ((GuiGiver)parent).currentItem(); }
            int x = frameLeft() + 6, y = sidebarTop();
            item(graphics, preview, x + (sidebarWidth() - 16) / 2, y - 22, mouseX, mouseY);
            item(graphics, preview, x + sidebarWidth() + 4, y + 52, mouseX, mouseY);
            item(graphics, dev.housingitems.ItemProperties.typePreview(preview), x + sidebarWidth() + 4, y + 77, mouseX, mouseY);
        }
        graphics.drawCenteredString(font, title, editorCenter(), titleY(), 0xFFFFAA00);
        if (!status.isEmpty()) graphics.drawCenteredString(font, font.plainSubstrByWidth(status, width - 20), width / 2, height - 14, 0xFFFFAA55);
    }
    protected ItemStack editorPreview() { return parent instanceof GuiGiver root ? root.currentItem() : ItemStack.EMPTY; }
    protected void applyDraft() throws Exception {
        if (draftAction == null) return;
        suppressClose = true;
        try { draftAction.run(); } finally { suppressClose = false; }
    }
    protected int titleY() { return 14; }
    public void complete() throws Exception {
        applyDraft();
        if (parent instanceof GuiACT editor) { editor.complete(); } else mc.setScreen(parent);
    }
    protected String backLabel() { return parent instanceof GuiGiver ? "Close" : "Back"; }
    public void back() throws Exception {
        dev.housingitems.CreativeAccess.cancel();
        if (parent instanceof GuiGiver) { mc.setScreen(null); return; }
        applyDraft(); mc.setScreen(parent);
    }
    protected void showMainMenu() throws Exception {
        Screen target = parent;
        while (target instanceof GuiACT editor && !(target instanceof GuiGiver)) {
            editor.applyDraft(); target = editor.parent;
        }
        GuiGiver.rememberMainMenu();
        if (target instanceof GuiGiver) mc.setScreen(target);
        else if (mc.player != null) mc.setScreen(new dev.housingitems.EditorScreen(null, mc.player.getMainHandItem()));
        else mc.setScreen(null);
    }
    @Override public void onClose() { if (!suppressClose) { dev.housingitems.CreativeAccess.cancel(); mc.setScreen(parent instanceof GuiGiver ? null : parent); } }
    @Override public boolean isPauseScreen() { return true; }
    @FunctionalInterface public interface Action { void run() throws Exception; }
}







