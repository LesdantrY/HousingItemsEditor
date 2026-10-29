package dev.housingitems;
import fr.atesab.act.gui.modifier.GuiModifier;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.function.Consumer;
public final class DurabilityScreen extends GuiModifier<ItemStack> {
    private final ItemStack stack;
    private int remaining;
    public DurabilityScreen(Screen parent, ItemStack stack, Consumer<ItemStack> setter) {
        super(parent, Component.literal("Durability"), setter); this.stack = stack.copy();
        remaining = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
    }
    @Override protected void init() {
        int x = width / 2 - 130, y = height / 2 - 24, maximum = stack.getMaxDamage();
        var slider = addRenderableWidget(new AbstractSliderButton(x,y,260,20,Component.literal(maximum > 0 ? "Durability: " + remaining + " / " + maximum : "This item has no durability"), remaining / (double)Math.max(1, maximum)) {
            protected void updateMessage() { setMessage(Component.literal("Durability: " + (int)Math.round(value * maximum) + " / " + maximum)); }
            protected void applyValue() { remaining = (int)Math.round(value * maximum); }
        });
        slider.active = maximum > 0;
        button(x,y+32,128,"Done",()-> { if (maximum > 0) stack.setDamageValue(maximum - remaining); set(stack); onClose(); });
        button(x+132,y+32,128,"Cancel",this::onClose);
    }
}

