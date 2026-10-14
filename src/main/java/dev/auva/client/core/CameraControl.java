package dev.auva.client.core;

import dev.auva.client.AuvaClient;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Smooth client-only camera state. The player's server-facing rotation/position is never moved. */
public final class CameraControl {
    public static double x,y,z, prevX,prevY,prevZ;
    public static float yaw,pitch;
    private static CameraType previousCameraType;
    private CameraControl() {}

    public static boolean freecam() { return AuvaClient.enabled("Freecam"); }
    public static boolean freelook() {
        Minecraft mc=Minecraft.getInstance();
        return mc.player!=null && mc.screen==null && AuvaClient.enabled("Freelook");
    }
    public static boolean independent() { return freecam() || freelook(); }

    public static void activate(String name) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null)return;
        lastFrameNanos=0;
        if(name.equals("Freecam")){
            if(freelook()) deactivate("Freelook");
            AuvaClient.module("Freelook").enabled=false;
            Vec3 eyes=mc.player.getEyePosition();
            x=prevX=eyes.x; y=prevY=eyes.y; z=prevZ=eyes.z;
            yaw=mc.player.getYRot(); pitch=mc.player.getXRot();
        }else if(name.equals("Freelook")){
            if(freecam()){
                AuvaClient.module("Freecam").enabled=false;
                deactivate("Freecam");
            }
            yaw=mc.player.getYRot(); pitch=mc.player.getXRot();
            previousCameraType=mc.options.getCameraType();
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }

    public static void deactivate(String name){
        if(!name.equals("Freelook"))return;
        Minecraft mc=Minecraft.getInstance();
        if(previousCameraType!=null){
            mc.options.setCameraType(previousCameraType);
            previousCameraType=null;
        }
    }

    public static void mouseDelta(float dx,float dy){
        yaw=Mth.wrapDegrees(yaw+dx);
        pitch=Mth.clamp(pitch+dy,-89.9f,89.9f);
    }

    private static long lastFrameNanos;

    /** Advance the detached camera once per rendered frame; never move the player or send movement packets. */
    public static void frame(Minecraft mc){
        long now=System.nanoTime();
        double elapsed=lastFrameNanos==0?0:(now-lastFrameNanos)*1.0e-9;
        lastFrameNanos=now;
        if(!freecam()||mc.player==null||mc.screen!=null)return;
        // Cap large stalls to avoid sudden jumps after pausing or tabbing out.
        double dt=Math.min(.05,Math.max(0,elapsed));
        double forward=(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0);
        double strafe=(mc.options.keyRight.isDown()?1:0)-(mc.options.keyLeft.isDown()?1:0);
        double lift=(mc.options.keyJump.isDown()?1:0)-(mc.options.keyShift.isDown()?1:0);
        double norm=Math.sqrt(forward*forward+strafe*strafe+lift*lift);
        if(norm<.001)return;
        double speed=8.0*Math.max(.35,AuvaClient.module("Freecam").scale);
        if(mc.options.keySprint.isDown())speed*=2.35;
        double a=Math.toRadians(yaw);
        x+=(-Math.sin(a)*forward+Math.cos(a)*strafe)*speed*dt/norm;
        z+=( Math.cos(a)*forward+Math.sin(a)*strafe)*speed*dt/norm;
        y+=lift*speed*dt/norm;
    }

    public static double renderX(float partial){return x;}
    public static double renderY(float partial){return y;}
    public static double renderZ(float partial){return z;}

    public static void reset(){
        if(freelook())deactivate("Freelook");
        AuvaClient.module("Freelook").enabled=false;
        AuvaClient.module("Freecam").enabled=false;
        previousCameraType=null;
        lastFrameNanos=0;
    }
}
