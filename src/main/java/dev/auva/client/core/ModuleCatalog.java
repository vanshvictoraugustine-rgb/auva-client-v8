package dev.auva.client.core;

import java.util.List;

/** Canonical UI descriptions; unavailable integrations are labeled instead of offering fake toggles. */
public final class ModuleCatalog {
  public record Definition(String name,String category,String description,boolean ready) {}
  public static final List<Definition> ALL = List.of(
    new Definition("Keystrokes", "HUD", "Displays a dynamic on-screen overlay showing your real-time WASD, LMB, RMB, and Spacebar keypresses with active neon magenta lighting animations.", true),
    new Definition("Armor HUD", "HUD", "Renders your equipped helmet, chestplate, leggings, and boots directly on your screen, complete with real-time durability bars and numerical durability values.", true),
    new Definition("Scoreboard", "HUD", "Modernizes the vanilla server sidebar with dark purple translucency, custom font shadow options, score toggles, and clean color customization.", true),
    new Definition("Potion Effects", "HUD", "Displays a clean list of active status effects along with effect icons and remaining duration timers.", true),
    new Definition("FPS HUD", "HUD", "Displays a lightweight, real-time Frames-Per-Second (FPS) widget to monitor your rendering performance.", true),
    new Definition("CPS HUD", "HUD", "Tracks and displays your Left Mouse Button and Right Mouse Button clicks per second (CPS) in real time.", true),
    new Definition("Ping HUD", "HUD", "Displays your current network latency (ping) in milliseconds directly on your HUD to monitor connection stability.", true),
    new Definition("Coordinates HUD", "HUD", "Displays your exact X, Y, Z coordinates, cardinal direction facing, biome name, and chunk location data on screen.", true),
    new Definition("Combo Counter", "HUD", "Displays the active number of consecutive melee hits landed on an opponent without taking damage in return.", true),
    new Definition("Direction HUD", "HUD", "Shows a horizontal compass bar across the screen highlighting cardinal directions and degree angles.", true),
    new Definition("Inventory HUD", "HUD", "Renders a compact, customizable preview of your main inventory contents directly on screen.", true),
    new Definition("Totem Counter", "HUD", "Displays the total number of Totems of Undying remaining in your inventory/offhand, along with real-time PvP totem pop counters.", true),
    new Definition("Durability HUD", "HUD", "Displays the remaining durability and maximum uses of your currently held item, tool, or weapon.", true),
    new Definition("Custom Crosshair", "Visual", "Replaces the default crosshair with a fully customizable overlay featuring custom shapes, dot toggles, line thickness, color pickers, and dynamic hit reaction colors.", true),
    new Definition("Hit Color", "Visual", "Customizes the damage tint color and outline intensity rendered on players and mobs when they take damage.", false),
    new Definition("Block Overlay", "Visual", "Customizes the bounding box outline, line thickness, color tint, and fill opacity of the block your crosshair is targeting.", false),
    new Definition("Motion Blur", "Visual", "Adds a cinematic camera blur effect during fast mouse movements with smooth intensity sliders.", false),
    new Definition("HurtCam", "Camera", "Controls or completely eliminates screen-tilt and camera shake when taking damage.", true),
    new Definition("Zoom", "Camera", "Smoothly zooms in your camera view using a hotkey, with customizable zoom levels and transition speeds.", true),
    new Definition("Freelook", "Camera", "Detaches your third-person camera perspective to look 360 degrees around your character while maintaining your movement direction.", true),
    new Definition("Freecam", "Camera", "Detaches the camera from your player entity, allowing free-flight camera movement to inspect structures or terrain.", true),
    new Definition("Fullbright", "Visual", "Maximizes internal world lighting, allowing clear visibility in dark caves, underwater, and night environments.", false),
    new Definition("Time Changer", "Visual", "Alters client-side world lighting without affecting the actual server time.", false),
    new Definition("Weather Changer", "Visual", "Controls client-side weather rendering without changing the server actual weather state.", false),
    new Definition("Hit Sounds", "Audio", "Plays custom audio triggers when attacking players and entities; client-side attack feedback is not proof of server damage.", true),
    new Definition("Item Highlighter", "Utility", "Draws a colored background glow around chosen inventory and container slots.", true),
    new Definition("AppleSkin / Food HUD", "HUD", "Adds visual food gain preview and hunger information on screen.", true),
    new Definition("Toggle Sprint/Sneak", "Utility", "Allows toggling sprint and sneak modes with a single hotkey press instead of holding down the keys continuously.", true),
    new Definition("FPS Booster / Performance", "Performance", "Adjusts client visual quality and performance options. Requires engine-specific implementations.", false),
    new Definition("Tier Tagger", "Integrations", "Renders competitive PvP tier tags from configured providers next to player usernames.", false),
    new Definition("Name Hider", "Utility", "Obfuscates or replaces your username with a local placeholder text for recording.", false),
    new Definition("Crystal Optimizer", "PvP", "Provides local Crystal PvP interaction and visual feedback controls without changing the server rules.", false),
    new Definition("Anchor Optimizer", "PvP", "Provides local Anchor PvP interaction and visual feedback controls without changing the server rules.", false),
    new Definition("Nametag & Badges", "Integrations", "Shows Auva badges for verified client users when the opt-in presence service is connected.", false),
    new Definition("Clock", "HUD", "Shows a small local clock overlay.", true),
    new Definition("Notifications", "Utility", "Controls small in-game toast alerts for client events.", true),
    new Definition("Resource Pack Browser", "Menus", "Searches verified Modrinth resource packs, downloads and validates ZIP archives.", true),
    new Definition("Music Player", "Menus", "Plays locally added audio files and Auva original bundled loops.", true),
    new Definition("Account Manager", "Menus", "Manages offline labels and authenticated Microsoft accounts with OAuth.", false),
    new Definition("Profiles / Configs", "Menus", "Save, load and export local client module configurations.", true),
    new Definition("Keybind Manager", "Menus", "Configure keyboard shortcuts for each module.", true),
    new Definition("Recorder", "Menus", "Records gameplay video to local files with configurable quality.", false),
    new Definition("Replay / Rewind", "Menus", "Record and edit clips with timeline controls and a rolling buffer.", false),
    new Definition("Screenshot Manager", "Menus", "Browse and manage Minecraft screenshots.", true),
    new Definition("HUD Editor", "Menus", "Click, drag, scale and reposition enabled HUD widgets.", true),
    new Definition("Main Menu", "Menus", "Auva-themed quick navigation interface.", true)
  );
  private ModuleCatalog() {}
  public static Definition get(String name) { for (var d : ALL) if (d.name().equals(name)) return d; return null; }
}
