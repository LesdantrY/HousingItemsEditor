package dev.housingitems;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

/** Temporarily requests Creative mode when needed and restores the player's previous mode. */
public final class CreativeAccess {
    private static Runnable pending;
    private static Object connection;
    private static long requestedAt;
    private static GameType restoreMode;
    private static boolean restoreWhenCreative;
    private CreativeAccess() {}
    public static void prepare() {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null || mc.getConnection() == null || mc.player.getAbilities().instabuild) return;
        if (restoreMode == null) restoreMode = mc.gameMode.getPlayerMode();
        restoreWhenCreative = false;
        request();
    }
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
        if (mc.player.getAbilities().instabuild) {
            if (restoreMode == null) { action.run(); return; }
            runAndRestore(action);
            return;
        }
        if (restoreMode == null && mc.gameMode != null) restoreMode = mc.gameMode.getPlayerMode();
        restoreWhenCreative = false;
        request(); pending = action;
    }
    public static void tick() {
        if (pending == null && !restoreWhenCreative && restoreMode == null) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() != connection) { clear(); return; }
        if (mc.player.getAbilities().instabuild) {
            if (restoreWhenCreative) restore();
            else if (pending != null) {
                Runnable action = pending; pending = null;
                runAndRestore(action);
            }
        } else if (System.currentTimeMillis() - requestedAt > 10000) {
            boolean hadPending = pending != null;
            pending = null;
            if (hadPending) {
                restoreMode = null;
                restoreWhenCreative = false;
                Ui.message(Component.literal("Creative mode was not granted. Your draft is still open; no inventory changes were sent."), false);
                connection = null;
                requestedAt = 0;
            }
        }
    }
    private static void runAndRestore(Runnable action) {
        try { action.run(); }
        catch (Exception e) { Ui.message(Component.literal(e.getMessage() == null ? "Could not apply item changes." : e.getMessage()), false); }
        finally { restore(); }
    }
    private static void restore() {
        var mc = Minecraft.getInstance();
        GameType mode = restoreMode;
        clear();
        if (mode == null || mode == GameType.CREATIVE || mc.player == null || mc.getConnection() == null
                || !mc.player.getAbilities().instabuild) return;
        var commands = mc.getConnection().getCommands().getRoot();
        String alias = switch (mode) {
            case SURVIVAL -> "gms";
            case ADVENTURE -> "gma";
            case SPECTATOR -> "gmsp";
            case CREATIVE -> "gmc";
        };
        if (commands.getChild(alias) == null && commands.getChild("gamemode") == null) {
            Ui.message(Component.literal("Could not restore your previous game mode; use /" + alias + " manually."), false);
            return;
        }
        String command = commands.getChild(alias) != null ? alias : "gamemode " + mode.getName();
        mc.getConnection().sendCommand(command);
    }
    private static void clear() {
        pending = null;
        restoreMode = null;
        restoreWhenCreative = false;
        connection = null;
        requestedAt = 0;
    }
    public static void cancel() {
        pending = null;
        if (restoreMode == null) return;
        var mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getAbilities().instabuild) restore();
        else restoreWhenCreative = true;
    }
}


