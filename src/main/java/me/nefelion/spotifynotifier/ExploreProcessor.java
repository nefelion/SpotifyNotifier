package me.nefelion.spotifynotifier;

import me.nefelion.spotifynotifier.data.TempData;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class ExploreProcessor {

    private final List<Artist> outputArtists = new ArrayList<>();
    private final List<FollowedArtist> followedArtists;
    private final HashSet<String> processedArtists = new HashSet<>();
    private Consumer<String> currentArtistConsumer;
    private Consumer<Integer> artistCountConsumer;
    private Consumer<Double> progressConsumer;
    private int currentArtist;
    private int iterations;


    public ExploreProcessor() {
        iterations = 1;
        followedArtists = TempData.getInstance().getFileData().getFollowedArtists();
    }

    public void process() {
        int currentFollowedArtist = 0;
        currentArtist = 0;

        for (FollowedArtist followedArtist : followedArtists) {
            extracted(followedArtist.getID(), iterations);

            acceptProgress(++currentFollowedArtist, followedArtists.size());
        }

        outputArtists.sort(Comparator.comparing(Artist::getName));
    }


    private void extracted(String artistID, int iterations) {
        if (iterations == 0) return;

        List<String> tempIDs = new ArrayList<>();
        for (Artist artist : TheEngine.getInstance().getRelatedArtists(artistID)) {
            String id = artist.getId();

            if (processedArtists.contains(id)) continue;
            if (TheEngine.isFollowed(id)) continue;

            processedArtists.add(id);
            outputArtists.add(artist);
            incrementArtistsCountConsumer();
            tempIDs.add(id);
        }

        for (String id : tempIDs) extracted(id, iterations - 1);
    }

    private void acceptProgress(int currentArtist, int totalArtists) {
        if (progressConsumer == null) return;
        progressConsumer.accept((double) currentArtist / totalArtists);
    }

    private void acceptCountConsumer(int currentArtist) {
        artistCountConsumer.accept(currentArtist);
    }

    private void incrementArtistsCountConsumer() {
        artistCountConsumer.accept(++currentArtist);
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

    public ExploreProcessor setIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

}
