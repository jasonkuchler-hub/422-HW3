package edu.utexas.ece422c.minispotify;

import java.io.InputStream;

public class MiniSpotifyApp {
    public static void main(String[] args) throws Exception {
        MusicService service=MusicServices.create();
        try(InputStream in=MiniSpotifyApp.class.getResourceAsStream("/songs.csv")){ for(Song s:CatalogLoader.load(in)) service.addSong(s); }
        service.createUser("alice"); service.createPlaylist("alice","Running"); service.addToPlaylist("alice","alice","Running","s1"); service.playSong("alice","s1");
        System.out.println(service.getPlaylist("alice","Running"));
        System.out.println("Recently played: " + service.getRecentlyPlayed("alice",5));
    }
}
