# Changelog

## 0.9.3

- Rename "Save Item" to "Save to Library".
- Add a setting to show or hide the creative-inventory Housing button, with a tooltip explaining the 1.8.9 filter.
- Restore the player's previous game mode after applying or cancelling item edits from a non-Creative mode.
- Request temporary Creative mode when the editor opens to reduce the wait when applying edits.

## 0.9.2

- Remove Housing Items Editor's duplicate advanced-tooltip durability line.

## 0.9.1

- Support Fabric Loader 0.19.5+ for Minecraft 26.2 and 26.3.
- Release update following maintainer in-game testing.

## 0.9.0

- Add Fabric builds for Minecraft 26.2 and 26.3 alongside 1.21.11.
- Adapt rendering, screen management, input and registry setup to modern versions.
- Publish the source under LGPL-3.0-only with retained upstream notices.
- Credit AdvancedCreativeTab for source/inspiration and HousingCreativeTab for feature inspiration.
- Add a three-version CI build and CurseForge/Modrinth publishing instructions.
- Preserve the shared editor tabs, live name/lore preview, library, enchantments,
  item model/type selection and Housing legacy filter.

All three targets compile and pass 44 automated tests each. The maintainer has completed
in-game testing; these builds are released.
