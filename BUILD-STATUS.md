# Verification — 29 October 2026

## Housing Items Editor 0.9.0

| Minecraft | Fabric Loader minimum | Fabric API | Java output | Automated tests |
| --- | --- | --- | --- | --- |
| 1.21.11 | 0.19.3 | 0.141.6+1.21.11 | 21 | 44 passed |
| 26.2 | 0.19.5 | 0.161.0+26.2 | 25 | 44 passed |
| 26.3 | 0.19.5 | 0.165.0+26.3 | 25 | 44 passed |

Built with JDK 25 and Gradle 9.5. The complete build, releaseBundle and
sourceBundle tasks succeeded for all three targets. No test failures or errors.
Tests cover legacy filters, stack conversion, formatting, undo/redo, lore,
command options, enchantment descriptions, layout calculations and library persistence.

Modern targets adapt rendering, screen management, key input, container access,
and registry initialization while sharing the editor implementation.

These are release builds. The maintainer has completed in-game testing and confirmed that they work.
Housing filtering does not guarantee server acceptance.

Use the 0.9.0 JAR matching your exact Minecraft version. Older local artifacts
are retained but are not part of this release.
