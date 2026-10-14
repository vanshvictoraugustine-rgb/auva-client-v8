package dev.auva.client;

import dev.auva.client.core.CameraControl;
import dev.auva.client.core.Config;
import dev.auva.client.core.ComboTracker;
import dev.auva.client.core.GameplayToggles;
import dev.auva.client.core.NotificationCenter;
import dev.auva.client.core.CpsTracker;
import dev.auva.client.core.HitSounds;
import dev.auva.client.core.Module;
import dev.auva.client.core.ModuleCatalog;
import dev.auva.client.render.HudRenderer;
import dev.auva.client.ui.AuvaMenu;
import dev.auva.client.ui.AuvaHomeScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import dev.auva.client.ui.HudEditor;
import dev.auva.client.ui.PackBrowserScreen;
import dev.auva.client.ui.MusicScreen;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

/** A truthful module registry: integrations without a working backend are visible, but cannot be enabled. */
public final class AuvaClient implements ClientModInitializer {
    public static final Map<String,Module> MODULES = new LinkedHashMap<>();
    private static KeyMapping menuKey;
    private static final Map<String,Boolean> previousPressed = new LinkedHashMap<>();
    private static boolean initialized;
    private static boolean homeShown;

    @Override public void onInitializeClient() {
        int row=0;
        for (var definition : ModuleCatalog.ALL) {
            boolean defaultOn = switch(definition.name()) {
                case "FPS HUD", "Potion Effects", "Armor HUD", "Coordinates HUD", "Totem Counter", "AppleSkin / Food HUD" -> true;
                default -> false;
            };
            Module m=new Module(definition.name(), definition.category(), definition.ready() && defaultOn, 10, 10+row*24);
            if (m.isHud()) row++;
            MODULES.put(m.name,m);
        }
        module("Scoreboard").x=-1;
        module("Scoreboard").y=80;
        module("Keystrokes").y=heightSlot(300);
        module("Keystrokes").layout="FULL";
        module("HUD Editor").keyCode=GLFW.GLFW_KEY_H;
        module("Freecam").keyCode=GLFW.GLFW_KEY_B;
        module("Freelook").keyCode=GLFW.GLFW_KEY_LEFT_ALT;
        module("Freelook").hold=true;
        module("Zoom").keyCode=GLFW.GLFW_KEY_C;
        module("Zoom").hold=true;
        module("Zoom").scale=0.35f;
        module("HurtCam").strength=0.0f;
        module("Hit Sounds").strength=0.65f;
        module("Custom Crosshair").color=0xFFD946EF;
        Config.load(MODULES);
        // Camera state cannot be restored between disconnected play sessions.
        module("Freecam").enabled=false;
        module("Freelook").enabled=false;
        module("Zoom").enabled=false;
        module("Freelook").hold=true;
        for(var m:MODULES.values()) if(!m.available())m.enabled=false;
        menuKey=key("key.auvaclient.menu",GLFW.GLFW_KEY_RIGHT_SHIFT);
        AttackEntityCallback.EVENT.register((player,world,hand,target,hit)->{
            // ATTACK callbacks are client intent, not server-confirmed damage events.
            if(world.isClientSide()) {
                if(enabled("Hit Sounds")) HitSounds.play(module("Hit Sounds"));
                if(enabled("Combo Counter")) ComboTracker.attacked();
            }
            return InteractionResult.PASS;
        });
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            if(!homeShown && client.player==null && client.screen instanceof TitleScreen){
                homeShown=true;
                client.setScreen(new AuvaHomeScreen());
            }
            if(client.screen!=null && module("Freelook").enabled){
                CameraControl.deactivate("Freelook");
                module("Freelook").enabled=false;
            }
            if(client.player==null){
                if(CameraControl.independent())CameraControl.reset();
                previousPressed.clear();
                return;
            }
            while(menuKey.consumeClick())client.setScreen(client.screen instanceof AuvaMenu?null:new AuvaMenu());
            trackModuleKeys(client);
            CpsTracker.prune();
            ComboTracker.tick(client.player.hurtTime);
            GameplayToggles.tick(client, enabled("Toggle Sprint/Sneak"));
        });
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("auvaclient","hud"),(graphics,tick)->HudRenderer.render(graphics));
        initialized=true;
    }
    private static int heightSlot(int y){return y;}
    private static KeyMapping key(String name,int code){
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(name,code,KeyMapping.Category.MISC));
    }
    private static void trackModuleKeys(Minecraft mc){
        boolean accepting=mc.screen==null;
        long handle=mc.getWindow().handle();
        for(Module m:MODULES.values()){
            if(!m.available() || m.keyCode<0) continue;
            boolean down=accepting && m.keyCode<=GLFW.GLFW_KEY_LAST
                && GLFW.glfwGetKey(handle,m.keyCode)==GLFW.GLFW_PRESS;
            boolean prior=previousPressed.getOrDefault(m.name,false);
            if(m.name.equals("Freelook")){
                if(down!=m.enabled){
                    if(down)CameraControl.activate("Freelook");
                    else CameraControl.deactivate("Freelook");
                    m.enabled=down;
                }
            }else if(m.hold){
                if(m.enabled!=down){
                    if(m.name.equals("Freecam")){
                        if(down)CameraControl.activate("Freecam");
                        else CameraControl.deactivate("Freecam");
                    }
                    m.enabled=down;
                }
            }else if(down && !prior){
                if("Menus".equals(m.category))openMenuFeature(mc,m.name);
                else toggle(m.name);
            }
            previousPressed.put(m.name,down);
        }
    }
    private static void openMenuFeature(Minecraft mc,String name){
        Screen parent=mc.screen;
        switch(name){
            case "HUD Editor" -> mc.setScreen(new HudEditor());
            case "Resource Pack Browser" -> mc.setScreen(new PackBrowserScreen(parent));
            case "Music Player" -> mc.setScreen(new MusicScreen(parent));
            case "Keybind Manager" -> mc.setScreen(new dev.auva.client.ui.KeybindManagerScreen(parent));
            case "Profiles / Configs" -> mc.setScreen(new dev.auva.client.ui.ProfilesScreen(parent));
            case "Screenshot Manager" -> mc.setScreen(new dev.auva.client.ui.ScreenshotManagerScreen(parent));
            case "Main Menu" -> mc.setScreen(new AuvaHomeScreen());
        }
    }
    public static Module module(String name){return MODULES.get(name);}
    public static boolean enabled(String name){Module m=module(name);return m!=null && m.available() && m.enabled;}
    public static boolean toggle(String name){
        Module m=module(name);
        if(m==null || !m.available() || name.equals("Freelook"))return false;
        if(name.equals("Freecam")){
            if(m.enabled)CameraControl.deactivate(name);
            else CameraControl.activate(name);
        }
        m.enabled=!m.enabled;
        if(enabled("Notifications") && !name.equals("Notifications")) NotificationCenter.push(name,m.enabled?"Enabled":"Disabled");
        save();
        return true;
    }
    public static void save(){if(initialized) Config.save(MODULES);}
    public static void openPacks(Screen from){Minecraft.getInstance().setScreen(new PackBrowserScreen(from));}
    public static void openMusic(Screen from){Minecraft.getInstance().setScreen(new MusicScreen(from));}
    public static void openEditor(){Minecraft.getInstance().setScreen(new HudEditor());}
}
