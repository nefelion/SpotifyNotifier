package me.nefelion.spotifynotifier.subcommands;

import me.nefelion.spotifynotifier.ISubCommand;
import me.nefelion.spotifynotifier.TheEngine;
import me.nefelion.spotifynotifier.Utilities;

public class Remove implements ISubCommand {

    private final TheEngine theEngine = TheEngine.getInstance();

    public Remove() {
    }

    @Override
    public void execute(String[] args) {

        switch (args.length) {
            case 0 -> {
                String str;
                do {
                    str = Utilities.showInputDialog("Enter Artist ID to remove", "Remove Artist ID");
                    theEngine.unfollowArtistID(str);
                } while (str != null && !str.trim().isEmpty());
            }

            case 1 -> theEngine.unfollowArtistID(args[0]);

            default -> System.err.println("Wrong number of arguments (" + args.length + ")");
        }

    }

}
