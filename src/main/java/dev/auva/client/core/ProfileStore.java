package dev.auva.client.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.Map;

/** Local settings-only profile store. Contains no account credentials. */
public final class ProfileStore {
    private static final Path DIR=Path.of("config","auvaclient","profiles");
    private ProfileStore(){}
    private static Path target(String name){
        if(name==null||!name.matches("[A-Za-z0-9_-]{1,32}"))
            throw new IllegalArgumentException("Use 1-32 letters, numbers, underscores or hyphens");
        return DIR.resolve(name+".json");
    }
    public static void save(String name,Map<String,Module> modules){Config.saveTo(modules,target(name));}
    public static void apply(String name,Map<String,Module> modules){Config.loadFrom(modules,target(name));}
    public static List<String> list(){
        try{
            if(!Files.exists(DIR))return List.of();
            try(Stream<Path> paths=Files.list(DIR)){
                return paths.filter(p->Files.isRegularFile(p)&&p.getFileName().toString().matches("[A-Za-z0-9_-]{1,32}\\.json"))
                    .map(p->p.getFileName().toString().replaceFirst("\\.json$",""))
                    .sorted().limit(100).toList();
            }
        }catch(Exception e){return List.of();}
    }
    public static boolean delete(String name)throws Exception{return Files.deleteIfExists(target(name));}
}
