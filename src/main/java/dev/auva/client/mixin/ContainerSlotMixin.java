package dev.auva.client.mixin;

import dev.auva.client.AuvaClient;
import dev.auva.client.core.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draw directly inside the slot, before its item model (player inventory, chests, shulkers, etc). */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerSlotMixin {
    @Inject(method="renderSlot",at=@At("HEAD"))
    private void auva$highlightInventoryItems(GuiGraphics graphics,Slot slot,int mouseX,int mouseY,CallbackInfo ci){
        if(!AuvaClient.enabled("Item Highlighter")||!slot.hasItem())return;
        Module m=AuvaClient.module("Item Highlighter");
        String id=BuiltInRegistries.ITEM.getKey(slot.getItem().getItem()).toString();
        boolean match=false;
        for(String target:m.targetItems.split(","))if(id.equals(target.trim())){match=true;break;}
        if(!match)return;
        int alpha=Math.min(230,Math.max(10,(int)(m.strength*230)));
        int fill=(alpha<<24)|(m.color&0xFFFFFF);
        graphics.fill(slot.x,slot.y,slot.x+16,slot.y+16,fill);
        graphics.renderOutline(slot.x,slot.y,16,16,0xFF000000|(m.color&0xFFFFFF));
    }
}
