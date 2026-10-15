package edu.utexas.ece422c.minispotify;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private final String name;
    private final String owner;
    private final List<Song> songs = new ArrayList<>();
    public Playlist(String name, String owner){ this.name=name; this.owner=owner; }
    public String getName(){return name;} public String getOwner(){return owner;}
    void addSong(Song song){ songs.add(song); }
    boolean removeFirst(Song song){ return songs.remove(song); }
    public List<Song> getSongs(){ return List.copyOf(songs); }
}
