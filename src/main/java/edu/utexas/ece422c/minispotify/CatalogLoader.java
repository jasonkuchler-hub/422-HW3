package edu.utexas.ece422c.minispotify;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class CatalogLoader {
    private CatalogLoader(){}
    public static List<Song> load(InputStream in) throws IOException {
        List<Song> songs=new ArrayList<>();
        try(BufferedReader br=new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))){
            String line=br.readLine();
            while((line=br.readLine())!=null){
                if(line.isBlank()) continue;
                String[] p=line.split(",", -1);
                songs.add(new Song(p[0],p[1],p[2],p[3],Integer.parseInt(p[4])));
            }
        }
        return songs;
    }
}
