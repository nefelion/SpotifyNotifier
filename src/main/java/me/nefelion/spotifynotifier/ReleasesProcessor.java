package me.nefelion.spotifynotifier;

import com.neovisionaries.i18n.CountryCode;
import javafx.application.Platform;
import me.nefelion.spotifynotifier.data.FileManager;
import se.michaelthelin.spotify.enums.AlbumGroup;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class ReleasesProcessor {

    private final TheEngine theEngine = TheEngine.getInstance();
    private final List<FollowedArtist> artists;
    private final HashSet<String> fileHashSet, loadedIDhashSet, remindIDhashSet;
    private final HashMap<String, ReleasedAlbum> featuringHashMap;
    private final List<ReleasedAlbum> newAlbums, allAlbums;
    private final CountryCode countryCode;
    private final boolean ignoreVarious, showOnlyAvailable;

    private DoubleConsumer progressConsumer;
    private Consumer<String> currentArtistConsumer, processedArtistsConsumer, releasesConsumer, newReleasesConsumer;

    public ReleasesProcessor(List<FollowedArtist> artists) {
        this.artists = artists;
        fileHashSet = new HashSet<>(FileManager.getHashSet(FileManager.ALBUM_DATA));
        remindIDhashSet = FileManager.getHashSet(FileManager.REMIND_DATA);

        newAlbums = new ArrayList<>();
        allAlbums = new ArrayList<>();
        featuringHashMap = new HashMap<>();
        loadedIDhashSet = new HashSet<>();
        countryCode = FileManager.getFileData().getCountryCode();
        ignoreVarious = FileManager.getFileData().isIgnoreVariousArtists();
        showOnlyAvailable = FileManager.getFileData().isShowOnlyAvailable();
    }

    public ReleasesProcessor(FollowedArtist... artists) {
        this(Arrays.asList(artists));
    }

    public List<ReleasedAlbum> getNewAlbums() {
        return newAlbums;
    }

    public List<ReleasedAlbum> getAllAlbums() {
        return allAlbums;
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

            for (AlbumSimplified a : albums) {
                if (isAlreadyLoaded(a)) continue;
                if (showOnlyAvailable && !isAvailable(a)) continue;
                if (isFeaturing(a)) {
                    if (ignoreVarious && isVariousArtists(a)) continue;
                    addToFeaturing(artist, a);
                    continue;
                }

                addToNewOrAllOnly(getProcessedReleasedAlbum(a, artist));
                markAsLoaded(a);
            }

            if (progressConsumer != null)
                Platform.runLater(() -> progressConsumer.accept((double) (i.incrementAndGet()) / artists.size()));
            if (processedArtistsConsumer != null)
                Platform.runLater(() -> processedArtistsConsumer.accept(i.intValue() + "/" + artists.size()));
        }
        loadUniqueFeaturing();
        FileManager.saveHashSet(FileManager.ALBUM_DATA, fileHashSet);
        FileManager.saveHashSet(FileManager.REMIND_DATA, remindIDhashSet);
    }

    private void loadUniqueFeaturing() {
        for (ReleasedAlbum album : featuringHashMap.values()) {
            if (loadedIDhashSet.contains(album.getId())) continue;

            addToNewOrAllOnly(album);
            loadedIDhashSet.add(album.getId());
        }
    }

    private void addToNewOrAllOnly(ReleasedAlbum album) {
        if (isNewAndFollowed(album) || album.isReminded()) addToNewAlbums(album);
        addToAllAlbums(album);
    }

    private void addToAllAlbums(ReleasedAlbum releasedAlbum) {
        allAlbums.add(releasedAlbum);
        if (releasesConsumer != null) Platform.runLater(() -> releasesConsumer.accept(allAlbums.size() + ""));
    }

    private void addToNewAlbums(ReleasedAlbum releasedAlbum) {
        fileHashSet.add(releasedAlbum.getId());
        newAlbums.add(releasedAlbum);
        if (newReleasesConsumer != null) Platform.runLater(() -> newReleasesConsumer.accept(newAlbums.size() + ""));
    }

    private void addToFeaturing(FollowedArtist artist, AlbumSimplified album) {
        featuringHashMap.put(album.getId(), getProcessedReleasedAlbum(album, artist));
    }


    private ReleasedAlbum getProcessedReleasedAlbum(AlbumSimplified a, FollowedArtist artist) {
        ReleasedAlbum album = new ReleasedAlbum(a, artist);
        boolean isRemind = isOnRemindList(a) && isAvailable(a);
        if (isRemind) remindIDhashSet.remove(a.getId());
        album.setReminded(isRemind);
        album.setAvailableEverywhere(isAvailableEverywere(a));
        return album;
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

    private static boolean isAvailableEverywere(AlbumSimplified a) {
        List<CountryCode> countryCodes = Arrays.asList(a.getAvailableMarkets());
        return countryCodes.contains(CountryCode.getByCode(776)) && countryCodes.contains(CountryCode.getByCode(882));
    }

    private boolean isAvailable(AlbumSimplified album) {
        return album.getAvailableMarkets().length > 0 && Arrays.asList(album.getAvailableMarkets()).contains(countryCode);
    }

    private static boolean isVariousArtists(AlbumSimplified album) {
        return album.getArtists()[0].getName().equals("Various Artists");
    }

    private static boolean isFeaturing(AlbumSimplified album) {
        return album.getAlbumGroup().equals(AlbumGroup.APPEARS_ON);
    }

    private boolean isAlreadyLoaded(AlbumSimplified album) {
        return loadedIDhashSet.contains(album.getId());
    }

    private void markAsLoaded(AlbumSimplified album) {
        loadedIDhashSet.add(album.getId());
    }


    private boolean isNewAndFollowed(ReleasedAlbum album) {
        return !fileHashSet.contains(album.getId()) && TheEngine.getInstance().isFollowed(album.getFollowedArtist().getID());
    }

    private boolean isOnRemindList(AlbumSimplified album) {
        return remindIDhashSet.contains(album.getId());
    }
}
