package dev.auva.client.ui;

import dev.auva.client.AuvaClient;
import dev.auva.client.core.Module;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** All configurable module binds in one searchable screen with conflict hints. */
public final class KeybindManagerScreen extends Screen {
    private final Screen parent;
    private EditBox search;
    private Module pending;
    private int scroll;
    public KeybindManagerScreen(Screen parent){super(Component.literal("Auva Keybinds"));this.parent=parent;}
    @Override protected void init(){
        search=new EditBox(font,width/2-150,40,235,20,Component.literal("Find a module"));
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal("Back"),b->minecraft.setScreen(parent)).bounds(width/2+100,40,60,20).build());
    }
    private List<Module> result(){
        var r=new ArrayList<Module>();String q=search.getValue().toLowerCase(Locale.ROOT);
        for(Module m:AuvaClient.MODULES.values())if(m.available()&&m.name.toLowerCase(Locale.ROOT).contains(q))r.add(m);
        return r;
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt){
        g.fill(0,0,width,height,0xED0D0814);
        int l=width/2-205;
        g.drawCenteredString(font,"AUVA  /  KEYBINDS",width/2,17,AuvaMenu.MAGENTA);
        g.drawCenteredString(font,"Click an action to bind  ·  Escape cancels  ·  Delete clears",width/2,29,AuvaMenu.MUTED);
        var modules=result();
        int count=Math.min(modules.size()-scroll,Math.max(1,(height-116)/25));
        for(int i=0;i<count;i++){
            Module m=modules.get(scroll+i);int y=73+i*25;
            AuvaMenu.rounded(g,l,y,410,22,3,AuvaMenu.hit(mx,my,l,y,410,22)?0xFF372345:0xFF21152F);
            g.drawString(font,m.name,l+12,y+7,AuvaMenu.WHITE);
            String binding=m==pending?"PRESS KEY...":m.keyCode<0?"NONE":"KEY "+m.keyCode;
            int conflicts=0;
            for(Module other:AuvaClient.MODULES.values())if(other!=m && m.keyCode>=0&&other.keyCode==m.keyCode)conflicts++;
            g.drawString(font,binding,l+278,y+7,conflicts>0?0xFFFFBF8E:AuvaMenu.MAGENTA);
        }
        g.drawCenteredString(font,modules.size()+" bindable modules  ·  scroll for more",width/2,height-24,AuvaMenu.MUTED);
        super.render(g,mx,my,dt);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean dbl){
        if(e.button()==0){
            int l=width/2-205;var modules=result();
            int count=Math.min(modules.size()-scroll,Math.max(1,(height-116)/25));
            for(int i=0;i<count;i++)if(AuvaMenu.hit(e.x(),e.y(),l,73+i*25,410,22)){
                pending=modules.get(i+scroll);return true;
            }
        }
        return super.mouseClicked(e,dbl);
    }
    @Override public boolean keyPressed(KeyEvent e){
        if(pending!=null){
            if(e.key()!=GLFW.GLFW_KEY_ESCAPE){
                pending.keyCode=(e.key()==GLFW.GLFW_KEY_DELETE||e.key()==GLFW.GLFW_KEY_BACKSPACE)?-1:e.key();
                AuvaClient.save();
            }
            pending=null;
            return true;
        }
        return super.keyPressed(e);
    }
    @Override public boolean mouseScrolled(double x,double y,double hz,double vert){
        scroll=Math.max(0,Math.min(Math.max(0,result().size()-(height-116)/25),scroll-(int)Math.signum(vert)*2));return true;
    }
    @Override public boolean isPauseScreen(){return false;}
}
