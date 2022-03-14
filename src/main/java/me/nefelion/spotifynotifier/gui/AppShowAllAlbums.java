package me.nefelion.spotifynotifier.gui;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.nefelion.spotifynotifier.ReleasesProcessor;
import me.nefelion.spotifynotifier.gui.controllers.ControllerProgress;
import me.nefelion.spotifynotifier.data.TempData;

public class AppShowAllAlbums extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ReleasesProcessor processor = new ReleasesProcessor(TempData.getInstance().getFileData().getFollowedArtists().get(15));


        FXMLLoader progressLoader = new FXMLLoader(getClass().getResource("/fxml/G_VBOX__PROGRESS.fxml"));
        Parent progress = progressLoader.load();
        ControllerProgress progressController = progressLoader.getController();

        Stage progressStage = new Stage(StageStyle.UTILITY);
        progressStage.setAlwaysOnTop(true);
        progressStage.setResizable(false);
        progressStage.setTitle("Downloading album data...");
        progressStage.setScene(new Scene(progress));
        progressStage.show();

        Task<Boolean> task = new Task<>() {
            @Override
            public Boolean call() {
                processor.process(progressController::setProgress, progressController::setInfo);
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            progressStage.close();
            primaryStage.setResizable(false);
            AppShowAlbums.start(primaryStage, processor.getNewAlbums(), processor.getAllAlbums());
        });
        new Thread(task).start();
    }


}
