package me.nefelion.spotifynotifier.subcommands;

import me.nefelion.spotifynotifier.ISubCommand;
import me.nefelion.spotifynotifier.TheEngine;

public class Explore implements ISubCommand {

    private final TheEngine theEngine = TheEngine.getInstance();

    public Explore() {
    }

    @Override
    public void execute(String[] args) {

        switch (args.length) {
            case 0 -> theEngine.showRelatedAlbums();

            default -> System.err.println("Wrong number of arguments (" + args.length + ")");
        }

    }
}
