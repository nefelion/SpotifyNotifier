package me.nefelion.spotifynotifier.subcommands;

import javafx.application.Application;
import me.nefelion.spotifynotifier.ISubCommand;
import me.nefelion.spotifynotifier.gui.apps.AppAllAlbums;

public class AllRecentAlbums implements ISubCommand {
    public AllRecentAlbums() {
    }

    @Override
    public void execute(String[] args) {

        switch (args.length) {
            case 0 -> Application.launch(AppAllAlbums.class);

            default -> System.err.println("Wrong number of arguments (" + args.length + ")");
        }

    }

}
