package me.nefelion.spotifynotifier.records;

import me.nefelion.spotifynotifier.Player;
import me.nefelion.spotifynotifier.ReleasedAlbum;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

public record CurrentPlaying(ReleasedAlbum releasedAlbum, TrackSimplified trackSimplified, Player player) {
}
