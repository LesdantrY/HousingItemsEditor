package dev.housingitems;

/** Short effect explanations for the vanilla enchantments in the supported client. */
final class EnchantmentDescriptions {
    static String describe(String id) {
        if (!id.startsWith("minecraft:")) return "This enchantment is defined by the server or a mod. Its effect is not described here.";
        return switch (id.substring(10)) {
            case "aqua_affinity" -> "Removes the usual underwater mining-speed penalty.";
            case "bane_of_arthropods" -> "Deals extra damage to arthropods such as spiders and briefly slows them.";
            case "binding_curse" -> "Prevents equipped armor from being removed normally until it breaks or you die.";
            case "blast_protection" -> "Reduces explosion damage and explosion knockback.";
            case "breach" -> "Makes mace attacks bypass more of the target's armor protection.";
            case "channeling" -> "A thrown trident can summon lightning during a thunderstorm when its target is exposed to the sky.";
            case "density" -> "Adds damage to mace smash attacks based on how far you fell.";
            case "depth_strider" -> "Lets you move faster underwater.";
            case "efficiency" -> "Speeds up mining with a suitable tool.";
            case "feather_falling" -> "Reduces fall damage when worn on boots.";
            case "fire_aspect" -> "Sets targets on fire when you hit them.";
            case "fire_protection" -> "Reduces fire and lava damage and shortens burning time.";
            case "flame" -> "Makes arrows fired from a bow set targets on fire.";
            case "fortune" -> "Increases drops from certain mined blocks, such as ores.";
            case "frost_walker" -> "Temporarily freezes water beneath your feet and protects against magma-block damage.";
            case "impaling" -> "In Java Edition, increases trident damage against aquatic mobs.";
            case "infinity" -> "Lets a bow fire ordinary arrows without consuming them, provided you have an arrow.";
            case "knockback" -> "Pushes targets farther away when you hit them.";
            case "looting" -> "Increases certain mob drops and the chance of rare drops.";
            case "loyalty" -> "Makes a thrown trident return to its owner.";
            case "luck_of_the_sea" -> "Improves the chance of fishing up treasure and reduces junk catches.";
            case "lunge" -> "A spear's jab attack propels you forward, using hunger and durability.";
            case "lure" -> "Shortens the wait for a bite while fishing.";
            case "mending" -> "Uses collected experience to repair eligible held or equipped items.";
            case "multishot" -> "Makes a crossbow fire three projectiles in a spread while consuming one.";
            case "piercing" -> "Lets crossbow arrows pass through multiple entities and shields.";
            case "power" -> "Increases the damage of arrows fired from a bow.";
            case "projectile_protection" -> "Reduces damage from projectiles such as arrows.";
            case "protection" -> "Reduces many kinds of incoming damage while worn.";
            case "punch" -> "Increases the knockback of arrows fired from a bow.";
            case "quick_charge" -> "Reduces the time needed to load a crossbow.";
            case "respiration" -> "Lets you stay underwater longer before running out of air.";
            case "riptide" -> "Launches you with your trident while you are in water or exposed to rain.";
            case "sharpness" -> "Increases melee attack damage.";
            case "silk_touch" -> "Makes compatible blocks drop themselves instead of their usual mined drops.";
            case "smite" -> "Increases melee damage against undead mobs.";
            case "soul_speed" -> "Lets you move faster on soul sand and soul soil, at a durability cost.";
            case "sweeping_edge" -> "Increases damage dealt to nearby targets by a sword's sweeping attack.";
            case "swift_sneak" -> "Lets you move faster while sneaking when worn on leggings.";
            case "thorns" -> "Can damage attackers when they hit you, at an extra durability cost.";
            case "unbreaking" -> "Gives an item a chance to avoid losing durability when used.";
            case "vanishing_curse" -> "Makes the item disappear instead of dropping when you die.";
            case "wind_burst" -> "A successful mace smash launches you upward for another attack.";
            default -> "This enchantment is defined by the server or a mod. Its effect is not described here.";
        };
    }
}
