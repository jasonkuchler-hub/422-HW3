package edu.utexas.ece422c.minispotify;

import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Public example tests. These are examples, not a specification: the hidden grading
 * tests are far more extensive. Hidden tests obtain the implementation exactly as
 * done here, via MusicServices.create(), and use it only through MusicService.
 */
class MusicServicePublicTest {
    private MusicService service(){
        MusicService s=MusicServices.create(); s.addSong(new Song("1","A","Artist","Rock",100)); s.addSong(new Song("2","B","Artist","Jazz",120)); s.createUser("u"); return s;
    }
    @Test void playlistPreservesOrderAndDuplicates(){
        var s=service(); s.createPlaylist("u","p"); s.addToPlaylist("u","u","p","1"); s.addToPlaylist("u","u","p","2"); s.addToPlaylist("u","u","p","1");
        assertEquals(java.util.List.of("1","2","1"), s.getPlaylist("u","p").stream().map(Song::getId).toList());
    }
    @Test void duplicateSongIdsRejected(){ var s=service(); assertThrows(IllegalArgumentException.class,()->s.addSong(new Song("1","Other","X","Y",1))); }
    @Test void topSongsUsesPlayCounts(){ var s=service(); s.playSong("u","2"); s.playSong("u","2"); s.playSong("u","1"); assertEquals("2",s.getTopSongs(1).get(0).getId()); }
    @Test void loadLargeCatalogIsFast() throws Exception {
        MusicService s = MusicServices.create();
        List<Song> songs;
        long start = System.currentTimeMillis();
        try (var in = getClass().getResourceAsStream("/minispotify_catalog_100000.csv")) {
            songs = CatalogLoader.load(in);
            for (Song song : songs) {
                s.addSong(song);
            }
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Loaded 100,000 songs in " + elapsed + " ms");

        start = System.currentTimeMillis();
        for (int i = 0; i < 100_000; i++) {
            String id = songs.get(i % songs.size()).getId();
            s.getSong(id);
        }
        elapsed = System.currentTimeMillis() - start;
        System.out.println("100,000 getSong calls in " + elapsed + " ms");

        start = System.currentTimeMillis();
        for (int i = 0; i < 10_000; i++) {
            String artist = songs.get(i % songs.size()).getArtist();
            s.searchByArtist(artist);
        }
        elapsed = System.currentTimeMillis() - start;
        System.out.println("10,000 searchByArtist calls in " + elapsed + " ms");
    }
    @Test void historyBoundedAndMostRecentUnique(){
        MusicService s = MusicServices.create();
        s.createUser("u");
        for (int i = 0; i <= 50; i++) {
            s.addSong(new Song("h" + i, "Song " + i, "Artist", "Genre", 100));
        }

        // 49 plays: no eviction yet, size should be exactly 49
        for (int i = 0; i <= 48; i++) {
            s.playSong("u", "h" + i);
        }
        assertEquals(49, s.getRecentlyPlayed("u", 50).size());

        // 50th play: still no eviction, size caps exactly at 50, oldest (h0) still present
        s.playSong("u", "h49");
        List<Song> after50 = s.getRecentlyPlayed("u", 50);
        assertEquals(50, after50.size());
        assertEquals("h49", after50.get(0).getId());
        assertTrue(after50.stream().anyMatch(song -> song.getId().equals("h0")));

        // 51st play (a new, 51st distinct song): oldest entry (h0) must now be evicted
        s.playSong("u", "h50");
        List<Song> after51 = s.getRecentlyPlayed("u", 50);
        assertEquals(50, after51.size());
        assertEquals("h50", after51.get(0).getId());
        assertFalse(after51.stream().anyMatch(song -> song.getId().equals("h0")));

        // Replaying an already-present song (h1) should move it to most-recent
        // position, WITHOUT creating a duplicate or growing the size
        s.playSong("u", "h1");
        List<Song> afterReplay = s.getRecentlyPlayed("u", 50);
        assertEquals(50, afterReplay.size());
        assertEquals("h1", afterReplay.get(0).getId());
        assertEquals("h50", afterReplay.get(1).getId());
        long countOfH1 = afterReplay.stream().filter(song -> song.getId().equals("h1")).count();
        assertEquals(1, countOfH1);

        // getRecentlyPlayed must also respect a smaller n
        assertEquals(3, s.getRecentlyPlayed("u", 3).size());
    }
    // --- Duplicate usernames ---
    @Test void duplicateUsernameRejected(){
        var s = service();
        assertThrows(IllegalArgumentException.class, () -> s.createUser("u"));
    }

    // --- Collaborator permissions ---
    @Test void collaboratorCanModifyButStrangerCannot(){
        var s = service();
        s.createUser("owner"); s.createUser("collab"); s.createUser("stranger");
        s.createPlaylist("owner", "P");
        s.addCollaborator("owner", "owner", "P", "collab");
        s.addToPlaylist("collab", "owner", "P", "1");
        assertEquals(1, s.getPlaylist("owner", "P").size());
        assertThrows(IllegalArgumentException.class, () -> s.addToPlaylist("stranger", "owner", "P", "2"));
    }

    @Test void onlyOwnerCanAddCollaborator(){
        var s = service();
        s.createUser("owner"); s.createUser("collab"); s.createUser("other");
        s.createPlaylist("owner", "P");
        assertThrows(IllegalArgumentException.class, () -> s.addCollaborator("other", "owner", "P", "collab"));
    }

    @Test void ownerCannotBeAddedAsOwnCollaborator(){
        var s = service();
        s.createUser("owner");
        s.createPlaylist("owner", "P");
        assertThrows(IllegalArgumentException.class, () -> s.addCollaborator("owner", "owner", "P", "owner"));
    }

    @Test void addingSameCollaboratorTwiceIsHarmless(){
        var s = service();
        s.createUser("owner"); s.createUser("collab");
        s.createPlaylist("owner", "P");
        s.addCollaborator("owner", "owner", "P", "collab");
        s.addCollaborator("owner", "owner", "P", "collab"); // should not throw
        s.addToPlaylist("collab", "owner", "P", "1");
        assertEquals(1, s.getPlaylist("owner", "P").size());
    }

    // --- Duplicate favorites / follows ---
    @Test void duplicateFavoriteHasNoExtraEffect(){
        var s = service();
        s.addFavorite("u", "1");
        s.addFavorite("u", "1");
        assertEquals(1, s.getFavorites("u").size());
    }

    @Test void removingNonFavoriteSongIsNoOp(){
        var s = service();
        assertDoesNotThrow(() -> s.removeFavorite("u", "1"));
        assertEquals(0, s.getFavorites("u").size());
    }

    @Test void duplicateFollowDoesNotDuplicate(){
        var s = service();
        s.createUser("owner"); s.createUser("follower");
        s.createPlaylist("owner", "P");
        s.followPlaylist("follower", "owner", "P");
        s.followPlaylist("follower", "owner", "P");
        assertEquals(1, s.getFollowedPlaylists("follower").size());
    }

    @Test void userCannotFollowOwnPlaylist(){
        var s = service();
        s.createUser("owner");
        s.createPlaylist("owner", "P");
        assertThrows(IllegalArgumentException.class, () -> s.followPlaylist("owner", "owner", "P"));
    }

    // --- Tie-breaking (the case-sensitivity bug you found) ---
    @Test void topSongsTieBreakIsCaseInsensitiveByTitle(){
        var s = MusicServices.create();
        s.addSong(new Song("b1", "Banana", "Artist", "Genre", 100));
        s.addSong(new Song("a1", "apple", "Artist", "Genre", 100));
        // Both have 0 plays, so title is the tiebreaker.
        // A naive case-sensitive compare would put "Banana" first (capital B < lowercase a).
        List<Song> top = s.getTopSongs(2);
        assertEquals("a1", top.get(0).getId());
        assertEquals("b1", top.get(1).getId());
    }

    // --- Recommendation exclusions, including songs evicted from bounded history ---
    @Test void recommendationsExcludeSongsDroppedFromHistory(){
        var s = MusicServices.create();
        s.createUser("u");
        s.addSong(new Song("fav1", "Fav Song", "Artist", "Rock", 100));
        s.addSong(new Song("old1", "Old Rock Song", "Artist", "Rock", 100));
        for (int i = 0; i < 60; i++) {
            s.addSong(new Song("filler" + i, "Filler " + i, "Artist", "Pop", 100));
        }
        s.addFavorite("u", "fav1");
        s.playSong("u", "old1");
        for (int i = 0; i < 60; i++) {
            s.playSong("u", "filler" + i); // evicts old1 from the 50-entry bounded history
        }
        assertFalse(s.getRecentlyPlayed("u", 50).stream().anyMatch(song -> song.getId().equals("old1")));
        List<Song> recs = s.getRecommendations("u", 10);
        assertFalse(recs.stream().anyMatch(song -> song.getId().equals("old1")));
    }

    // --- Mutation attempts on returned collections ---
    @Test void returnedPlaylistIsImmutable(){
        var s = service();
        s.createPlaylist("u", "P");
        s.addToPlaylist("u", "u", "P", "1");
        List<Song> playlist = s.getPlaylist("u", "P");
        assertThrows(UnsupportedOperationException.class, () -> playlist.add(s.getSong("2")));
    }

    @Test void returnedFavoritesAreImmutable(){
        var s = service();
        s.addFavorite("u", "1");
        Set<Song> favs = s.getFavorites("u");
        assertThrows(UnsupportedOperationException.class, () -> favs.add(s.getSong("2")));
    }

    @Test void mutatingReturnedFollowedPlaylistsDoesNotAffectInternalState(){
        var s = service();
        s.createUser("owner");
        s.createPlaylist("owner", "P");
        s.followPlaylist("u", "owner", "P");
        List<Playlist> followed = s.getFollowedPlaylists("u");
        followed.clear(); // this is a copy, so it won't throw, but shouldn't affect real state
        assertEquals(1, s.getFollowedPlaylists("u").size());
    }

    // --- Large-catalog correctness (not just timing) ---
    @Test void largeCatalogReturnsCorrectData() throws Exception {
        MusicService s = MusicServices.create();
        try (var in = getClass().getResourceAsStream("/minispotify_catalog_100000.csv")) {
            for (Song song : CatalogLoader.load(in)) {
                s.addSong(song);
            }
        }
        Song known = s.getSong("S000001");
        assertNotNull(known);
        assertEquals("Quiet Harbor 000001", known.getTitle());

        List<Song> byArtist = s.searchByArtist("Quiet Voyagers 1489");
        assertEquals(73, byArtist.size());
        assertTrue(byArtist.stream().allMatch(song -> song.getArtist().equals("Quiet Voyagers 1489")));
    }
}
