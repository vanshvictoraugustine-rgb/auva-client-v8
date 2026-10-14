package dev.auva.client.ui;

import dev.auva.client.AuvaClient;
import dev.auva.client.core.Module;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import dev.auva.client.render.HudRenderer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class HudEditor extends Screen {
    private static final int ACCENT=0xFFB785FF;
    private final Map<Module,int[]> bounds = new LinkedHashMap<>();
    private Module active;
    private double offsetX,offsetY;
    private boolean resize;
    private boolean snap=true;
    private Module menuModule;
    private int menuX,menuY;
    private final java.util.Set<String> lockedWidgets=new java.util.HashSet<>();
    public HudEditor(){super(Component.literal("Auva HUD Editor"));}
    @Override protected void init(){
        int cx=width/2;
        addRenderableWidget(Button.builder(Component.literal("Snap: ON"),b->{snap=!snap;b.setMessage(Component.literal("Snap: "+(snap?"ON":"OFF")));})
            .bounds(cx-185,6,81,20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset"),b->{
            int index=0;
            for(Module m:AuvaClient.MODULES.values())if(m.isHud()){
                m.x=9;m.y=42+index*22;m.scale=1;index++;
            }
            AuvaClient.save();
        }).bounds(cx-97,6,58,20).build());
        addRenderableWidget(Button.builder(Component.literal("Save & Exit"),b->onClose())
            .bounds(cx+75,6,110,20).build());
    }
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        super.render(g,mx,my,delta);
        g.fill(0,0,width,height,0x330D0814);
        g.fill(width/2-195,0,width/2+195,36,0xDD160D23);
        g.drawCenteredString(font,"AUVA HUD EDITOR  ·  Drag widgets / resize corner handles",width/2,29,ACCENT);
        bounds.clear();
        for(Module m:AuvaClient.MODULES.values()){
            if(!m.isHud()||!m.enabled)continue;
            int ox=m.name.equals("Scoreboard")&&m.x<0?Math.max(0,width-180):m.x;
            int[] actual=HudRenderer.widgetSize(m,Minecraft.getInstance());
            int w=actual[0],h=actual[1];
            HudRenderer.drawWidget(g,m,Minecraft.getInstance());
            bounds.put(m,new int[]{ox,m.y,w,h});
            if(active==m){
                g.renderOutline(ox-2,m.y-2,w+4,h+4,ACCENT);
                g.fill(ox+w-4,m.y+h-4,ox+w+2,m.y+h+2,ACCENT);
            }
        }
        if(menuModule!=null){
            int right=Math.min(width-146,menuX),top=Math.min(height-100,menuY);
            AuvaMenu.rounded(g,right,top,142,96,7,0xF3160D23);
            g.renderOutline(right,top,142,96,ACCENT);
            String[] actions={"Settings", "Disable Module", "Reset Position",lockedWidgets.contains(menuModule.name)?"Unlock":"Lock"};
            for(int i=0;i<actions.length;i++){
                if(AuvaMenu.hit(mx,my,right+5,top+4+i*22,132,21))g.fill(right+5,top+4+i*22,right+137,top+24+i*22,0xFF422153);
                g.drawString(font,actions[i],right+12,top+10+i*22,0xFFF7F1FF);
            }
        }
        if(active!=null && menuModule==null){
            int sx=width/2,sy=height/2;
            g.fill(sx,40,sx+1,height,0x55E6B965);g.fill(0,sy,width,sy+1,0x55E6B965);
        }
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean doubleClick){
        if(menuModule!=null){
            int right=Math.min(width-146,menuX),top=Math.min(height-100,menuY);
            if(AuvaMenu.hit(e.x(),e.y(),right,top,142,96) && e.button()==0){
                int choice=(int)((e.y()-top-4)/22);
                Module chosen=menuModule;
                menuModule=null;
                switch(choice){
                    case 0 -> {minecraft.setScreen(new ModuleSettingsScreen(this,chosen));return true;}
                    case 1 -> {AuvaClient.toggle(chosen.name);active=null;}
                    case 2 -> {chosen.x=12;chosen.y=42;chosen.scale=1;}
                    case 3 -> {if(!lockedWidgets.add(chosen.name))lockedWidgets.remove(chosen.name);}
                }
                AuvaClient.save();return true;
            }
            menuModule=null;
        }
        for(var entry:bounds.entrySet()){
            int[] b=entry.getValue();
            if(e.x()>=b[0]-3&&e.x()<=b[0]+b[2]+4&&e.y()>=b[1]-3&&e.y()<=b[1]+b[3]+4){
                active=entry.getKey();
                if(e.button()==1){menuModule=active;menuX=(int)e.x();menuY=(int)e.y();return true;}
                if(lockedWidgets.contains(active.name))return true;
                resize=e.x()>b[0]+b[2]-12&&e.y()>b[1]+b[3]-12;
                offsetX=e.x()-b[0];offsetY=e.y()-b[1];
                if(active.x<0)active.x=b[0];
                return true;
            }
        }
        return super.mouseClicked(e,doubleClick);
    }
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){
        if(active!=null && !lockedWidgets.contains(active.name)){
            if(resize){
                int base=Math.max(1,Math.round(HudRenderer.widgetSize(active,Minecraft.getInstance())[0]/Math.max(.35f,active.scale)));
                active.scale=Math.max(.35f,Math.min(4f,(float)(e.x()-active.x)/base));
            }else{
                int[] size=HudRenderer.widgetSize(active,Minecraft.getInstance());
                active.x=Math.max(0,Math.min(width-size[0],(int)(e.x()-offsetX)));
                active.y=Math.max(40,Math.min(height-size[1],(int)(e.y()-offsetY)));
                if(snap){
                    if(Math.abs(active.x+size[0]/2-width/2)<=6)active.x=width/2-size[0]/2;
                    if(Math.abs(active.y+size[1]/2-height/2)<=6)active.y=height/2-size[1]/2;
                }
            }
            return true;
        }
        return super.mouseDragged(e,dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent e){AuvaClient.save();return super.mouseReleased(e);}
    @Override public void onClose(){AuvaClient.save();super.onClose();}
    @Override public boolean isPauseScreen(){return false;}
}
