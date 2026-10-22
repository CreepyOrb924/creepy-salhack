# port.2 compatibility update — 2026-10-22

Fixed the crash when running Creepy SalHack and KTHREADD together. Both clients formerly used exclusive `@ModifyConstant` hooks for the same item-use slowdown value. They now use MixinExtras `@ModifyExpressionValue`, which composes with the other client's value rather than competing for the instruction. Both client JARs must be updated.

The actual remapped release JARs were launched together with **Minecraft 1.20.4, Java 17, Fabric Loader 0.19.5 and Fabric API 0.97.2+1.20.4** in an isolated production game directory. Both menus opened, both clients entered/rendered a temporary integrated world, and all four NoSlow states were checked against actual item-use input: both off = 0.2; either one on = 1.0; both on = 1.0. All **9 combined-client regression checks passed**. Singleplayer smoke testing is not multiplayer/anti-cheat validation.

Evidence: `verification/coexistence-results.txt` and `verification/coexistence-launch.log`. Original per-client smoke records remain below/as separate evidence; current release validation additionally includes this production compatibility run. The two installed JARs were backed up before replacement; no user configuration/world was changed.

