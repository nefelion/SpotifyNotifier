package me.nefelion.spotifynotifier;

import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;


public class FileManager {
    private static final String FOLLOWED_DATA_YML = "followedData.yml";
    private static final String ALBUM_DATA = "album.data";

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


    public static void createAlbumFile() {
        File f = new File(path.toString() + "/" + ALBUM_DATA);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Couldn't create AlbumData!");
                System.exit(-1092);
            }
        }
    }

    public static void saveAlbumHashSet(HashSet<String> hashSet) {
        createFile(ALBUM_DATA);

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path.toString() + "/" + ALBUM_DATA));
            for (String s : hashSet) {
                out.write(s);
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't save AlbumData!");
            System.exit(-1093);
        }

    }

    public static HashSet<String> getAlbumHashSet() {

        HashSet<String> hashSet = new HashSet<>();

        try {
            String line;
            BufferedReader in = new BufferedReader(new FileReader(path.toString() + "/" + ALBUM_DATA));
            while ((line = in.readLine()) != null) hashSet.add(line);
            in.close();
        } catch (FileNotFoundException e) {
            createFile(ALBUM_DATA);
            saveAlbumHashSet(new HashSet<>());
            return getAlbumHashSet();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't save AlbumData!");
            System.exit(-1093);
        }

        return hashSet;
    }


}
