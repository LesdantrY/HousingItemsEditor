package dev.housingitems;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Waits for the server's actual game-mode update before changing inventory. */
public final class CreativeAccess {
    private static Runnable pending;
    private static Object connection;
    private static long requestedAt;
    private CreativeAccess() {}
    public static void request() {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null || mc.player.getAbilities().instabuild) return;
        long now = System.currentTimeMillis();
        if (connection == mc.getConnection() && now - requestedAt < 10000) return;
        connection = mc.getConnection(); requestedAt = now;
        String command = mc.getConnection().getCommands().getRoot().getChild("gmc") != null ? "gmc" : "gamemode creative";
        mc.getConnection().sendCommand(command);
        Ui.message(Component.literal("Requested /" + command + "; waiting for Creative mode."), true);
    }
    public static void run(Runnable action) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) throw new IllegalStateException("Join a world first.");
        if (mc.player.getAbilities().instabuild) { action.run(); return; }
        request(); pending = action;
    }
    public static void tick() {
        if (pending == null) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() != connection) { pending = null; return; }
        if (mc.player.getAbilities().instabuild) {
            Runnable action = pending; pending = null;
            try { action.run(); } catch (Exception e) { Ui.message(Component.literal(e.getMessage()), false); }
        } else if (System.currentTimeMillis() - requestedAt > 10000) {
            pending = null;
            Ui.message(Component.literal("Creative mode was not granted. Your draft is still open; no inventory changes were sent."), false);
        }
    }
    public static void cancel() { pending = null; }
}


