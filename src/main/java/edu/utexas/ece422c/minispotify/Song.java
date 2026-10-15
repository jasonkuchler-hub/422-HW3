package edu.utexas.ece422c.minispotify;

public final class Song {
    private final String id, title, artist, genre;
    private final int durationSeconds;
    public Song(String id, String title, String artist, String genre, int durationSeconds) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id");
        if (title == null) throw new IllegalArgumentException("title");
        if (artist == null) throw new IllegalArgumentException("artist");
        if (genre == null) throw new IllegalArgumentException("genre");
        if (durationSeconds <= 0) throw new IllegalArgumentException("durationSeconds");
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.durationSeconds = durationSeconds;
    }
    public String getId(){return id;} public String getTitle(){return title;} public String getArtist(){return artist;} public String getGenre(){return genre;} public int getDurationSeconds(){return durationSeconds;}
    @Override public boolean equals(Object o){ return o instanceof Song s && id.equals(s.id); }
    @Override public int hashCode(){ return id.hashCode(); }
    @Override public String toString(){ return title + " - " + artist + " [" + id + "]"; }
}
