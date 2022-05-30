package me.nefelion.spotifynotifier;

import javafx.application.Platform;
import me.nefelion.spotifynotifier.data.FileData;
import me.nefelion.spotifynotifier.data.FileManager;
import me.nefelion.spotifynotifier.data.TempData;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.AlbumType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.stream.IntStream;

public class TheEngine {
    private static final int TRY_AGAIN = 10;
    private static TheEngine instance;
    private SpotifyApi spotifyAPI;
    private int tried = 0;

    private TheEngine() {
    }

    public synchronized static TheEngine getInstance() {
        if (instance == null) {
            instance = new TheEngine();
        }
        return instance;
    }

    public void setSpotifyAPI(SpotifyApi spotifyApi) {
        this.spotifyAPI = spotifyApi;
    }

    public synchronized void followArtistID(String id) {
        if (id == null || id.trim().isEmpty()) return;

        FileData fileData = TempData.getInstance().getFileData();
        List<FollowedArtist> followedArtists = fileData.getFollowedArtists();

        HashSet<String> hashSet = FileManager.getAlbumHashSet();

        OptionalInt indexOfExistingID = IntStream.range(0, followedArtists.size()).filter(i -> id.equalsIgnoreCase(followedArtists.get(i).getID())).findFirst();
        if (indexOfExistingID.isPresent()) {
            Utilities.showMessageDialog(followedArtists.get(indexOfExistingID.getAsInt()).getName() + " is already followed.", "Already followed!", JOptionPane.ERROR_MESSAGE);
            System.out.println(followedArtists.get(indexOfExistingID.getAsInt()).getName() + " is already followed.");
            return;
        }


        try {
            Artist artist = spotifyAPI.getArtist(id).build().execute();
            FollowedArtist newArtist = new FollowedArtist(artist.getName(), artist.getId());
            for (AlbumSimplified album : getAlbums(id)) hashSet.add(album.getId());
            followedArtists.add(newArtist);
            FileManager.saveFileData(fileData);
            FileManager.saveAlbumHashSet(hashSet);
            String message = "Added: " + artist.getName();
            System.out.println(message);

            Platform.runLater(() -> {
                if (Utilities.okUndoMSGBOX(message)) unfollowArtistID(artist.getId());
            });

        } catch (SpotifyWebApiException e) {
            System.out.println("followArtistID: Something went wrong!\n" + e.getMessage());
            Utilities.showMessageDialog(e.getMessage(), "Something went wrong!", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | ParseException e) {
            if (Utilities.tryAgainMSGBOX("followArtistID: Something went wrong!\n" + e.getMessage())) {
                followArtistID(id);
                return;
            }
            System.exit(-1005);
        }

    }

    public synchronized void unfollowArtistID(String id) {
        if (id == null || id.trim().isEmpty()) return;

        FileData fileData = TempData.getInstance().getFileData();
        List<FollowedArtist> followedArtists = fileData.getFollowedArtists();

        OptionalInt indexOfExistingID = IntStream.range(0, followedArtists.size()).filter(i -> id.equalsIgnoreCase(followedArtists.get(i).getID())).findFirst();
        if (indexOfExistingID.isEmpty()) {
            System.out.println(id + " is not followed.");
            Utilities.showMessageDialog(id + " is not followed.", "Something went wrong!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        FollowedArtist artistToBeRemoved = followedArtists.get(indexOfExistingID.getAsInt());

        if (followedArtists.remove(artistToBeRemoved)) {
            FileManager.saveFileData(fileData);
            String message = "Removed: " + artistToBeRemoved.getName() + ".";
            System.out.println(message);

            Platform.runLater(() -> {
                if (Utilities.okUndoMSGBOX(message)) followArtistID(artistToBeRemoved.getID());
            });

        } else {
            System.err.println("Error while removing " + artistToBeRemoved.getID());
            Utilities.showMessageDialog("Error while removing " + artistToBeRemoved.getID(), "Something went wrong!", JOptionPane.ERROR_MESSAGE);
        }

    }


    public synchronized List<AlbumSimplified> getAlbums(String artistID) {

        List<AlbumSimplified> allAlbums = new ArrayList<>();

        try {
            Paging<AlbumSimplified> paging;

            int offset = 0;
            int n = 50;

            do {
                paging = spotifyAPI.getArtistsAlbums(artistID).offset(offset).limit(n).build().execute();
                allAlbums.addAll(List.of(paging.getItems()));
                offset += n;
            } while (paging.getNext() != null);
        } catch (CancellationException ignored) {
        } catch (Exception e) {
            if (tried++ < TRY_AGAIN || Utilities.tryAgainMSGBOX("getAlbums: Something went wrong!\n" + e.getMessage()))
                return getAlbums(artistID);
            System.exit(-1006);
        }

        tried = 0;
        allAlbums.removeIf(p -> p.getAlbumType().equals(AlbumType.COMPILATION));
        return allAlbums;
    }

    public synchronized List<TrackSimplified> getTracks(String albumID) {
        List<TrackSimplified> allTracks = new ArrayList<>();

        try {
            Paging<TrackSimplified> paging;

            int offset = 0;
            int n = 50;

            do {
                paging = spotifyAPI.getAlbumsTracks(albumID).offset(offset).limit(n).build().execute();
                allTracks.addAll(List.of(paging.getItems()));
                offset += n;
            } while (paging.getNext() != null);

        } catch (Exception e) {
            if (tried++ < TRY_AGAIN || Utilities.tryAgainMSGBOX("getTracks: Something went wrong!\n" + e.getMessage()))
                return getTracks(albumID);
            System.exit(-1016);
        }

        tried = 0;
        return allTracks;
    }

    public synchronized Album getAlbum(String albumID) {
        Album album;
        try {
            album = spotifyAPI.getAlbum(albumID).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            if (tried++ < TRY_AGAIN || Utilities.tryAgainMSGBOX("getAlbum: Something went wrong!\n" + e.getMessage()))
                return getAlbum(albumID);
            System.exit(-1007);
            return null;
        }

        tried = 0;
        return album;
    }

    public synchronized Artist getArtist(String albumID) {
        Artist artist;
        try {
            artist = spotifyAPI.getArtist(albumID).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            if (tried++ < TRY_AGAIN || Utilities.tryAgainMSGBOX("getArtist: Something went wrong!\n" + e.getMessage()))
                return getArtist(albumID);
            System.exit(-1367);
            return null;
        }

        tried = 0;
        return artist;
    }

    public synchronized Artist[] getRelatedArtists(String id) {
        Artist[] artists;
        try {
            artists = spotifyAPI.getArtistsRelatedArtists(id).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            if (tried++ < TRY_AGAIN || Utilities.tryAgainMSGBOX("getSimilarArtists: Something went wrong!\n" + e.getMessage()))
                return getRelatedArtists(id);
            System.exit(-17327);
            return null;
        }

        tried = 0;
        return artists;
    }

    public synchronized List<Artist> searchArtist(String name) {
        List<Artist> artists;
        try {
            artists = new ArrayList<>(List.of(spotifyAPI.searchArtists(name).offset(0).limit(50).build().execute().getItems()));
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            if (tried++ < TRY_AGAIN || Utilities.tryAgainMSGBOX("searchArtist: Something went wrong!\n" + e.getMessage()))
                return searchArtist(name);
            System.exit(-1367);
            return null;
        }

        tried = 0;
        return artists;
    }

    public synchronized AudioFeatures[] getAudioFeatures(List<String> idList) {
        AudioFeatures[] features;
        try {
            features = spotifyAPI.getAudioFeaturesForSeveralTracks(String.join(",", idList)).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            if (tried++ < TRY_AGAIN || Utilities.tryAgainMSGBOX("getAudioFeatures: Something went wrong!\n" + e.getMessage()))
                return getAudioFeatures(idList);
            System.exit(-17322);
            return null;
        }

        tried = 0;
        return features;
    }

    public synchronized boolean isFollowed(String id) {
        return TempData.getInstance().getFileData().getFollowedArtists().stream().anyMatch(p -> p.getID().equals(id));
    }

}
