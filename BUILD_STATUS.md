# Auva Client 0.5.0 — module implementation matrix

This matrix records **source implementation presence, not verified runtime functionality**. Entries marked SOURCE PRESENT still need the GitHub Gradle build and Minecraft in-game tests.

| Module | Category | Status |
|---|---|---|
| Keystrokes | HUD | Source present / UNVERIFIED |
| Armor HUD | HUD | Source present / UNVERIFIED |
| Scoreboard | HUD | Source present / UNVERIFIED |
| Potion Effects | HUD | Source present / UNVERIFIED |
| FPS HUD | HUD | Source present / UNVERIFIED |
| CPS HUD | HUD | Source present / UNVERIFIED |
| Ping HUD | HUD | Source present / UNVERIFIED |
| Coordinates HUD | HUD | Source present / UNVERIFIED |
| Combo Counter | HUD | Source present / UNVERIFIED |
| Direction HUD | HUD | Source present / UNVERIFIED |
| Inventory HUD | HUD | Source present / UNVERIFIED |
| Totem Counter | HUD | Source present / UNVERIFIED |
| Durability HUD | HUD | Source present / UNVERIFIED |
| Custom Crosshair | Visual | Source present / UNVERIFIED |
| Hit Color | Visual | Not implemented / DISABLED |
| Block Overlay | Visual | Not implemented / DISABLED |
| Motion Blur | Visual | Not implemented / DISABLED |
| HurtCam | Camera | Source present / UNVERIFIED |
| Zoom | Camera | Source present / UNVERIFIED |
| Freelook | Camera | Source present / UNVERIFIED |
| Freecam | Camera | Source present / UNVERIFIED |
| Fullbright | Visual | Not implemented / DISABLED |
| Time Changer | Visual | Not implemented / DISABLED |
| Weather Changer | Visual | Not implemented / DISABLED |
| Hit Sounds | Audio | Source present / UNVERIFIED |
| Item Highlighter | Utility | Source present / UNVERIFIED |
| AppleSkin / Food HUD | HUD | Source present / UNVERIFIED |
| Toggle Sprint/Sneak | Utility | Source present / UNVERIFIED |
| FPS Booster / Performance | Performance | Not implemented / DISABLED |
| Tier Tagger | Integrations | Not implemented / DISABLED |
| Name Hider | Utility | Not implemented / DISABLED |
| Crystal Optimizer | PvP | Not implemented / DISABLED |
| Anchor Optimizer | PvP | Not implemented / DISABLED |
| Nametag & Badges | Integrations | Not implemented / DISABLED |
| Clock | HUD | Source present / UNVERIFIED |
| Notifications | Utility | Source present / UNVERIFIED |
| Resource Pack Browser | Menus | Source present / UNVERIFIED |
| Music Player | Menus | Source present / UNVERIFIED |
| Account Manager | Menus | Not implemented / DISABLED |
| Profiles / Configs | Menus | Source present / UNVERIFIED |
| Keybind Manager | Menus | Source present / UNVERIFIED |
| Recorder | Menus | Not implemented / DISABLED |
| Replay / Rewind | Menus | Not implemented / DISABLED |
| Screenshot Manager | Menus | Source present / UNVERIFIED |
| HUD Editor | Menus | Source present / UNVERIFIED |
| Main Menu | Menus | Source present / UNVERIFIED |

## Validation
- ZIP archive validator smoke tests: passed 6/6.
- Java module metadata sources: compile with JDK21.
- Mod metadata and mixin JSON: parsed.
- No Fabric/Minecraft Gradle compilation in this environment.

## Limitations
The original spec requires provider APIs, authenticated presence backend, full OAuth account switching, GPU video capture/encoding and interactive replay editor. Those systems do not exist yet in the source and must not be advertised as working.
