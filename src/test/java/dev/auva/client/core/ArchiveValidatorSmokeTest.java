package dev.auva.client.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Standalone test (no JUnit required) for dangerous and normal archives. */
public class ArchiveValidatorSmokeTest {
    private static Path archive(String... names) throws Exception {
        Path output=Files.createTempFile("auva-archive-test",".zip");
        try(ZipOutputStream zip=new ZipOutputStream(Files.newOutputStream(output))){
            for(String name:names){
                zip.putNextEntry(new ZipEntry(name));
                zip.write(name.equals("pack.mcmeta")?"{\"pack\":{\"pack_format\":46,\"description\":\"test\"}}".getBytes():new byte[]{1,2,3});
                zip.closeEntry();
            }
        }
        return output;
    }
    private static void expect(boolean allowed,String... names) throws Exception {
        Path p=archive(names);
        try{
            try{ArchiveValidator.validate(p);if(!allowed)throw new AssertionError("Unsafe archive accepted");}
            catch(SecurityException ex){if(allowed)throw ex;}
        }finally{Files.deleteIfExists(p);}
    }
    public static void main(String[] args) throws Exception {
        expect(true,"pack.mcmeta","assets/minecraft/textures/item/a.png");
        expect(false,"assets/foo.png");
        expect(false,"pack.mcmeta","../evil.jar");
        expect(false,"pack.mcmeta","assets/script.exe");
        expect(false,"pack.mcmeta","C:/system.ini");
        expect(false,"pack.mcmeta","/outside.png");
        System.out.println("AUVA archive safety tests passed (6/6)");
    }
}
