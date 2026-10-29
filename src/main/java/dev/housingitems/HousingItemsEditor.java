package dev.housingitems;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static dev.housingitems.ClientCommands.literal;

public final class HousingItemsEditor implements ClientModInitializer {
    private static boolean pendingEditor, pendingLibrary;

    @Override public void onInitializeClient() {
        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("housing_items_editor", "editor"));
        KeyMapping edit = FabricCompat.key(new KeyMapping("key.housing_items_editor.edit",
                InputConstants.KEY_H, category));
        KeyMapping library = FabricCompat.key(new KeyMapping("key.housing_items_editor.library",
                InputConstants.KEY_N, category));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            CreativeAccess.tick();
            while (edit.consumeClick()) if (Ui.isGameplay()) pendingEditor = true;
            while (library.consumeClick()) if (Ui.isGameplay()) pendingLibrary = true;
            if (mc.player != null) {
                if (pendingEditor) {
                    var screen = new EditorScreen(null, mc.player.getMainHandItem());
                    Ui.open(screen);
                    try { screen.reopenLastMenu(); } catch (Exception ignored) { Ui.open(screen); }
                }
                else if (pendingLibrary) Ui.open(new LibraryScreen(null));
            }
            pendingEditor = pendingLibrary = false;
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            EditorCommands.register(dispatcher);
            dispatcher.register(literal("hie").executes(ctx -> { pendingEditor = true; return 1; })
                    .then(literal("edit").executes(ctx -> { pendingEditor = true; return 1; }))
                    .then(literal("library").executes(ctx -> { pendingLibrary = true; return 1; })));

        });
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            HousingData.tooltip(stack, lines);
            if (!type.isAdvanced()) return;
            if (stack.isDamageableItem()) lines.add(Component.literal("Durability: " + (stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage()));
            Integer repairCost = stack.get(DataComponents.REPAIR_COST);
            if (repairCost != null && repairCost > 0 && stack.getMaxDamage() > 0) lines.add(Component.literal("Repair cost: " + repairCost));
        });
    }
}



