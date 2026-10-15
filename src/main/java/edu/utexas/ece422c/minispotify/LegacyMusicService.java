package edu.utexas.ece422c.minispotify;

import java.util.*;

/** Original implementation shipped with MiniSpotify 0.8.
 * It is intentionally small-scale and incomplete relative to Assignment #3.
 */
public class LegacyMusicService implements MusicService {
    private final Map<String, Song> catalog = new LinkedHashMap<>();
    private final List<User> users = new ArrayList<>();
    private final Map<String,Integer> playCounts = new HashMap<>();
    private final Map<String, List<Song>> artistIndex = new HashMap<>();
    private final Map<String, List<Song>> genreIndex = new HashMap<>();

    @Override public void addSong(Song song){
        if(getSong(song.getId()) != null) throw new IllegalArgumentException("Duplicate song id");
        catalog.put(song.getId(),song);
        artistIndex.computeIfAbsent(song.getArtist().toLowerCase(), k -> new ArrayList<>()).add(song);
        genreIndex.computeIfAbsent(song.getGenre().toLowerCase(), k -> new ArrayList<>()).add(song);
    }
    @Override public Song getSong(String songId){
        return catalog.get(songId);
    }
    private User user(String username){
        for(User u: users) if(u.getUsername().equals(username)) return u;
        throw new IllegalArgumentException("Unknown user: " + username);
    }
    private Playlist playlist(String owner, String name){
        for(Playlist p: user(owner).getPlaylists()) if(p.getName().equals(name)) return p;
        throw new IllegalArgumentException("Unknown playlist: " + name);
    }
    @Override public List<Song> searchByArtist(String artist){
        List<Song> matches = artistIndex.get(artist.toLowerCase());
        if(matches == null){ return new ArrayList<Song>();}
        else
            return List.copyOf(matches);
    }
    @Override public List<Song> searchByGenre(String genre){
        List<Song> matches = genreIndex.get(genre.toLowerCase());
        if(matches == null){ return new ArrayList<Song>();}
        else
            return List.copyOf(matches);
    }
    @Override public List<Song> searchByTitle(String q){
        List<Song> out=new ArrayList<>(); String x=q.toLowerCase(); for(Song s:catalog.values()) if(s.getTitle().toLowerCase().contains(x)) out.add(s); return out;
    }
    @Override public void createUser(String username){
        for(User u:users) if(u.getUsername().equals(username)) throw new IllegalArgumentException("Duplicate user"); users.add(new User(username));
    }
    @Override public void createPlaylist(String username,String name){
        User u=user(username); for(Playlist p:u.getPlaylists()) if(p.getName().equals(name)) throw new IllegalArgumentException("Duplicate playlist"); u.getPlaylists().add(new Playlist(name,username));
    }
    @Override public void addToPlaylist(String actingUser,String owner,String name,String songId){
        if(!actingUser.equals(owner)) throw new UnsupportedOperationException("Collaborators not implemented");
        Song s=getSong(songId); if(s==null) throw new IllegalArgumentException("Unknown song"); playlist(owner,name).addSong(s);
    }
    @Override public void removeFromPlaylist(String actingUser,String owner,String name,String songId){
        if(!actingUser.equals(owner)) throw new UnsupportedOperationException("Collaborators not implemented");
        Song s=getSong(songId); if(s==null || !playlist(owner,name).removeFirst(s)) throw new IllegalArgumentException("Song not in playlist");
    }
    @Override public List<Song> getPlaylist(String owner,String name){ return playlist(owner,name).getSongs(); }
    @Override public void addCollaborator(String actingUser,String owner,String name,String collaborator){ throw new UnsupportedOperationException("Not implemented"); }
    @Override public void followPlaylist(String username,String owner,String name){ throw new UnsupportedOperationException("Not implemented"); }
    @Override public List<Playlist> getFollowedPlaylists(String username){ throw new UnsupportedOperationException("Not implemented"); }
    @Override public void addFavorite(String username,String songId){ Song s=getSong(songId); if(s==null) throw new IllegalArgumentException("Unknown song"); user(username).getFavorites().add(s); }
    @Override public void removeFavorite(String username,String songId){ Song s=getSong(songId); if(s!=null) user(username).getFavorites().remove(s); }
    @Override public Set<Song> getFavorites(String username){ return new HashSet<>(user(username).getFavorites()); }
    @Override public void playSong(String username,String songId){
        Song s=getSong(songId); if(s==null) throw new IllegalArgumentException("Unknown song"); user(username).getHistory().add(s); playCounts.merge(songId,1,Integer::sum);
    }
    @Override public List<Song> getRecentlyPlayed(String username,int n){
        List<Song> h=user(username).getHistory(), out=new ArrayList<>();
        for(int i=h.size()-1;i>=0 && out.size()<n;i--) out.add(h.get(i));
        return out;
    }
    @Override public List<Song> getTopSongs(int n){
        List<Song> copy=new ArrayList<>(catalog.values());
        copy.sort(Comparator.<Song>comparingInt(s -> playCounts.getOrDefault(s.getId(),0)).reversed().thenComparing(Song::getTitle).thenComparing(Song::getId));
        return new ArrayList<>(copy.subList(0,Math.min(n,copy.size())));
    }
    @Override public List<Song> getRecommendations(String username,int n){ throw new UnsupportedOperationException("Not implemented"); }
}
