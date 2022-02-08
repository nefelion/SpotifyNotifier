package me.grothgar.spotifynotifier.subcommands;

import me.grothgar.spotifynotifier.ISubCommand;
import me.grothgar.spotifynotifier.TheEngine;

public class CheckReleases implements ISubCommand {

    private final TheEngine theEngine;

    public CheckReleases(TheEngine theEngine) {
        this.theEngine = theEngine;
    }

    @Override
    public void execute(String[] args) {

        switch (args.length) {
            case 0 -> theEngine.checkForNewReleases(false);

            case 1 -> theEngine.checkForNewReleases(args[0].equalsIgnoreCase("quiet"));

            default -> System.err.println("Wrong number of arguments (" + args.length + ")");
        }

    }

}
