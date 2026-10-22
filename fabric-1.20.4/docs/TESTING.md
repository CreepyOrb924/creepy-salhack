# Verification for CreepySalHack-1.20.4-2.08.2-port.4-render.jar

The release binary passed 26 unit tests, 49 targeted local game checks, and 28 combined production checks with the companion client on Fabric Loader 0.19.5. SHA-256: `b6cf6d8f66c403653900d911e19e29fc159c37b466abf6be1479400db3f339db`.

The combined checks include both menus, NoSlow composition, combat ownership/resumption, offhand and velocity integration, visible ESP/tracer pixels behind an opaque wall, normal occlusion when ThroughWalls is disabled, view-bob anchor math, and interpolation near world-border coordinates. Local game checks cover inventory, UI, movement, and real crystal placement/breaking; SalHack additionally verifies the shulker preview and removal/restoration of its text list.

These are targeted local checks. Full historical feature parity, every setting, remote-server performance, every modpack/shader pack, anti-cheat acceptance and competitive win rate are not verified. See the feature/parity reports for known gaps. No personal worlds, raw machine logs, credentials or legacy IngrosWare binary/source are included.
