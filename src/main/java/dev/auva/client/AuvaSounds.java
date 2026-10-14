package dev.auva.client;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** Register ten original client sound assets through Minecraft's own audio engine. */
public final class AuvaSounds implements ModInitializer {
    private static final Map<String,SoundEvent> SOUNDS=new LinkedHashMap<>();
    private static final String[] NAMES={"bonk","boing","duck","squeak","goofy_bark","tiny_woof","laser","retro","pipe","cartoon_punch"};
    @Override public void onInitialize(){
        for(String name:NAMES){
            Identifier id=Identifier.fromNamespaceAndPath("auvaclient",name);
            SOUNDS.put(name,Registry.register(BuiltInRegistries.SOUND_EVENT,id,SoundEvent.createVariableRangeEvent(id)));
        }
    }
    public static SoundEvent get(int index){
        if(index<0||index>=NAMES.length)return null;
        return SOUNDS.get(NAMES[index]);
    }
}
