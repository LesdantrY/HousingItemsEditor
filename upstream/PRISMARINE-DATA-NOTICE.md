# PrismarineJS minecraft-data

Source: https://github.com/PrismarineJS/minecraft-data
Retrieved 2026-10-26. The project README declares its data MIT-licensed.
The bundled `legacy-housing.json` was generated from factual ID/name mappings in
`data/pc/common/legacy.json`, restricted using `data/pc/1.8/items.json` and
`data/pc/1.8/blocks.json`. It includes name updates for Minecraft 1.21.11 and
explicit legacy item variants. It is not an authoritative server permission list.

MIT License

Copyright (c) PrismarineJS contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

0.6.0 audit (2026-10-27): checked all 1.8 item IDs/metadata against legacy item mappings; red bed explicitly replaces the mapping's white-bed fallback. Spawn eggs use the 27 original creative egg types because the generic legacy mapping only contains pig_spawn_egg. Added command block/minecart and removed post-1.8 egg exceptions. Reference files: data/pc/1.8/items.json and entities.json. Potion and book variants are also restricted to legacy types.

0.7.4 audit (2026-10-27): rechecked data/pc/1.8/items.json against data/pc/common/legacy.json and the bundled allowlist. Legacy grass, stone slab, sign, dyes and bed resolve to current names (short_grass, smooth_stone_slab, oak_sign, red/green/yellow dyes, red_bed). Petrified oak slab, dead bush, head/banner colors and the original creative spawn eggs are already represented. Potion/book variants were lost by registry-default-only picker enumeration; pickers now preserve creative catalog stacks. Non-item entity spawn metadata is not treated as a separate usable item.
