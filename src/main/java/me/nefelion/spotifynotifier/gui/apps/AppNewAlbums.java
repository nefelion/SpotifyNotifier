package me.nefelion.spotifynotifier.gui.apps;

import javafx.application.Application;
import javafx.stage.Stage;
import me.nefelion.spotifynotifier.gui.apps.util.UtilAlbums;

public class AppNewAlbums extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        UtilAlbums.process(primaryStage, true, false);
    }

}
