package me.nefelion.spotifynotifier.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.nefelion.spotifynotifier.Main;
import me.nefelion.spotifynotifier.ReleasedAlbum;
import me.nefelion.spotifynotifier.gui.controllers.ControllerAlbums;
import me.nefelion.spotifynotifier.gui.controllers.ControllerFollowed;
import me.nefelion.spotifynotifier.gui.controllers.ControllerOutline;

import java.io.IOException;
import java.util.List;

public class AppShowAlbums {

    private static ControllerOutline controller;

    public static void start(Stage stage, List<ReleasedAlbum> newAlbums, List<ReleasedAlbum> allAlbums) {

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/G_VBOX__OUTLINE.fxml"));
        FXMLLoader loaderFollowed = new FXMLLoader(Main.class.getResource("/fxml/G_VBOX__MANAGE_FOLLOWED.fxml"));
        FXMLLoader loaderAlbums = new FXMLLoader(Main.class.getResource("/fxml/G_VBOX__SHOW_RESULTS.fxml"));

        Parent outline;
        try {
            outline = loader.load();
            loaderFollowed.load();
            loaderAlbums.load();
        } catch (IOException e) {
            System.exit(1);
            return;
        }
        controller = loader.getController();
        ControllerFollowed controllerFollowed = loaderFollowed.getController();
        controllerFollowed.setControllerOutline(controller);
        ControllerAlbums controllerAlbums = loaderAlbums.getController();
        controllerAlbums.setControllerOutline(controller);


        controllerAlbums.setNewAlbums(newAlbums);
        controllerAlbums.setAllAlbums(allAlbums);

        stage.setTitle("Title");

        controller.setAlbumsVBOX(controllerAlbums.getGMainVBOX());
        controller.setFollowedVBOX(controllerFollowed.getGMainVBOX());
        controller.selectTab(ControllerOutline.TAB.ALBUMS);
        Scene home = new Scene(outline);
        stage.setScene(home);
        stage.show();
    }


    public static VBox getAlbumsVBOX(List<ReleasedAlbum> newAlbums, List<ReleasedAlbum> allAlbums) {
        FXMLLoader loaderAlbums = new FXMLLoader(Main.class.getResource("/fxml/G_VBOX__SHOW_RESULTS.fxml"));
        try {
            loaderAlbums.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ControllerAlbums controllerAlbums = loaderAlbums.getController();
        controllerAlbums.setNewAlbums(newAlbums);
        controllerAlbums.setAllAlbums(allAlbums);
        controllerAlbums.setControllerOutline(controller);
        return controllerAlbums.getGMainVBOX();
    }


}
