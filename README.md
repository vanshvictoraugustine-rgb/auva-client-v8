# AUVA CLIENT — Fabric 1.21.11 source rebuild

**Author:** AuvaPlays  
**Status:** v0.4.0 development source; **not a finished/full client and not a verified compiled JAR**.

Auva Client is a sleek, next-generation PvP & utility client featuring a high-contrast neon magenta and purple interface, real-time cross-player badge synchronization, advanced performance boosters, and fully customizable HUD modules.

**Important:** The sentence above is the requested official description and a *product goal*, **not a certification that all advertised systems are implemented**. See `BUILD_STATUS.md` for the exact current status. This archive contains 46 catalog entries; 28 have initial implementations (not compile/runtime verified here), and 18 are deliberately marked unavailable because their required systems have not been built. Unsupported entries **cannot be toggled on**.

## Implemented in source (requires GitHub compilation + Minecraft runtime verification)

- **UI/branding:** all-new searchable Auva module card dashboard, category navigation, real per-module settings screens, slider/toggle/dropdown/bind/color controls, custom initial landing screen, supplied original artwork without corner watermark.
- **HUD framework:** FPS, CPS (mouse event timestamps), latency, clock, X/Y/Z/chunks/direction, compass, armor and durability, potion timers, scoreboard sidebar, inventory preview, totem count, food-gain hint, WASD/LMB/RMB/Space keystrokes. Drag/resize in HUD editor.
- **Camera:** Freelook with configurable **hold** key (Left Alt default), Freecam (B default) with render-frame movement, Zoom (C default), and HurtCam slider. These require in-game validation for smoothness and mixin compatibility.
- **Visuals:** Custom Crosshair, in-inventory Item Highlighter (not dropped-item ESP).
- **Audio:** ten original sound assets converted to OGG and registered with Minecraft's sound system, selectable volume/preset and attack-intent event playback. The client attack callback is **not confirmation of server-side damage**. Optional custom WAV file via the Java audio fallback.
- **Tools:** Modrinth resource-pack search, SHA-512 checksum verification, ZIP safety checking, local WAV music playback, basic screenshot file manager with explicit delete confirmation, searchable keybind manager, named local config profiles.

## NOT complete

The following modules are present as **disabled catalog entries**, not functional features: Combo Counter, Hit Color, Block Overlay, Motion Blur, Fullbright, Time Changer, Weather Changer, Toggle Sprint/Sneak, FPS Booster, Tier Tagger, Name Hider, Crystal Optimizer, Anchor Optimizer, Nametag & Badges (cross-player sync), Notifications, Account Manager, Recorder, and Replay/Rewind.

Some initial implementations are also narrower than the final UX specification, including the music player (local WAV only; external music services are links), screenshot manager (file list, not a thumbnail gallery), scoreboard (simple styled text; more server formatting cases to verify), Food HUD (food-gain hints rather than full AppleSkin saturation), resource packs (Modrinth compatible packs, not a multi-source catalog), and HUD/editor animations.

No external Auva Presence backend, Microsoft OAuth registration, licensed provider integration, cross-player authentication, recorder encoder, or Tier API credentials are embedded. User accounts, UUIDs and raw multiplayer server addresses are **not sent to any Auva presence endpoint** by this source.

## Build with GitHub Actions

1. Extract this ZIP at the repository **root**, so `build.gradle`, `src/`, and `.github/workflows/build.yml` are at the top level.
2. Commit and push to `main`, then select the **Build Auva Client 1.21.11** workflow in GitHub Actions.
3. Run workflow if it hasn't started. Read failures in the `Compile Fabric mod` step; this build has **not been run in a Minecraft/Fabric compiler here**.
4. Only if the workflow is green, download `auva-client-0.4.0-fabric-1.21.11` and use the generated `auva-client-0.4.0.jar` from that artifact.
5. Put the JAR with compatible Fabric Loader and Fabric API in your Minecraft 1.21.11 `mods` folder, then test in a throwaway profile/world.

You can alternatively run `gradle build -x test` with **Java 21** and a compatible **Gradle 9.5.1** environment.

### Default controls

- **Right Shift**: Modules directory (closes while in game).
- **H**: HUD Editor (configurable in Keybind Manager).
- **Left Alt**: Freelook (hold, release to restore original view).
- **B**: Freecam toggle (when connected to a world).
- **C**: Zoom while held.

Modules, colors, scale, layout and keycodes persist under `config/auva-client.json` after use. Named profiles are under `config/auvaclient/profiles`.

## Architecture

- `core/ModuleCatalog.java` — authoritative names, categories, descriptions, readiness flags.
- `core/Module.java`, `core/Config.java` — module state, persisted settings; modules without implementations cannot enable.
- `render/HudRenderer.java` — unified HUD render path.
- `ui/AuvaMenu.java`, `ui/ModuleSettingsScreen.java` — module directory and configuration.
- `ui/HudEditor.java` — drag/resize HUD with previews.
- `core/CameraControl.java`, `mixin/CameraMixin.java` — independent camera behavior.
- `AuvaSounds.java`, `core/HitSounds.java` — OpenAL hit audio.
- `ui/*Screen.java` — resource packs, music, profiles, screenshots, keybinds, custom home.
- `docs/auva_client_ui_design_specification.txt` — user-provided design specification.

## Verification already done in this environment

- Java 21 syntax/isolated compilation: `ModuleCatalog.java` + `Module.java` — passed.
- ZIP extraction protection smoke tests: **6/6 passed**.
- Resource JSON and mixin JSON: parse successfully.
- Sound `.ogg` files created via FFmpeg; original samples retained for reference.
- GitHub Action workflow YAML: parses with quoted `'on'` trigger.

**Not verified:** Gradle remapped JAR build, Fabric mixin application, actual Minecraft launch, frame-time performance, multiplayer server behavior, long-running audio and network behavior, third-party compatibility. No installed Gradle/Minecraft/Fabric libraries or network download access is available to compile the whole project in this runtime. Any dependency/mapping errors found in GitHub Actions must be corrected before considering this playable.

All logo/sound resources included here are generated/original client assets, not copied Lunar Client assets. The UI is reference-inspired, not Lunar proprietary source.
