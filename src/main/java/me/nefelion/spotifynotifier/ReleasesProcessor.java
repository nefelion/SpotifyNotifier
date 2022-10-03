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
    private final boolean ignoreVarious, showOnlyAvailable, ignoreNotWorldwide;

    private DoubleConsumer progressC;
    private Consumer<String> currentArtistC;
    private Consumer<ReleasedAlbum> newReleaseC, todayReleaseC, tomorrowReleaseC;
    private Consumer<Integer> numberOfLoadedReleasesC, numberOfProcessedArtistsC, numberOfNewReleasesC,
            numberOfTodayReleasesC, numberOfTomorrowReleases, pageNumberC, numberOfPagesC;
    private int today = 0, tomorrow = 0;

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
        ignoreNotWorldwide = FileManager.getFileData().isIgnoreNotWorldwide();
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
        if (numberOfProcessedArtistsC != null)
            Platform.runLater(() -> numberOfProcessedArtistsC.accept(0));

        AtomicInteger i = new AtomicInteger();

        for (FollowedArtist artist : artists) {
            if (currentArtistC != null)
                Platform.runLater(() -> currentArtistC.accept(artist.getName()));

            List<AlbumSimplified> albums;

            try {
                albums = theEngine.getAlbums(artist.getID(), pageNumberC, numberOfPagesC);
            } catch (CancellationException e) {
                return;
            }

            for (AlbumSimplified a : albums) {
                if (isAlreadyLoaded(a)) continue;
                if (showOnlyAvailable && !isAvailable(a)) continue;
                if (ignoreNotWorldwide && !isWorldwide(a)) continue;
                if (isFeaturing(a)) {
                    if (ignoreVarious && isVariousArtists(a)) continue;
                    addToFeaturing(artist, a);
                    continue;
                }

                addToNewOrAllOnly(getProcessedReleasedAlbum(a, artist));
                markAsLoaded(a);
            }

            if (progressC != null)
                Platform.runLater(() -> progressC.accept((double) (i.incrementAndGet()) / artists.size()));
            if (numberOfProcessedArtistsC != null)
                Platform.runLater(() -> numberOfProcessedArtistsC.accept(i.intValue()));
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
        addToAllAlbums(album);
        if (isNewAndFollowed(album) || album.isReminded()) addToNewAlbums(album);
    }

    private void addToAllAlbums(ReleasedAlbum releasedAlbum) {
        allAlbums.add(releasedAlbum);
        if (numberOfLoadedReleasesC != null) Platform.runLater(() -> numberOfLoadedReleasesC.accept(allAlbums.size()));
        if (numberOfTodayReleasesC != null && releasedAlbum.isToday()) {
            Platform.runLater(() -> {
                numberOfTodayReleasesC.accept(++today);
                todayReleaseC.accept(releasedAlbum);
            });
        }
        if (numberOfTomorrowReleases != null && releasedAlbum.isTomorrow()) {
            Platform.runLater(() -> {
                numberOfTomorrowReleases.accept(++tomorrow);
                tomorrowReleaseC.accept(releasedAlbum);
            });
        }
    }

    private void addToNewAlbums(ReleasedAlbum releasedAlbum) {
        fileHashSet.add(releasedAlbum.getId());
        newAlbums.add(releasedAlbum);
        if (numberOfNewReleasesC != null) {
            Platform.runLater(() -> {
                numberOfNewReleasesC.accept(newAlbums.size());
                newReleaseC.accept(releasedAlbum);
            });
        }
    }

    private void addToFeaturing(FollowedArtist artist, AlbumSimplified album) {
        featuringHashMap.put(album.getId(), getProcessedReleasedAlbum(album, artist));
    }


    private ReleasedAlbum getProcessedReleasedAlbum(AlbumSimplified a, FollowedArtist artist) {
        ReleasedAlbum album = new ReleasedAlbum(a, artist);
        boolean isRemind = isOnRemindList(a) && isAvailable(a);
        if (isRemind) remindIDhashSet.remove(a.getId());
        album.setReminded(isRemind);
        album.setAvailableEverywhere(isWorldwide(a));
        return album;
    }


    public ReleasesProcessor processedArtistsNumberConsumer(Consumer<Integer> numberOfProcessedArtistsC) {
        this.numberOfProcessedArtistsC = numberOfProcessedArtistsC;
        return this;
    }

    public ReleasesProcessor loadedReleasesNumberConsumer(Consumer<Integer> numberOfLoadedReleasesC) {
        this.numberOfLoadedReleasesC = numberOfLoadedReleasesC;
        return this;
    }

    public ReleasesProcessor newReleasesNumberConsumer(Consumer<Integer> numberOfNewReleasesC) {
        this.numberOfNewReleasesC = numberOfNewReleasesC;
        return this;
    }

    public ReleasesProcessor progressConsumer(DoubleConsumer progressC) {
        this.progressC = progressC;
        return this;
    }

    public ReleasesProcessor currentArtistConsumer(Consumer<String> currentArtistC) {
        this.currentArtistC = currentArtistC;
        return this;
    }

    public ReleasesProcessor todayReleasesNumberConsumer(Consumer<Integer> numberOfTodayReleasesC) {
        this.numberOfTodayReleasesC = numberOfTodayReleasesC;
        return this;
    }

    public ReleasesProcessor tomorrowReleasesNumberConsumer(Consumer<Integer> numberOfTomorrowReleases) {
        this.numberOfTomorrowReleases = numberOfTomorrowReleases;
        return this;
    }

    public ReleasesProcessor todayReleaseConsumer(Consumer<ReleasedAlbum> todayReleaseConsumer) {
        this.todayReleaseC = todayReleaseConsumer;
        return this;
    }

    public ReleasesProcessor tomorrowReleaseConsumer(Consumer<ReleasedAlbum> tomorrowReleaseConsumer) {
        this.tomorrowReleaseC = tomorrowReleaseConsumer;
        return this;
    }

    public ReleasesProcessor newReleaseConsumer(Consumer<ReleasedAlbum> newReleaseConsumer) {
        this.newReleaseC = newReleaseConsumer;
        return this;
    }

    public ReleasesProcessor pageNumberConsumer(Consumer<Integer> pageNumberConsumer) {
        this.pageNumberC = pageNumberConsumer;
        return this;
    }

    public ReleasesProcessor numberOfPagesConsumer(Consumer<Integer> numberOfPagesConsumer) {
        this.numberOfPagesC = numberOfPagesConsumer;
        return this;
    }

    private boolean isWorldwide(AlbumSimplified a) {
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
        return !fileHashSet.contains(album.getId()) && TheEngine.isFollowed(album.getFollowedArtist().getID());
    }

    private boolean isOnRemindList(AlbumSimplified album) {
        return remindIDhashSet.contains(album.getId());
    }

}
