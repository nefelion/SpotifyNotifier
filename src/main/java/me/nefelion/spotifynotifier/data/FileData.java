package me.nefelion.spotifynotifier.data;

import com.neovisionaries.i18n.CountryCode;
import me.nefelion.spotifynotifier.FollowedArtist;

import java.util.LinkedList;
import java.util.List;

public class FileData {
    private final List<FollowedArtist> followedArtists;
    private String lastChecked, clientId, clientSecret;
    private int countryCodeNumeric;
    private boolean showOnlyAvailable, ignoreVariousArtists, ignoreNotWorldwide, ignoreCompilations, useBrowserInsteadOfApp;

    public FileData() {
        this.followedArtists = new LinkedList<>();
        this.lastChecked = "-";
        this.clientId = "-";
        this.clientSecret = "-";
        this.countryCodeNumeric = 840;
        this.showOnlyAvailable = false;
        this.ignoreVariousArtists = false;
        this.ignoreCompilations = false;
        this.ignoreNotWorldwide = false;
        this.useBrowserInsteadOfApp = true;
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

    public boolean isIgnoreNotWorldwide() {
        return ignoreNotWorldwide;
    }

    public void setIgnoreNotWorldwide(boolean ignoreNotWorldwide) {
        this.ignoreNotWorldwide = ignoreNotWorldwide;
    }

    public boolean isIgnoreCompilations() {
        return ignoreCompilations;
    }

    public void setIgnoreCompilations(boolean ignoreCompilations) {
        this.ignoreCompilations = ignoreCompilations;
    }

    public boolean isShowOnlyAvailable() {
        return showOnlyAvailable;
    }

    public void setShowOnlyAvailable(boolean showOnlyAvailable) {
        this.showOnlyAvailable = showOnlyAvailable;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public boolean isUseBrowserInsteadOfApp() {
        return useBrowserInsteadOfApp;
    }

    public void setUseBrowserInsteadOfApp(boolean useBrowserInsteadOfApp) {
        this.useBrowserInsteadOfApp = useBrowserInsteadOfApp;
    }

}
