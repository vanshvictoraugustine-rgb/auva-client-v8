package dev.auva.client.core;

public final class Module {
    public String name, category;
    public boolean enabled;
    public int x, y;
    public float scale = 1.0f;
    public int color = 0xFFD946EF;
    public float opacity = 0.70f;
    public int soundPreset = 0;
    public float strength = 0.65f;
    public int gap = 3, length = 6, thickness = 2;
    public boolean outline = true, centerDot = false, dynamic = false;
    public boolean background = true, showNumbers = true;
    public String targetItems = "minecraft:totem_of_undying";
    public int keyCode = -1;
    public boolean hold = false;
    public int radius = 4;
    public float shadow = 0.4f;
    public int padding = 6;
    public String layout = "FULL";
    public Module(String name, String category, boolean enabled, int x, int y) {
        this.name=name; this.category=category; this.enabled=enabled; this.x=x; this.y=y;
    }
    public boolean isHud() { return "HUD".equals(category); }
    public boolean available() { var d=ModuleCatalog.get(name); return d!=null && d.ready(); }
    public String description() { var d=ModuleCatalog.get(name); return d==null?"":d.description(); }
}
