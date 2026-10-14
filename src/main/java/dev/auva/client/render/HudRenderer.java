package dev.auva.client.render;

import dev.auva.client.AuvaClient;
import dev.auva.client.core.CameraControl;
import dev.auva.client.core.CpsTracker;
import dev.auva.client.core.ComboTracker;
import dev.auva.client.core.GameplayToggles;
import dev.auva.client.core.NotificationCenter;
import dev.auva.client.core.Module;
import dev.auva.client.ui.AuvaMenu;
import dev.auva.client.ui.HudEditor;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import org.lwjgl.glfw.GLFW;

/** Shared Auva widget renderer: one scale/background/text language for every HUD element. */
public final class HudRenderer {
    private HudRenderer(){}
    private static final int TEXT=0xFFF7F2FF, SUB=0xFFC1AACF;
    private static final EquipmentSlot[] ARMOR={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    private static final DateTimeFormatter TIME=DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void render(GuiGraphics g){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.options.hideGui||mc.screen instanceof AuvaMenu||mc.screen instanceof HudEditor)return;
        for(Module m:AuvaClient.MODULES.values())if(m.isHud()&&m.enabled&&m.available()&&!m.name.equals("Scoreboard"))drawWidget(g,m,mc);
        drawCrosshair(g,mc);
        if(CameraControl.freecam()){
            g.drawCenteredString(mc.font,"AUVA FREECAM  |  press bind to return",g.guiWidth()/2,g.guiHeight()-55,0xFFE6B1FF);
        }else if(CameraControl.freelook()){
            g.drawCenteredString(mc.font,"FREELOOK",g.guiWidth()/2,g.guiHeight()-55,0xFFDFBAFF);
        }
        if(AuvaClient.enabled("Toggle Sprint/Sneak")){
            String state=GameplayToggles.sprint()?"[Sprinting (Toggled)]":GameplayToggles.sneak()?"[Sneaking (Toggled)]":"";
            if(!state.isEmpty())g.drawString(mc.font,state,7,g.guiHeight()-28,0xFFD946EF,true);
        }
        if(AuvaClient.enabled("Notifications")) renderNotifications(g,mc);
    }
    private static void renderNotifications(GuiGraphics g,Minecraft mc){
        var active=NotificationCenter.active();
        int y=g.guiHeight()-42;
        for(int i=active.length-1;i>=0;i--){
            var toast=active[i];
            int w=Math.max(150,Math.max(mc.font.width(toast.title()),mc.font.width(toast.body()))+24);
            int x=g.guiWidth()-w-8;
            g.fill(x,y-1,x+w,y+30,0xDD160D23);
            g.fill(x,y-1,x+3,y+30,0xFFD946EF);
            g.drawString(mc.font,toast.title(),x+10,y+5,0xFFFFFFFF,true);
            g.drawString(mc.font,toast.body(),x+10,y+17,0xFFA78BFA,false);
            y-=36;
        }
    }
    public static void drawWidget(GuiGraphics g,Module m,Minecraft mc){
        if(!m.isHud()||!m.enabled||mc.player==null)return;
        int x=m.x<0?Math.max(0,g.guiWidth()-scoreWidth(mc,m)-7):m.x;
        int y=m.y;
        g.pose().pushMatrix();
        g.pose().translate(x,y);
        g.pose().scale(m.scale,m.scale);
        switch(m.name){
            case "FPS HUD" -> line(g,mc,m,"FPS  "+mc.getFps());
            case "CPS HUD" -> line(g,mc,m,"CPS  "+CpsTracker.left()+" | "+CpsTracker.right());
            case "Combo Counter" -> line(g,mc,m,"COMBO  "+ComboTracker.value());
            case "Ping HUD" -> {
                int ping=-1;
                if(mc.getConnection()!=null){
                    var info=mc.getConnection().getPlayerInfo(mc.player.getUUID());
                    if(info!=null)ping=info.getLatency();
                }
                line(g,mc,m,"PING  "+(ping<0?"--":ping+"ms"));
            }
            case "Clock" -> line(g,mc,m,"TIME  "+LocalTime.now().format(TIME));
            case "Coordinates HUD" -> {
                var pos=mc.player.blockPosition();
                String cardinal=mc.player.getDirection().getName().toUpperCase(java.util.Locale.ROOT);
                multi(g,mc,m,List.of("X "+pos.getX()+"  Y "+pos.getY()+"  Z "+pos.getZ(),
                    "DIR "+cardinal+"  |  CHUNK "+(pos.getX()>>4)+", "+(pos.getZ()>>4)));
            }
            case "Direction HUD" -> compass(g,mc,m);
            case "Armor HUD" -> armor(g,mc,m);
            case "Potion Effects" -> potions(g,mc,m);
            case "Scoreboard" -> { /* Native renderer preserves server components and team styling. */ }
            case "Keystrokes" -> keystrokes(g,mc,m);
            case "Inventory HUD" -> inventory(g,mc,m);
            case "Totem Counter" -> {
                int total=0;
                for(int i=0;i<mc.player.getInventory().getContainerSize();i++){
                    var stack=mc.player.getInventory().getItem(i);
                    if(stack.is(Items.TOTEM_OF_UNDYING))total+=stack.getCount();
                }
                if(mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING))total+=mc.player.getOffhandItem().getCount();
                line(g,mc,m,"TOTEMS  "+total);
            }
            case "Durability HUD" -> {
                var hand=mc.player.getMainHandItem();
                if(!hand.isEmpty()){
                    if(hand.isDamageableItem()){
                        int remaining=hand.getMaxDamage()-hand.getDamageValue();
                        line(g,mc,m,"DURABILITY  "+remaining+" / "+hand.getMaxDamage());
                    }else line(g,mc,m,hand.getHoverName().getString()+"  x"+hand.getCount());
                }
            }
            case "AppleSkin / Food HUD" -> {
                var food=mc.player.getMainHandItem().get(net.minecraft.core.component.DataComponents.FOOD);
                if(food==null)food=mc.player.getOffhandItem().get(net.minecraft.core.component.DataComponents.FOOD);
                if(food!=null)line(g,mc,m,"FOOD  +"+food.nutrition()+"  SAT  +"+String.format(java.util.Locale.ROOT,"%.1f",food.saturation()));
            }
        }
        g.pose().popMatrix();
    }
    /** Editor geometry derived from the same live text and dimensions as the renderer. */
    public static int[] widgetSize(Module m, Minecraft mc) {
        int pad=m.padding, w=80, h=13+pad;
        switch(m.name){
            case "FPS HUD" -> w=mc.font.width("FPS  "+mc.getFps())+pad*2;
            case "CPS HUD" -> w=mc.font.width("CPS  "+CpsTracker.left()+" | "+CpsTracker.right())+pad*2;
            case "Combo Counter" -> w=mc.font.width("COMBO  "+ComboTracker.value())+pad*2;
            case "Ping HUD" -> w=mc.font.width("PING  999ms")+pad*2;
            case "Clock" -> w=mc.font.width("TIME  23:59:59")+pad*2;
            case "Totem Counter" -> w=mc.font.width("TOTEMS  999")+pad*2;
            case "Durability HUD" -> w=mc.font.width("DURABILITY  9999 / 9999")+pad*2;
            case "AppleSkin / Food HUD" -> w=mc.font.width("FOOD  +20  SAT  +20.0")+pad*2;
            case "Keystrokes" -> {w=72;h="WASD".equalsIgnoreCase(m.layout)?44:"MOUSE".equalsIgnoreCase(m.layout)?67:84;}
            case "Inventory HUD" -> {w=179;h=64;}
            case "Direction HUD" -> {w=180;h=24;}
            case "Armor HUD" -> {
                int count=0;
                if(mc.player!=null){
                    for(EquipmentSlot slot:ARMOR)if(!mc.player.getItemBySlot(slot).isEmpty())count++;
                    if(!mc.player.getOffhandItem().isEmpty())count++;
                }
                w=104;h=Math.max(18,count*19+8);
            }
            case "Scoreboard" -> {
                List<String> lines=scoreLines(mc,m.showNumbers);
                w=100;
                for(String line:lines)w=Math.max(w,mc.font.width(line)+pad*2);
                h=pad*2+12+Math.max(0,lines.size()-1)*11;
            }
            case "Potion Effects" -> {
                w=90;int count=0;
                if(mc.player!=null)for(var effect:mc.player.getActiveEffects()){
                    count++;
                    String name=effect.getEffect().value().getDisplayName().getString();
                    w=Math.max(w,mc.font.width(name+" III  99:59")+pad*2);
                }
                h=pad*2+Math.max(1,count)*12;
            }
            case "Coordinates HUD" -> {
                if(mc.player!=null){
                    var pos=mc.player.blockPosition();
                    w=Math.max(mc.font.width("X "+pos.getX()+"  Y "+pos.getY()+"  Z "+pos.getZ()),mc.font.width("DIR NORTH  |  CHUNK "+(pos.getX()>>4)+", "+(pos.getZ()>>4)))+pad*2;
                }
                h=pad*2+24;
            }
        }
        return new int[]{Math.max(12,Math.round(w*m.scale)),Math.max(12,Math.round(h*m.scale))};
    }

    private static void box(GuiGraphics g,Module m,int w,int h){
        if(!m.background)return;
        int alpha=Math.max(0,Math.min(235,Math.round(m.opacity*220)));
        int bg=(alpha<<24)|0x160D23;
        // No giant colored side-bars or invasive screen-sized textures.
        g.fill(0,0,w,h,bg);
        if(m.outline)g.renderOutline(0,0,w,h,(Math.min(alpha+50,230)<<24)|(m.color&0xFFFFFF));
    }
    private static void line(GuiGraphics g,Minecraft mc,Module m,String text){
        int w=mc.font.width(text)+m.padding*2;
        box(g,m,w,13+m.padding);
        g.drawString(mc.font,text,m.padding,Math.max(3,m.padding-1),m.color,true);
    }
    private static void multi(GuiGraphics g,Minecraft mc,Module m,List<String> lines){
        if(lines.isEmpty())return;
        int w=40;
        for(String s:lines)w=Math.max(w,mc.font.width(s)+m.padding*2);
        int h=m.padding*2+lines.size()*12;
        box(g,m,w,h);
        for(int i=0;i<lines.size();i++)g.drawString(mc.font,lines.get(i),m.padding,m.padding+i*12,i==0?TEXT:SUB,true);
    }
    private static void potions(GuiGraphics g,Minecraft mc,Module m){
        List<String> lines=new ArrayList<>();
        for(var effect:mc.player.getActiveEffects()){
            int secs=Math.max(0,effect.getDuration()/20);
            String time=effect.isInfiniteDuration()?"∞":String.format(java.util.Locale.ROOT,"%d:%02d",secs/60,secs%60);
            lines.add(effect.getEffect().value().getDisplayName().getString()+" "+(effect.getAmplifier()+1)+"  "+time);
        }
        if(!lines.isEmpty())multi(g,mc,m,lines);
    }
    private static void armor(GuiGraphics g,Minecraft mc,Module m){
        List<ItemStack> items=new ArrayList<>();
        for(EquipmentSlot slot:ARMOR){
            ItemStack item=mc.player.getItemBySlot(slot);
            if(!item.isEmpty())items.add(item);
        }
        if(!mc.player.getOffhandItem().isEmpty())items.add(mc.player.getOffhandItem());
        if(items.isEmpty())return;
        box(g,m,104,items.size()*19+8);
        for(int i=0;i<items.size();i++){
            int y=i*19+4;
            ItemStack stack=items.get(i);
            g.renderItem(stack,4,y);
            int color=m.color;
            String detail="x"+stack.getCount();
            if(stack.isDamageableItem()){
                int remaining=stack.getMaxDamage()-stack.getDamageValue();
                int pct=100*remaining/Math.max(1,stack.getMaxDamage());
                detail=pct+"%";
                if(pct<=20)color=0xFFFF6C79;else if(pct<=50)color=0xFFFFD477;
                g.fill(27,y+14,99,y+16,0x665B5368);
                g.fill(27,y+14,27+(72*pct/100),y+16,color);
            }
            g.drawString(mc.font,detail,28,y+3,color,true);
        }
    }
    private static List<String> scoreLines(Minecraft mc, boolean numbers){
        var out=new ArrayList<String>();
        if(mc.level==null)return out;
        var sb=mc.level.getScoreboard();
        Objective obj=sb.getDisplayObjective(DisplaySlot.SIDEBAR);
        if(obj==null)return out;
        out.add(obj.getDisplayName().getString());
        var entries=new ArrayList<PlayerScoreEntry>(sb.listPlayerScores(obj));
        entries.removeIf(PlayerScoreEntry::isHidden);
        entries.sort(Comparator.comparingInt(PlayerScoreEntry::value).reversed());
        if(entries.size()>15)entries=new ArrayList<>(entries.subList(0,15));
        for(var entry:entries){
            String name=entry.display()==null?entry.owner():entry.display().getString();
            out.add(name+(numbers?"  "+entry.value():""));
        }
        return out;
    }
    private static int scoreWidth(Minecraft mc,Module m){
        int w=100;
        for(String s:scoreLines(mc,m.showNumbers))w=Math.max(w,mc.font.width(s)+m.padding*2);
        return Math.round(w*m.scale);
    }
    private static void scoreboard(GuiGraphics g,Minecraft mc,Module m){
        List<String> lines=scoreLines(mc,m.showNumbers);
        if(lines.isEmpty())return;
        int w=100;
        for(String s:lines)w=Math.max(w,mc.font.width(s)+m.padding*2);
        int h=m.padding*2+12+(lines.size()-1)*11;
        box(g,m,w,h);
        g.fill(0,0,w,15,(int)(0xDD000000L)|(m.color&0xFFFFFF));
        g.drawCenteredString(mc.font,lines.get(0),w/2,3,TEXT);
        for(int i=1;i<lines.size();i++)g.drawString(mc.font,lines.get(i),m.padding,16+(i-1)*11,TEXT,true);
    }
    private static boolean mouse(Minecraft mc,int code){
        return GLFW.glfwGetMouseButton(mc.getWindow().handle(),code)==GLFW.GLFW_PRESS;
    }
    private static void cap(GuiGraphics g,Minecraft mc,int x,int y,int w,int h,String text,boolean on,int color){
        g.fill(x,y,x+w,y+h,on?color:0xAA21192C);
        g.renderOutline(x,y,w,h,on?0xFFFAE8FF:0x77482E60);
        g.drawCenteredString(mc.font,text,x+w/2,y+(h-8)/2,TEXT);
    }
    private static void keystrokes(GuiGraphics g,Minecraft mc,Module m){
        boolean w=mc.options.keyUp.isDown(),a=mc.options.keyLeft.isDown(),s=mc.options.keyDown.isDown(),d=mc.options.keyRight.isDown();
        int fg=m.color;
        cap(g,mc,25,0,22,21,"W",w,fg);
        cap(g,mc,1,23,22,21,"A",a,fg);
        cap(g,mc,25,23,22,21,"S",s,fg);
        cap(g,mc,49,23,22,21,"D",d,fg);
        if(!"WASD".equalsIgnoreCase(m.layout)){
            boolean l=mouse(mc,GLFW.GLFW_MOUSE_BUTTON_LEFT),r=mouse(mc,GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            cap(g,mc,1,46,34,21,m.dynamic?""+CpsTracker.left():"LMB",l,fg);
            cap(g,mc,37,46,34,21,m.dynamic?""+CpsTracker.right():"RMB",r,fg);
            if(!"MOUSE".equalsIgnoreCase(m.layout))cap(g,mc,1,69,70,15,"SPACE",mc.options.keyJump.isDown(),fg);
        }
    }
    private static void inventory(GuiGraphics g,Minecraft mc,Module m){
        box(g,m,179,64);
        for(int i=9;i<36;i++){
            int x=4+((i-9)%9)*19,y=4+((i-9)/9)*19;
            var item=mc.player.getInventory().getItem(i);
            if(!item.isEmpty())g.renderItem(item,x,y);
        }
    }
    private static void compass(GuiGraphics g,Minecraft mc,Module m){
        box(g,m,180,24);
        String[] dirs={"S","SW","W","NW","N","NE","E","SE"};
        int index=Math.floorMod(Math.round(mc.player.getYRot()/45f),8);
        g.drawCenteredString(mc.font,dirs[Math.floorMod(index-1,8)],37,8,SUB);
        g.drawCenteredString(mc.font,dirs[index],90,8,m.color);
        g.drawCenteredString(mc.font,dirs[(index+1)%8],143,8,SUB);
        g.fill(89,2,91,5,m.color);
    }
    private static void drawCrosshair(GuiGraphics g,Minecraft mc){
        Module m=AuvaClient.module("Custom Crosshair");
        if(m==null||!m.enabled||mc.screen!=null||!mc.options.getCameraType().isFirstPerson())return;
        int cx=g.guiWidth()/2,cy=g.guiHeight()/2;
        int gap=m.gap+(m.dynamic&&(mc.player.isSprinting()||mc.player.getDeltaMovement().horizontalDistanceSqr()>.04)?3:0);
        gap=Math.round(gap*m.scale);
        int len=Math.max(1,Math.round(m.length*m.scale));
        int thick=Math.max(1,Math.round(m.thickness*m.scale));
        int half=thick/2;
        if(m.outline){
            int o=0xEE0D0814;
            g.fill(cx-half-1,cy-gap-len-1,cx-half+thick+1,cy-gap+1,o);
            g.fill(cx-half-1,cy+gap-1,cx-half+thick+1,cy+gap+len+1,o);
            g.fill(cx-gap-len-1,cy-half-1,cx-gap+1,cy-half+thick+1,o);
            g.fill(cx+gap-1,cy-half-1,cx+gap+len+1,cy-half+thick+1,o);
        }
        g.fill(cx-half,cy-gap-len,cx-half+thick,cy-gap,m.color);
        g.fill(cx-half,cy+gap,cx-half+thick,cy+gap+len,m.color);
        g.fill(cx-gap-len,cy-half,cx-gap,cy-half+thick,m.color);
        g.fill(cx+gap,cy-half,cx+gap+len,cy-half+thick,m.color);
        if(m.centerDot)g.fill(cx-half,cy-half,cx-half+thick,cy-half+thick,m.color);
    }
}
