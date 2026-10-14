package dev.auva.client.ui;

import dev.auva.client.AuvaClient;
import dev.auva.client.core.ProfileStore;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Save and load module configuration snapshots, locally. */
public final class ProfilesScreen extends Screen {
    private final Screen parent;
    private EditBox name;
    private String message="Choose a profile to load or enter a name to save";
    private List<String> names=List.of();
    private int scroll;
    public ProfilesScreen(Screen parent){super(Component.literal("Auva Profiles"));this.parent=parent;}
    @Override protected void init(){
        names=ProfileStore.list();
        name=new EditBox(font,width/2-160,56,190,20,Component.literal("Profile name"));
        name.setMaxLength(32);name.setHint(Component.literal("New profile name"));
        addRenderableWidget(name);
        addRenderableWidget(Button.builder(Component.literal("Save"),b->{
            try{ProfileStore.save(name.getValue(),AuvaClient.MODULES);names=ProfileStore.list();message="Saved "+name.getValue();}
            catch(Exception e){message=e.getMessage();}
        }).bounds(width/2+35,56,55,20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"),b->minecraft.setScreen(parent)).bounds(width/2+102,56,58,20).build());
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt){
        g.fill(0,0,width,height,0xF20D0814);
        g.drawCenteredString(font,"AUVA  /  PROFILES",width/2,21,AuvaMenu.MAGENTA);
        g.drawCenteredString(font,message,width/2,39,AuvaMenu.MUTED);
        int rows=Math.min(names.size()-scroll,Math.max(1,(height-130)/31));
        for(int i=0;i<rows;i++){
            int y=90+i*31,l=width/2-170;
            AuvaMenu.rounded(g,l,y,340,27,4,AuvaMenu.hit(mx,my,l,y,340,27)?0xFF46305C:0xFF21142C);
            g.drawString(font,names.get(scroll+i),l+11,y+9,AuvaMenu.WHITE);
            g.drawString(font,"CLICK TO APPLY",l+230,y+9,AuvaMenu.MAGENTA);
        }
        g.drawCenteredString(font,"Saved profiles contain only module settings; no accounts or tokens",width/2,height-18,AuvaMenu.MUTED);
        super.render(g,mx,my,dt);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean dbl){
        if(e.button()==0){
            for(int i=0;i<Math.min(names.size()-scroll,Math.max(1,(height-130)/31));i++)if(AuvaMenu.hit(e.x(),e.y(),width/2-170,90+i*31,340,27)){
                String selected=names.get(scroll+i);
                ProfileStore.apply(selected,AuvaClient.MODULES);
                AuvaClient.save();
                message="Applied profile "+selected;
                return true;
            }
        }
        return super.mouseClicked(e,dbl);
    }
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){
        scroll=Math.max(0,Math.min(Math.max(0,names.size()-(height-130)/31),scroll-(int)Math.signum(vertical)));return true;
    }
    @Override public boolean isPauseScreen(){return false;}
}
