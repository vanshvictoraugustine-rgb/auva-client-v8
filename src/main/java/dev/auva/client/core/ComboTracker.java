package dev.auva.client.core;

/** Lightweight local PvP combo estimate. Server hit confirmation is not exposed by vanilla networking. */
public final class ComboTracker {
    private static int combo;
    private static int lastHurtTime;
    private static long lastAttack;
    private ComboTracker() {}
    public static void attacked(){
        long now=System.currentTimeMillis();
        if(now-lastAttack>2500) combo=0;
        combo++;
        lastAttack=now;
    }
    public static void tick(int hurtTime){
        if(hurtTime>0 && lastHurtTime==0) combo=0;
        if(combo>0 && System.currentTimeMillis()-lastAttack>5000) combo=0;
        lastHurtTime=hurtTime;
    }
    public static int value(){ return combo; }
    public static void reset(){ combo=0; lastHurtTime=0; lastAttack=0; }
}
