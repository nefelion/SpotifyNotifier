package me.nefelion.spotifynotifier;

import me.nefelion.spotifynotifier.data.TempData;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class ExploreProcessor {

    private Consumer<String> currentArtistConsumer;
    private Consumer<Integer> artistCountConsumer;
    private Consumer<Double> progressConsumer;
    private final List<Artist> outputArtists = new ArrayList<>();

    public ExploreProcessor() {
    }

    public void process() {
        TheEngine instance = TheEngine.getInstance();

        List<FollowedArtist> followedArtists = TempData.getInstance().getFileData().getFollowedArtists();
        HashSet<String> processedArtists = new HashSet<>();

        int totalFollowedArtists = followedArtists.size();
        int currentFollowedArtist = 0;
        int currentArtist = 0;

        for (FollowedArtist followedArtist : followedArtists) {
            ++currentFollowedArtist;

            for (Artist artist : instance.getRelatedArtists(followedArtist.getID())) {
                String artistName = artist.getName();
                String id = artist.getId();

                if (processedArtists.contains(id)) continue;
                if (instance.isFollowed(id)) continue;

                processedArtists.add(id);
                outputArtists.add(artist);

                acceptArtistConsumer(artistName);
                acceptCountConsumer(++currentArtist);
                acceptProgress(currentFollowedArtist, totalFollowedArtists);
            }
        }
        outputArtists.sort(Comparator.comparing(Artist::getName));
    }

    private void acceptProgress(int currentArtist, int totalArtists) {
        if (progressConsumer == null) return;
        progressConsumer.accept((double) currentArtist / totalArtists);
    }

    private void acceptCountConsumer(int currentArtist) {
        artistCountConsumer.accept(currentArtist);
    }

    private void acceptArtistConsumer(String artistName) {
        if (currentArtistConsumer == null) return;
        currentArtistConsumer.accept(artistName);
    }

    public List<Artist> getOutputArtists() {
        return outputArtists;
    }

    public ExploreProcessor setCurrentArtistConsumer(Consumer<String> currentArtistConsumer) {
        this.currentArtistConsumer = currentArtistConsumer;
        return this;
    }

    public ExploreProcessor setArtistCountConsumer(Consumer<Integer> artistCountConsumer) {
        this.artistCountConsumer = artistCountConsumer;
        return this;
    }

    public ExploreProcessor setProgressConsumer(Consumer<Double> progressConsumer) {
        this.progressConsumer = progressConsumer;
        return this;
    }

}
