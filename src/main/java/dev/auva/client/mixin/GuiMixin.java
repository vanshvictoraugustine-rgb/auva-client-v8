package dev.auva.client.mixin;

import dev.auva.client.AuvaClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method="renderCrosshair",at=@At("HEAD"),cancellable=true)
    private void auva$replaceVanillaCrosshair(GuiGraphics graphics, DeltaTracker counter,CallbackInfo ci){
        if(AuvaClient.enabled("Custom Crosshair"))ci.cancel();
    }
    @Inject(method="displayScoreboardSidebar",at=@At("HEAD"),cancellable=true)
    private void auva$replaceVanillaScoreboard(GuiGraphics graphics,net.minecraft.world.scores.Objective obj,CallbackInfo ci){
        // Preserve the server's full styled scoreboard until Auva can render
        // team prefixes/suffixes and component formatting losslessly.
        // Do not cancel vanilla scoreboard rendering.
    }
}
