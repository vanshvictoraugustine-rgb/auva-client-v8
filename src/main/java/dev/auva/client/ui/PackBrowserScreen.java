package dev.auva.client.ui;

import dev.auva.client.core.SafePackService;
import dev.auva.client.core.SafePackService.Pack;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Search and install resource packs from Modrinth. No arbitrary URL downloads. */
public final class PackBrowserScreen extends Screen {
    private EditBox searchBox;
    private volatile List<Pack> found=List.of();
    private volatile String status="Search for a resource pack on Modrinth";
    private boolean busy=false;
    private int page=0;
    private final Screen parent;
    public PackBrowserScreen(Screen parent){super(Component.literal("Auva Pack Browser"));this.parent=parent;}
    @Override protected void init(){
        int left=width/2-245,top=height/2-145;
        searchBox=new EditBox(font,left+18,top+44,325,20,Component.literal("Search resource packs"));
        searchBox.setMaxLength(80);
        searchBox.setHint(Component.literal("Search: PvP, purple, 16x..."));
        addRenderableWidget(searchBox);
        addRenderableWidget(Button.builder(Component.literal("Search"),btn->search())
            .bounds(left+350,top+44,90,20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"),btn->minecraft.setScreen(parent))
            .bounds(left+440,top+8,40,19).build());
        addRenderableWidget(Button.builder(Component.literal("<"),btn->page=Math.max(0,page-1))
            .bounds(left+360,top+261,30,20).build());
        addRenderableWidget(Button.builder(Component.literal(">"),btn->page=Math.min(Math.max(0,(found.size()-1)/5),page+1))
            .bounds(left+399,top+261,30,20).build());
    }
    private void search(){
        if(busy)return;
        busy=true;status="Searching Modrinth...";
        String query=searchBox.getValue();
        Thread worker=new Thread(()->{
            try{List<Pack> result=SafePackService.search(query);
                Minecraft.getInstance().execute(()->{found=result;page=0;status=result.size()+" packs found";busy=false;});
            }catch(Exception ex){Minecraft.getInstance().execute(()->{status="Search failed: "+shortError(ex);busy=false;});}
        },"Auva-Pack-Search");worker.setDaemon(true);worker.start();
    }
    private void download(Pack pack){
        if(busy)return;
        busy=true;status="Downloading and verifying: "+pack.title();
        Thread worker=new Thread(()->{
            try{
                Path target=SafePackService.download(pack,Path.of("resourcepacks"),"1.21.11");
                Minecraft.getInstance().execute(()->{status="Installed: "+target.getFileName()+" | Enable in Resource Packs";busy=false;});
            }catch(Exception ex){Minecraft.getInstance().execute(()->{status="Download blocked/failed: "+shortError(ex);busy=false;});}
        },"Auva-Pack-Download");worker.setDaemon(true);worker.start();
    }
    private static String shortError(Exception e){String m=e.getMessage();return m==null?e.getClass().getSimpleName():m.length()>80?m.substring(0,80):m;}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        int l=width/2-245,t=height/2-145;
        g.fill(l,t,l+490,t+290,0xF3110D20);
        g.fill(l,t,l+490,t+3,0xFFAD77FF);
        g.drawString(font,"AUVA   /   RESOURCE PACKS",l+18,t+13,0xFFCF9FFF,true);
        g.drawString(font,"Source: Modrinth only  •  verified ZIPs  •  Minecraft 1.21.11",l+18,t+32,0xFFA7A2BE);
        int start=page*5;
        for(int i=0;i<5&&start+i<found.size();i++){
            Pack pack=found.get(start+i);int y=t+77+i*34;
            g.fill(l+17,y,l+470,y+30,i%2==0?0xFF211B34:0xFF19152A);
            String title=pack.title();if(font.width(title)>290)title=font.plainSubstrByWidth(title,280)+"...";
            g.drawString(font,title,l+25,y+4,0xFFF1EAFB,true);
            g.drawString(font,"by "+pack.author(),l+25,y+17,0xFFA8A0BB);
            g.fill(l+377,y+4,l+457,y+25,busy?0xFF4D4759:0xFF7947B5);
            g.drawCenteredString(font,busy?"WAIT":"DOWNLOAD",l+417,y+10,0xFFFFFFFF);
        }
        g.drawString(font,"Page "+(page+1)+" / "+Math.max(1,(found.size()+4)/5),l+20,t+267,0xFFB6A8CB);
        String s=status;if(font.width(s)>448)s=font.plainSubstrByWidth(s,437)+"...";
        g.drawString(font,s,l+18,t+250,0xFFD7BFED);
        super.render(g,mx,my,delta);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean dbl){
        int l=width/2-245,t=height/2-145;
        if(e.button()==0&&!busy&&e.x()>=l+377&&e.x()<l+457){
            for(int i=0;i<5;i++){
                int y=t+77+i*34,index=page*5+i;
                if(index<found.size()&&e.y()>=y+4&&e.y()<y+25){download(found.get(index));return true;}
            }
        }
        return super.mouseClicked(e,dbl);
    }
    @Override public boolean isPauseScreen(){return false;}
}
