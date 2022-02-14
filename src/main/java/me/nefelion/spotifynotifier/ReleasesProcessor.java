package me.nefelion.spotifynotifier;

import me.nefelion.spotifynotifier.gui.ProgressGUI;
import se.michaelthelin.spotify.enums.AlbumGroup;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.util.*;

public class ReleasesProcessor {

    private final TheEngine theEngine;
    private final List<FollowedArtist> followedArtists;
    private final HashSet<String> IDhashSet = new HashSet<>();
    private final HashMap<String, ReleasedAlbum> featuringHashMap = new HashMap<>();
    private final List<ReleasedAlbum> releasedAlbums = new ArrayList<>();
    private boolean showProgressBar = false;

    public ReleasesProcessor(TheEngine theEngine, List<FollowedArtist> followedArtists) {
        this.theEngine = theEngine;
        this.followedArtists = followedArtists;
    }

    public ReleasesProcessor(TheEngine theEngine, FollowedArtist... followedArtists) {
        this.theEngine = theEngine;
        this.followedArtists = new ArrayList<>();
        this.followedArtists.addAll(Arrays.asList(followedArtists));
    }

    public void process() {
        ProgressGUI progressBar = new ProgressGUI(0, followedArtists.size());
        if (showProgressBar) progressBar.show();
        int i = 0;

        for (FollowedArtist artist : followedArtists) {
            progressBar.setTitle(artist.getName());
            for (AlbumSimplified album : theEngine.getAlbums(artist.getID())) {
                if (IDhashSet.contains(album.getId())) continue;
                if (album.getAlbumGroup().equals(AlbumGroup.APPEARS_ON))
                    featuringHashMap.put(album.getId(), new ReleasedAlbum(album, artist));
                else {
                    IDhashSet.add(album.getId());
                    releasedAlbums.add(new ReleasedAlbum(album, artist));
                }
            }
            progressBar.setValue(++i);
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

    public List<ReleasedAlbum> getReleasedAlbums() {
        return releasedAlbums;
    }

    private void loadUniqueFeaturing() {
        for (ReleasedAlbum album : featuringHashMap.values()) {
            if (IDhashSet.contains(album.getId())) continue;
            IDhashSet.add(album.getId());
            releasedAlbums.add(album);
        }
    }


}
