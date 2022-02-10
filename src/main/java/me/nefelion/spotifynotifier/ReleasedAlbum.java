package me.nefelion.spotifynotifier;

import se.michaelthelin.spotify.enums.AlbumGroup;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class ReleasedAlbum {
    private final String id;
    private final String albumName;
    private final String artistName;
    private final String artistId;
    private final String albumType;
    private final String releaseDate;
    private final boolean featuring;

    public ReleasedAlbum(String id, String albumName, String artistName, String artistId, String albumType, String releaseDate, boolean featuring) {
        this.id = id;
        this.albumName = albumName;
        this.artistName = artistName;
        this.artistId = artistId;
        this.albumType = albumType;
        this.releaseDate = releaseDate;
        this.featuring = featuring;
    }

    public ReleasedAlbum(AlbumSimplified album, FollowedArtist artist) {
        String finalArtistName = (album.getAlbumGroup().equals(AlbumGroup.APPEARS_ON) ?
                album.getArtists()[0].getName() + (album.getArtists().length == 1 ? " " : " ... ") + "(feat. " + artist.getName() + ")"
                : artist.getName() + (album.getArtists().length == 1 ? "" : " ..."));

        this.id = album.getId();
        this.albumName = album.getName();
        this.artistName = finalArtistName;
        this.artistId = artist.getID();
        this.albumType = album.getAlbumType().toString();
        this.releaseDate = album.getReleaseDate();
        this.featuring = album.getAlbumGroup().equals(AlbumGroup.APPEARS_ON);
    }

    public String getArtistId() {
        return artistId;
    }

    public String getId() {
        return id;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumType() {
        return albumType;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getLink() {
        return "https://open.spotify.com/album/" + getId();
    }

    public boolean isFeaturing() {
        return featuring;
    }

    @Override
    public String toString() {
        return new String((albumType + "  |  " + releaseDate + "  |       " + artistName + "  —  " + albumName).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    public LocalDate getLocalDate() {
        String dateToBeParsed = releaseDate;
        if (releaseDate.length() == 4) dateToBeParsed += "-01-01";
        else if (releaseDate.length() == 7) dateToBeParsed += "-01";

        return LocalDate.parse(dateToBeParsed);
    }

}
