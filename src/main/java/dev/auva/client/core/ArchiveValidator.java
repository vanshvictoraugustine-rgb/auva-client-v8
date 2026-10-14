package dev.auva.client.core;

import java.nio.file.Path;
import java.util.Locale;
import java.util.zip.ZipFile;

/** Checks pack archives without extracting or executing them. No external dependencies. */
public final class ArchiveValidator {
    private static final long MAX_EXPANDED = 512L * 1024 * 1024;
    private ArchiveValidator(){}
    public static void validate(Path archive) throws Exception {
        try(ZipFile zip=new ZipFile(archive.toFile())){
            if(zip.size()>2048)throw new SecurityException("Too many files in resource pack");
            boolean packMeta=false;long expanded=0;
            var entries=zip.entries();
            while(entries.hasMoreElements()){
                var entry=entries.nextElement();
                String name=entry.getName();
                if(name.equals("pack.mcmeta")&&!entry.isDirectory())packMeta=true;
                if(name.startsWith("/")||name.startsWith("\\")||name.contains("\\")||name.indexOf('\0')!=-1)
                    throw new SecurityException("Unsafe archive path");
                Path normalized=Path.of(name).normalize();
                if(normalized.isAbsolute()||normalized.startsWith("..")||name.contains(":"))throw new SecurityException("Unsafe archive path");
                String lower=name.toLowerCase(Locale.ROOT);
                if(lower.endsWith(".jar")||lower.endsWith(".exe")||lower.endsWith(".dll")||lower.endsWith(".bat")||lower.endsWith(".cmd")||lower.endsWith(".ps1")||lower.endsWith(".class")||lower.endsWith(".sh"))
                    throw new SecurityException("Executable files are not accepted");
                long length=entry.getSize();
                if(length<0)throw new SecurityException("Unknown uncompressed size");
                expanded+=length;
                if(expanded>MAX_EXPANDED)throw new SecurityException("Pack expands beyond safety limit");
            }
            if(!packMeta)throw new SecurityException("Missing root pack.mcmeta");
        }
    }
}
