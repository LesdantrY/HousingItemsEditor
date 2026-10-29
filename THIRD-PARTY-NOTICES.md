# Source provenance

Housing Items Editor is a modified port of selected AdvancedCreativeTab editor classes
by ate47 / ATE47 (https://github.com/ate47/AdvancedCreativeTab), revision
`79eb9017dda8868e990e4fdf245419e06b5f3fcd`, branch `1.19.3-forge`.

Unmodified reference files and both upstream license files are retained under
`upstream/AdvancedCreativeTab/`. Ported classes retain the `fr.atesab.act` namespace.
Changes dated 2026-10-26 adapt rendering, input, item data components, registry lookup,
and inventory synchronization to Minecraft 1.21.11 / Fabric; they add Housing-oriented
item controls and retain the compact editor/giver workflow and Done/Cancel semantics.
Version 0.3.0 further changes the main field to item-name editing, commits with Done,
and implements interactive firework controls following the upstream
`GuiFireworksModifer` shape/trail/flicker/color-list workflow.
Version 0.4.0 (2026-10-27) combines the main controls, replaces row-based lore
editing with a multiline document, and adds item sorting, attribute selection,
tooltip flags, and a responsive single-page enchantment grid.

The current derivative is distributed under LGPL-3.0-only, matching the upstream
root LICENSE. Its older LICENSE.txt is also preserved. Original HIE code was MIT;
that notice remains in `upstream/HIE-original-MIT-LICENSE`.

HousingCreativeTab by BusterBrown1218 is a feature reference only. Its LICENSE.txt
states All rights reserved. No source, resource pack, item allowlist, or artwork
from that repository is included. Lore copy/paste, item-model, trim, count, and
damage tools are independently implemented against Minecraft's public APIs.

The creative filter's legacy ID data is derived from PrismarineJS/minecraft-data
(MIT), with updated names and explicit variants. See
`upstream/PRISMARINE-DATA-NOTICE.md` for attribution and source paths.

Version 0.9.0 (2026-10-28) adds Minecraft 26.2/26.3 builds through generated API adaptations and version-specific registry test setup. Maintainer: LesdantrY. HousingCreativeTab reference: https://github.com/BusterBrown1218/HousingCreativeTab . Its all-rights-reserved license was rechecked before publication; it remains inspiration only.
