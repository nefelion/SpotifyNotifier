package me.nefelion.spotifynotifier.subcommands;

import me.nefelion.spotifynotifier.ISubCommand;
import me.nefelion.spotifynotifier.TheEngine;

public class AllRecentAlbums implements ISubCommand {

    private final TheEngine theEngine;

    public AllRecentAlbums(TheEngine theEngine) {
        this.theEngine = theEngine;
    }

    @Override
    public void execute(String[] args) {

        switch (args.length) {
            case 0 -> theEngine.printAllRecentAlbums();

            default -> System.err.println("Wrong number of arguments (" + args.length + ")");
        }

    }

}
