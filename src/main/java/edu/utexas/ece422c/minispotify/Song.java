package edu.utexas.ece422c.minispotify;

import java.util.Objects;

public final class Song {
    private final String id, title, artist, genre;
    private final int durationSeconds;
    public Song(String id, String title, String artist, String genre, int durationSeconds) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id");
        this.id=id; this.title=Objects.requireNonNull(title); this.artist=Objects.requireNonNull(artist); this.genre=Objects.requireNonNull(genre); this.durationSeconds=durationSeconds;
    }
    public String getId(){return id;} public String getTitle(){return title;} public String getArtist(){return artist;} public String getGenre(){return genre;} public int getDurationSeconds(){return durationSeconds;}
    @Override public boolean equals(Object o){ return o instanceof Song s && id.equals(s.id); }
    @Override public int hashCode(){ return id.hashCode(); }
    @Override public String toString(){ return title + " - " + artist + " [" + id + "]"; }
}
