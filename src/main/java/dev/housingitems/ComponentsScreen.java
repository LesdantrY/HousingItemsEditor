package dev.housingitems;


import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.function.Consumer;

/** Component editors use Minecraft's own validation and preserve unrelated fields. */
final class ComponentsScreen extends PortableScreen {
    private record Field(String name, String key, String initial) {}
    private static final Field[] FIELDS = {
        new Field("Enchantments", "enchantments", "{\"minecraft:sharpness\":5}"),
        new Field("Player head", "profile", "{name:\"Steve\"}"),
        new Field("Potion effects", "potion_contents", "{potion:\"minecraft:swiftness\"}"),
        new Field("Firework rocket", "fireworks", "{flight_duration:1,explosions:[]}"),
        new Field("Firework star", "firework_explosion", "{shape:\"small_ball\",colors:[I;16711680],fade_colors:[I;],has_trail:false,has_twinkle:false}"),
        new Field("Dye color", "dyed_color", "16711680"),
        new Field("Container contents", "container", "[]"),
        new Field("Block entity / command", "block_entity_data", "{id:\"minecraft:command_block\",Command:\"say Hello\"}"),
        new Field("Custom data", "custom_data", "{}"),
        new Field("Attributes", "attribute_modifiers", "[]"),
        new Field("Repair cost", "repair_cost", "0"),
        new Field("Item model", "item_model", "\"minecraft:diamond\"")
    };
    private final Screen parent;
    private ItemStack draft;
    private final Consumer<ItemStack> apply;
    private String status = "Choose a component to edit its SNBT value. Empty input removes it.";

    ComponentsScreen(Screen parent, ItemStack draft, Consumer<ItemStack> apply) {
        super(Component.literal("Item components"));
        this.parent = parent;
        this.draft = draft.copy();
        this.apply = apply;
    }
    @Override protected void init() {
        int w = Math.min(420, width - 32), x = (width - w) / 2;
        for (int i = 0; i < FIELDS.length; i++) {
            Field field = FIELDS[i];
            addRenderableWidget(Button.builder(Component.literal(field.name()), b -> open(field))
                    .bounds(x + (i % 2) * (w / 2 + 2), 42 + (i / 2) * 25, w / 2 - 2, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> { apply.accept(draft); onClose(); }).bounds(x, 198, w / 2 - 2, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose()).bounds(x + w / 2 + 2, 198, w / 2 - 2, 20).build());
    }
    private void open(Field field) {
        try {
            CompoundTag root = TagParser.parseCompoundFully(ItemData.encode(draft));
            CompoundTag components = root.getCompound("components").orElseGet(CompoundTag::new);
            String key = "minecraft:" + field.key();
            Tag current = components.get(key);
            Ui.open(new DataScreen(this, field.name() + " — " + key, current == null ? field.initial() : current.toString(), value -> {
                CompoundTag updated = components.copy();
                if (value.isBlank()) updated.remove(key);
                else updated.put(key, TagParser.parseCompoundFully("{value:" + value + "}").get("value"));
                CompoundTag next = root.copy();
                next.put("components", updated);
                draft = ItemData.decode(next.toString());
                status = "Updated " + field.name() + ". Choose Done to keep these component changes.";
            }));
        } catch (Exception e) { status = "Error: " + e.getMessage(); }
    }
    @Override protected void draw(Canvas graphics, int mouseX, int mouseY, float delta) {

        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFFFF);
        graphics.drawString(font, font.plainSubstrByWidth(status, width - 32), 16, height - 16, 0xFFFFD68A);
    }
    @Override public void onClose() { Ui.open(parent); }
    @Override public boolean isPauseScreen() { return false; }
}

