# Creepy SalHack — Fabric 1.20.4

Community Fabric modernization of Creepy SalHack for **Minecraft Java 1.20.4**, **Fabric**, and **Java 17**.

[Download the latest release](https://github.com/bengonzalesdr/creepy-salhack/releases) · [Report an issue](https://github.com/bengonzalesdr/creepy-salhack/issues) · [Rendering changes](docs/RENDER-UPDATE.md) · [Testing](docs/TESTING.md)

## Install

1. Install Minecraft Java **1.20.4** with Fabric Loader (**0.15.11 or later**; tested together on **0.19.5**) and Java **17**.
2. Download **`CreepySalHack-1.20.4-2.08.2-port.4-render.jar`** from Releases, plus **`fabric-api-0.97.2+1.20.4.jar`** from the same release or the official [Fabric API project](https://github.com/FabricMC/fabric).
3. Put the client JAR and one copy of Fabric API into your instance's `mods` folder. Move older versions of this client out first. Source ZIPs and development/QA JARs do not belong in `mods`.
4. Start Fabric 1.20.4. Press **Left Arrow** to open the menu. Right-click a module for its settings.

The default Minecraft instance is `%APPDATA%\.minecraft` on Windows; other launchers may use their own instance folder. Exit code 1 is generic: attach the relevant crash report or `latest.log` with a bug report after removing personal/server details.

## Features

Storage ESP and Entity ESP can display through blocks in loaded chunks. Tracers use thin, configurable lines and compensate for view bob. Geometry uses camera-relative coordinates and interpolated entity positions. Profiles, friend lists and HUD settings are stored locally.

SalHack provides utility, movement, rendering, inventory and combat modules, a draggable HUD, and visual shulker previews without a redundant item list. Historical feature parity remains incomplete: [full-parity audit](docs/FULL-PARITY-AUDIT.md).

This community port is independently authored from the original module inventory and behavior. It is not an official release by CreepyOrb924 or ionar2 and should not be presented as their endorsement. Original attribution and license notices are retained in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Optional companion: [KTHREADD](https://github.com/bengonzalesdr/KTHREADD), which opens with Delete. With both clients installed, enabled equivalent KTHREADD combat/ESP modules take priority by default. Install only one copy of Fabric API.

## Build from source

Install a JDK **17**. The checked-in wrapper downloads **Gradle 8.6** with a pinned checksum. Run from this directory (`fabric-1.20.4`):

```sh
./gradlew build
```

Windows PowerShell:

```powershell
.\gradlew.bat build
```

The installable remapped JAR is written to `build/libs/`. For this version, use `CreepySalHack-1.20.4-2.08.2-port.4-render.jar`. Unit tests run as part of `build`. `runQaClient` launches an isolated local test game and creates temporary test worlds under the configured QA directory.

## Contributing and license

See [CONTRIBUTING.md](CONTRIBUTING.md). Authored source is distributed under **MIT**, with inherited notices preserved in [LICENSE](LICENSE). Dependency and original-project attribution is documented separately. Minecraft itself is not included.

This is a development release with targeted tests, not complete historical parity or a guarantee for every server. Please report reproducible issues with Minecraft/Fabric versions, the exact client build, relevant settings, and sanitized logs.
