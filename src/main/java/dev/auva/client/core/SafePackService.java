package dev.auva.client.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/** Resource-pack acquisition is deliberately confined to Modrinth's public API and CDN. */
public final class SafePackService {
    public record Pack(String id, String title, String author, String description, String icon) {}
    private static final long MAX_ARCHIVE_BYTES = 128L * 1024 * 1024;
    private static final long MAX_EXPANDED_BYTES = 512L * 1024 * 1024;
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER).connectTimeout(Duration.ofSeconds(10)).build();
    private SafePackService() {}

    public static List<Pack> search(String text) throws Exception {
        String safeQuery = URLEncoder.encode(text == null ? "" : text.strip(), StandardCharsets.UTF_8);
        String facets = URLEncoder.encode("[[\"project_type:resourcepack\"]]", StandardCharsets.UTF_8);
        JsonObject root = json(api("https://api.modrinth.com/v2/search?limit=20&query="+safeQuery+"&facets="+facets));
        List<Pack> packs = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray("hits")) {
            JsonObject hit=element.getAsJsonObject();
            packs.add(new Pack(str(hit,"project_id"),str(hit,"title"),str(hit,"author"),str(hit,"description"),str(hit,"icon_url")));
        }
        return List.copyOf(packs);
    }

    public static Path download(Pack pack, Path resourcePacks, String gameVersion) throws Exception {
        if (!pack.id().matches("[A-Za-z0-9]{3,40}")) throw new SecurityException("Invalid project identifier");
        String versions = URLEncoder.encode("[\""+gameVersion+"\"]", StandardCharsets.UTF_8);
        JsonArray found=JsonParser.parseString(api("https://api.modrinth.com/v2/project/"+pack.id()+"/version?game_versions="+versions)).getAsJsonArray();
        if(found.isEmpty())throw new IllegalStateException("No compatible resource pack for Minecraft "+gameVersion);
        JsonObject version=found.get(0).getAsJsonObject();
        JsonArray files=version.getAsJsonArray("files");
        JsonObject chosen=null;
        for(JsonElement el:files){
            JsonObject file=el.getAsJsonObject();
            if(file.has("filename")&&str(file,"filename").toLowerCase(Locale.ROOT).endsWith(".zip")){
                if(chosen==null || (file.has("primary")&&file.get("primary").getAsBoolean()))chosen=file;
            }
        }
        if(chosen==null)throw new SecurityException("Provider did not supply a .zip archive");
        URI source=URI.create(str(chosen,"url"));
        if(!"https".equals(source.getScheme()) || !"cdn.modrinth.com".equalsIgnoreCase(source.getHost()) || source.getPort()!=-1)
            throw new SecurityException("Download host not permitted");
        JsonObject hashes=chosen.getAsJsonObject("hashes");
        if(hashes==null || !hashes.has("sha512"))throw new SecurityException("Missing SHA-512 checksum");
        String expected=hashes.get("sha512").getAsString();
        if(!expected.matches("[a-fA-F0-9]{128}"))throw new SecurityException("Bad SHA-512 checksum");
        Files.createDirectories(resourcePacks);
        Path temporary=Files.createTempFile(resourcePacks,".auva-download-",".tmp");
        boolean success=false;
        try {
            HttpRequest request=HttpRequest.newBuilder(source).timeout(Duration.ofSeconds(90))
                .header("User-Agent","AuvaClient/0.4 (Minecraft resource pack browser)").GET().build();
            HttpResponse<InputStream> response=HTTP.send(request,HttpResponse.BodyHandlers.ofInputStream());
            if(response.statusCode()!=200)throw new IllegalStateException("CDN HTTP "+response.statusCode());
            MessageDigest digest=MessageDigest.getInstance("SHA-512");
            try(InputStream in=response.body(); var out=Files.newOutputStream(temporary)){
                byte[] buffer=new byte[32768];int read;long total=0;
                while((read=in.read(buffer))!=-1){
                    total+=read;
                    if(total>MAX_ARCHIVE_BYTES)throw new SecurityException("Archive exceeds size limit");
                    digest.update(buffer,0,read);out.write(buffer,0,read);
                }
            }
            if(!HexFormat.of().formatHex(digest.digest()).equalsIgnoreCase(expected))throw new SecurityException("SHA-512 mismatch");
            ArchiveValidator.validate(temporary);
            String basename=safeName(pack.title())+"-"+safeName(version.has("version_number")?str(version,"version_number"):pack.id());
            Path destination=resourcePacks.resolve(basename+".zip");
            if(Files.exists(destination)){
                int n=2;
                while(Files.exists(resourcePacks.resolve(basename+"-"+n+".zip")))n++;
                destination=resourcePacks.resolve(basename+"-"+n+".zip");
            }
            Files.move(temporary,destination,StandardCopyOption.ATOMIC_MOVE);
            success=true;
            return destination;
        } finally {if(!success)Files.deleteIfExists(temporary);}
    }
    private static String api(String url) throws Exception {
        URI uri=URI.create(url);
        if(!"https".equals(uri.getScheme())||!"api.modrinth.com".equals(uri.getHost()))throw new SecurityException("Disallowed API host");
        HttpRequest req=HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15))
            .header("User-Agent","AuvaClient/0.4 (Minecraft resource pack browser)").GET().build();
        HttpResponse<String> response=HTTP.send(req,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!=200)throw new IllegalStateException("Modrinth HTTP "+response.statusCode());
        if(response.body().length()>2_000_000)throw new SecurityException("Oversized API response");
        return response.body();
    }
    private static JsonObject json(String s){return JsonParser.parseString(s).getAsJsonObject();}
    private static String str(JsonObject obj,String key){return obj.has(key)&&!obj.get(key).isJsonNull()?obj.get(key).getAsString():"";}
    private static String safeName(String name){
        String normalized=name.replaceAll("[^A-Za-z0-9._-]+","-").replaceAll("^\\.+","");
        return normalized.length()>60?normalized.substring(0,60):normalized.isBlank()?"resource-pack":normalized;
    }
}
