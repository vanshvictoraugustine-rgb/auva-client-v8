package dev.auva.client.ui;

import dev.auva.client.AuvaClient;
import dev.auva.client.core.Module;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;

/** Searchable, scalable card directory. Card body opens a real configuration screen. */
public final class AuvaMenu extends Screen {
    private static final Identifier LOGO=Identifier.fromNamespaceAndPath("auvaclient","textures/gui/logo_banner.png");
    public static final int MAGENTA=0xFFD946EF,VIOLET=0xFF8B5CF6,WHITE=0xFFF7F1FF,MUTED=0xFFA78BFA;
    private static final String[] CATEGORIES={"All","HUD","Visual","Camera","PvP","Utility","Audio","Performance","Integrations","Menus"};
    private String category="All";
    private EditBox search;
    private final Screen parent;
    private int scroll;
    private double visualScroll;
    private long previousFrame;
    private double lastMx,lastMy;
    public AuvaMenu(){this(null);}
    public AuvaMenu(Screen parent){super(Component.literal("Auva Client"));this.parent=parent;}
    private int w(){return Math.min(900,width-20);}
    private int h(){return Math.min(530,height-20);}
    private int x(){return (width-w())/2;}
    private int y(){return (height-h())/2;}
    private int contentLeft(){return x()+151;}
    private int contentRight(){return x()+w()-16;}
    private int gridWidth(){return contentRight()-contentLeft();}
    private int colWidth(){return (gridWidth()-22)/3;}
    private int gridTop(){return y()+99;}
    private int rows(){return Math.max(1,(h()-150)/104);}
    @Override protected void init(){
        int sw=Math.min(220,Math.max(100,w()/3));
        String old=search==null?"":search.getValue();
        search=new EditBox(font,x()+w()-sw-16,y()+20,sw,22,Component.literal("Search Auva modules"));
        search.setMaxLength(60);
        search.setHint(Component.literal("Search modules..."));
        search.setValue(old);
        addRenderableWidget(search);
    }
    private List<Module> visible(){
        String q=search==null?"":search.getValue().strip().toLowerCase(Locale.ROOT);
        var out=new ArrayList<Module>();
        for(Module m:AuvaClient.MODULES.values()){
            if(!category.equals("All")&&!m.category.equals(category))continue;
            if(!q.isBlank() && !m.name.toLowerCase(Locale.ROOT).contains(q)
                && !m.description().toLowerCase(Locale.ROOT).contains(q))continue;
            out.add(m);
        }
        return out;
    }
    public static void rounded(GuiGraphics g,int x,int y,int w,int h,int radius,int color){
        if(w<radius*2 || h<radius*2){g.fill(x,y,x+w,y+h,color);return;}
        g.fill(x+radius,y,x+w-radius,y+h,color);
        g.fill(x,y+radius,x+w,y+h-radius,color);
        for(int k=0;k<radius;k++){
            int d=radius-k-1;
            g.fill(x+d,y+k,x+w-d,y+k+1,color);
            g.fill(x+d,y+h-k-1,x+w-d,y+h-k,color);
        }
    }
    public static boolean hit(double mx,double my,int x,int y,int w,int h){
        return mx>=x&&mx<x+w&&my>=y&&my<y+h;
    }
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        lastMx=mx;lastMy=my;
        g.fill(0,0,width,height,0xAA0D0814);
        long frame=System.nanoTime();
        double dt=previousFrame==0?0.016:Math.min(.05,(frame-previousFrame)/1.0e9);
        previousFrame=frame;
        visualScroll+=(scroll-visualScroll)*(1-Math.exp(-18*dt));
        if(Math.abs(scroll-visualScroll)<.01)visualScroll=scroll;
        int l=x(),t=y(),ww=w(),hh=h();
        rounded(g,l,t,ww,hh,10,0xF30D0814);
        g.fill(l+10,t+5,l+ww-10,t+7,MAGENTA);
        rounded(g,l+12,t+14,124,hh-28,8,0xFF160D23);
        g.blit(RenderPipelines.GUI_TEXTURED,LOGO,l+20,t+15,0,0,106,58,512,280);
        g.drawCenteredString(font,"v0.4 BUILD",l+74,t+71,MUTED);
        int navY=t+84;
        for(int i=0;i<CATEGORIES.length;i++){
            int yy=navY+i*30;
            if(yy+26>t+hh-112)break;
            boolean active=category.equals(CATEGORIES[i]);
            if(active || hit(mx,my,l+19,yy,109,25))rounded(g,l+19,yy,109,25,5,active?0xFF452655:0xFF281832);
            if(active)g.fill(l+19,yy+4,l+22,yy+20,MAGENTA);
            g.drawString(font,CATEGORIES[i],l+29,yy+8,active?WHITE:0xFFBCA6CE,false);
        }
        int footer=t+hh-95;
        g.drawString(font,"TOOLS",l+26,footer,0xFF9A7BAF);
        tool(g,l+19,footer+16,109,"HUD Editor",mx,my);
        tool(g,l+19,footer+45,53,"Packs",mx,my);
        tool(g,l+75,footer+45,53,"Music",mx,my);
        g.drawString(font,"MODULE LIBRARY",contentLeft(),t+19,WHITE,true);
        g.drawString(font,"Search, configure, and personalize your client",contentLeft(),t+36,0xFF9C87AB);
        int count=visible().size();
        g.drawString(font,count+" modules  ·  purple system",contentLeft(),t+62,MUTED,false);
        g.fill(contentLeft(),t+84,contentRight(),t+85,0xFF4A2A63);
        var modules=visible();
        int perPage=rows()*3;
        scroll=Math.max(0,Math.min(scroll,Math.max(0,modules.size()-perPage)));
        int firstRow=Math.max(0,(int)Math.floor(visualScroll/3));
        int subOffset=(int)Math.round((visualScroll/3-firstRow)*104);
        g.enableScissor(contentLeft()-2,gridTop()-2,contentRight()+2,t+hh-35);
        for(int slot=0;slot<perPage+6;slot++){
            int idx=firstRow*3+slot;
            if(idx>=modules.size())break;
            Module m=modules.get(idx);
            int cx=contentLeft()+(slot%3)*(colWidth()+11);
            int cy=gridTop()+(slot/3)*104-subOffset;
            drawCard(g,m,cx,cy,colWidth(),95,mx,my);
        }
        g.disableScissor();
        g.drawString(font,"AUVA CLIENT  ·  Made by AuvaPlays",contentLeft(),t+hh-23,0xFF9A7DAA);
        g.drawString(font,"Scroll to explore  |  RShift to close",Math.max(contentLeft(),contentRight()-215),t+hh-23,0xFF846F93);
        super.render(g,mx,my,delta);
    }
    private void drawCard(GuiGraphics g,Module m,int xx,int yy,int cw,int ch,int mx,int my){
        boolean hover=hit(mx,my,xx,yy,cw,ch);
        rounded(g,xx,yy,cw,ch,6,hover?0xFF352044:0xFF21152E);
        g.renderOutline(xx,yy,cw,ch,hover?0xFF8247A0:0xFF452651);
        rounded(g,xx+10,yy+9,24,24,5,m.available()?0xFF4E2460:0xFF302838);
        String icon=m.name.substring(0,Math.min(1,m.name.length())).toUpperCase(Locale.ROOT);
        g.drawCenteredString(font,icon,xx+22,yy+17,m.available()?MAGENTA:0xFF807387);
        String name=m.name;
        if(font.width(name)>cw-50)name=font.plainSubstrByWidth(name,cw-60)+"…";
        g.drawString(font,name,xx+41,yy+12,WHITE,true);
        g.drawString(font,m.category.toUpperCase(Locale.ROOT),xx+41,yy+26,0xFFA88AAF);
        String description=m.description();
        String summary=font.plainSubstrByWidth(description,cw-22);
        g.drawString(font,summary+(summary.length()<description.length()?"…":""),xx+10,yy+45,0xFFB3A4C0,false);
        rounded(g,xx+10,yy+65,cw-56,21,4,m.available()?(m.enabled?0xFF732A83:0xFF33233E):0xFF29212D);
        g.drawCenteredString(font,m.available()?(m.category.equals("Menus")?"OPEN":m.name.equals("Freelook")?"HOLD":m.enabled?"ENABLED":"DISABLED"):"UNAVAILABLE",xx+10+(cw-56)/2,yy+72,m.available()?WHITE:0xFF988AA0);
        rounded(g,xx+cw-38,yy+65,28,21,4,0xFF49305B);
        g.drawCenteredString(font,"⚙",xx+cw-24,yy+71,WHITE);
    }
    private void tool(GuiGraphics g,int x,int y,int w,String title,int mx,int my){
        rounded(g,x,y,w,25,5,hit(mx,my,x,y,w,25)?0xFF65317A:0xFF39234A);
        g.drawCenteredString(font,title,x+w/2,y+8,WHITE);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean dbl){
        int l=x(),t=y(),hh=h();
        if(e.button()==0){
            int footer=t+hh-95;
            if(hit(e.x(),e.y(),l+19,footer+16,109,25)){minecraft.setScreen(new HudEditor());return true;}
            if(hit(e.x(),e.y(),l+19,footer+45,53,25)){AuvaClient.openPacks(this);return true;}
            if(hit(e.x(),e.y(),l+75,footer+45,53,25)){AuvaClient.openMusic(this);return true;}
            int navY=t+84;
            for(int i=0;i<CATEGORIES.length;i++){
                int yy=navY+i*30;
                if(yy+26>t+hh-112)break;
                if(hit(e.x(),e.y(),l+19,yy,109,25)){category=CATEGORIES[i];scroll=0;visualScroll=0;return true;}
            }
            var modules=visible();int page=rows()*3;
            int firstRow=Math.max(0,(int)Math.floor(visualScroll/3));
            int subOffset=(int)Math.round((visualScroll/3-firstRow)*104);
            for(int slot=0;slot<page+6;slot++){
                int idx=firstRow*3+slot;if(idx>=modules.size())break;
                int cx=contentLeft()+(slot%3)*(colWidth()+11);
                int cy=gridTop()+(slot/3)*104-subOffset;
                if(cy>=gridTop()-95 && cy<y()+h()-35 && hit(e.x(),e.y(),cx,cy,colWidth(),95)){
                    Module m=modules.get(idx);
                    if(m.available() && m.category.equals("Menus")){
                        switch(m.name){
                            case "Resource Pack Browser" -> AuvaClient.openPacks(this);
                            case "Music Player" -> AuvaClient.openMusic(this);
                            case "HUD Editor" -> minecraft.setScreen(new HudEditor());
                            case "Main Menu" -> minecraft.setScreen(new AuvaHomeScreen());
                            case "Keybind Manager" -> minecraft.setScreen(new KeybindManagerScreen(this));
                            case "Profiles / Configs" -> minecraft.setScreen(new ProfilesScreen(this));
                            case "Screenshot Manager" -> minecraft.setScreen(new ScreenshotManagerScreen(this));
                            default -> minecraft.setScreen(new ModuleSettingsScreen(this,m));
                        }
                    }else if(hit(e.x(),e.y(),cx+10,cy+65,colWidth()-56,21))AuvaClient.toggle(m.name);
                    else minecraft.setScreen(new ModuleSettingsScreen(this,m));
                    return true;
                }
            }
        }
        return super.mouseClicked(e,dbl);
    }
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){
        scroll=Math.max(0,Math.min(Math.max(0,visible().size()-rows()*3),scroll-(int)Math.signum(vertical)*3));
        return true;
    }
    @Override public void onClose(){AuvaClient.save();minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
