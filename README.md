# MiniSpotify Starter Code (Assignment #3)

This project is the required starting point for Assignment #3. The code is intentionally a legacy implementation (MiniSpotify 0.8): it works for its original small feature set, but it is not well designed for the new requirements in the assignment specification.

Do not replace the project wholesale. Read it, run it, preserve existing public behavior, and refactor/extend it.

## Layout

```
src/main/java/edu/utexas/ece422c/minispotify/
  MusicService.java        the public API (frozen; do not change signatures)
  MusicServices.java       grading entry point: MusicServices.create() returns your implementation
  LegacyMusicService.java  the inherited implementation you will refactor and extend
  Song.java, User.java, Playlist.java
  CatalogLoader.java       loads songs from a CSV stream
  MiniSpotifyApp.java      small command-line demo
src/main/resources/songs.csv                          8-song sample catalog used by the demo
src/test/java/.../MusicServicePublicTest.java          public example tests (not a complete specification)
src/test/resources/minispotify_catalog_100000.csv     100,000-song catalog for your own large-catalog tests
```

## Building and running

The project is a Maven project targeting Java 17 and JUnit 5. The grader builds your submission with `mvn test` from a clean checkout, so that command must succeed.

```
mvn test                      # compile and run all tests under src/test
mvn -q compile exec:java      # run the MiniSpotifyApp demo
```

IDE: import the folder as a Maven project (IntelliJ: Open the folder; Eclipse: File > Import > Maven > Existing Maven Projects). Use JDK 17.

Note: the legacy implementation takes close to a minute to load the 100,000-song catalog. That is one of the problems you are fixing.

## What is frozen

Hidden tests obtain your implementation only through `MusicServices.create()` and use it only through:

- the `MusicService` interface (do not change any method signature),
- the constructor `Song(String id, String title, String artist, String genre, int durationSeconds)` and the `Song` getters,
- `Playlist.getName()` and `Playlist.getOwner()`.

Everything else, including class names, fields, and the other methods of `Playlist` and `User`, may be refactored freely. If you rename or replace `LegacyMusicService`, update the one line in `MusicServices.create()`.

## Submission

Submit one ZIP of the complete project (all source files, your JUnit tests, and `Design.pdf` at the project root). Do not include `target/`, IDE folders, or other generated files; the supplied `.gitignore` lists what to leave out.
