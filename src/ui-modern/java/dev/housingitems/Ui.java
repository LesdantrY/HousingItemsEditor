package dev.housingitems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
final class Ui {
    private Ui() {}
    static void open(Screen screen) { Minecraft.getInstance().gui.setScreen(screen); }
    static boolean isGameplay() { return Minecraft.getInstance().gui.screen() == null; }
    static void message(net.minecraft.network.chat.Component text, boolean overlay) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (overlay) player.sendOverlayMessage(text); else player.sendSystemMessage(text);
    }
}
