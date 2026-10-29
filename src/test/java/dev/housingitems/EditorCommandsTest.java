package dev.housingitems;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EditorCommandsTest {
    @org.junit.jupiter.api.BeforeAll static void bootstrap() { TestBootstrap.initialize(); }
    @Test void commandTreeUsesDurabilityAndOmitsRemovedCommands() {
        var dispatcher = new CommandDispatcher<FabricClientCommandSource>();
        EditorCommands.register(dispatcher);
        var hie = dispatcher.getRoot().getChild("hie");
        assertNotNull(hie.getChild("durability"));
        assertNull(hie.getChild("damage")); assertNull(hie.getChild("head"));
        assertNull(hie.getChild("armortrim"));
        assertNull(hie.getChild("color")); assertNull(hie.getChild("count"));
    }
    @Test void enchantmentLevelCompletionListsOneThroughTen() throws Exception {
        var dispatcher = new CommandDispatcher<FabricClientCommandSource>();
        EditorCommands.register(dispatcher);
        var suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("hie enchant minecraft:sharpness ", null)).get();
        var actual = suggestions.getList().stream().map(s -> s.getText()).collect(java.util.stream.Collectors.toSet());
        assertEquals(java.util.stream.IntStream.rangeClosed(1, 10).mapToObj(Integer::toString).collect(java.util.stream.Collectors.toSet()), actual);
    }
    @Test void flagsCompleteGroupsAndBooleans() throws Exception {
        var dispatcher = new CommandDispatcher<FabricClientCommandSource>();
        EditorCommands.register(dispatcher);
        var groups = dispatcher.getCompletionSuggestions(dispatcher.parse("hie flags ", null)).get();
        assertEquals(java.util.Set.of("Enchants", "Modifiers", "Additional"), groups.getList().stream().map(s -> s.getText()).collect(java.util.stream.Collectors.toSet()));
        var values = dispatcher.getCompletionSuggestions(dispatcher.parse("hie flags Enchants ", null)).get();
        assertEquals(java.util.Set.of("true", "false"), values.getList().stream().map(s -> s.getText()).collect(java.util.stream.Collectors.toSet()));
    }
    @Test void flagsOnlyChangeRequestedTooltipGroup() {
        var stack = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
        var components = net.minecraft.core.component.DataComponents.class;
        for (String group : new String[]{"Enchants", "Modifiers", "Additional"}) {
            var before = stack.getComponentsPatch();
            EditorCommands.setFlags(stack, group, true);
            var hidden = stack.get(net.minecraft.core.component.DataComponents.TOOLTIP_DISPLAY).hiddenComponents();
            var expected = switch(group) { case "Enchants" -> HideFlagsScreen.ENCHANTMENTS; case "Modifiers" -> HideFlagsScreen.MODIFIERS; default -> HideFlagsScreen.ADDITIONAL; };
            assertEquals(java.util.Set.copyOf(expected), hidden);
            EditorCommands.setFlags(stack, group, false);
            assertTrue(stack.get(net.minecraft.core.component.DataComponents.TOOLTIP_DISPLAY).hiddenComponents().isEmpty());
            assertFalse(stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME));
        }
    }
    @Test void saveFeedbackUsesEditedItemName() {
        var stack = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("My Item"));
        assertEquals("My Item saved to library", EditorCommands.savedMessage(stack).getString());
    }
}

