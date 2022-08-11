package me.nefelion.spotifynotifier;

import com.neovisionaries.i18n.CountryCode;
import javafx.application.Platform;
import me.nefelion.spotifynotifier.data.FileManager;
import me.nefelion.spotifynotifier.data.TempData;
import se.michaelthelin.spotify.enums.AlbumGroup;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.io.File;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class ReleasesProcessor {

    private final TheEngine theEngine = TheEngine.getInstance();
    private final List<FollowedArtist> artists;
    private final HashSet<String> savedIDhashSet, loadedIDhashSet, remindIDhashSet;
    private final HashMap<String, ReleasedAlbum> featuringHashMap;
    private final List<ReleasedAlbum> newAlbums, allAlbums;
    private final CountryCode countryCode;

    private DoubleConsumer progressConsumer;
    private Consumer<String> currentArtistConsumer, processedArtistsConsumer, releasesConsumer, newReleasesConsumer;

    public ReleasesProcessor(List<FollowedArtist> artists) {
        this.artists = artists;
        savedIDhashSet = new HashSet<>(FileManager.getHashSet(FileManager.ALBUM_DATA));
        remindIDhashSet = FileManager.getHashSet(FileManager.REMIND_DATA);

        newAlbums = new ArrayList<>();
        allAlbums = new ArrayList<>();
        featuringHashMap = new HashMap<>();
        loadedIDhashSet = new HashSet<>();
        countryCode = FileManager.getFileData().getCountryCode();
    }

    public ReleasesProcessor(FollowedArtist... artists) {
        this(Arrays.asList(artists));
    }

    public void process() {
        if (processedArtistsConsumer != null)
            Platform.runLater(() -> processedArtistsConsumer.accept(0 + "/" + artists.size()));

        AtomicInteger i = new AtomicInteger();

        for (FollowedArtist artist : artists) {
            if (currentArtistConsumer != null)
                Platform.runLater(() -> currentArtistConsumer.accept(artist.getName()));

            List<AlbumSimplified> albums;

            try {
                albums = theEngine.getAlbums(artist.getID());
            } catch (CancellationException e) {
                return;
            }

            for (AlbumSimplified album : albums) {
                if (album.getAlbumGroup().equals(AlbumGroup.APPEARS_ON))
                    featuringHashMap.put(album.getId(), new ReleasedAlbum(album, artist));
                else if (!loadedIDhashSet.contains(album.getId())) {
                    boolean isNewAndFollowed = !savedIDhashSet.contains(album.getId()) && TheEngine.getInstance().isFollowed(artist.getID());
                    boolean isRemind = remindIDhashSet.contains(album.getId()) && Arrays.asList(album.getAvailableMarkets()).contains(countryCode);
                    if (isRemind) remindIDhashSet.remove(album.getId());


                    if (isNewAndFollowed || isRemind) {
                        savedIDhashSet.add(album.getId());
                        addToNewAlbums(new ReleasedAlbum(album, artist));
                    }
                    addToAllAlbums(new ReleasedAlbum(album, artist));
                    loadedIDhashSet.add(album.getId());
                }
            }

            if (progressConsumer != null)
                Platform.runLater(() -> progressConsumer.accept((double) (i.incrementAndGet()) / artists.size()));
            if (processedArtistsConsumer != null)
                Platform.runLater(() -> processedArtistsConsumer.accept(i.intValue() + "/" + artists.size()));
        }
        loadUniqueFeaturing();
        FileManager.saveHashSet(FileManager.ALBUM_DATA, savedIDhashSet);
        FileManager.saveHashSet(FileManager.REMIND_DATA, remindIDhashSet);
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


    public ReleasesProcessor setProcessedArtistsConsumer(Consumer<String> processedArtistsConsumer) {
        this.processedArtistsConsumer = processedArtistsConsumer;
        return this;
    }

    public ReleasesProcessor setReleasesConsumer(Consumer<String> releasesConsumer) {
        this.releasesConsumer = releasesConsumer;
        return this;
    }

    public ReleasesProcessor setNewReleasesConsumer(Consumer<String> newReleasesConsumer) {
        this.newReleasesConsumer = newReleasesConsumer;
        return this;
    }

    public ReleasesProcessor setProgressConsumer(DoubleConsumer progressConsumer) {
        this.progressConsumer = progressConsumer;
        return this;
    }

    public ReleasesProcessor setCurrentArtistConsumer(Consumer<String> currentArtistConsumer) {
        this.currentArtistConsumer = currentArtistConsumer;
        return this;
    }
}
