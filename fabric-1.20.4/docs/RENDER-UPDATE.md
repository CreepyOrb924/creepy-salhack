# Rendering update — Minecraft 1.20.4

Versions: Creepy SalHack `2.08.2-port.4-render`, KTHREADD `4.0-port.6-render`.

## Fixed in both clients

- Storage ESP and Entity ESP use dedicated line layers with **ThroughWalls enabled by default**. They remain visible behind opaque blocks and do not write into the world's depth buffer.
- Tracers also render through blocks. They default to **LineWidth 1**; width is configurable from 0.5 to 3 pixels in quarter-pixel steps. One pixel avoids the dotted appearance that sub-pixel lines can have.
- World coordinates are converted to camera-relative doubles before submitting float vertices. This addresses precision jitter at large coordinates.
- Moving entity boxes, tracer endpoints, and name labels interpolate between tick positions.
- The tracer origin is transformed from screen center using the inverse view matrix, including view bob, rather than an approximate camera-forward vector.
- ESP uses its own vertex buffers, avoiding interference with other modules' buffered lines.
- Storage caches refresh immediately on activation and every ten ticks afterward. Unloaded or removed storage is filtered before rendering. Storage recognition also includes dispensers, droppers and brewing stands through the common container block-entity base class.
- Search outlines use the same through-block pipeline. Name labels also benefit from camera-relative positioning.

## Running both clients together

SalHack's StorageESP, EntityESP, and Tracers each have **PreferKTHREADD enabled by default**. If the matching KTHREADD module is enabled, only KTHREADD draws that overlay. SalHack resumes when the KTHREADD counterpart is disabled. This prevents stacked outlines and tracers. With both enabled, adjust the KTHREADD module's range/width/filter settings. Disable PreferKTHREADD only if you deliberately want both renderers.

## Shulker preview

SalHack's ShulkerPreview retains the 27-slot item grid and removes Minecraft's redundant item-count text list. The item title and other item metadata remain. Disabling ShulkerPreview restores the normal vanilla text list. KTHREADD does not add a separate shulker preview; the shared setup uses SalHack's implementation.

## Install

Close Minecraft, remove the older SalHack and KTHREADD JARs from the instance's mods folder, and keep them as backups. Install the bundle's two client JARs plus one copy of the included Fabric API for 1.20.4. Use Fabric Loader and Java 17 (production checks use Loader 0.19.5).

Menus: **Left Arrow** for SalHack; **Delete** for KTHREADD. Right-click a module to expand settings. New settings use their defaults in existing profiles; previous settings and combat behavior are retained.

## Verification

The original production build failed an opaque-wall screenshot test with zero Storage ESP pixels. The updated builds are checked separately for visible Storage ESP, Entity ESP, and tracer pixels behind the same opaque stone wall. Combined tests also cover view-bob anchor math, sub-block interpolation near 30-million-block coordinates, and overlay ownership/resumption. Screenshots and logs are included under verification.

Per-client game tests cover the shulker tooltip change and existing movement, inventory, UI, and combat behavior. These tests cover the standard renderer; external shader packs and every third-party rendering mod are not verified. ESP can display only storage/entities supplied by the server in loaded chunks.
