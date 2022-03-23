package me.nefelion.spotifynotifier.subcommands;

import javafx.application.Application;
import me.nefelion.spotifynotifier.ISubCommand;
import me.nefelion.spotifynotifier.gui.apps.AppNewAlbums;
import me.nefelion.spotifynotifier.gui.apps.AppNewAlbumsQuiet;

public class CheckReleases implements ISubCommand {

    public CheckReleases() {
    }

    @Override
    public void execute(String[] args) {

        switch (args.length) {
            case 0 -> Application.launch(AppNewAlbums.class);

            case 1 -> {
                if (args[0].equalsIgnoreCase("quiet")) Application.launch(AppNewAlbumsQuiet.class);
                else Application.launch(AppNewAlbums.class);
            }

            default -> System.err.println("Wrong number of arguments (" + args.length + ")");
        }

    }

}
