package edu.utexas.ece422c.minispotify;

/**
 * Grading entry point.
 *
 * Hidden tests obtain your implementation ONLY by calling {@link #create()} and then
 * use it through the {@link MusicService} interface. If you rename or replace
 * {@link LegacyMusicService}, update the single line in {@code create()} so it
 * returns your implementation. Do not change this method's signature.
 */
public final class MusicServices {
    private MusicServices() {}

    public static MusicService create() {
        return new LegacyMusicService();
    }
}
