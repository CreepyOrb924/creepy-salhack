# Independent code review and resolutions

Read-only independent reviewer inspected the implementation after its first successful build. Review was static, not runtime validation. No upstream legacy code was executed.

| Finding | Resolution | Evidence |
|---|---|---|
| Line consumer reused after text switched the immediate fallback buffer | Separate complete line pass and deferred text-label pass | Runtime smoke with two dummy player nametags, boxes, tracers, storage/search/waypoints enabled together; no render exception |
| Sparse profiles inherited active modules/keys/settings | ModuleProfiles captures startup defaults and applies every profile over those defaults | ModuleProfilesTest covers missing module and missing setting reset |
| Crystal self-distance default exceeded reachable range | Compatible default self-distance (3.5) and range (4), higher min-health default; still explicitly partial and unsafe without damage prediction | Source inspection/build; combat behavior untested |
| Existing stash-coordinate notifications survived Privacy activation | Clear queued notices whenever Privacy becomes enabled | Runtime regression asserts old coordinate notice is absent |
| Different worlds with equal display name shared a profile | Hash normalized save-directory path instead of display name | Source inspection and runtime singleplayer profile selection |
| Bind row toggled module | Explicit bind action in GUI hit geometry | Compilation; middle-click key capture runtime-tested; Bind-row click not separately exercised |

Additional author visual review found Minecraft 1.20.4 Screen.render paints its default background, covering custom panels when called last. Both custom widget screens now override renderBackground. Runtime framebuffer check verifies original header RGB exactly, followed by screenshot inspection. Colors/ClickGUI are explicitly non-toggleable settings groups rather than inactive module toggles.

Remaining review/validation limits are in TESTING.md; no claim of full original parity or exhaustive security review is made.
