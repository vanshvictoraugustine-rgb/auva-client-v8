# Auva Client 0.6 — incremental source patch

Base: 0.5.0 source. Target: Fabric 1.21.11, Java 21.

## Changed in this patch
- Zoom FOV interpolates toward its multiplier and returns smoothly to normal on release.
- Zoom wheel adjusts the zoom multiplier, consumes the scroll event during active zoom (protects hotbar), and persists the level.
- Modules GUI renders an interpolated, clipped scrolling grid, with mouse hit-testing following drawn cards.
- HUD Editor stops drawing large opaque/debug backgrounds over each HUD element; selected widget has subtle outline and a small resize handle.
- HUD Editor right-click context menu offers Settings, Disable Module, Reset Position, and Lock/Unlock. Lock currently applies to the editor session.

## Not yet implemented in 0.6
- Main menu redesign and smooth open/close animation
- New smooth-font renderer; animated per-element transitions and unified HUD styling
- Scoreboard server-formatting fix; Music Player rebuild; Freelook replacement
- Complete overhaul of remaining placeholder modules or external integrations

## Validation
This workspace does not have a Gradle command/wrapper or cached build toolchain, so this patch has NOT been compiled or Minecraft-tested here. Use the included GitHub Actions workflow to compile, and report Java/mixin mapping errors if any.

## Music Folder hotfix
- Folder button creates the directory and opens it with Windows Explorer, macOS open, or Linux xdg-open; falls back to Java Desktop and copies path to clipboard if launching is unavailable.
- Not compiled or runtime-tested in Minecraft.
