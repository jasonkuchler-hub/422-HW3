package edu.utexas.ece422c.minispotify;

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
}
