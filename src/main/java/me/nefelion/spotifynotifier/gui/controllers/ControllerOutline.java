package me.nefelion.spotifynotifier.gui.controllers;

import com.neovisionaries.i18n.CountryCode;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import me.nefelion.spotifynotifier.Main;
import me.nefelion.spotifynotifier.ReleasesProcessor;
import me.nefelion.spotifynotifier.data.FileData;
import me.nefelion.spotifynotifier.data.FileManager;
import me.nefelion.spotifynotifier.data.TempData;
import me.nefelion.spotifynotifier.gui.SettingsDialog;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;

public class ControllerOutline {

    private static ControllerOutline instance;

    @FXML
    private TabPane GTabPane;
    @FXML
    private AnchorPane GFollowedAnchorPane, GAlbumAnchorPane;
    @FXML
    private Label GLabelVersionNumber;
    @FXML
    private Button GButtonDownloadUpdate, GButtonSettings;

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
        GLabelVersionNumber.setText(Main.getFullVersion());

        GButtonDownloadUpdate.setOnAction(e -> {
            Runtime rt = Runtime.getRuntime();
            String url = "https://dl.dropbox.com/s/nninh41jr0itdlh/spotifynotifier.ver";
            try {
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        setGButtonSettings();

        checkForUpdates();
    }

    private void setGButtonSettings() {
        GButtonSettings.setOnAction(e -> {
            Dialog<ButtonType> dialog = new SettingsDialog();
            dialog.showAndWait();
        });
        Tooltip settings = new Tooltip("Settings");
        settings.setShowDelay(new javafx.util.Duration(0));
        GButtonSettings.setTooltip(settings);
    }

    private void checkForUpdates() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                if (isUpdateAvailable()) Platform.runLater(ControllerOutline.this::showUpdateButton);
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
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

    public void selectTab(TAB tab) {
        GTabPane.getSelectionModel().select(tab.ordinal());
    }

    public void showAlbums(String title, ReleasesProcessor processor) {
        controllerAlbums.showReleases(title, processor);
    }

    private boolean isUpdateAvailable() {
        URL url;
        try {
            url = new URL("https://dl.dropbox.com/s/nninh41jr0itdlh/spotifynotifier.ver");
            Scanner s = new Scanner(url.openStream());
            int onlineBuildNumber = Integer.parseInt(s.nextLine());
            if (onlineBuildNumber > Main.getBuildNumber()) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void showUpdateButton() {
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
        ALBUMS
    }

}
