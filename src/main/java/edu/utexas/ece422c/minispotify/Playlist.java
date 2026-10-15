package edu.utexas.ece422c.minispotify;

import java.util.*;

public class Playlist {
    private final String name;
    private final String owner;
    private final List<Song> songs = new ArrayList<>();
    public Playlist(String name, String owner){ this.name=name; this.owner=owner; }
    public String getName(){return name;} public String getOwner(){return owner;}
    void addSong(Song song){ songs.add(song); }
    boolean removeFirst(Song song){ return songs.remove(song); }
    public List<Song> getSongs(){ return List.copyOf(songs); }

    private final Set<String> collaborators = new HashSet<>();
    void addCollaborator(String username){
        collaborators.add(username);
    }
    boolean isCollaborator(String username){
        return collaborators.contains(username);
    }
}
