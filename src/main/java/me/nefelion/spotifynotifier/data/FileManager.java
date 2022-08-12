package me.nefelion.spotifynotifier.data;

import com.google.gson.Gson;
import me.nefelion.spotifynotifier.FollowedArtist;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;


public class FileManager {
    private static final String FOLLOWED_DATA_YML = "followedData.yml";
    public static final String ALBUM_DATA = "album.data";
    public static final String REMIND_DATA = "remind.data";

    private static Path path;

    public static Path getPath() {
        return path;
    }

    public static void setPath(String path) {
        FileManager.path = Path.of(path);
    }

    public static void createFile(String filename) {
        File f = new File(path.toString() + "/" + FOLLOWED_DATA_YML);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Couldn't create file " + filename);
                System.exit(-1002);
            }
        }
    }

    public static void saveFileData(FileData fd) {

        createFile(FOLLOWED_DATA_YML);
        fd.getFollowedArtists().sort(Comparator.comparing(FollowedArtist::getName));

        try (Writer writer = new FileWriter(path.toString() + "/" + FOLLOWED_DATA_YML, StandardCharsets.UTF_8)) {
            new Gson().toJson(fd, writer);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't save FileData!");
            System.exit(-1003);
        }

    }

    public static FileData getFileData() {

        try (Reader reader = new FileReader(path.toString() + "/" + FOLLOWED_DATA_YML, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, FileData.class);
        } catch (FileNotFoundException e) {
            createFile(FOLLOWED_DATA_YML);
            FileData fd = new FileData();
            saveFileData(fd);
            return getFileData();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't get GlobalData!");
            System.exit(-1203);
            return new FileData();
        }

    }

    public static void saveHashSet(String fileName, HashSet<String> hashSet) {
        createFile(fileName);

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path.toString() + "/" + fileName));
            for (String s : hashSet) {
                out.write(s);
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't save " + fileName + "!");
            System.exit(-1093);
        }

    }

    public static HashSet<String> getHashSet(String fileName) {

        HashSet<String> hashSet = new HashSet<>();

        try {
            String line;
            BufferedReader in = new BufferedReader(new FileReader(path.toString() + "/" + fileName));
            while ((line = in.readLine()) != null) hashSet.add(line);
            in.close();
        } catch (FileNotFoundException e) {
            createFile(fileName);
            saveHashSet(fileName, new HashSet<>());
            return getHashSet(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't get " + fileName + "!");
            System.exit(-1093);
        }

        return hashSet;
    }

    public static boolean addToRemind(String albumID) {
        HashSet<String> hashSet = getHashSet(REMIND_DATA);
        if (hashSet.contains(albumID)) return false;
        hashSet.add(albumID);
        saveHashSet(REMIND_DATA, hashSet);
        System.out.println("Added " + albumID + " to remind list. (" + hashSet.size() + ")");
        return true;
    }

    public static boolean removeFromRemind(String albumID) {
        HashSet<String> hashSet = getHashSet(REMIND_DATA);
        if (!hashSet.contains(albumID)) return false;
        hashSet.remove(albumID);
        saveHashSet(REMIND_DATA, hashSet);
        System.out.println("Removed " + albumID + " from remind list. (" + hashSet.size() + ")");
        return true;
    }
}
