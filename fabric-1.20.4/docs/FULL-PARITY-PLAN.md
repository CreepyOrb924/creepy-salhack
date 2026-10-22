# Creepy SalHack full feature parity work

## Objective and acceptance
The user now explicitly requests every original Creepy SalHack 2.08.2 feature in the 1.20.4 Fabric client. The earlier subset acceptance is not a full-parity acceptance criterion. Audit all 127 registrations plus commands, settings, GUI/HUD services and assets. Do not label a stub, a shared alias, a generic preset or a compile-only feature as original parity. Preserve the original explicit exclusions for packet crashers, credential/malicious behavior and obsolete duplication exploits. Features depending on removed game/server behavior must be documented as unavailable or explicitly reduced, never silently counted as complete.

## Architecture
Preserve existing Java17/Fabric 1.20.4 core and tested composable NoSlow hook. Add typed mode/text settings without breaking existing numeric profiles, expand gameplay modules, independent rendering/movement/automation groups, protocol hooks and per-world cleanup. Use original source as the behavioral authority. New implementations should prefer the game's normal interaction APIs, bounded scans/render work, explicit enable/disable cleanup and local state only. Keep KTHREADD source unchanged and test both release JARs together before installation.

## Task groups and interfaces
1. Coordinator: core settings/profile changes, commands/HUD integration, combat (including damage evaluation) and full-parity inventory generation. APIs: ClientModule.number/flag plus mode(String,String...) and text(String,String); Setting.text()/setText()/isText(), display(), existing numeric APIs; Profile.ModuleState gains textSettings. ModuleManager.add/get/on and SalHackClient.INSTANCE remain stable.
2. Movement/network implementer: independently implement missing movement/exploit/network modules based on original source. Register through ExpansionMovementModules.register(ModuleManager); own separate movement mixin JSON and package. Include actual cleanup and no unsupported bypass claims.
3. Render implementer: Freecam and missing render modules, render services, block/chunk/entity diagnostics. Register through ExpansionRenderModules.register(ModuleManager), own mixin JSON. Events may self-register once during module registration. Document where modern APIs change fidelity.
4. Automation implementer: misc/player/world/building modules and practical Schematica replacement only if genuinely implementable. Register through ExpansionAutomationModules.register(ModuleManager), own mixins. Existing original module names must not be duplicated. No dummy toggle or nonfunctional imported settings.
5. Verification/integration: compile all groups, independently review important defects, exercise new behaviors in temporary game worlds, repeat packaged production coexistence test, generate exact per-module/per-setting parity report, package and preserve backups before replacing installed SalHack. Do not alter existing configurations/worlds or install an unverified build.

## Tests and review focus
- Profile compatibility with missing/new text/mode settings and unknown legacy values.
- Disconnect/death/dimension changes restore camera, keys, packet queues and owned state.
- Inventory cursor/container contention never discards or duplicates user items.
- Bounded scans and packet buffering; avoid main-thread unbounded work or network floods.
- Two clients' hooks compose; dormant toggles do not change vanilla behavior.
- All current registered modules must have traceable behavior and all original settings must be classified supported/changed/missing, with runtime scope recorded separately.

Work groups are independent except the stated core interface; coordinator owns shared registration, metadata, packaging and reports. Agent-authored notes record exact implementation reductions. A final result with any missing viable behavior is still incomplete, regardless of module count.
