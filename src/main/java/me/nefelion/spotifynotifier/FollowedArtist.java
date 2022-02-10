package me.nefelion.spotifynotifier;

public class FollowedArtist {
    private final String name;
    private final String ID;


    public FollowedArtist(String name, String ID) {
        this.name = name;
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return ID;
    }
}
