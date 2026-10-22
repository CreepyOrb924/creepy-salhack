# Static security audit

Date: 2026-10-22. This is a scoped static review, not a security certification. Neither the supplied legacy JAR nor upstream legacy code/build scripts was executed. The new Java/Fabric implementation alone was built and tested.

## Reviewed inputs and limitations

1. Creepy SalHack `v2.08.2`, commit `28379f14fb97c08bb06d65e8ea741328c87ccc06`, downloaded directly from the requested GitHub repository. Read initialization, module registration/custom loading, updates/capes, RPC, suspicious-string matches and selected sensitive features. Broad source regex evidence is in `upstream-security-hits.txt`. Bundled libraries were pattern-scanned, not exhaustively audited line by line.
2. Supplied `ingrosware-v4.0.jar`, **not Creepy SalHack**. SHA-256: `078622b4bdc9a11f4a32dadce2263bca8d00bc37999147bb0027faeb8f13c44f`. Parsed all **800** class constant pools with zero parse errors; inspected entry names/resources, URLs, session/network/reflection references, and selected `javap -c -p` disassembly. Evidence: `supplied-jar-static-scan.json`, `supplied-jar-javap.txt` and `scripts/audit_reference.py`.
3. The rewritten client and build metadata were inspected for credential, network, native, downloader, persistence and reflective external-code paths. An independent code review examined state cleanup, render buffers, profile isolation and privacy behavior.

No Creepy SalHack binary or partial-port ZIP was present, so this review cannot compare a SalHack binary to the source or attest to its contents. Constant-pool/source pattern scans can miss dynamically assembled/encrypted endpoints, reflective code and obfuscation. The supplied binary was not fully decompiled and no dependency supply-chain audit was performed.

## Upstream findings

| Finding | Evidence | Port disposition |
|---|---|---|
| Remote update/version text requests | `managers/UpdateManager.java:28,47`, GitHub raw assets endpoints | Excluded. No updater or remote messages. These inspected methods read text, not a demonstrated executable downloader. |
| Remote cape lists/images | `managers/CapeManager.java:64,85` | Excluded. No cape network activity. |
| Remote username/UUID profile lookups | `managers/UUIDManager.java`, FakePlayer, MobOwner | Excluded. No identity lookups. |
| Custom local JAR/ZIP module class loading and instantiation | `util/ReflectionUtil.java:33`, `managers/ModuleManager.java:LoadExternalModules` | Excluded. Loading arbitrary local custom modules executes their code and expands the trusted surface. This alone is not evidence of malicious intent. |
| Discord RPC/native libraries | build.gradle and packaged `discord-rpc.dll`, `.so`, `.dylib` | Excluded along with the old RPC dependency and all native assets. |
| Server crash / packet flood module | `module/exploit/CrashExploitModule.java` | Excluded. |
| Obsolete duplication / illegal item features | AutoDuper, ManualDupe, PopBobSexDupe, Auto32k, dupe GUI | Excluded rather than recreated. |
| Session references | HUD/player/fake-camera identity in inspected matches | No demonstrated token-exfiltration flow found in the inspected SalHack paths. Identity/session access was not carried into the new mod. |

The scoped scans found no explicit webhook/token-stealing/persistence path in inspected upstream client code. This absence does not prove the entire repository or any historical binary safe.

## Supplied Ingrosware findings

- `best/reich/ingros/util/thealtening/TheAltening` contains `http://api.thealtening.com/v1/` (unencrypted HTTP endpoint).
- `AltService` rewrites authentication/session endpoints through reflection, including construction of TheAltening authentication URLs. `AltCommand` creates/replaces Minecraft sessions. These are credential/session manipulation features; they were not reused. Their presence is not by itself proof of theft, but they change where authentication is trusted.
- `SSLVerification` installs a global SSL socket factory using a trust manager whose `checkClientTrusted` / `checkServerTrusted` methods immediately return, and replaces the default hostname verifier with one accepting TheAltening auth/session host names. This bypasses certificate checks and alters process-wide TLS behavior if called. Static bytecode evidence is preserved; no attempt was made to activate it.
- `ClassUtil` references local `URLClassLoader` functionality. Included Mixin/ASM/ReflectASM libraries also contain reflection/class-definition machinery; distinguish those library mechanisms from evidence of an external payload.
- The archive entry scan found no nested JAR/ZIP, DLL/SO/dylib, executable, batch or PowerShell file. The scoped constant-pool review found no explicit Discord webhook, OS startup persistence or shell-spawn path. Obfuscated or dynamically assembled behavior remains outside this claim.

**Disposition:** this unrelated JAR was excluded in its entirety. No account/session/authentication/TLS-changing code was imported into the port.

## Rewritten port

- No credential access, session replacement, HTTP client, telemetry, webhooks, update checks, remote class loading, dynamic plugins, bundled natives, subprocess creation, file downloaders or OS persistence.
- Runtime filesystem writes are local JSON profiles inside Fabric's configuration directory. Server addresses are hashed into filenames; waypoint coordinates remain plaintext locally. Corrupt files are copied for recovery and protected from automatic overwriting.
- Normal gameplay interactions are sent through Minecraft's interaction manager, including inventory swaps, placement, attacks and respawn. This is not a no-network mod: it necessarily interacts with the connected game server through the vanilla protocol.
- Privacy is an overlay control, not encryption or complete screenshot sanitization. It gates this mod's coordinate output and clears stale notifications on activation.
- Legacy API libraries, Discord RPC, obsolete Forge/Alpine/reflections/typetools/Slick/SnakeYAML, old natives and vendored rendering were not carried forward. Dependencies are pinned to the versions in build.gradle; Gradle distribution checksum matches the official published checksum.
- Review fixes included resetting missing profile fields to defaults, using stable world save paths as profile identity, keeping line/text rendering in separate passes, and removing stale location notices on enabling Privacy.

Residual uncertainty: no multiplayer-server validation, anti-cheat compatibility claims, exhaustive dependency audit or proof of absence of vulnerabilities. Static review cannot establish complete safety.
