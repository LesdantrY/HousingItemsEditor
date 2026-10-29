# Housing Items Editor

A Fabric client mod for editing creative items, designed with Hypixel Housing players in mind.

**Minecraft 1.21.11, 26.2 and 26.3 · Fabric · LGPL-3.0-only**

Press **H** to edit the held item, or **N** to open your item library. Both keys can be rebound in Minecraft's controls. `/hie` opens the editor too.

## Features

- One name-and-lore text editor with a live formatted preview, undo/redo, and a color-code cheatsheet. First line is the name; remaining lines are lore.
- Grouped enchantments, level fields, ±1 arrows, per-enchantment Max, and Max All (levels 0–10; 0 removes).
- Unbreakable and durability controls. Durability controls disable while Unbreakable is on.
- Separate item type and item model pickers with creative categories, search, remembered sorting/filter settings, and full item tooltips.
- Leather armor gradient/RGB color picker, plus advanced SNBT editing.
- Tooltip visibility controls for enchantments, attribute modifiers, and additional details.
- Local item library with item previews, multiple selection, import, give, and delete. Save the current draft without replacing your held item.
- Housing filter for modern equivalents of the Minecraft 1.8.9 item set, including legacy potions and spawn eggs. Spawners, command blocks, and command-block minecarts are excluded while Housing is on.
- Legacy block IDs in item tooltips when Housing filtering is enabled.

**Done** applies the draft. Switching tabs keeps edits; Escape closes without applying. The editor remembers the last-used tab for the game session. Library shows **Back** when opened from the editor and **Close** when opened using its keybind.

## Installation

Download the artifact matching your **exact Minecraft version** from a successful [GitHub Actions build](https://github.com/LesdantrY/HousingItemsEditor/actions/workflows/build.yml) and extract its JAR (GitHub sign-in is required to download Actions artifacts). Release builds will also be listed under [Releases](https://github.com/LesdantrY/HousingItemsEditor/releases) when published. Use the main JAR, not the `-sources.jar`. Install only one Housing Items Editor JAR in your instance's `mods` folder, together with Fabric API for the same Minecraft version.

| Minecraft | Fabric Loader minimum | Fabric API used to build | Java minimum |
| --- | --- | --- | --- |
| 1.21.11 | 0.19.3 | 0.141.6+1.21.11 | 21 |
| 26.2 | 0.20.1 | 0.161.0+26.2 | 25 |
| 26.3 | 0.20.1 | 0.165.0+26.3 | 25 |

This is a **client-only** mod. YACL is not required. Fabric API is required. The three JARs are separate builds, not interchangeable files.

## Housing behavior

Item Model starts with Housing **OFF**; Item Type starts with Housing **ON**. Each picker remembers your subsequent choice. The vanilla creative-inventory Housing button combines item filtering and legacy-ID display; its initial state detects a `housing` scoreboard objective.

The filter describes legacy item availability, not every rule enforced by a server. Server permissions and item validation still apply. Creative inventory updates require Creative mode. The editor can request `/gmc` or `/gamemode creative` and waits for the server to grant it; it does not bypass permissions. Model selection changes appearance, not potion effects or the item's underlying type. Legacy 1.8.9 clients do not display modern custom item models.

## Commands

- `/hie`, `/hie edit`, `/hie library`
- `/hie saveItem` — saves the held item; reports its edited name
- `/hie rename <name>` — supports `&` formatting and `&#RRGGBB`
- `/hie enchant <enchantment> <1–10>` — includes completion
- `/hie unbreakable <true|false>`
- `/hie durability <remaining>`
- `/hie flags <Enchants|Modifiers|Additional> <true|false>` — **true hides**, **false shows**
- `/hie itemtype <id>`, `/hie itemmodel <id|reset>`
- `/hie lore add <text>`, `/hie lore edit <line> <text>`, `/hie lore remove <line>`
- `/hie copyLore`, `/hie pasteLore`, `/hie help`

## Credits and license

- **[AdvancedCreativeTab by ate47 / ATE47](https://github.com/ate47/AdvancedCreativeTab)** — source port and editor workflow. Selected classes derive from revision `79eb9017dda8868e990e4fdf245419e06b5f3fcd` on `1.19.3-forge`. Original files and license notices are preserved in `upstream/AdvancedCreativeTab/`.
- **[HousingCreativeTab by BusterBrown1218](https://github.com/BusterBrown1218/HousingCreativeTab)** — inspiration for Housing-focused features. Its source is all-rights-reserved; no source, resource pack, allowlist, or artwork from it is redistributed here. The corresponding features were implemented independently.
- **[PrismarineJS/minecraft-data](https://github.com/PrismarineJS/minecraft-data)** — MIT-licensed historical item/block data used by the legacy filter.

Maintained by **LesdantrY**. This derivative is open source under **LGPL-3.0-only**; see [LICENSE](LICENSE), [COPYING-GPL-3.0](COPYING-GPL-3.0), and [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md). This project is not affiliated with or endorsed by Hypixel, Mojang, Microsoft, or the inspiration projects.

## Building

Use JDK 25 and the included Gradle wrapper:

```sh
./gradlew build releaseBundle sourceBundle
# Or build a single Minecraft target:
./gradlew -PminecraftTarget=26.2 build
```

On Windows, use `gradlew.bat`. Build outputs are in `versions/<minecraft>/build/libs/`; release bundles are in `dist/`. The 1.21.11 output targets Java 21; 26.2/26.3 target Java 25.

Shared editor code lives in `src/main/java`. `gradle/modern-sources.gradle` generates the 26.2/26.3 API spelling adaptations under each build directory. Edit the shared sources, not generated files. Version-specific Fabric, rendering and UI bridges live in the corresponding source folders. Tests use version-specific registry bootstraps.

See [BUILD-STATUS.md](BUILD-STATUS.md) for verification, [PUBLISHING.md](PUBLISHING.md) for distribution settings, and [CONTRIBUTING.md](CONTRIBUTING.md) for contributions.
