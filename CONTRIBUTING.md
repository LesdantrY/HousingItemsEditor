# Contributing

Use JDK 25 and run `./gradlew build` (or `gradlew.bat build` on Windows) to check all supported versions. Keep compatibility with 1.21.11, 26.2 and 26.3.

Edit `src/main/java` and the small version-specific bridges. Do not edit generated modern sources in build directories. Add regression tests for behavior changes, and describe which game versions you tested manually. Include a screenshot for visible UI changes when possible.

Keep upstream notices intact. New contributions are distributed under the repository's LGPL-3.0-only license. Do not copy all-rights-reserved code or resources from HousingCreativeTab; it is credited as inspiration only.

Report bugs with the Minecraft version, Fabric Loader/API versions, HIE version, reproduction steps, and a sanitized log. Do not include access tokens, private server addresses, or other personal information.
