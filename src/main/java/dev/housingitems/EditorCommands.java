package dev.housingitems;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import java.util.function.UnaryOperator;
import static dev.housingitems.ClientCommands.*;

final class EditorCommands {
    static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        registerHousingTools(dispatcher);
        for (String group : new String[]{"Enchants", "Modifiers", "Additional"}) {
            dispatcher.register(literal("hie").then(literal("flags").then(literal(group)
                .then(argument("hidden", BoolArgumentType.bool()).executes(ctx -> mutate(ctx.getSource(), stack ->
                    setFlags(stack, group, BoolArgumentType.getBool(ctx, "hidden"))))))));
        }
        dispatcher.register(literal("hie")
            .then(literal("rename").then(argument("name", StringArgumentType.greedyString()).executes(ctx -> mutate(ctx.getSource(), stack -> {
                stack.set(DataComponents.CUSTOM_NAME, FormattedText.parse(StringArgumentType.getString(ctx, "name"))); return stack;
            }))))
            .then(literal("unbreakable").then(argument("enabled", BoolArgumentType.bool()).executes(ctx -> mutate(ctx.getSource(), stack -> {
                if (BoolArgumentType.getBool(ctx, "enabled")) stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
                else stack.remove(DataComponents.UNBREAKABLE); return stack;
            }))))
            .then(literal("enchant").then(argument("enchantment", net.minecraft.commands.arguments.IdentifierArgument.id()).suggests((ctx, builder) -> {
                var connection = Minecraft.getInstance().getConnection();
                if (connection == null) return builder.buildFuture();
                return net.minecraft.commands.SharedSuggestionProvider.suggestResource(connection.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).keySet(), builder);
            })
                .then(argument("level", IntegerArgumentType.integer(1, 10)).suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(java.util.stream.IntStream.rangeClosed(1, 10).mapToObj(Integer::toString), builder)).executes(ctx -> mutate(ctx.getSource(), stack -> {
                    try {
                        String id = ctx.getArgument("enchantment", Identifier.class).toString();
                        CompoundTag root = TagParser.parseCompoundFully(ItemData.encode(stack));
                        CompoundTag components = root.getCompound("components").orElseGet(CompoundTag::new);
                        CompoundTag enchants = components.getCompound("minecraft:enchantments").orElseGet(CompoundTag::new).copy();
                        enchants.putInt(id, IntegerArgumentType.getInteger(ctx, "level"));
                        components.put("minecraft:enchantments", enchants);
                        root.put("components", components);
                        return ItemData.decode(root.toString());
                    } catch (Exception e) { throw new IllegalArgumentException(e.getMessage()); }
                })))))
            .then(literal("help").executes(ctx -> {
                ctx.getSource().sendFeedback(Component.literal("/hie edit | library | rename <name> | enchant <id> <1–10> | unbreakable <true/false> | flags <Enchants/Modifiers/Additional> <true/false> (true hides, false shows) | durability <remaining> | itemtype <id> | itemmodel <id/reset> | saveItem | copyLore | pasteLore | lore add <text> | lore edit <line> <text> | lore remove <line>"));
                return 1;
            })));
    }

    private static void registerHousingTools(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("hie")
            .then(literal("durability").then(argument("remaining", IntegerArgumentType.integer(0)).executes(ctx -> mutate(ctx.getSource(), stack -> {
                int remaining = IntegerArgumentType.getInteger(ctx, "remaining");
                if (stack.getMaxDamage() <= 0 || remaining > stack.getMaxDamage()) throw new IllegalArgumentException("Remaining durability must be 0–" + stack.getMaxDamage() + " for this item.");
                stack.setDamageValue(stack.getMaxDamage() - remaining); return stack;
            }))))
            .then(literal("itemtype").then(argument("id", StringArgumentType.word()).executes(ctx -> mutate(ctx.getSource(), stack -> ItemProperties.type(stack, StringArgumentType.getString(ctx, "id"))))))
            .then(literal("itemmodel").then(argument("id", StringArgumentType.word()).executes(ctx -> mutate(ctx.getSource(), stack -> {
                String id = StringArgumentType.getString(ctx, "id");
                if (id.equals("reset")) ItemProperties.resetModel(stack); else stack.set(DataComponents.ITEM_MODEL, Identifier.parse(id)); return stack;
            }))))
            .then(literal("saveItem").executes(ctx -> {
                try {
                    var player = Minecraft.getInstance().player;
                    if (player == null || player.getMainHandItem().isEmpty()) throw new IllegalArgumentException("Hold an item first.");
                    var stack = player.getMainHandItem();
                    ItemLibrary.save(stack.getHoverName().getString(), ItemData.encode(stack));
                    ctx.getSource().sendFeedback(savedMessage(stack)); return 1;
                } catch (Exception e) { ctx.getSource().sendError(Component.literal(e.getMessage())); return 0; }
            }))
            .then(literal("copyLore").executes(ctx -> {
                var player = Minecraft.getInstance().player; if (player == null) return 0;
                LoreClipboard.copy(player.getMainHandItem().getOrDefault(DataComponents.LORE, net.minecraft.world.item.component.ItemLore.EMPTY).lines());
                ctx.getSource().sendFeedback(Component.literal("Lore copied with formatting.")); return 1;
            }))
            .then(literal("pasteLore").executes(ctx -> mutate(ctx.getSource(), stack -> {
                stack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(LoreClipboard.read())); return stack;
            })))
            .then(literal("lore")
                .then(literal("add").then(argument("text", StringArgumentType.greedyString()).executes(ctx -> mutate(ctx.getSource(), stack -> {
                    var lines = new java.util.ArrayList<>(stack.getOrDefault(DataComponents.LORE, net.minecraft.world.item.component.ItemLore.EMPTY).lines());
                    if (lines.size() >= 256) throw new IllegalArgumentException("Maximum 256 lore lines.");
                    lines.add(FormattedText.parse(StringArgumentType.getString(ctx, "text"))); stack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lines)); return stack;
                }))))
                .then(literal("remove").then(argument("line", IntegerArgumentType.integer(1)).executes(ctx -> mutate(ctx.getSource(), stack -> changeLore(stack, IntegerArgumentType.getInteger(ctx, "line"), null)))))
                .then(literal("edit").then(argument("line", IntegerArgumentType.integer(1)).then(argument("text", StringArgumentType.greedyString()).executes(ctx -> mutate(ctx.getSource(), stack -> changeLore(stack, IntegerArgumentType.getInteger(ctx, "line"), StringArgumentType.getString(ctx, "text")))))))));
    }

    static Component savedMessage(ItemStack stack) {
        return Component.empty().append(stack.getHoverName().copy()).append(Component.literal(" saved to library"));
    }
    static ItemStack setFlags(ItemStack stack, String group, boolean hidden) {
        var types = switch (group) {
            case "Enchants" -> HideFlagsScreen.ENCHANTMENTS;
            case "Modifiers" -> HideFlagsScreen.MODIFIERS;
            case "Additional" -> HideFlagsScreen.ADDITIONAL;
            default -> throw new IllegalArgumentException("Unknown flag group: " + group);
        };
        var display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, net.minecraft.world.item.component.TooltipDisplay.DEFAULT);
        for (var type : types) display = display.withHidden(type, hidden);
        stack.set(DataComponents.TOOLTIP_DISPLAY, display);
        return stack;
    }
    private static ItemStack changeLore(ItemStack stack, int line, String text) {
        var lines = new java.util.ArrayList<>(stack.getOrDefault(DataComponents.LORE, net.minecraft.world.item.component.ItemLore.EMPTY).lines());
        if (line > lines.size()) throw new IllegalArgumentException("There is no lore line " + line);
        if (text == null) lines.remove(line - 1); else lines.set(line - 1, FormattedText.parse(text));
        stack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lines)); return stack;
    }

    private static ItemStack component(ItemStack stack, String key, String value) {
        try {
            CompoundTag root = TagParser.parseCompoundFully(ItemData.encode(stack));
            CompoundTag components = root.getCompound("components").orElseGet(CompoundTag::new);
            components.put("minecraft:" + key, TagParser.parseCompoundFully("{value:" + value + "}").get("value"));
            root.put("components", components);
            return ItemData.decode(root.toString());
        } catch (Exception e) { throw new IllegalArgumentException(e.getMessage()); }
    }

    private static int mutate(FabricClientCommandSource source, UnaryOperator<ItemStack> update) {
        try {
            ItemData.requireCreative();
            var player = Minecraft.getInstance().player;
            ItemStack original = player.getMainHandItem().copy();
            if (original.isEmpty()) throw new IllegalArgumentException("Hold an item first.");
            ItemStack edited = update.apply(original.copy());
            ItemData.replaceHeld(ItemData.decode(ItemData.encode(edited)), player.getInventory().getSelectedSlot(), original);
            source.sendFeedback(Component.literal("Item updated."));
            return 1;
        } catch (Exception e) { source.sendError(Component.literal(e.getMessage())); return 0; }
    }
}





