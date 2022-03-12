package me.nefelion.spotifynotifier.records;

import javafx.scene.image.Image;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;


import java.util.List;

public record TempAlbumInfo(Album album, Image cover, List<TrackSimplified> trackList) {
}
