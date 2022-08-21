package me.nefelion.spotifynotifier;

import se.michaelthelin.spotify.enums.AlbumGroup;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

public class ReleasedAlbum {
    private final AlbumSimplified album;
    private final String id;
    private final String albumName;
    private final String followedArtistName;
    private final String artistId;
    private final String albumType;
    private final String releaseDate;
    private final boolean featuring;
    private boolean reminded = false;


    public ReleasedAlbum(AlbumSimplified album, FollowedArtist artist) {
        this.album = album;
        this.id = album.getId();
        this.albumName = album.getName();
        this.followedArtistName = artist.getName();
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

    public String getArtistString() {
        return (album.getAlbumGroup().equals(AlbumGroup.APPEARS_ON) ?
                album.getArtists()[0].getName() + (album.getArtists().length == 1 ? " " : " ... ") + "(feat. " + followedArtistName + ")"
                : followedArtistName + (album.getArtists().length == 1 ? "" : " ..."));
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

    public String getFollowedArtistName() {
        return followedArtistName;
    }

    @Override
    public String toString() {
        return new String((albumType + "  |  " + releaseDate + "  |       " + getArtistString() + "  —  " + albumName).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    public LocalDate getLocalDate() {
        String dateToBeParsed = releaseDate;
        if (releaseDate.length() == 4) dateToBeParsed += "-01-01";
        else if (releaseDate.length() == 7) dateToBeParsed += "-01";

        return LocalDate.parse(dateToBeParsed);
    }

    public int getDaysAgo() {
        return (int) DAYS.between(getLocalDate(), LocalDate.now());
    }

    public AlbumSimplified getAlbum() {
        return album;
    }

    public boolean isReminded() {
        return reminded;
    }

    public void setReminded(boolean reminded) {
        this.reminded = reminded;
    }
}
