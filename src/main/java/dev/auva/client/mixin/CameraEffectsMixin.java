package dev.auva.client.mixin;

import dev.auva.client.AuvaClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class CameraEffectsMixin {
    // The third Axis.rotationDegrees in GameRenderer.bobHurt is the damage tilt,
    // whereas the first is death animation. Leave death camera untouched.
    @ModifyArg(method="bobHurt",at=@At(value="INVOKE",target="Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;",ordinal=2),index=0,require=0)
    private float auva$damageTilt(float degrees){
        if(!AuvaClient.enabled("HurtCam"))return degrees;
        return degrees*AuvaClient.module("HurtCam").strength;
    }
    @Inject(method="getFov",at=@At("RETURN"),cancellable=true)
    private void auva$zoom(Camera camera,float partialTicks,boolean changingFov,CallbackInfoReturnable<Float> cir){
        cir.setReturnValue(cir.getReturnValue()*dev.auva.client.core.SmoothZoom.multiplier());
    }
}
