package edu.utexas.ece422c.minispotify;

import java.util.*;

/** Original implementation shipped with MiniSpotify 0.8.
 * It is intentionally small-scale and incomplete relative to Assignment #3.
 */
public class LegacyMusicService implements MusicService {
    private final Map<String, Song> catalog = new LinkedHashMap<>();
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Integer> playCounts = new HashMap<>();
    private final Map<String, List<Song>> artistIndex = new HashMap<>();
    private final Map<String, List<Song>> genreIndex = new HashMap<>();

    @Override
    public void addSong(Song song) {
        if (getSong(song.getId()) != null) throw new IllegalArgumentException("Duplicate song id");
        catalog.put(song.getId(), song);
        artistIndex.computeIfAbsent(song.getArtist().toLowerCase(), k -> new ArrayList<>()).add(song);
        genreIndex.computeIfAbsent(song.getGenre().toLowerCase(), k -> new ArrayList<>()).add(song);
    }

    @Override
    public Song getSong(String songId) {
        return catalog.get(songId);
    }

    private User user(String username) {
        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("Unknown user: " + username);
        }
        return users.get(username);
    }

    private Playlist playlist(String owner, String name) {
        Playlist p = user(owner).getPlaylists().get(name);
        if (p == null) {
            throw new IllegalArgumentException("Unknown playlist: " + name);
        }
        return p;
    }

    @Override
    public List<Song> searchByArtist(String artist) {
        List<Song> matches = artistIndex.get(artist.toLowerCase());
        if (matches == null) {
            return new ArrayList<>();
        }
        return List.copyOf(matches);
    }

    @Override
    public List<Song> searchByGenre(String genre) {
        List<Song> matches = genreIndex.get(genre.toLowerCase());
        if (matches == null) {
            return new ArrayList<>();
        }
        return List.copyOf(matches);
    }

    @Override
    public List<Song> searchByTitle(String q) {
        List<Song> out = new ArrayList<>();
        String x = q.toLowerCase();
        for (Song s : catalog.values()) if (s.getTitle().toLowerCase().contains(x)) out.add(s);
        return out;
    }

    @Override
    public void createUser(String username) {
        if (users.containsKey(username)) {
            throw new IllegalArgumentException("Duplicate user");
        }
        users.put(username, new User(username));
    }

    @Override
    public void createPlaylist(String username, String name) {
        User u = user(username);
        if (u.getPlaylists().containsKey(name)) {
            throw new IllegalArgumentException("Duplicate playlist");
        }
        u.getPlaylists().put(name, new Playlist(name, username));
    }

    @Override
    public void addToPlaylist(String actingUser, String owner, String name, String songId) {
        Playlist p = playlist(owner, name);
        if (!actingUser.equals(owner) && !p.isCollaborator(actingUser)) {
            throw new IllegalArgumentException("actingUser must be a collaborator or owner");
        }
        Song s = getSong(songId);
        if (s == null) throw new IllegalArgumentException("Unknown song");
        p.addSong(s);
    }

    @Override
    public void removeFromPlaylist(String actingUser, String owner, String name, String songId) {
        Playlist p = playlist(owner, name);
        if (!actingUser.equals(owner) && !p.isCollaborator(actingUser)) {
            throw new IllegalArgumentException("actingUser must be a collaborator or owner");
        }
        Song s = getSong(songId);
        if (s == null) throw new IllegalArgumentException("Unknown song");
        if (!p.removeFirst(s)) throw new IllegalArgumentException("No song was removed");
    }

    @Override
    public List<Song> getPlaylist(String owner, String name) {
        return playlist(owner, name).getSongs();
    }

    @Override
    public void addCollaborator(String actingUser, String owner, String name, String collaborator) {
        if (!actingUser.equals(owner)) throw new IllegalArgumentException("actingUser must be Owner");
        if (collaborator.equals(owner)) throw new IllegalArgumentException("Owner cannot be a collaborator");
        if (!users.containsKey(collaborator)) throw new IllegalArgumentException("Collaborator must be a real user");
        playlist(owner, name).addCollaborator(collaborator);
    }

    @Override
    public void followPlaylist(String username, String owner, String name) {
        Playlist p = playlist(owner, name);
        if (username.equals(owner)) {
            throw new IllegalArgumentException("Cannot follow your own playlist");
        }
        user(username).getFollowedPlaylists().add(p);
    }

    @Override
    public List<Playlist> getFollowedPlaylists(String username) {
        return new ArrayList<>(user(username).getFollowedPlaylists());
    }

    @Override
    public void addFavorite(String username, String songId) {
        Song s = getSong(songId);
        if (s == null) {
            throw new IllegalArgumentException("Unknown song");
        }
        user(username).getFavorites().add(s);
    }

    @Override
    public void removeFavorite(String username, String songId) {
        Song s = getSong(songId);
        if (s == null) {
            throw new IllegalArgumentException("Unknown song");
        }
        user(username).getFavorites().remove(s);
    }

    @Override
    public Set<Song> getFavorites(String username) {
        return Set.copyOf(user(username).getFavorites());
    }

    @Override
    public void playSong(String username, String songId) {
        Song s = getSong(songId);
        if (s == null) throw new IllegalArgumentException("Unknown song");
        user(username).getHistory().put(songId, s);
        user(username).getAllTimePlayed().add(s);
        playCounts.merge(songId, 1, Integer::sum);
    }

    @Override
    public List<Song> getRecentlyPlayed(String username, int n) {
        List<Song> ordered = new ArrayList<>(user(username).getHistory().values());
        Collections.reverse(ordered);
        return ordered.subList(0, Math.min(n, ordered.size()));
    }

    @Override
    public List<Song> getTopSongs(int n) {
        List<Song> copy = new ArrayList<>(catalog.values());
        copy.sort(Comparator.<Song>comparingInt(s -> playCounts.getOrDefault(s.getId(), 0)).reversed().thenComparing(s -> s.getTitle().toLowerCase()).thenComparing(Song::getId));
        return new ArrayList<>(copy.subList(0, Math.min(n, copy.size())));
    }

    @Override
    public List<Song> getRecommendations(String username, int n) {
        Set<Song> favorites = user(username).getFavorites();
        Set<String> genres = new HashSet<>();
        for (Song s : favorites) {
            genres.add(s.getGenre().toLowerCase());
        }
        List<Song> candidates = new ArrayList<>();
        for (String genre : genres) {
            if (genreIndex.containsKey(genre)) candidates.addAll(genreIndex.get(genre));
        }
        List<Song> filtered = new ArrayList<>();
        for (Song s : candidates) {
            if (!favorites.contains(s) && !user(username).getAllTimePlayed().contains(s)) filtered.add(s);
        }
        filtered.sort(Comparator.<Song>comparingInt(s -> playCounts.getOrDefault(s.getId(), 0))
                .reversed()
                .thenComparing(s -> s.getTitle().toLowerCase())
                .thenComparing(Song::getId));
        return filtered.subList(0, Math.min(n, filtered.size()));
    }
}