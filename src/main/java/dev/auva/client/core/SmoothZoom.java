package dev.auva.client.core;

import dev.auva.client.AuvaClient;

/** Client-side FOV animation. Never changes the saved Minecraft FOV option. */
public final class SmoothZoom {
    private static float current=1f;
    private static long lastNanos;
    private SmoothZoom() {}

    public static void scroll(double steps) {
        if (!AuvaClient.enabled("Zoom") || steps == 0) return;
        Module zoom=AuvaClient.module("Zoom");
        zoom.scale=Math.max(.08f,Math.min(.95f,(float)(zoom.scale*Math.pow(0.84,steps))));
        AuvaClient.save();
    }

    public static float multiplier() {
        long now=System.nanoTime();
        float dt=lastNanos==0?0.016f:Math.min(.05f,Math.max(0f,(now-lastNanos)/1_000_000_000f));
        lastNanos=now;
        float target=AuvaClient.enabled("Zoom")?Math.max(.08f,Math.min(.95f,AuvaClient.module("Zoom").scale)):1f;
        float ease=1f-(float)Math.exp(-15f*dt);
        current += (target-current)*ease;
        if(Math.abs(current-target)<0.0005f)current=target;
        return current;
    }
}
