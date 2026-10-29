package dev.housingitems;
import fr.atesab.act.gui.GuiGiver;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
/** Entry point into the ported ACT giver/editor workflow. */
public final class EditorScreen extends GuiGiver {
    public EditorScreen(Screen parent, ItemStack initial) { super(parent, initial); }
}
