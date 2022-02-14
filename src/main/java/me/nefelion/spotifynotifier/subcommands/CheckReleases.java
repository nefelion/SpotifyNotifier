package me.nefelion.spotifynotifier.subcommands;

import me.nefelion.spotifynotifier.ISubCommand;
import me.nefelion.spotifynotifier.TheEngine;

public class CheckReleases implements ISubCommand {

    private final TheEngine theEngine = TheEngine.getInstance();

    public CheckReleases() {
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
