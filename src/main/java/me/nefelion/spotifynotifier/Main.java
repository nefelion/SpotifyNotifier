package me.nefelion.spotifynotifier;

import me.nefelion.spotifynotifier.data.FileManager;
import me.nefelion.spotifynotifier.data.TempData;
import me.nefelion.spotifynotifier.gui.Starter;
import me.nefelion.spotifynotifier.subcommands.*;
import se.michaelthelin.spotify.SpotifyApi;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;

public class Main {

    private final static HashMap<String, ISubCommand> subCommands = new HashMap<>();
    private static FileLock lock = null;

    public static void main(String[] args) {

        try {
            FileManager.setPath(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(123);
        }

        // initialize singleton ram
        TempData.getInstance().setFileData(FileManager.getFileData());

        // :)
        //TokenGetter tokenGetter = new TokenGetter("d31c4bbf7a6c41e4b1ce6c47656de668", "c1f3be4db5104a54bc2fac060fdbdf14");
        TokenGetter tokenGetter = new TokenGetter("44d4e6f2c0ec49ccba1af136cfc57389", "dab320e0e78a464aa773cf3983732049");

        String token = tokenGetter.getToken();
        if (token == null) {
            Utilities.showMessageDialog("Invalid token!", "Error!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(token)
                .build();


        if (!lock()) {
            Utilities.showMessageDialog("Another instance is running!", "Error!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        TheEngine.getInstance().setSpotifyAPI(spotifyApi);

        loadSubcommands();


        if (args.length == 0) {
            Starter.main(new String[]{""});
        } else if (subCommands.containsKey(args[0].toLowerCase()))
            subCommands.get(args[0].toLowerCase()).execute(Arrays.copyOfRange(args, 1, args.length));
        else Utilities.showMessageDialog("Wrong arg '" + args[0] + "'", "Error!", JOptionPane.ERROR_MESSAGE);

    }

    private static void loadSubcommands() {
        subCommands.put("add", new Add());
        subCommands.put("remove", new Remove());
        subCommands.put("check", new CheckReleases());
        subCommands.put("list", new Followed());
        subCommands.put("allby", new AllBy());
        subCommands.put("recent", new AllRecentAlbums());
        subCommands.put("explore", new Explore());
    }

    private static boolean lock() {
        File file = new File(FileManager.getPath().toString(), "spotifynotifier.lock");
        try {
            FileChannel fc = FileChannel.open(file.toPath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
            lock = fc.tryLock();
            if (lock == null) return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
