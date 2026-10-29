package dev.housingitems;
import fr.atesab.act.gui.GuiACT;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class DataScreen extends GuiACT {
    @FunctionalInterface public interface Apply { void accept(String value) throws Exception; }
    private final Apply apply;
    private String value;
    private final boolean importing;
    public DataScreen(Screen parent,String title,String value,Apply apply) {
        super(parent,Component.literal(title)); this.value=value; this.apply=apply; importing = title.equals("Import item SNBT");
    }
    @Override protected void init() {
        int w=Math.min(620,width-32),x=(width-w)/2;
        var input=MultiLineEditBox.builder().setX(x).setY(40).setPlaceholder(Component.literal("Item data")).build(font,w,height-104,Component.literal("SNBT"));
        input.setCharacterLimit(131072); input.setValue(value); input.setValueListener(v->value=v); addRenderableWidget(input);
        button(x,height-42,90,importing ? "Save" : "Done",()->{apply.accept(value); if (importing) mc.setScreen(parent); else onClose();});
        button(x+w-90,height-42,90,"Cancel",this::onClose); setInitialFocus(input);
    }
    @Override public void complete() throws Exception { applyDraft(); mc.setScreen(parent); }
    @Override protected String backLabel() { return "Back"; }
    @Override public void back() throws Exception {
        if (importing) { CreativeAccess.cancel(); mc.setScreen(parent); }
        else { CreativeAccess.cancel(); applyDraft(); mc.setScreen(parent); }
    }
}




