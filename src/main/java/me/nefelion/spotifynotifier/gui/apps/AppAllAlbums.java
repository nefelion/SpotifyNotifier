package me.nefelion.spotifynotifier.gui.apps;

import javafx.application.Application;
import javafx.stage.Stage;

public class AppAllAlbums extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppAlbums.start(primaryStage, false);
    }


}
