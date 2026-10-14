package dev.auva.client.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = Path.of("config", "auva-client.json");
    private Config() {}

    public static void save(Map<String, Module> modules) { saveTo(modules,PATH); }
    public static void saveTo(Map<String, Module> modules,Path path) {
        try {
            Files.createDirectories(path.getParent());
            Path temporary = path.resolveSibling(path.getFileName()+".tmp");
            Files.writeString(temporary, GSON.toJson(modules), StandardCharsets.UTF_8);
            try { Files.move(temporary,path,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
            catch (java.nio.file.AtomicMoveNotSupportedException e) { Files.move(temporary,path,StandardCopyOption.REPLACE_EXISTING); }
        } catch (Exception e) { System.err.println("[Auva Client] Could not save config: " + e); }
    }
    public static void load(Map<String, Module> modules) { loadFrom(modules,PATH); }
    public static void loadFrom(Map<String, Module> modules, Path path) {
        if (!Files.isRegularFile(path)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            for (var entry : modules.entrySet()) {
                if (!root.has(entry.getKey()) || !root.get(entry.getKey()).isJsonObject()) continue;
                Module loaded = GSON.fromJson(root.get(entry.getKey()), Module.class);
                if (loaded == null) continue;
                loaded.name = entry.getKey();
                loaded.category = entry.getValue().category;
                loaded.scale = Math.max(0.35f,Math.min(4f, loaded.scale));
                loaded.padding = Math.max(2, Math.min(24, loaded.padding));
                loaded.radius = Math.max(0, Math.min(16, loaded.radius));
                loaded.keyCode = Math.max(-1,Math.min(512,loaded.keyCode));
                if (loaded.layout == null) loaded.layout = "FULL";
                loaded.opacity = Math.max(0f,Math.min(1f,loaded.opacity));
                loaded.soundPreset = Math.max(0,Math.min(11,loaded.soundPreset));
                loaded.strength = Math.max(0f,Math.min(1f,loaded.strength));
                loaded.length = Math.max(1,Math.min(24,loaded.length));
                loaded.gap = Math.max(0,Math.min(20,loaded.gap));
                loaded.thickness = Math.max(1,Math.min(10,loaded.thickness));
                if (loaded.targetItems == null) loaded.targetItems = "minecraft:totem_of_undying";
                if(!loaded.available()) loaded.enabled=false;
                entry.setValue(loaded);
            }
        } catch (Exception e) { System.err.println("[Auva Client] Could not load config: " + e); }
    }
}
