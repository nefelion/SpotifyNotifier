package me.nefelion.spotifynotifier;

import javafx.application.Application;
import me.nefelion.spotifynotifier.data.FileManager;
import me.nefelion.spotifynotifier.data.TempData;
import me.nefelion.spotifynotifier.gui.apps.AppMain;
import me.nefelion.spotifynotifier.subcommands.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

public class Main {
    private final static HashMap<String, ISubCommand> subCommands = new HashMap<>();
    private static int buildNumber;
    private static String version;
    private static FileLock lock = null;

    public static void main(String[] args) {
        loadVersionNumber();

        try {
            FileManager.setPath(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(123);
        }

        // initialize singleton ram
        TempData.getInstance().setFileData(FileManager.getFileData());

        if (!lock()) {
            Utilities.showSwingMessageDialog("Another instance is running!", "Error!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ClientManager cm = new ClientManager();
        TheEngine.getInstance().setSpotifyAPI(cm.getSpotifyApi());

        loadSubcommands();


        if (args.length == 0) {
            Application.launch(AppMain.class);
        } else if (subCommands.containsKey(args[0].toLowerCase()))
            subCommands.get(args[0].toLowerCase()).execute(Arrays.copyOfRange(args, 1, args.length));
        else Utilities.showSwingMessageDialog("Wrong arg '" + args[0] + "'", "Error!", JOptionPane.ERROR_MESSAGE);

    }

    private static void loadVersionNumber() {
        final Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        version = properties.getProperty("version");
        buildNumber = Integer.parseInt(properties.getProperty("buildNumber"));
    }

    public static int getBuildNumber() {
        return buildNumber;
    }

    public static String getVersion() {
        return version;
    }

    public static String getFullVersion() {
        return getVersion() + "." + getBuildNumber();
    }

    private static void loadSubcommands() {
        subCommands.put("add", new Add());
        subCommands.put("remove", new Remove());
        subCommands.put("check", new CheckReleases());
        subCommands.put("list", new Followed());
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
