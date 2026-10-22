# port.2 compatibility update — 2026-10-22

Fixed the crash when running Creepy SalHack and KTHREADD together. Both clients formerly used exclusive `@ModifyConstant` hooks for the same item-use slowdown value. They now use MixinExtras `@ModifyExpressionValue`, which composes with the other client's value rather than competing for the instruction. Both client JARs must be updated.

The actual remapped release JARs were launched together with **Minecraft 1.20.4, Java 17, Fabric Loader 0.19.5 and Fabric API 0.97.2+1.20.4** in an isolated production game directory. Both menus opened, both clients entered/rendered a temporary integrated world, and all four NoSlow states were checked against actual item-use input: both off = 0.2; either one on = 1.0; both on = 1.0. All **9 combined-client regression checks passed**. Singleplayer smoke testing is not multiplayer/anti-cheat validation.

Evidence: `verification/coexistence-results.txt` and `verification/coexistence-launch.log`. Original per-client smoke records remain below/as separate evidence; current release validation additionally includes this production compatibility run. The two installed JARs were backed up before replacement; no user configuration/world was changed.

# 2.08.2-port.1 — 2026-10-22

Independent partial Fabric 1.20.4 modernization; not an official upstream release.

- Fresh Java 17 / Fabric client entrypoint, event registration and five targeted mixins replace Forge 1.12.2, Alpine and obsolete rendering.
- Restored original yellow bitmap watermark, cyan category headers, category ordering, enable rows and right-aligned HUD ArrayList. Modern text renderer, adaptive panel columns, search, configurable scale/RGB/rainbow/opacity/rounding/tooltips/GUI opening animation.
- Added draggable HUD editor, automatic local JSON saves, named profiles, per-server/per-save profiles, local commands, dimension-aware waypoints, Nether conversion, Privacy, notifications and diagnostic logging.
- Reimplemented AutoTotem, base AutoArmor, reachable-support Surround, basic range-based AutoCrystal, AutoTool, ChestStealer, AutoRespawn, Sprint, AutoWalk, Speed, ElytraFly, Step, SafeWalk, NoSlow and Yaw.
- Added world overlays for EntityESP, StorageESP, Tracers, Nametags, Waypoints and bounded Search; loaded-chunk StashFinder, NoWeather and HighwayNavigation HUD. Brightness uses a lightmap mixin rather than editing gamma or applying status effects.
- Removed all original build/runtime libraries, native RPC, updater/capes, arbitrary external module loading, account/TLS manipulation, packet crashers and obsolete duplication features.
- Added core persistence/isolation tests and an isolated development-only Minecraft smoke harness. Reviewed actual screenshots and fixed vanilla background overdraw. Fixed stale line/text render consumer usage, default profile carryover, singleplayer profile collisions, stale coordinate notifications and Bind-row behavior.

## Explicit omissions / reduced scope

This is a verified foundation and selected feature subset. Freecam, NewChunks, full CrystalAura damage logic, most original modules, Schematica and optional advanced visual polish are absent. AutoCrystal, armor ranking, ESP, movement and other equivalents are simplified as described in README/compatibility. No placeholder implementation is presented as a ported module. Multiplayer, anti-cheat and long-duration behavior are unverified.
