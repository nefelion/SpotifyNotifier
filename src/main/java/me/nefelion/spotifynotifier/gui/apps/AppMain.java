package me.nefelion.spotifynotifier.gui.apps;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import me.nefelion.spotifynotifier.Main;
import me.nefelion.spotifynotifier.gui.controllers.ControllerAlbums;
import me.nefelion.spotifynotifier.gui.controllers.ControllerFollowed;
import me.nefelion.spotifynotifier.gui.controllers.ControllerOutline;

import java.io.IOException;

public class AppMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
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
        ControllerOutline controller = loader.getController();
        ControllerFollowed controllerFollowed = loaderFollowed.getController();
        ControllerAlbums controllerAlbums = loaderAlbums.getController();

        controllerFollowed.setControllerOutline(controller);
        controllerAlbums.setControllerOutline(controller);
        controller.setControllerAlbums(controllerAlbums);

        primaryStage.setTitle("Spotify Notifier");


        controller.setFollowedVBOX(controllerFollowed.getGMainVBOX());
        controller.selectTab(ControllerOutline.TAB.FOLLOWED);
        Scene home = new Scene(outline);
        primaryStage.setScene(home);
        primaryStage.show();
    }


}
