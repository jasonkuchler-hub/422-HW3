package edu.utexas.ece422c.minispotify;

import java.util.ArrayList;
import java.util.List;

public class User {
    private final String username;
    private final List<Song> favorites = new ArrayList<>();
    private final List<Song> history = new ArrayList<>();
    private final List<Playlist> playlists = new ArrayList<>();
    public User(String username){ this.username=username; }
    public String getUsername(){return username;}
    public List<Song> getFavorites(){return favorites;}
    public List<Song> getHistory(){return history;}
    public List<Playlist> getPlaylists(){return playlists;}
}
