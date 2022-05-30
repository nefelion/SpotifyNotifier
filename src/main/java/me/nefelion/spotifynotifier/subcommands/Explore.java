package me.nefelion.spotifynotifier.subcommands;

import me.nefelion.spotifynotifier.ISubCommand;

public class Explore implements ISubCommand {

    public Explore() {
    }

    @Override
    public void execute(String[] args) {

        switch (args.length) {
            //case 0 -> theEngine.showRelatedAlbums();

            default -> System.err.println("Wrong number of arguments (" + args.length + ")");
        }

    }
}
