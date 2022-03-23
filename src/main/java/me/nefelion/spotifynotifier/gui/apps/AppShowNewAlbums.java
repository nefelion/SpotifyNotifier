package me.nefelion.spotifynotifier.gui.apps;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import me.nefelion.spotifynotifier.ReleasesProcessor;
import me.nefelion.spotifynotifier.data.TempData;
import me.nefelion.spotifynotifier.gui.controllers.ControllerProgress;

public class AppShowNewAlbums extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ReleasesProcessor processor = new ReleasesProcessor(TempData.getInstance().getFileData().getFollowedArtists());


        FXMLLoader progressLoader = new FXMLLoader(getClass().getResource("/fxml/G_VBOX__PROGRESS.fxml"));
        Parent progress = progressLoader.load();
        ControllerProgress progressController = progressLoader.getController();

        Stage progressStage = new Stage(StageStyle.UTILITY);
        progressStage.setAlwaysOnTop(true);
        progressStage.setResizable(false);
        progressStage.setTitle("Checking for new releases...");
        progressStage.setScene(new Scene(progress));
        progressStage.show();

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

            if (processor.getNewAlbums().isEmpty()) {
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

    private void startTheApp(Stage primaryStage, ReleasesProcessor processor) {
        primaryStage.setResizable(false);
        AppShowAlbums.start(primaryStage, processor.getNewAlbums(), processor.getAllAlbums());
    }


}
