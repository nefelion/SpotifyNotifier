package me.nefelion.spotifynotifier.data;

import com.neovisionaries.i18n.CountryCode;
import me.nefelion.spotifynotifier.FollowedArtist;

import java.util.LinkedList;
import java.util.List;

public class FileData {
    private final List<FollowedArtist> followedArtists;
    private String lastChecked;
    private int countryCodeNumeric;
    private boolean ignoreVariousArtists;
    private boolean showOnlyAvailable;

    public FileData() {
        this.followedArtists = new LinkedList<>();
        this.lastChecked = "-";
        this.countryCodeNumeric = 840;
        this.ignoreVariousArtists = false;
        this.showOnlyAvailable = false;
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

    public int getCountryCodeNumeric() {
        return countryCodeNumeric;
    }

    public CountryCode getCountryCode() {
        return CountryCode.getByCode(countryCodeNumeric);
    }

    public void setCountryCodeNumeric(int countryCodeNumeric) {
        this.countryCodeNumeric = countryCodeNumeric;
    }

    public boolean isIgnoreVariousArtists() {
        return ignoreVariousArtists;
    }

    public void setIgnoreVariousArtists(boolean ignoreVariousArtists) {
        this.ignoreVariousArtists = ignoreVariousArtists;
    }

    public boolean isShowOnlyAvailable() {
        return showOnlyAvailable;
    }

    public void setShowOnlyAvailable(boolean showOnlyAvailable) {
        this.showOnlyAvailable = showOnlyAvailable;
    }
}
