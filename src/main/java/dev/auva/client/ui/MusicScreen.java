package dev.auva.client.ui;

import dev.auva.client.core.LocalMusicPlayer;
import java.awt.Desktop;
import java.nio.file.Files;
import java.util.Locale;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Local WAV player. Spotify/YouTube Music are official website shortcuts, not stream rippers. */
public final class MusicScreen extends Screen {
    private final Screen parent;
    private List<Path> songs=List.of();
    private int scroll;
    private String message="Drop WAV files into the Auva music folder";
    public MusicScreen(Screen parent){super(Component.literal("Auva Music"));this.parent=parent;}
    @Override protected void init(){
        songs=LocalMusicPlayer.tracks();
        int l=width/2-230,t=height/2-135;
        addRenderableWidget(Button.builder(Component.literal("Back"),b->minecraft.setScreen(parent)).bounds(l+401,t+10,48,20).build());
        addRenderableWidget(Button.builder(Component.literal("Refresh"),b->songs=LocalMusicPlayer.tracks()).bounds(l+15,t+38,72,20).build());
        addRenderableWidget(Button.builder(Component.literal("Play/Pause"),b->LocalMusicPlayer.pauseOrResume()).bounds(l+95,t+38,95,20).build());
        addRenderableWidget(Button.builder(Component.literal("Stop"),b->LocalMusicPlayer.stop()).bounds(l+195,t+38,55,20).build());
        addRenderableWidget(Button.builder(Component.literal("Folder"),b->{
            openMusicFolder();
        }).bounds(l+257,t+38,67,20).build());
        addRenderableWidget(Button.builder(Component.literal("Spotify"),b->open("https://open.spotify.com/")).bounds(l+15,t+232,88,20).build());
        addRenderableWidget(Button.builder(Component.literal("YT Music"),b->open("https://music.youtube.com/")).bounds(l+109,t+232,88,20).build());
        addRenderableWidget(Button.builder(Component.literal("Previous"),b->skip(-1)).bounds(l+215,t+232,81,20).build());
        addRenderableWidget(Button.builder(Component.literal("Next"),b->skip(1)).bounds(l+303,t+232,81,20).build());
        addRenderableWidget(Button.builder(Component.literal("Vol -"),b->LocalMusicPlayer.setVolume(LocalMusicPlayer.volume()-.1f)).bounds(l+335,t+38,52,20).build());
        addRenderableWidget(Button.builder(Component.literal("Vol +"),b->LocalMusicPlayer.setVolume(LocalMusicPlayer.volume()+.1f)).bounds(l+395,t+38,52,20).build());
    }
    private void openMusicFolder(){
        Path folder=LocalMusicPlayer.folder().toAbsolutePath().normalize();
        try{
            Files.createDirectories(folder);
            String os=System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            ProcessBuilder launcher;
            if(os.contains("win")){
                launcher=new ProcessBuilder("explorer.exe", folder.toString());
            }else if(os.contains("mac")){
                launcher=new ProcessBuilder("open", folder.toString());
            }else{
                launcher=new ProcessBuilder("xdg-open", folder.toString());
            }
            try{
                launcher.start();
                message="Opened music folder: "+folder;
            }catch(Exception nativeFailure){
                if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)){
                    Desktop.getDesktop().open(folder.toFile());
                    message="Opened music folder: "+folder;
                }else{
                    throw nativeFailure;
                }
            }
            songs=LocalMusicPlayer.tracks();
        }catch(Exception e){
            try{
                minecraft.keyboardHandler.setClipboard(folder.toString());
                message="File manager unavailable. Folder path copied: "+folder;
            }catch(Exception clipboardFailure){
                message="Music folder: "+folder;
            }
        }
    }
    private void open(String url){
        try{java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));}
        catch(Exception ex){message="Open this URL in your browser: "+url;}
    }
    private void skip(int direction){
        try{LocalMusicPlayer.next(direction);}catch(Exception ex){message="Could not play track: "+ex.getMessage();}
    }
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        int l=width/2-230,t=height/2-135;
        g.fill(l,t,l+460,t+272,0xF3110D20);
        g.fill(l,t,l+460,t+3,0xFFAD77FF);
        g.drawString(font,"AUVA   /   MUSIC",l+15,t+14,0xFFCF9FFF,true);
        g.drawString(font,"LOCAL   -   WAV playback",l+16,t+69,0xFFCF9FFF);
        for(int i=0;i<6&&scroll+i<songs.size();i++){
            Path song=songs.get(scroll+i);int y=t+84+i*22;
            g.fill(l+14,y,l+445,y+19,i%2==0?0xFF262038:0xFF1D182E);
            String title=song.getFileName().toString();if(font.width(title)>385)title=font.plainSubstrByWidth(title,371)+"...";
            g.drawString(font,title,l+24,y+5,0xFFDCD4EA);
        }
        g.drawString(font,"Now: "+LocalMusicPlayer.nowPlaying()+"   |   "+(int)(LocalMusicPlayer.volume()*100)+"%",l+15,t+217,0xFFE2D6EE);
        String m=message;if(font.width(m)>429)m=font.plainSubstrByWidth(m,413)+"...";
        g.drawString(font,m,l+15,t+257,0xFFAAA0BF);
        super.render(g,mx,my,delta);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean dbl){
        int l=width/2-230,t=height/2-135;
        if(e.button()==0&&e.x()>=l+14&&e.x()<l+445){
            for(int i=0;i<6;i++){
                int y=t+84+i*22,index=scroll+i;
                if(index<songs.size()&&e.y()>=y&&e.y()<y+19){
                    try{LocalMusicPlayer.play(songs.get(index));message="Playing "+songs.get(index).getFileName();}
                    catch(Exception ex){message="Playback failed: "+ex.getMessage();}
                    return true;
                }
            }
        }
        return super.mouseClicked(e,dbl);
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        scroll=Math.max(0,Math.min(Math.max(0,songs.size()-6),scroll-(int)Math.signum(vertical)));return true;
    }
    @Override public boolean isPauseScreen(){return false;}
}
