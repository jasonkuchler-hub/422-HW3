package edu.utexas.ece422c.minispotify;

import java.util.*;

public class User {
    private final String username;
    private final Set<Song> favorites = new HashSet<>();
    private final List<Song> history = new ArrayList<>();
    private final Map<String, Playlist> playlists = new HashMap<>();
    public User(String username){ this.username=username; }
    public String getUsername(){return username;}
    public Set<Song> getFavorites(){return favorites;}
    public List<Song> getHistory(){return history;}
    public Map<String, Playlist> getPlaylists(){return playlists;}
}
