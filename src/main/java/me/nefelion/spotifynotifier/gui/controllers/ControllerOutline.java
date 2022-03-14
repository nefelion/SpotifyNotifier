package me.nefelion.spotifynotifier.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class ControllerOutline {

    @FXML
    private TabPane GTabPane;
    @FXML
    private AnchorPane GFollowedAnchorPane, GAlbumAnchorPane, GSettingsAnchorPane;

    public enum TAB {
        FOLLOWED,
        ALBUMS,
        SETTINGS
    }

    public void setFollowedVBOX(VBox vbox) {
        GFollowedAnchorPane.getChildren().clear();
        GFollowedAnchorPane.getChildren().add(vbox);
    }

    public void setAlbumsVBOX(VBox vbox) {
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

}
