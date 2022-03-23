package me.nefelion.spotifynotifier.gui.apps.util;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import me.nefelion.spotifynotifier.ReleasesProcessor;
import me.nefelion.spotifynotifier.data.TempData;
import me.nefelion.spotifynotifier.gui.controllers.ControllerProgress;

import java.io.IOException;

public class UtilAlbums {

    public static void process(Stage primaryStage, boolean newOnly, boolean quiet) throws IOException {
        ReleasesProcessor processor = new ReleasesProcessor(TempData.getInstance().getFileData().getFollowedArtists());


        FXMLLoader progressLoader = new FXMLLoader(UtilAlbums.class.getResource("/fxml/G_VBOX__PROGRESS.fxml"));
        Parent progress = progressLoader.load();
        ControllerProgress progressController = progressLoader.getController();

        Stage progressStage = new Stage(StageStyle.UTILITY);
        progressStage.setAlwaysOnTop(true);
        progressStage.setResizable(false);
        progressStage.setTitle("Downloading album data...");
        progressStage.setScene(new Scene(progress));
        if (!quiet) progressStage.show();

        Task<Boolean> task = new Task<>() {
            @Override
            public Boolean call() {
                processor.setCurrentArtistConsumer(progressController::setInfo);
                processor.setProgressConsumer(progressController::setProgress);
                processor.process();
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            progressStage.close();

            if (!quiet && newOnly && processor.getNewAlbums().isEmpty()) {
                ButtonType closeButton = new ButtonType("Close");
                ButtonType anywayButton = new ButtonType("Show anyway");
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "No new releases", closeButton, anywayButton);
                Window window = alert.getDialogPane().getScene().getWindow();
                window.setOnCloseRequest(event -> window.hide());
                alert.setTitle("");
                alert.setHeaderText(null);
                alert.showAndWait();
                if (alert.getResult() == anywayButton) startTheApp(primaryStage, processor);
            } else startTheApp(primaryStage, processor);


        });
        new Thread(task).start();
    }


    private static void startTheApp(Stage primaryStage, ReleasesProcessor processor) {
        primaryStage.setResizable(false);
        UtilShowAlbums.start(primaryStage, processor.getNewAlbums(), processor.getAllAlbums());
    }


}
