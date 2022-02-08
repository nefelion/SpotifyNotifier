package me.grothgar.spotifynotifier.subcommands;

import me.grothgar.spotifynotifier.ISubCommand;
import me.grothgar.spotifynotifier.TheEngine;
import me.grothgar.spotifynotifier.Utilities;

public class AllBy implements ISubCommand {

    private final TheEngine theEngine;

    public AllBy(TheEngine theEngine) {
        this.theEngine = theEngine;
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
