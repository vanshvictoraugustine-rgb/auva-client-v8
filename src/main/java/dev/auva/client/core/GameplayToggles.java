package dev.auva.client.core;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/** Toggle-sprint/sneak controller. Uses raw default Ctrl/Shift so the latched KeyMappings don't self-trigger. */
public final class GameplayToggles {
    private static boolean sprint,sneak,prevSprint,prevSneak;
    private GameplayToggles(){}
    public static void tick(Minecraft mc, boolean enabled){
        if(mc.player==null){ reset(mc); return; }
        long window=mc.getWindow().handle();
        boolean sprintRaw=GLFW.glfwGetKey(window,GLFW.GLFW_KEY_LEFT_CONTROL)==GLFW.GLFW_PRESS;
        boolean sneakRaw=GLFW.glfwGetKey(window,GLFW.GLFW_KEY_LEFT_SHIFT)==GLFW.GLFW_PRESS;
        if(enabled && mc.screen==null){
            if(sprintRaw&&!prevSprint)sprint=!sprint;
            if(sneakRaw&&!prevSneak)sneak=!sneak;
            mc.options.keySprint.setDown(sprint || sprintRaw);
            mc.options.keyShift.setDown(sneak || sneakRaw);
        }else if(!enabled){
            sprint=false;sneak=false;
        }
        prevSprint=sprintRaw;prevSneak=sneakRaw;
    }
    public static boolean sprint(){return sprint;}
    public static boolean sneak(){return sneak;}
    public static void reset(Minecraft mc){
        sprint=sneak=prevSprint=prevSneak=false;
        if(mc!=null){mc.options.keySprint.setDown(false);mc.options.keyShift.setDown(false);}
    }
}
