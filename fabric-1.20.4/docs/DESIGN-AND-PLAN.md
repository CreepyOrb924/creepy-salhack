# Creepy SalHack 1.20.4 port

The user requests autonomous investigation, implementation, compilation and packaging, and explicitly permits delivery of the largest verified subset. This project is a fresh Fabric rewrite based on the v2.08.2 inventory, not a binary patch. Preserve original names, category panels, colored header/active rows, right-aligned ArrayList, configurable HUD and keybinds. No upstream code is executed.

## Architecture
Java 17, Minecraft 1.20.4, Yarn build 3, Fabric Loader 0.15.11, Fabric API 0.97.2, Loom 1.6.12. Client-only entrypoint. Tick, HUD, world-render and command events replace Forge/Alpine. Small mixins implement brightness, edge protection, no-slow and step only where events are insufficient. No legacy libraries or native assets.

Modules expose name/category/description, enable state, key and typed numeric/boolean settings. A manager owns lifecycle, tick and cleanup. JSON profiles use validated local names, atomic saves, preserved malformed-file backups and world/server keys hashed locally. HUD positions and waypoints persist per profile. No web requests or external class loading in the mod.

GUI uses scalable category panels, a search field, module toggles, expandable settings, middle-click key capture and configurable accent/rounding/animation. HUD editor uses draggable measured bounds. Privacy gates coordinate overlays, waypoints and location notifications. It cannot erase coordinates already printed into chat or another mod's display.

## Execution tasks
1. Pin source, inventory all registered modules and static-scan source/JAR. Record audit evidence, licensing and exclusions.
2. Bootstrap Loom/wrapper/JDK17; compile actual remapped artifact. Never run legacy build scripts.
3. Implement module/settings and profile persistence. Tests: state round trips, malformed JSON recovery, unsafe profile names, invalid settings, isolated server keys, coordinate conversion.
4. Implement prioritized module subset using modern APIs. Inventory interactions use normal screen-handler click slots and yield when a GUI is open; placement restores selected slot; module disable restores owned state. Riskier algorithms remain explicitly unimplemented if not reliably supportable.
5. Implement classic GUI, HUD editor, commands, waypoints, privacy and bounded world scanning. Test real runtime in isolated development game directory if possible.
6. Run complete tests/build, inspect remapped metadata/class version and packaged classes, independently review code, fix important findings, write compatibility/testing/security reports and package full source ZIP and release JAR.

## Validation boundaries
Core tests do not establish Minecraft module correctness. Runtime launch proves loading only unless actual GUI/game interactions are observed. Combat, movement and render behavior require in-world validation. Report those boundaries explicitly, with no placeholders presented as modules.
