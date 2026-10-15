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
}
