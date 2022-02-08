package me.grothgar.spotifynotifier.records;

import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.awt.image.BufferedImage;
import java.util.List;

public record TempAlbumInfo(Album album, BufferedImage cover, List<TrackSimplified> trackList) {
}
