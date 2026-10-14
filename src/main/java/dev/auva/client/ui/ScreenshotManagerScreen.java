package dev.auva.client.ui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Local screenshot directory browser; destructive removal requires two explicit clicks. */
public final class ScreenshotManagerScreen extends Screen {
    private final Screen parent;
    private final Path folder=Path.of("screenshots").toAbsolutePath().normalize();
    private List<Path> photos=List.of();
    private String status="Click a screenshot to copy its absolute path";
    private Path selected,confirmDelete;
    private int scroll;
    public ScreenshotManagerScreen(Screen parent){super(Component.literal("Auva Screenshots"));this.parent=parent;}
    private void refresh(){
        try{
            Files.createDirectories(folder);
            try(Stream<Path> ls=Files.list(folder)){
                photos=ls.filter(p->Files.isRegularFile(p)&&p.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".png"))
                    .sorted((a,b)->b.toString().compareTo(a.toString())).limit(500).toList();
            }
        }catch(Exception e){status="Cannot read screenshot directory";photos=List.of();}
    }
    @Override protected void init(){
        refresh();
        int y=height-50;
        addRenderableWidget(Button.builder(Component.literal("Back"),b->minecraft.setScreen(parent)).bounds(width/2-186,y,74,20).build());
        addRenderableWidget(Button.builder(Component.literal("Refresh"),b->refresh()).bounds(width/2-95,y,74,20).build());
        addRenderableWidget(Button.builder(Component.literal("Copy Path"),b->{
            if(selected!=null){minecraft.keyboardHandler.setClipboard(selected.toString());status="Copied screenshot path";}
        }).bounds(width/2-4,y,92,20).build());
        addRenderableWidget(Button.builder(Component.literal("Delete (2x)"),b->{
            if(selected==null)return;
            if(!selected.equals(confirmDelete)){
                confirmDelete=selected;
                status="Click Delete again to permanently remove "+selected.getFileName();
                return;
            }
            try{
                if(selected.normalize().startsWith(folder)&&Files.isRegularFile(selected))Files.delete(selected);
                status="Deleted screenshot";selected=null;confirmDelete=null;refresh();
            }catch(Exception e){status="Delete failed: "+e.getMessage();}
        }).bounds(width/2+105,y,101,20).build());
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt){
        g.fill(0,0,width,height,0xED0D0814);
        g.drawCenteredString(font,"AUVA  /  SCREENSHOTS",width/2,17,AuvaMenu.MAGENTA);
        g.drawCenteredString(font,status,width/2,36,AuvaMenu.MUTED);
        int per=Math.max(1,(height-124)/28);
        for(int i=0;i<per&&i+scroll<photos.size();i++){
            Path file=photos.get(i+scroll);
            int x=width/2-206,y=64+i*28;
            AuvaMenu.rounded(g,x,y,412,24,4,file.equals(selected)?0xFF58316F:0xFF21162E);
            g.drawString(font,font.plainSubstrByWidth(file.getFileName().toString(),335),x+12,y+8,AuvaMenu.WHITE);
        }
        g.drawCenteredString(font,photos.size()+" screenshot(s) · Page scroll",width/2,height-66,AuvaMenu.MUTED);
        super.render(g,mx,my,dt);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean dbl){
        if(e.button()==0){
            int per=Math.max(1,(height-124)/28);
            for(int i=0;i<per&&i+scroll<photos.size();i++){
                if(AuvaMenu.hit(e.x(),e.y(),width/2-206,64+i*28,412,24)){
                    selected=photos.get(i+scroll);confirmDelete=null;
                    status="Selected "+selected.getFileName();return true;
                }
            }
        }
        return super.mouseClicked(e,dbl);
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        int visible=Math.max(1,(height-124)/28);
        scroll=Math.max(0,Math.min(Math.max(0,photos.size()-visible),scroll-(int)Math.signum(vertical)));return true;
    }
    @Override public boolean isPauseScreen(){return false;}
}
