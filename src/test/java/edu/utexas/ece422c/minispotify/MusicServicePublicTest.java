package edu.utexas.ece422c.minispotify;

import java.util.List;
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
}
