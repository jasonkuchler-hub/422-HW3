package edu.utexas.ece422c.minispotify;

import java.util.*;

public class User {
    private static final int HISTORY_LIMIT = 50;

    private static class RecentHistory extends LinkedHashMap<String, Song> {
        RecentHistory() {
            super(16, 0.75f, true);
        }
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Song> eldest) {
            return size() > HISTORY_LIMIT;
        }
    }
    private final Set<Song> allTimePlayed = new HashSet<>();
    private final String username;
    private final Set<Song> favorites = new HashSet<>();
    private final Map<String, Song> history = new RecentHistory();
    private final Map<String, Playlist> playlists = new HashMap<>();

    public Set<Song> getAllTimePlayed(){ return allTimePlayed; }
    public User(String username){ this.username=username; }
    public String getUsername(){return username;}
    public Set<Song> getFavorites(){return favorites;}
    public Map<String, Song> getHistory(){return history;}
    public Map<String, Playlist> getPlaylists(){return playlists;}

    private final Set<Playlist> followedPlaylists = new HashSet<>();
    public Set<Playlist> getFollowedPlaylists(){ return followedPlaylists; }
}