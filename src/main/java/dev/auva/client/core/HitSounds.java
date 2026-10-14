package dev.auva.client.core;

import dev.auva.client.AuvaSounds;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

/** Native OpenAL sound effects; custom WAV import remains an explicitly limited fallback. */
public final class HitSounds {
    public static final String[] LABELS={"Bonk","Cartoon Boing","Duck Quack","Squeak","Goofy Dog Bark","Tiny Woof","Retro Laser","8-bit","Metal Pipe","Cartoon Punch","Custom WAV","Random Funny"};
    private static final AtomicLong LAST=new AtomicLong();
    private HitSounds(){}
    public static void play(Module module){
        long now=System.nanoTime(),last=LAST.get();
        if(now-last<75_000_000L || !LAST.compareAndSet(last,now))return;
        int preset=Math.max(0,Math.min(LABELS.length-1,module.soundPreset));
        int index=preset==11?ThreadLocalRandom.current().nextInt(10):preset;
        float volume=Math.max(0,Math.min(1,module.strength));
        if(index<10){
            var sound=AuvaSounds.get(index);
            if(sound!=null)Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound,1.0f,volume));
        }else playCustomWav(volume);
    }
    private static void playCustomWav(float volume){
        Thread thread=new Thread(()->{
            Path path=Path.of("auvaclient","sfx","custom.wav");
            try{
                if(!Files.isRegularFile(path)||Files.size(path)>5_000_000)return;
                try(AudioInputStream input=AudioSystem.getAudioInputStream(path.toFile())){
                    Clip clip=AudioSystem.getClip();
                    clip.open(input);
                    if(clip.isControlSupported(FloatControl.Type.MASTER_GAIN)){
                        var gain=(FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                        float decibels=(float)(20*Math.log10(Math.max(.001,volume)));
                        gain.setValue(Math.max(gain.getMinimum(),Math.min(gain.getMaximum(),decibels)));
                    }
                    clip.addLineListener(event->{if(event.getType()==javax.sound.sampled.LineEvent.Type.STOP)clip.close();});
                    clip.start();
                }
            }catch(Exception ignored){ /* No audio output device / invalid custom WAV */ }
        },"Auva-Custom-WAV");
        thread.setDaemon(true);thread.start();
    }
}
