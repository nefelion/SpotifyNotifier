package me.nefelion.spotifynotifier.gui.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import me.nefelion.spotifynotifier.Main;
import me.nefelion.spotifynotifier.ReleasesProcessor;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class ControllerOutline {

    private static ControllerOutline instance;

    @FXML
    private TabPane GTabPane;
    @FXML
    private AnchorPane GFollowedAnchorPane, GAlbumAnchorPane, GSettingsAnchorPane;
    @FXML
    private Label GLabelVersionNumber;
    @FXML
    private Button GButtonDownloadUpdate;

    private ControllerAlbums controllerAlbums;


    public ControllerOutline() {
        instance = this;
    }

    public static ControllerOutline getInstance() {
        return instance;
    }

    public void setControllerAlbums(ControllerAlbums controllerAlbums) {
        this.controllerAlbums = controllerAlbums;
    }

    @FXML
    private void initialize() {
        GTabPane.getTabs().get(TAB.ALBUMS.ordinal()).setDisable(true);
        GTabPane.getTabs().get(TAB.SETTINGS.ordinal()).setDisable(true);
        GLabelVersionNumber.setText(Main.getFullVersion());

        GButtonDownloadUpdate.setOnAction(e -> {
            Runtime rt = Runtime.getRuntime();
            String url = "https://dl.dropbox.com/s/t2yndvh2xqo074d/SpotifyNotifier.jar";
            try {
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        checkForUpdates();
    }

    private void checkForUpdates() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                if (isUpdateAvailable()) Platform.runLater(ControllerOutline.this::setLabelForUpdate);
                return null;
            }
        };
        new Thread(task).start();
    }

    public void setFollowedVBOX(VBox vbox) {
        GFollowedAnchorPane.getChildren().clear();
        GFollowedAnchorPane.getChildren().add(vbox);
    }

    public VBox getAlbumsVBOX() {
        return (VBox) (GAlbumAnchorPane.getChildren().isEmpty() ? null : GAlbumAnchorPane.getChildren().get(0));
    }

    public void setAlbumsVBOX(VBox vbox) {
        GTabPane.getTabs().get(TAB.ALBUMS.ordinal()).setDisable(false);
        GAlbumAnchorPane.getChildren().clear();
        GAlbumAnchorPane.getChildren().add(vbox);
    }

    public void setSettingsVBOX(VBox vbox) {
        GSettingsAnchorPane.getChildren().clear();
        GSettingsAnchorPane.getChildren().add(vbox);
    }

    public void selectTab(TAB tab) {
        GTabPane.getSelectionModel().select(tab.ordinal());
    }

    public void showAlbums(String title, ReleasesProcessor processor) {
        controllerAlbums.showReleases(title, processor);
    }

    private boolean isUpdateAvailable() {
        URL url;
        try {
            url = new URL("https://dl.dropbox.com/s/4duqne8r4mvwadn/spotifynotifier.ver");
            Scanner s = new Scanner(url.openStream());
            int onlineBuildNumber = Integer.parseInt(s.nextLine());
            if (onlineBuildNumber > Main.getBuildNumber()) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void setLabelForUpdate() {
        GLabelVersionNumber.setVisible(false);
        GButtonDownloadUpdate.setVisible(true);
    }

    public void setAlbumsTitle(String s) {
        GTabPane.getTabs().get(TAB.ALBUMS.ordinal()).setText(s);
    }

    public String getAlbumsTitle() {
        return GTabPane.getTabs().get(TAB.ALBUMS.ordinal()).getText();
    }

    public enum TAB {
        FOLLOWED,
        ALBUMS,
        SETTINGS
    }

}
