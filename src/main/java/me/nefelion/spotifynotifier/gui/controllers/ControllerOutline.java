package me.nefelion.spotifynotifier.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import me.nefelion.spotifynotifier.ReleasesProcessor;

public class ControllerOutline {

    private static ControllerOutline instance;

    @FXML
    private TabPane GTabPane;
    @FXML
    private AnchorPane GFollowedAnchorPane, GAlbumAnchorPane, GSettingsAnchorPane;
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

    public void showAlbums(ReleasesProcessor processor) {
        controllerAlbums.showReleases(processor);
    }


    public enum TAB {
        FOLLOWED,
        ALBUMS,
        SETTINGS
    }

}
