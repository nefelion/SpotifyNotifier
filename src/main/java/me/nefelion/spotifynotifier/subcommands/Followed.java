package me.nefelion.spotifynotifier.subcommands;

import javafx.application.Application;
import me.nefelion.spotifynotifier.ISubCommand;
import me.nefelion.spotifynotifier.gui.apps.AppMain;

public class Followed implements ISubCommand {

    public Followed() {
    }

    @Override
    public void execute(String[] args) {

        switch (args.length) {
            case 0 -> Application.launch(AppMain.class);

            default -> System.err.println("Wrong number of arguments (" + args.length + ")");
        }

    }

}
