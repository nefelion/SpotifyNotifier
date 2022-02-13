package me.nefelion.spotifynotifier;

import me.nefelion.spotifynotifier.gui.FollowedGUI;
import me.nefelion.spotifynotifier.gui.GUIFrame;
import me.nefelion.spotifynotifier.gui.ProgressGUI;
import me.nefelion.spotifynotifier.gui.ReleasedAlbumsGUI;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.AlbumType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TheEngine {
    private final SpotifyApi spotifyApi;

    public TheEngine(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }


    public void followArtistID(String id) {
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
            Artist artist = spotifyApi.getArtist(id).build().execute();
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
            e.printStackTrace();
            System.exit(-1005);
        }

    }

    public void unfollowArtistID(String id) {
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
            Utilities.showMessageDialog("Removed: " + artistToBeRemoved.getName() + ".", "Done!", JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.err.println("Error while removing " + artistToBeRemoved.getID());
            Utilities.showMessageDialog("Error while removing " + artistToBeRemoved.getID(), "Something went wrong!", JOptionPane.ERROR_MESSAGE);
        }

    }

    public void checkForNewReleases(boolean quiet) {
        FileData fd = TempData.getInstance().getFileData();
        List<FollowedArtist> followedArtists = fd.getFollowedArtists();
        HashSet<String> hashSet = FileManager.getAlbumHashSet();

        List<ReleasedAlbum> releasedAlbums = new LinkedList<>();

        ProgressGUI progressBar = new ProgressGUI(0, followedArtists.size());
        if (!quiet) progressBar.show();
        int i = 0;


        for (FollowedArtist artist : followedArtists.stream().sorted(Comparator.comparing(FollowedArtist::getName)).collect(Collectors.toList())) {
            progressBar.setTitle(artist.getName());

            for (AlbumSimplified album : getAlbums(artist.getID())) {
                if (hashSet.contains(album.getId())) continue;
                hashSet.add(album.getId());
                releasedAlbums.add(new ReleasedAlbum(album, artist));
                //System.out.println(artist.getName() + "\t-\t" + album.getName() + "\t/\t" + album.getAlbumType().toString() + "\t/\t" + album.getReleaseDate() + "\t/\thttps://open.spotify.com/album/" + album.getId());
            }

            progressBar.setValue(++i);
        }
        fd.setLastChecked(Utilities.now());
        FileManager.saveFileData(fd);
        FileManager.saveAlbumHashSet(hashSet);
        progressBar.close();

        if (!releasedAlbums.isEmpty()) {
            GUIFrame gui = new ReleasedAlbumsGUI(JFrame.EXIT_ON_CLOSE, this, releasedAlbums, "New releases: " + releasedAlbums.size());
            gui.show();
        } else if (!quiet) {
            Utilities.showMessageDialog("No new releases.", "Check releases", JOptionPane.INFORMATION_MESSAGE);
        }


    }


    public void showFollowedList() {
        List<FollowedArtist> followedArtistSortedList = TempData.getInstance().getFileData().getFollowedArtists().stream().sorted(Comparator.comparing(FollowedArtist::getName)).collect(Collectors.toList());

        GUIFrame gui = new FollowedGUI(JFrame.EXIT_ON_CLOSE, this, followedArtistSortedList);
        gui.show();
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


        List<AlbumSimplified> albums = getAlbums(id);
        List<ReleasedAlbum> releasedAlbums = new ArrayList<>();
        for (AlbumSimplified album : albums) releasedAlbums.add(new ReleasedAlbum(album, artist));

        GUIFrame gui = new ReleasedAlbumsGUI(JFrame.HIDE_ON_CLOSE, this, releasedAlbums,
                "All releases by " + artist.getName() + " (" + releasedAlbums.size() + ")");
        gui.show();
    }

    public void printAllRecentAlbums() {
        List<ReleasedAlbum> releasedAlbums = new ArrayList<>();
        List<FollowedArtist> followedArtists = TempData.getInstance().getFileData().getFollowedArtists();
        HashSet<String> IDHashset = new HashSet<>();

        ProgressGUI progressBar = new ProgressGUI(0, followedArtists.size());
        progressBar.show();
        int i = 0;
        for (FollowedArtist artist : followedArtists) {
            progressBar.setTitle(artist.getName());
            for (AlbumSimplified album : getAlbums(artist.getID()))
                if (IDHashset.add(album.getId())) releasedAlbums.add(new ReleasedAlbum(album, artist));

            progressBar.setValue(++i);
        }

        progressBar.close();

        GUIFrame gui = new ReleasedAlbumsGUI(JFrame.EXIT_ON_CLOSE, this, releasedAlbums, "Recent albums (" + releasedAlbums.size() + ")");
        gui.show();
    }

    private List<AlbumSimplified> getAlbums(String artistID) {

        List<AlbumSimplified> allAlbums = new ArrayList<>();

        try {
            Paging<AlbumSimplified> paging;

            int offset = 0;
            int n = 50;

            do {
                paging = spotifyApi.getArtistsAlbums(artistID).offset(offset).limit(n).build().execute();
                allAlbums.addAll(List.of(paging.getItems()));
                offset += n;
            } while (paging.getNext() != null);

        } catch (Exception e) {
            System.out.println("getAlbums: Something went wrong!\n" + e.getMessage());
            System.exit(-1006);
        }

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
                paging = spotifyApi.getAlbumsTracks(albumID).offset(offset).limit(n).build().execute();
                allTracks.addAll(List.of(paging.getItems()));
                offset += n;
            } while (paging.getNext() != null);

        } catch (Exception e) {
            System.out.println("getTracks: Something went wrong!\n" + e.getMessage());
            System.exit(-1016);
        }

        return allTracks;
    }

    public Album getAlbum(String albumID) {
        try {
            return spotifyApi.getAlbum(albumID).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("getAlbum: Something went wrong!\n" + e.getMessage());
            System.exit(-1007);
            return null;
        }
    }

    public Artist getArtist(String albumID) {
        try {
            return spotifyApi.getArtist(albumID).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("getArtist: Something went wrong!\n" + e.getMessage());
            System.exit(-1367);
            return null;
        }
    }

    public List<Artist> searchArtist(String name) {

        List<Artist> artists = new ArrayList<>();
        try {
            artists.addAll(List.of(spotifyApi.searchArtists(name).offset(0).limit(50).build().execute().getItems()));
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Utilities.showMessageDialog(e.getMessage(), "searchArtist ERROR", JOptionPane.ERROR_MESSAGE);
        }

        return artists;
    }

    public boolean isFollowed(String id) {
        return TempData.getInstance().getFileData().getFollowedArtists().stream().anyMatch(p -> p.getID().equals(id));
    }

}
