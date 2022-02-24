package me.nefelion.spotifynotifier;

import me.nefelion.spotifynotifier.gui.ProgressGUI;
import se.michaelthelin.spotify.enums.AlbumGroup;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.util.*;

public class ReleasesProcessor {

    private final TheEngine theEngine = TheEngine.getInstance();
    private final List<FollowedArtist> artists;
    private final HashSet<String> IDhashSet = new HashSet<>();
    private final HashMap<String, ReleasedAlbum> featuringHashMap = new HashMap<>();
    private final List<ReleasedAlbum> albums = new ArrayList<>();
    private boolean showProgressBar = false;

    public ReleasesProcessor(List<FollowedArtist> artists) {
        this.artists = artists;
    }

    public ReleasesProcessor(FollowedArtist... artists) {
        this.artists = new ArrayList<>();
        this.artists.addAll(Arrays.asList(artists));
    }

    public void process() {
        ProgressGUI progressBar = new ProgressGUI(0, artists.size());
        if (showProgressBar) progressBar.show();

        for (FollowedArtist artist : artists) {
            progressBar.setTitle(artist.getName());
            for (AlbumSimplified album : theEngine.getAlbums(artist.getID())) {
                if (IDhashSet.contains(album.getId())) continue;
                if (album.getAlbumGroup().equals(AlbumGroup.APPEARS_ON))
                    featuringHashMap.put(album.getId(), new ReleasedAlbum(album, artist));
                else {
                    IDhashSet.add(album.getId());
                    albums.add(new ReleasedAlbum(album, artist));
                }
            }
            progressBar.increment();
        }
        loadUniqueFeaturing();

        progressBar.close();
    }

    public HashSet<String> getIDhashSet() {
        return IDhashSet;
    }

    public void setProgressBarVisible(boolean showProgressBar) {
        this.showProgressBar = showProgressBar;
    }

    public void processOnlyNewReleases(boolean doOnlyNew) {
        if (doOnlyNew) {
            IDhashSet.clear();
            IDhashSet.addAll(FileManager.getAlbumHashSet());
        }
    }

    public List<ReleasedAlbum> getAlbums() {
        return albums;
    }

    private void loadUniqueFeaturing() {
        for (ReleasedAlbum album : featuringHashMap.values()) {
            if (IDhashSet.contains(album.getId())) continue;
            IDhashSet.add(album.getId());
            albums.add(album);
        }
    }


}
