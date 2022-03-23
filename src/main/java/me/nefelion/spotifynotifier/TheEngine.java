package me.nefelion.spotifynotifier;

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
    private static TheEngine instance;
    private SpotifyApi spotifyAPI;

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

        OptionalInt indexOfExistingID = IntStream.range(0, followedArtists.size())
                .filter(i -> id.equalsIgnoreCase(followedArtists.get(i).getID()))
                .findFirst();
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
            System.out.println("Added: " + artist.getName() + " / " + artist.getId());
            Utilities.showMessageDialog("Added: " + artist.getName(), "Done!", JOptionPane.INFORMATION_MESSAGE);
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

        OptionalInt indexOfExistingID = IntStream.range(0, followedArtists.size())
                .filter(i -> id.equalsIgnoreCase(followedArtists.get(i).getID()))
                .findFirst();
        if (indexOfExistingID.isEmpty()) {
            System.out.println(id + " is not followed.");
            Utilities.showMessageDialog(id + " is not followed.", "Something went wrong!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        FollowedArtist artistToBeRemoved = followedArtists.get(indexOfExistingID.getAsInt());

        if (followedArtists.remove(artistToBeRemoved)) {
            FileManager.saveFileData(fileData);
            System.out.println("Removed: " + artistToBeRemoved.getName() + ".");

            String message = "Removed: " + artistToBeRemoved.getName() + ".";

            String[] choices = {"OK", "Undo"};
            int response = JOptionPane.showOptionDialog(
                    null,
                    message,
                    "Done!",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    choices,
                    "OK"
            );

            if (response == 1) followArtistID(artistToBeRemoved.getID());

        } else {
            System.err.println("Error while removing " + artistToBeRemoved.getID());
            Utilities.showMessageDialog("Error while removing " + artistToBeRemoved.getID(), "Something went wrong!", JOptionPane.ERROR_MESSAGE);
        }

    }


    public void printAllArtistAlbums(String id) {
        if (id == null || id.trim().isEmpty()) return;

        FileData fileData = TempData.getInstance().getFileData();
        List<FollowedArtist> followedArtists = fileData.getFollowedArtists();
        FollowedArtist artist;

        OptionalInt indexOfExistingID = IntStream.range(0, followedArtists.size())
                .filter(i -> id.equalsIgnoreCase(followedArtists.get(i).getID()))
                .findFirst();
        if (indexOfExistingID.isPresent()) artist = followedArtists.get(indexOfExistingID.getAsInt());
        else {
            Artist temp = getArtist(id);
            artist = new FollowedArtist(temp.getName(), temp.getId());
        }

        ReleasesProcessor processor = new ReleasesProcessor(artist);
        processor.process();

        List<ReleasedAlbum> releasedAlbums = processor.getAllAlbums();


    }

    public void printAllRecentAlbums() {

        ReleasesProcessor processor = new ReleasesProcessor(TempData.getInstance().getFileData().getFollowedArtists());
        processor.process();

        List<ReleasedAlbum> releasedAlbums = processor.getAllAlbums();


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
            if (Utilities.tryAgainMSGBOX("getAlbums: Something went wrong!\n" + e.getMessage()))
                return getAlbums(artistID);
            System.exit(-1006);
        }

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
            if (Utilities.tryAgainMSGBOX("getTracks: Something went wrong!\n" + e.getMessage()))
                return getTracks(albumID);
            System.exit(-1016);
        }
        return allTracks;
    }

    public synchronized Album getAlbum(String albumID) {
        try {
            return spotifyAPI.getAlbum(albumID).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            if (Utilities.tryAgainMSGBOX("getAlbum: Something went wrong!\n" + e.getMessage()))
                return getAlbum(albumID);
            System.exit(-1007);
            return null;
        }
    }

    public synchronized Artist getArtist(String albumID) {
        try {
            return spotifyAPI.getArtist(albumID).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            if (Utilities.tryAgainMSGBOX("getArtist: Something went wrong!\n" + e.getMessage()))
                return getArtist(albumID);
            System.exit(-1367);
            return null;
        }
    }

    public synchronized Artist[] getRelatedArtists(String id) {
        try {
            return spotifyAPI.getArtistsRelatedArtists(id).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            if (Utilities.tryAgainMSGBOX("getSimilarArtists: Something went wrong!\n" + e.getMessage()))
                return getRelatedArtists(id);
            System.exit(-17327);
            return null;
        }
    }

    public synchronized List<Artist> searchArtist(String name) {

        List<Artist> artists = new ArrayList<>();
        try {
            artists.addAll(List.of(spotifyAPI.searchArtists(name).offset(0).limit(50).build().execute().getItems()));
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Utilities.showMessageDialog(e.getMessage(), "searchArtist ERROR", JOptionPane.ERROR_MESSAGE);
        }

        return artists;
    }

    public boolean isFollowed(String id) {
        return TempData.getInstance().getFileData().getFollowedArtists().stream().anyMatch(p -> p.getID().equals(id));
    }

    public void showRelatedAlbums() {
        HashSet<String> uniqueArtistsID = new HashSet<>();
        ArrayList<FollowedArtist> relatedArtists = new ArrayList<>();
        List<FollowedArtist> followedArtists = TempData.getInstance().getFileData().getFollowedArtists();


        for (FollowedArtist followedArtist : followedArtists) {
            for (Artist artist : getRelatedArtists(followedArtist.getID())) {
                String id = artist.getId();
                if (isFollowed(id)) continue;
                if (!uniqueArtistsID.add(id)) continue;
                relatedArtists.add(new FollowedArtist(artist.getName(), id));
            }

        }


        relatedArtists.sort(Comparator.comparing(FollowedArtist::getName));
        ReleasesProcessor processor = new ReleasesProcessor(relatedArtists);
        processor.process();

        List<ReleasedAlbum> albums = new ArrayList<>(processor.getAllAlbums());

    }

}
