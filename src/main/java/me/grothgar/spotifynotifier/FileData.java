package me.grothgar.spotifynotifier;

import java.util.LinkedList;
import java.util.List;

public class FileData {
    private final List<FollowedArtist> followedArtists;
    private String lastChecked;

    public FileData() {
        this.followedArtists = new LinkedList<>();
        this.lastChecked = "-";
    }

    public List<FollowedArtist> getFollowedArtists() {
        return followedArtists;
    }

    public String getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(String lastChecked) {
        this.lastChecked = lastChecked;
    }

}
