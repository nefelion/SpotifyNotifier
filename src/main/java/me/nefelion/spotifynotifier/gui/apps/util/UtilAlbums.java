package me.nefelion.spotifynotifier.gui.apps.util;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.nefelion.spotifynotifier.FollowedArtist;
import me.nefelion.spotifynotifier.ReleasesProcessor;
import me.nefelion.spotifynotifier.Utilities;
import me.nefelion.spotifynotifier.data.FileData;
import me.nefelion.spotifynotifier.data.FileManager;
import me.nefelion.spotifynotifier.data.TempData;
import me.nefelion.spotifynotifier.gui.controllers.ControllerProgress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UtilAlbums {

    public static void process(Stage primaryStage, boolean newOnly, boolean quiet) throws IOException {
        List<FollowedArtist> artists = new ArrayList<>(TempData.getInstance().getFileData().getFollowedArtists().stream()
                .sorted(Comparator.comparing(FollowedArtist::getName)).toList());
        ReleasesProcessor processor = new ReleasesProcessor(artists);


        FXMLLoader progressLoader = new FXMLLoader(UtilAlbums.class.getResource("/fxml/G_VBOX__PROGRESS.fxml"));
        Parent progress = progressLoader.load();
        ControllerProgress progressController = progressLoader.getController();


        Stage progressStage = new Stage(StageStyle.UTILITY);
        if (!quiet) {
            progressStage.setAlwaysOnTop(true);
            progressStage.setResizable(false);
            progressStage.setTitle("Downloading album data...");
            progressStage.setScene(new Scene(progress));
            progressStage.show();
        }

        // set background color to green


        Task<Boolean> task = new Task<>() {
            @Override
            public Boolean call() {
                processor.currentArtistConsumer(progressController::setInfo);
                processor.progressConsumer(progressController::setProgress);
                processor.process();
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            FileData fileData = TempData.getInstance().getFileData();
            fileData.setLastChecked(Utilities.now());
            FileManager.saveFileData(fileData);
            progressStage.close();

            if (quiet && processor.getNewAlbums().isEmpty()) System.exit(0);

            if (newOnly && processor.getNewAlbums().isEmpty()) {
                ButtonType closeButton = new ButtonType("Close");
                ButtonType anywayButton = new ButtonType("Show anyway");
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "No new releases", closeButton, anywayButton);
                Utilities.showAlert(alert);
                if (alert.getResult() == anywayButton) startTheApp(primaryStage, processor);
            } else startTheApp(primaryStage, processor);


        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }


    private static void startTheApp(Stage primaryStage, ReleasesProcessor processor) {
        primaryStage.setResizable(false);
        UtilShowAlbums.start(primaryStage, processor.getNewAlbums(), processor.getAllAlbums());
    }


}
