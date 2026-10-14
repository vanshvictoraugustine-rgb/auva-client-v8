package dev.auva.client.mixin;

import dev.auva.client.core.CameraControl;
import dev.auva.client.core.MotionState;
import dev.auva.client.core.CpsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Capture the exact vanilla mouse sensitivity result, then restore the actual player's aim. */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Unique private float auva$oldYaw,auva$oldPitch,auva$oldHeadYaw,auva$oldBodyYaw,auva$oldHeadYawOld,auva$oldBodyYawOld;
    @Inject(method="onButton",at=@At("HEAD"),require=0)
    private void auva$mouseCps(long window,net.minecraft.client.input.MouseButtonInfo input,int action,CallbackInfo ci){
        if(action==org.lwjgl.glfw.GLFW.GLFW_PRESS && Minecraft.getInstance().screen==null)
            CpsTracker.click(input.button());
    }

    @Inject(method="onScroll",at=@At("HEAD"),cancellable=true,require=0)
    private void auva$zoomWheel(long window,double horizontal,double vertical,CallbackInfo ci){
        if(Minecraft.getInstance().screen==null && dev.auva.client.AuvaClient.enabled("Zoom") && vertical!=0){
            dev.auva.client.core.SmoothZoom.scroll(vertical);
            ci.cancel();
        }
    }

    @Inject(method="turnPlayer",at=@At("HEAD"))
    private void auva$beforeTurn(double timeDelta,CallbackInfo ci){
        var player=Minecraft.getInstance().player;
        if(player==null)return;
        auva$oldYaw=player.getYRot();auva$oldPitch=player.getXRot();
        auva$oldHeadYaw=player.yHeadRot;auva$oldBodyYaw=player.yBodyRot;
        auva$oldHeadYawOld=player.yHeadRotO;auva$oldBodyYawOld=player.yBodyRotO;
    }
    @Inject(method="turnPlayer",at=@At("TAIL"))
    private void auva$afterTurn(double timeDelta,CallbackInfo ci){
        var player=Minecraft.getInstance().player;
        if(player==null)return;
        float changeYaw=player.getYRot()-auva$oldYaw;
        float changePitch=player.getXRot()-auva$oldPitch;
        MotionState.update(changeYaw);
        if(CameraControl.independent()){
            CameraControl.mouseDelta(changeYaw,changePitch);
            player.setYRot(auva$oldYaw);
            player.setXRot(auva$oldPitch);
            player.yHeadRot=auva$oldHeadYaw;
            player.yBodyRot=auva$oldBodyYaw;
            player.yHeadRotO=auva$oldHeadYawOld;
            player.yBodyRotO=auva$oldBodyYawOld;
        }
    }
}
