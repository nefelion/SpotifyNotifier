package me.grothgar.spotifynotifier.subcommands;

import me.grothgar.spotifynotifier.ISubCommand;
import me.grothgar.spotifynotifier.TheEngine;
import me.grothgar.spotifynotifier.Utilities;

public class Add implements ISubCommand {

    private final TheEngine theEngine;

    public Add(TheEngine theEngine) {
        this.theEngine = theEngine;
    }

    @Override
    public void execute(String[] args) {

        switch (args.length) {
            case 0 -> {
                String str;
                do {
                    str = Utilities.showInputDialog("Enter Artist ID to add", "Add Artist ID");
                    theEngine.followArtistID(str);
                } while (str != null && !str.trim().isEmpty());
            }

            case 1 -> theEngine.followArtistID(args[0]);

            default -> System.err.println("Wrong number of arguments (" + args.length + ")");
        }

    }

}
