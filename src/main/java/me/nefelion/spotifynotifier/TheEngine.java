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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class TheEngine {
    private static final int TRY_AGAIN = 10;
    private static TheEngine instance;
    private SpotifyApi spotifyAPI;
    private int tried = 0;

    private TheEngine() {
    }

    public static TheEngine getInstance() {
        if (instance == null) {
            instance = new TheEngine();
        }
        return instance;
    }

    public void setSpotifyAPI(SpotifyApi spotifyApi) {
        this.spotifyAPI = spotifyApi;
    }

    public void followArtistID(String id) {
        if (id == null || id.trim().isEmpty()) return;

        FileData fileData = TempData.getInstance().getFileData();
        List<FollowedArtist> followedArtists = fileData.getFollowedArtists();

        HashSet<String> hashSet = FileManager.getHashSet(FileManager.ALBUM_DATA);

        OptionalInt indexOfExistingID = IntStream.range(0, followedArtists.size()).filter(i -> id.equalsIgnoreCase(followedArtists.get(i).getID())).findFirst();
        if (indexOfExistingID.isPresent()) {
            Utilities.showSwingMessageDialog(followedArtists.get(indexOfExistingID.getAsInt()).getName() + " is already followed.", "Already followed!", JOptionPane.ERROR_MESSAGE);
            System.out.println(followedArtists.get(indexOfExistingID.getAsInt()).getName() + " is already followed.");
            return;
        }


        try {
            Artist artist = spotifyAPI.getArtist(id).build().execute();
            FollowedArtist newArtist = new FollowedArtist(artist.getName(), artist.getId());
            for (AlbumSimplified album : getAlbums(id)) hashSet.add(album.getId());
            followedArtists.add(newArtist);
            FileManager.saveFileData(fileData);
            FileManager.saveHashSet(FileManager.ALBUM_DATA, hashSet);
            String message = "Added: " + artist.getName();
            System.out.println(message);

            Platform.runLater(() -> {
                if (Utilities.okUndoMSGBOX(message)) unfollowArtistID(artist.getId());
            });

        } catch (SpotifyWebApiException e) {
            System.out.println("followArtistID: Something went wrong!\n" + e.getMessage());
            Utilities.showSwingMessageDialog(e.getMessage(), "Something went wrong!", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | ParseException e) {
            if (Utilities.tryAgainMSGBOX("followArtistID: Something went wrong!\n" + e.getMessage())) {
                followArtistID(id);
                return;
            }
            System.exit(-1005);
        }

    }

    public void unfollowArtistID(String id) {
        if (id == null || id.trim().isEmpty()) return;

        FileData fileData = TempData.getInstance().getFileData();
        List<FollowedArtist> followedArtists = fileData.getFollowedArtists();

        OptionalInt indexOfExistingID = IntStream.range(0, followedArtists.size()).filter(i -> id.equalsIgnoreCase(followedArtists.get(i).getID())).findFirst();
        if (indexOfExistingID.isEmpty()) {
            System.out.println(id + " is not followed.");
            Utilities.showSwingMessageDialog(id + " is not followed.", "Something went wrong!", JOptionPane.ERROR_MESSAGE);
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
            Utilities.showSwingMessageDialog("Error while removing " + artistToBeRemoved.getID(), "Something went wrong!", JOptionPane.ERROR_MESSAGE);
        }

    }

    public List<AlbumSimplified> getAlbums(String artistID) {
        return getAlbums(artistID, null, null, -1);
    }

    public List<AlbumSimplified> getAlbums(String artistID, Consumer<Integer> page, Consumer<Integer> all, int maxPages) throws CancellationException {

        List<AlbumSimplified> allAlbums = new ArrayList<>();

        try {
            Paging<AlbumSimplified> paging;
            int offset = 0;
            int n = 50;

            do {
                paging = spotifyAPI.getArtistsAlbums(artistID).offset(offset).limit(n).build().execute();

                int allPages = (paging.getTotal() / 50) + 1;
                if (allPages > maxPages && maxPages != -1) return allAlbums;
                if (page != null) page.accept(offset / 50);
                if (all != null) all.accept((allPages));


                allAlbums.addAll(List.of(paging.getItems()));
                offset += n;
            } while (paging.getNext() != null);
        } catch (CancellationException e) {
            throw e;
        } catch (Exception e) {
            if (tried++ < TRY_AGAIN || Utilities.tryAgainMSGBOX("getAlbums: Something went wrong!\n" + e.getMessage()))
                return getAlbums(artistID);
            System.exit(-1006);
        }

        tried = 0;
        allAlbums.removeIf(p -> p.getAlbumType().equals(AlbumType.COMPILATION));
        return allAlbums;
    }

    public List<TrackSimplified> getTracks(String albumID) {
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

    public Album getAlbum(String albumID) {
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

    public Artist getArtist(String albumID) {
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

    public Artist[] getRelatedArtists(String id) {
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

    public List<Artist> searchArtist(String name) {
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

    public AudioFeatures[] getAudioFeatures(List<String> idList) {
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

    public static boolean isFollowed(String id) {
        return TempData.getInstance().getFileData().getFollowedArtists().stream().anyMatch(p -> p.getID().equals(id));
    }

}
