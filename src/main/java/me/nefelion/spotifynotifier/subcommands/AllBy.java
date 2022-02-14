package me.nefelion.spotifynotifier.subcommands;

import me.nefelion.spotifynotifier.ISubCommand;
import me.nefelion.spotifynotifier.TheEngine;
import me.nefelion.spotifynotifier.Utilities;

public class AllBy implements ISubCommand {

    private final TheEngine theEngine = TheEngine.getInstance();

    public AllBy() {
    }

    @Override
    public void execute(String[] args) {

        switch (args.length) {
            case 0 -> theEngine.printAllArtistAlbums(Utilities.showInputDialog("Enter Artist ID to show albums", "Enter Artist ID"));

            case 1 -> theEngine.printAllArtistAlbums(args[0]);

            default -> System.err.println("Wrong number of arguments (" + args.length + ")");
        }

    }

}
