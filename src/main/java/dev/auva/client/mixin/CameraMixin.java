package dev.auva.client.mixin;

import dev.auva.client.core.CameraControl;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setPosition(double x,double y,double z);
    @Shadow protected abstract void setRotation(float yaw,float pitch);

    @Inject(method="setup",at=@At("TAIL"))
    private void auva$camera(Level level,Entity entity,boolean thirdPerson,boolean inverseView,float tickProgress,CallbackInfo ci){
        CameraControl.frame(net.minecraft.client.Minecraft.getInstance());
        if(CameraControl.freecam()){
            setRotation(CameraControl.yaw,CameraControl.pitch);
            setPosition(CameraControl.renderX(tickProgress),CameraControl.renderY(tickProgress),CameraControl.renderZ(tickProgress));
        }else if(CameraControl.freelook()){
            setRotation(CameraControl.yaw,CameraControl.pitch);
            // Use Minecraft's interpolated entity position instead of 20 TPS raw coordinates.
            Vec3 eye=entity.getEyePosition(tickProgress);
            double yaw=Math.toRadians(CameraControl.yaw);
            double pitch=Math.toRadians(CameraControl.pitch);
            double reach=4.0;
            Vec3 desired=eye.add(Math.sin(yaw)*Math.cos(pitch)*reach,-Math.sin(pitch)*reach,-Math.cos(yaw)*Math.cos(pitch)*reach);
            var hit=level.clip(new ClipContext(eye,desired,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,entity));
            if(hit.getType()!=net.minecraft.world.phys.HitResult.Type.MISS){
                Vec3 ray=desired.subtract(eye);
                double length=ray.length();
                double safe=Math.max(0,eye.distanceTo(hit.getLocation())-0.15);
                if(length>1.0e-5)desired=eye.add(ray.scale(Math.min(1,safe/length)));
            }
            setPosition(desired.x,desired.y,desired.z);
        }
    }
}
