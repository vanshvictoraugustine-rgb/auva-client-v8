package dev.auva.client.core;

public final class MotionState {
    public static float lastYaw,smoothed;
    private MotionState(){}
    public static void update(float change) {
        lastYaw=change;
        smoothed=smoothed*.8f+Math.abs(change)*.2f;
    }
}
