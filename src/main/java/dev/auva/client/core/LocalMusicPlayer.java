package dev.auva.client.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/** Actual local WAV playback via standard Java Sound; online DRM services are not scraped. */
public final class LocalMusicPlayer {
    private static final Path DIRECTORY=Path.of("auvaclient","music");
    private static Clip clip;
    private static Path current;
    private static float volume=0.6f;
    private LocalMusicPlayer(){}
    public static synchronized List<Path> tracks() {
        try{
            Files.createDirectories(DIRECTORY);
            for(String name:new String[]{"auva_combat_loop.wav","auva_nightfall_loop.wav"}){
                Path target=DIRECTORY.resolve(name);
                if(Files.notExists(target)){
                    try(var source=LocalMusicPlayer.class.getResourceAsStream("/assets/auvaclient/music/"+name)){
                        if(source!=null)Files.copy(source,target);
                    }
                }
            }
            try(var stream=Files.list(DIRECTORY)){
                return stream.filter(Files::isRegularFile).filter(p->p.getFileName().toString().toLowerCase().endsWith(".wav"))
                    .sorted(Comparator.comparing(p->p.getFileName().toString().toLowerCase())).toList();
            }
        }catch(Exception e){return List.of();}
    }
    public static synchronized void play(Path file) throws Exception {
        Path safe=file.toAbsolutePath().normalize();
        if(!safe.startsWith(DIRECTORY.toAbsolutePath().normalize())||!Files.isRegularFile(safe)||!safe.getFileName().toString().toLowerCase().endsWith(".wav"))
            throw new SecurityException("Only WAV files from Auva music folder can be played");
        stop();
        try(AudioInputStream input=AudioSystem.getAudioInputStream(safe.toFile())){
            Clip loaded=AudioSystem.getClip();
            loaded.open(input);clip=loaded;current=safe;setVolume(volume);loaded.start();
        }
    }
    public static synchronized void next(int direction) throws Exception {
        List<Path> available=tracks();
        if(available.isEmpty())return;
        int pos=available.indexOf(current);
        if(pos<0)pos=direction>=0?-1:0;
        play(available.get(Math.floorMod(pos+(direction>=0?1:-1),available.size())));
    }
    public static synchronized void stop(){if(clip!=null){clip.stop();clip.close();clip=null;}current=null;}
    public static synchronized void pauseOrResume(){if(clip==null)return;if(clip.isRunning())clip.stop();else clip.start();}
    public static synchronized void setVolume(float v){
        volume=Math.max(0.0f,Math.min(1.0f,v));
        if(clip!=null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)){
            FloatControl c=(FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            float db=(float)(20*Math.log10(Math.max(volume,0.0001f)));
            c.setValue(Math.max(c.getMinimum(),Math.min(c.getMaximum(),db)));
        }
    }
    public static synchronized float volume(){return volume;}
    public static synchronized String nowPlaying(){return current==null?"Nothing playing":current.getFileName().toString();}
    public static synchronized boolean isPlaying(){return clip!=null&&clip.isRunning();}
    public static Path folder(){return DIRECTORY;}
}
