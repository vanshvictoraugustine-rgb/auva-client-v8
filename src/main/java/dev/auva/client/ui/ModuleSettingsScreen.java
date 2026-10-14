package dev.auva.client.ui;

import dev.auva.client.AuvaClient;
import dev.auva.client.core.HitSounds;
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

/** Module-specific controls with actual persisted settings, sliders, key binds and live values. */
public final class ModuleSettingsScreen extends Screen {
    private record Row(String title,String id,String type,float min,float max){}
    private final Screen parent;
    private final Module m;
    private final List<Row> rows=new ArrayList<>();
    private EditBox hex;
    private int scroll;
    private String dragging;
    private boolean capturingKey;
    public ModuleSettingsScreen(Screen parent,Module module){
        super(Component.literal("Auva / "+module.name));
        this.parent=parent;this.m=module;
        configure();
    }
    private static Row toggle(String title,String id){return new Row(title,id,"toggle",0,1);}
    private static Row slider(String title,String id,float min,float max){return new Row(title,id,"slider",min,max);}
    private static Row select(String title,String id){return new Row(title,id,"select",0,0);}
    private static Row action(String title,String id){return new Row(title,id,"action",0,0);}
    private static Row heading(String title){return new Row(title,"","heading",0,0);}
    private void configure(){
        rows.add(heading("GENERAL"));
        if(!m.name.equals("Freelook"))rows.add(toggle("Enabled","enabled"));
        rows.add(select("Keybind","keyCode"));
        if(!m.name.equals("Freelook")&&!m.isHud())rows.add(toggle("Hold activation","hold"));
        if(!m.available()){rows.add(heading("REQUIRES EXTERNAL IMPLEMENTATION"));return;}
        if(m.isHud()){
            rows.add(heading("HUD APPEARANCE"));
            rows.add(slider("Scale","scale",0.35f,4f));
            rows.add(toggle("Background","background"));
            rows.add(slider("Background opacity","opacity",0f,1f));
            rows.add(toggle("Border","outline"));
            rows.add(slider("Padding","padding",2,24));
        }
        rows.add(heading("MODULE OPTIONS"));
        switch(m.name){
            case "Keystrokes"->{rows.add(select("Layout","layout"));rows.add(toggle("Show CPS in mouse keys","dynamic"));}
            case "Scoreboard"->rows.add(toggle("Show score numbers","showNumbers"));
            case "Custom Crosshair"->{
                rows.add(slider("Crosshair scale","scale",0.5f,4));
                rows.add(slider("Gap","gap",0,20));
                rows.add(slider("Length","length",1,24));
                rows.add(slider("Thickness","thickness",1,10));
                rows.add(toggle("Center dot","centerDot"));
                rows.add(toggle("Dynamic movement","dynamic"));
                rows.add(toggle("Dark outline","outline"));
            }
            case "Freecam"->rows.add(slider("Flight speed","scale",0.35f,4));
            case "HurtCam"->rows.add(slider("Hurt tilt multiplier","strength",0,1));
            case "Zoom"->rows.add(slider("Zoom FOV multiplier","scale",0.10f,0.95f));
            case "Hit Sounds"->{
                rows.add(select("Sound preset","soundPreset"));
                rows.add(slider("Sound volume","strength",0,1));
                rows.add(action("Test sound","testSound"));
            }
            case "Item Highlighter"->{rows.add(select("Highlighted items","targetItems"));rows.add(slider("Highlight strength","strength",.10f,1));}
        }
        rows.add(heading("COLORS"));
        rows.add(action("Cycle palette","color"));
        rows.add(action("Apply HEX value","hex"));
        rows.add(heading("RESET"));
        rows.add(action("Restore module settings","reset"));
    }
    private int x(){return Math.max(8,width/2-350);}
    private int y(){return Math.max(8,height/2-215);}
    private int w(){return Math.min(700,width-16);}
    private int h(){return Math.min(430,height-16);}
    private int listX(){return x()+21;}
    private int listY(){return y()+105;}
    private int listW(){return Math.min(w()-42,405);}
    private int rowH(){return 36;}
    private int maxRows(){return Math.max(2,(h()-145)/rowH());}
    @Override protected void init(){
        hex=new EditBox(font,x()+w()-170,y()+h()-42,120,20,Component.literal("RGBA color"));
        hex.setMaxLength(8);
        hex.setValue(String.format(Locale.ROOT,"%08X",m.color));
        addRenderableWidget(hex);
        addRenderableWidget(Button.builder(Component.literal("Back"),b->minecraft.setScreen(parent)).bounds(x()+17,y()+13,47,20).build());
        addRenderableWidget(Button.builder(Component.literal("HUD"),b->minecraft.setScreen(new HudEditor())).bounds(x()+w()-84,y()+13,60,20).build());
    }
    private String value(Row r){
        return switch(r.id()){
            case "enabled" -> m.enabled?"ON":"OFF";
            case "keyCode" -> capturingKey?"PRESS A KEY...":m.keyCode<0?"NONE":GLFW.glfwGetKeyName(m.keyCode,0)==null?"KEY "+m.keyCode:GLFW.glfwGetKeyName(m.keyCode,0).toUpperCase(Locale.ROOT);
            case "hold" -> m.hold?"HOLD":"TOGGLE";
            case "background" -> m.background?"ON":"OFF";
            case "outline" -> m.outline?"ON":"OFF";
            case "showNumbers" -> m.showNumbers?"ON":"OFF";
            case "centerDot" -> m.centerDot?"ON":"OFF";
            case "dynamic" -> m.dynamic?"ON":"OFF";
            case "scale" -> String.format(Locale.ROOT,"%.2fx",m.scale);
            case "strength" -> Math.round(m.strength*100)+"%";
            case "opacity" -> Math.round(m.opacity*100)+"%";
            case "gap" -> ""+m.gap;
            case "length" -> ""+m.length;
            case "thickness" -> ""+m.thickness;
            case "padding" -> ""+m.padding;
            case "layout" -> m.layout;
            case "targetItems" -> m.targetItems.replace("minecraft:","").replace(","," + ");
            case "soundPreset" -> HitSounds.LABELS[Math.max(0,Math.min(HitSounds.LABELS.length-1,m.soundPreset))];
            default -> "";
        };
    }
    private float getNumber(String id){
        return switch(id){
            case "scale"->m.scale;
            case "strength"->m.strength;
            case "opacity"->m.opacity;
            case "gap"->m.gap;
            case "length"->m.length;
            case "thickness"->m.thickness;
            case "padding"->m.padding;
            default->0;
        };
    }
    private void setNumber(String id,float v){
        switch(id){
            case "scale"->m.scale=v;
            case "strength"->m.strength=v;
            case "opacity"->m.opacity=v;
            case "gap"->m.gap=Math.round(v);
            case "length"->m.length=Math.round(v);
            case "thickness"->m.thickness=Math.round(v);
            case "padding"->m.padding=Math.round(v);
        }
        AuvaClient.save();
    }
    private void setSlider(Row row,double px){
        int right=listX()+listW()-15;
        int left=right-112;
        float t=(float)Math.max(0,Math.min(1,(px-left)/(right-left)));
        setNumber(row.id(),row.min()+(row.max()-row.min())*t);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        g.fill(0,0,width,height,0xB30D0814);
        int l=x(),t=y(),ww=w(),hh=h();
        AuvaMenu.rounded(g,l,t,ww,hh,9,0xF30D0814);
        g.fill(l+12,t+5,l+ww-12,t+7,AuvaMenu.MAGENTA);
        g.drawString(font,m.name,l+79,t+16,AuvaMenu.WHITE,true);
        g.drawString(font,m.category.toUpperCase(Locale.ROOT)+(m.available()?"  /  LIVE SETTINGS":"  /  NOT YET IMPLEMENTED"),l+22,t+48,m.available()?AuvaMenu.MAGENTA:0xFFFFBB80);
        String descr=m.description();
        String a=font.plainSubstrByWidth(descr,ww-48);
        g.drawString(font,a,l+22,t+66,0xFFCEB9D9);
        if(a.length()<descr.length()){
            g.drawString(font,font.plainSubstrByWidth(descr.substring(a.length()),ww-48),l+22,t+79,0xFFCEB9D9);
        }
        g.fill(listX(),t+99,l+ww-18,t+100,0xFF48255E);
        scroll=Math.max(0,Math.min(scroll,Math.max(0,rows.size()-maxRows())));
        for(int slot=0;slot<maxRows();slot++){
            int idx=scroll+slot;if(idx>=rows.size())break;
            Row r=rows.get(idx);
            int yy=listY()+slot*rowH();
            if(r.type().equals("heading")){
                g.drawString(font,r.title(),listX()+5,yy+8,AuvaMenu.MAGENTA,true);
                continue;
            }
            boolean hover=AuvaMenu.hit(mx,my,listX(),yy,listW(),31);
            AuvaMenu.rounded(g,listX(),yy,listW(),30,4,hover?0xFF33203E:0xFF21152B);
            g.drawString(font,r.title(),listX()+10,yy+11,AuvaMenu.WHITE,false);
            if(r.type().equals("slider")){
                int xx=listX()+listW()-125;
                float pct=Math.max(0,Math.min(1,(getNumber(r.id())-r.min())/(r.max()-r.min())));
                g.fill(xx,yy+18,xx+112,yy+22,0xFF4F3563);
                g.fill(xx,yy+18,xx+Math.round(112*pct),yy+22,AuvaMenu.MAGENTA);
                g.drawString(font,value(r),xx,yy+5,0xFFDDA7EE);
            }else{
                String v=r.type().equals("action")?"OPEN / APPLY":value(r);
                if(font.width(v)>122)v=font.plainSubstrByWidth(v,117)+"…";
                g.drawString(font,v,listX()+listW()-font.width(v)-11,yy+11,AuvaMenu.MUTED);
            }
        }
        g.drawString(font,"HEX COLOR",l+ww-235,t+hh-35,AuvaMenu.MUTED);
        g.fill(l+ww-40,t+hh-42,l+ww-20,t+hh-22,m.color);
        g.drawString(font,"Scroll settings  •  Values save automatically",l+18,t+hh-35,0xFF9483A0);
        super.render(g,mx,my,delta);
    }
    private void activate(Row r,int button,double mx){
        switch(r.type()){
            case "slider"->setSlider(r,mx);
            case "toggle"-> {
                switch(r.id()){
                    case "enabled" -> AuvaClient.toggle(m.name);
                    case "hold" -> m.hold=!m.hold;
                    case "background" -> m.background=!m.background;
                    case "outline" -> m.outline=!m.outline;
                    case "showNumbers" -> m.showNumbers=!m.showNumbers;
                    case "centerDot" -> m.centerDot=!m.centerDot;
                    case "dynamic" -> m.dynamic=!m.dynamic;
                }
                AuvaClient.save();
            }
            case "select" -> {
                switch(r.id()){
                    case "keyCode" ->capturingKey=true;
                    case "layout" -> m.layout=switch(m.layout){case "WASD"->"MOUSE";case "MOUSE"->"FULL";default->"WASD";};
                    case "soundPreset"->m.soundPreset=(m.soundPreset+1)%HitSounds.LABELS.length;
                    case "targetItems"->m.targetItems=switch(m.targetItems){
                        case "minecraft:totem_of_undying"->"minecraft:diamond,minecraft:netherite_ingot";
                        case "minecraft:diamond,minecraft:netherite_ingot"->"minecraft:enchanted_golden_apple";
                        default->"minecraft:totem_of_undying";
                    };
                }
                AuvaClient.save();
            }
            case "action" -> {
                switch(r.id()){
                    case "color"->{
                        int[] colors={0xFFD946EF,0xFF8B5CF6,0xFFC084FC,0xFFFFFFFF,0xFF50D9D0,0xFFFFAABB};
                        int pos=0;
                        for(int k=0;k<colors.length;k++)if(colors[k]==m.color)pos=k+1;
                        m.color=colors[pos%colors.length];
                        hex.setValue(String.format(Locale.ROOT,"%08X",m.color));
                    }
                    case "hex"->{
                        try{
                            String txt=hex.getValue().strip().replace("#","");
                            if(txt.length()==6)m.color=0xFF000000|(int)Long.parseLong(txt,16);
                            else if(txt.length()==8)m.color=(int)Long.parseLong(txt,16);
                        }catch(NumberFormatException ignored){}
                    }
                    case "testSound"->HitSounds.play(m);
                    case "reset"->{
                        m.scale=1;m.opacity=.70f;m.padding=6;m.color=0xFFD946EF;
                        m.background=true;m.outline=true;m.dynamic=false;m.centerDot=false;
                        m.gap=3;m.length=6;m.thickness=2;m.strength=.65f;
                    }
                }
                AuvaClient.save();
            }
        }
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean doubleClick){
        if(e.button()==0 && m.available()){
            for(int slot=0;slot<maxRows();slot++){
                int idx=scroll+slot;if(idx>=rows.size())break;
                Row row=rows.get(idx);int yy=listY()+slot*rowH();
                if(AuvaMenu.hit(e.x(),e.y(),listX(),yy,listW(),30)){
                    if(!row.type().equals("heading")){
                        activate(row,e.button(),e.x());
                        dragging=row.type().equals("slider")?row.id():null;
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(e,doubleClick);
    }
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){
        if(dragging!=null){
            for(Row r:rows)if(dragging.equals(r.id())){setSlider(r,e.x());return true;}
        }
        return super.mouseDragged(e,dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent e){dragging=null;return super.mouseReleased(e);}
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){
        scroll=Math.max(0,Math.min(Math.max(0,rows.size()-maxRows()),scroll-(int)Math.signum(vertical)*2));return true;
    }
    @Override public boolean keyPressed(KeyEvent e){
        if(capturingKey){
            if(e.key()==GLFW.GLFW_KEY_ESCAPE){capturingKey=false;return true;}
            m.keyCode=(e.key()==GLFW.GLFW_KEY_BACKSPACE||e.key()==GLFW.GLFW_KEY_DELETE)?-1:e.key();
            capturingKey=false;AuvaClient.save();return true;
        }
        return super.keyPressed(e);
    }
    @Override public void onClose(){AuvaClient.save();minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
