package me.nefelion.spotifynotifier.TESTING_JAVAFX;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import me.nefelion.spotifynotifier.ReleasedAlbum;
import me.nefelion.spotifynotifier.ReleasesProcessor;
import me.nefelion.spotifynotifier.TESTING_JAVAFX.controllers.ControllerAlbums;
import me.nefelion.spotifynotifier.TempData;

import java.util.List;

public class Test extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/G_VBOX__SHOW_RESULTS.fxml"));
        Parent homeRoot = loader.load();
        ControllerAlbums controller = loader.getController();

        ReleasesProcessor processor = new ReleasesProcessor(TempData.getInstance().getFileData().getFollowedArtists().get(9));
        processor.processOnlyNewReleases(false);
        processor.setProgressBarVisible(true);
        processor.process();

        List<ReleasedAlbum> releasedAlbums = processor.getAlbums();


        controller.setOldAlbums(releasedAlbums);
        controller.setNewAlbums(releasedAlbums);




        Scene home = new Scene(homeRoot);
        primaryStage.setScene(home);
        primaryStage.show();
    }
}
