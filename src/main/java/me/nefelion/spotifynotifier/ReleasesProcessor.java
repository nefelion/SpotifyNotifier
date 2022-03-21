package me.nefelion.spotifynotifier;

import javafx.application.Platform;
import me.nefelion.spotifynotifier.data.FileManager;
import se.michaelthelin.spotify.enums.AlbumGroup;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class ReleasesProcessor {

    private final TheEngine theEngine = TheEngine.getInstance();
    private final List<FollowedArtist> artists;
    private final HashSet<String> savedIDhashSet;
    private final HashSet<String> loadedIDhashSet = new HashSet<>();
    private final HashMap<String, ReleasedAlbum> featuringHashMap = new HashMap<>();
    private final List<ReleasedAlbum> newAlbums = new ArrayList<>();
    private final List<ReleasedAlbum> allAlbums = new ArrayList<>();

    private DoubleConsumer progressConsumer;
    private Consumer<String> currentArtistConsumer, processedArtistsConsumer, releasesConsumer, newReleasesConsumer;

    public ReleasesProcessor(List<FollowedArtist> artists) {
        this.artists = artists;
        savedIDhashSet = new HashSet<>(FileManager.getAlbumHashSet());
    }

    public ReleasesProcessor(FollowedArtist... artists) {
        this(Arrays.asList(artists));
    }

    public void process() {
        AtomicInteger i = new AtomicInteger();

        for (FollowedArtist artist : artists) {
            if (currentArtistConsumer != null) Platform.runLater(() -> currentArtistConsumer.accept(artist.getName()));

            for (AlbumSimplified album : theEngine.getAlbums(artist.getID())) {
                if (album.getAlbumGroup().equals(AlbumGroup.APPEARS_ON))
                    featuringHashMap.put(album.getId(), new ReleasedAlbum(album, artist));
                else if (!loadedIDhashSet.contains(album.getId())) {
                    ReleasedAlbum releasedAlbum = new ReleasedAlbum(album, artist);
                    if (!savedIDhashSet.contains(album.getId()) && TheEngine.getInstance().isFollowed(artist.getID())) {
                        savedIDhashSet.add(album.getId());
                        addToNewAlbums(releasedAlbum);
                    }
                    addToAllAlbums(releasedAlbum);
                    loadedIDhashSet.add(album.getId());
                }
            }

            if (progressConsumer != null)
                Platform.runLater(() -> progressConsumer.accept((double) (i.incrementAndGet()) / artists.size()));
            if (processedArtistsConsumer != null)
                Platform.runLater(() -> processedArtistsConsumer.accept(i.intValue() + "/" + artists.size()));
        }

        loadUniqueFeaturing();

        FileManager.saveAlbumHashSet(savedIDhashSet);
    }


    private void loadUniqueFeaturing() {
        for (ReleasedAlbum album : featuringHashMap.values()) {
            if (loadedIDhashSet.contains(album.getId())) continue;

            if (!savedIDhashSet.contains(album.getId()) && TheEngine.getInstance().isFollowed(album.getArtistId())) {
                savedIDhashSet.add(album.getId());
                addToNewAlbums(album);
            }
            addToAllAlbums(album);
            loadedIDhashSet.add(album.getId());
        }
    }

    private void addToAllAlbums(ReleasedAlbum releasedAlbum) {
        allAlbums.add(releasedAlbum);
        if (releasesConsumer != null) Platform.runLater(() -> releasesConsumer.accept(allAlbums.size() + ""));
    }

    private void addToNewAlbums(ReleasedAlbum releasedAlbum) {
        newAlbums.add(releasedAlbum);
        if (newReleasesConsumer != null) Platform.runLater(() -> newReleasesConsumer.accept(newAlbums.size() + ""));
    }


    public List<ReleasedAlbum> getNewAlbums() {
        return newAlbums;
    }

    public List<ReleasedAlbum> getAllAlbums() {
        return allAlbums;
    }


    public void setProcessedArtistsConsumer(Consumer<String> processedArtistsConsumer) {
        this.processedArtistsConsumer = processedArtistsConsumer;
    }

    public void setReleasesConsumer(Consumer<String> releasesConsumer) {
        this.releasesConsumer = releasesConsumer;
    }

    public void setNewReleasesConsumer(Consumer<String> newReleasesConsumer) {
        this.newReleasesConsumer = newReleasesConsumer;
    }

    public void setProgressConsumer(DoubleConsumer progressConsumer) {
        this.progressConsumer = progressConsumer;
    }

    public void setCurrentArtistConsumer(Consumer<String> currentArtistConsumer) {
        this.currentArtistConsumer = currentArtistConsumer;
    }
}
