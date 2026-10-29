# Publishing Housing Items Editor

## Project settings

- Name: **Housing Items Editor**
- Suggested slug: **housing-items-editor**
- Summary: **Edit item names, lore, enchantments and models, with a saved-item library and a legacy item filter for Housing.**
- Loader: **Fabric**
- Environment: **Client required; server unsupported**
- License: **LGPL-3.0-only**
- Source: https://github.com/LesdantrY/HousingItemsEditor
- Issues: https://github.com/LesdantrY/HousingItemsEditor/issues
- Required dependency: **Fabric API** (choose the platform's Fabric API project)
- Categories: Utility / Equipment / Storage as applicable to the platform's current list.
- Use the README's Features, Installation, Housing behavior, and Credits sections as the project description. Keep both original mod authors' credits visible.

## Upload one file per Minecraft version

| Version number | Primary mod JAR | Minecraft tag |
| --- | --- | --- |
| 0.9.0+1.21.11 | housing-items-editor-0.9.0+1.21.11.jar | 1.21.11 only |
| 0.9.0+26.2 | housing-items-editor-0.9.0+26.2.jar | 26.2 only |
| 0.9.0+26.3 | housing-items-editor-0.9.0+26.3.jar | 26.3 only |

Do not upload the multi-version ZIP as an installable mod and do not mark one JAR compatible with all three versions. Source archives can be linked separately. Keep the exact corresponding source and included license notices available with every release.

Use **Beta** until the runtime checklist below is completed. Build and unit-test success alone do not establish multiplayer compatibility.

## Runtime checklist before promoting to Release

For each target, launch a clean Fabric instance with the matching Fabric API and only the matching HIE JAR. Check:

- Title screen loads without mixin errors.
- Join a Creative world; H opens Lore and N opens Library; remapped keybinds work.
- Untouched Done preserves the item; name/lore editing and undo work.
- Enchantment arrows/Max and durability controls update correctly.
- Selecting model then type preserves the model, and type/model tooltips retain potion/book details.
- Housing toggle and creative search work both on and off.
- Save Item, `/hie saveItem`, Library import/multiple selection and Back/Close work.
- A permissioned Housing session accepts ordinary supported item edits. Document server-side rejections separately.

## Platform submission

The repository and files are prepared for upload; CurseForge and Modrinth projects require the owner's account, project creation, and each platform's review. This document does not mean a listing has been submitted or approved.

Official guidance: [CurseForge project submission](https://support.curseforge.com/support/solutions/articles/9000199552-project-submission-guide-and-tips), [CurseForge project creation](https://support.curseforge.com/support/solutions/articles/9000197241-creating-and-submitting-a-project), [Modrinth additional files](https://support.modrinth.com/en/articles/8793363-additional-files).
