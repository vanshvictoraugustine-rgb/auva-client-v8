package dev.auva.client.ui;

import dev.auva.client.AuvaClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Auva's original responsive glass-style home. All button input uses these exact bounds. */
public final class AuvaHomeScreen extends Screen {
    private static final int MAGENTA=0xFFD946EF, PURPLE=0xFF8B5CF6, WHITE=0xFFF6EDFF;
    private long opened;
    private int hover=-1;
    private final float[] glow=new float[9];
    private static final String[] MAIN={"Singleplayer","Multiplayer","Mods","Vanilla Menu"};
    private static final String[] DOCK={"Music","Packs","Profiles","Vanilla"};
    public AuvaHomeScreen(){super(Component.literal("Auva Client"));}
    @Override protected void init(){opened=System.nanoTime();}
    private int panelWidth(){return Math.min(422,Math.max(255,width-30));}
    private int left(){return (width-panelWidth())/2;}
    private int top(){return Math.max(70,(height-360)/2);}
    private static boolean hit(double mx,double my,int x,int y,int w,int h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
    private void rectangle(GuiGraphics g,int x,int y,int w,int h,int bg,int edge){
        g.fill(x+5,y,x+w-5,y+h,bg);
        g.fill(x,y+5,x+w,y+h-5,bg);
        g.fill(x+1,y+3,x+w-1,y+h-3,bg);
        g.renderOutline(x,y,w,h,edge);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt){
        g.fill(0,0,width,height,0xFF090712);
        // Static low-cost atmospheric background, with no heavyweight framebuffer effects.
        for(int i=0;i<14;i++){
            int y=i*height/14;
            int color=((14+i*2)<<24)|0x9425BC;
            g.fill(0,y,width,y+height/14,color);
        }
        long now=System.nanoTime();
        float entrance=Math.min(1,(now-opened)/240_000_000f);
        float ease=1-(1-entrance)*(1-entrance)*(1-entrance);
        int pw=panelWidth(),x=left(),t=top();
        int shift=Math.round((1-ease)*18);
        g.drawCenteredString(font,"✦  A U V A",width/2,t-53-shift,WHITE);
        g.drawCenteredString(font,"C L I E N T",width/2,t-37-shift,0xFFCA97EC);
        rectangle(g,x,t+shift,pw,290,0xE9130D20,0xBBAD53EA);
        int mainX=x+24, mainW=pw-48;
        hover=-1;
        for(int i=0;i<4;i++){
            int by=t+19+i*53+shift;
            if(hit(mx,my,mainX,by,mainW,45))hover=i;
            glow[i]+=( (hover==i?1:0)-glow[i])*Math.min(1,dt*0.23f+0.08f);
            int alpha=(int)(80+glow[i]*110);
            rectangle(g,mainX,by,mainW,45,0xD51C122B,(alpha<<24)|0xD946EF);
            g.drawString(font,MAIN[i],mainX+24,by+18,WHITE,false);
            g.drawString(font,">",mainX+mainW-23,by+18,glow[i]>.2?MAGENTA:0xFFAB90BC,false);
        }
        int dockY=t+245+shift,dockGap=5,dockW=(pw-34-3*dockGap)/4;
        for(int i=0;i<4;i++){
            int bx=x+17+i*(dockW+dockGap);
            if(hit(mx,my,bx,dockY,dockW,32))hover=4+i;
            glow[i+4]+=((hover==i+4?1:0)-glow[i+4])*Math.min(1,dt*.23f+.08f);
            rectangle(g,bx,dockY,dockW,32,0xDD1C122B,((int)(70+glow[i+4]*160)<<24)|0x9D67DE);
            g.drawCenteredString(font,DOCK[i],bx+dockW/2,dockY+12,WHITE);
        }
        g.drawString(font,"AUVA CLIENT  ·  Made by AuvaPlays",13,height-18,0xFFC299DD,false);
        String vers="Fabric 1.21.11  ·  v0.7 dev";
        g.drawString(font,vers,width-font.width(vers)-12,height-18,0xFFAC89C8,false);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean doubleClick){
        if(e.button()!=0)return super.mouseClicked(e,doubleClick);
        int pw=panelWidth(),x=left(),t=top(),mainX=x+24,mainW=pw-48;
        for(int i=0;i<4;i++)if(hit(e.x(),e.y(),mainX,t+19+i*53,mainW,45)){
            switch(i){
                case 0 -> minecraft.setScreen(new SelectWorldScreen(this));
                case 1 -> minecraft.setScreen(new JoinMultiplayerScreen(this));
                case 2 -> minecraft.setScreen(new AuvaMenu(this));
                case 3 -> minecraft.setScreen(new TitleScreen());
            }
            return true;
        }
        int dockW=(pw-34-15)/4;
        for(int i=0;i<4;i++)if(hit(e.x(),e.y(),x+17+i*(dockW+5),t+245,dockW,32)){
            switch(i){
                case 0 -> AuvaClient.openMusic(this);
                case 1 -> AuvaClient.openPacks(this);
                case 2 -> minecraft.setScreen(new ProfilesScreen(this));
                case 3 -> minecraft.setScreen(new TitleScreen());
            }
            return true;
        }
        return super.mouseClicked(e,doubleClick);
    }
    @Override public boolean isPauseScreen(){return false;}
}
