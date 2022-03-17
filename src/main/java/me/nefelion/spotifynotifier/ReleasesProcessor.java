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

    public ReleasesProcessor(List<FollowedArtist> artists) {
        this.artists = artists;
        savedIDhashSet = new HashSet<>(FileManager.getAlbumHashSet());
    }

    public ReleasesProcessor(FollowedArtist... artists) {
        this(Arrays.asList(artists));
    }

    public void process() {
        process(null, null);
    }

    public void process(DoubleConsumer progressConsumer) {
        process(progressConsumer, null);
    }

    public void process(Consumer<String> infoConsumer) {
        process(null, infoConsumer);
    }


    public void process(DoubleConsumer progressConsumer, Consumer<String> infoConsumer) {
        AtomicInteger i = new AtomicInteger();

        for (FollowedArtist artist : artists) {
            if (infoConsumer != null) Platform.runLater(() -> infoConsumer.accept(artist.getName()));

            for (AlbumSimplified album : theEngine.getAlbums(artist.getID())) {
                if (album.getAlbumGroup().equals(AlbumGroup.APPEARS_ON))
                    featuringHashMap.put(album.getId(), new ReleasedAlbum(album, artist));
                else if (!loadedIDhashSet.contains(album.getId())) {
                    if (!savedIDhashSet.contains(album.getId()) && TheEngine.getInstance().isFollowed(artist.getID())) {
                        savedIDhashSet.add(album.getId());
                        newAlbums.add(new ReleasedAlbum(album, artist));
                    }
                    allAlbums.add(new ReleasedAlbum(album, artist));
                    loadedIDhashSet.add(album.getId());
                }
            }

            if (progressConsumer != null)
                Platform.runLater(() -> progressConsumer.accept((double) (i.incrementAndGet()) / artists.size()));
        }

        loadUniqueFeaturing();

        FileManager.saveAlbumHashSet(savedIDhashSet);
    }

    private void loadUniqueFeaturing() {
        for (ReleasedAlbum album : featuringHashMap.values()) {
            if (loadedIDhashSet.contains(album.getId())) continue;

            if (!savedIDhashSet.contains(album.getId()) && TheEngine.getInstance().isFollowed(album.getArtistId())) {
                savedIDhashSet.add(album.getId());
                newAlbums.add(album);
            }
            allAlbums.add(album);
            loadedIDhashSet.add(album.getId());
        }
    }


    public List<ReleasedAlbum> getNewAlbums() {
        return newAlbums;
    }

    public List<ReleasedAlbum> getAllAlbums() {
        return allAlbums;
    }


}
