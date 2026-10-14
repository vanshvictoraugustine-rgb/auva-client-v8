package dev.auva.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.auva.client.AuvaClient;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Player character visuals only; entity physics / hitboxes / multiplayer authority are not changed. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method="scale",at=@At("TAIL"))
    private void auva$playerModelSize(LivingEntityRenderState state,PoseStack matrices,CallbackInfo ci){
        if(!AuvaClient.enabled("Model Resizer") || state.entityType!=EntityType.PLAYER)return;
        float factor=AuvaClient.module("Model Resizer").scale;
        matrices.scale(factor,factor,factor);
    }
}
