package edu.utexas.ece422c.minispotify;

import java.util.List;
import java.util.Set;

public interface MusicService {
    void addSong(Song song);
    Song getSong(String songId);
    List<Song> searchByArtist(String artist);
    List<Song> searchByGenre(String genre);
    List<Song> searchByTitle(String query);
    void createUser(String username);
    void createPlaylist(String username, String playlistName);
    void addToPlaylist(String actingUser, String owner, String playlistName, String songId);
    void removeFromPlaylist(String actingUser, String owner, String playlistName, String songId);
    List<Song> getPlaylist(String owner, String playlistName);
    void addCollaborator(String actingUser, String owner, String playlistName, String collaborator);
    void followPlaylist(String username, String owner, String playlistName);
    List<Playlist> getFollowedPlaylists(String username);
    void addFavorite(String username, String songId);
    void removeFavorite(String username, String songId);
    Set<Song> getFavorites(String username);
    void playSong(String username, String songId);
    List<Song> getRecentlyPlayed(String username, int n);
    List<Song> getTopSongs(int n);
    List<Song> getRecommendations(String username, int n);
}
