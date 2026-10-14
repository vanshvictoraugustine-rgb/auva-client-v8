# Auva Client 0.5.0 rebuild progress

This revision continues from the exact 0.4.0 source that passed the user's GitHub Actions build.

## Newly implemented source
- Combo Counter: local attack-chain counter, resets when the local player takes damage or the chain times out.
- Toggle Sprint/Sneak: client-side latched sprint/sneak input with on-screen state text.
- Notifications: bounded timed Auva toast queue, module enable/disable notifications, no unbounded allocation.

## Existing working-source areas retained
HUD renderer, Keystrokes, Armor HUD, compact Scoreboard, Potion Effects, FPS/CPS/Ping/Coordinates/Direction/Inventory/Totem/Durability/Food HUDs, Custom Crosshair, HurtCam, Zoom, Freelook, Freecam, Hit Sounds, inventory Item Highlighter, Resource Pack Browser, local Music Player, Profiles, Keybind Manager, Screenshot Manager, HUD Editor, Main Menu.

## Still not honestly complete
Hit Color, Block Overlay, Motion Blur, Fullbright, Time Changer, Weather Changer, FPS Booster, Tier Tagger provider integration, Name Hider interception, Crystal Optimizer, Anchor Optimizer, authenticated Auva badge presence, Microsoft OAuth Account Manager, native video Recorder, and Replay/Rewind editor.

These remain disabled rather than presenting fake controls as functional features. They require Minecraft render/network hooks or external authenticated/provider systems and still need implementation plus compile/runtime verification.
