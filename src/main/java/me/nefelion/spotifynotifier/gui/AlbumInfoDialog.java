package me.nefelion.spotifynotifier.gui;

import api.deezer.objects.Album;
import com.neovisionaries.i18n.CountryCode;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.nefelion.spotifynotifier.Main;
import me.nefelion.spotifynotifier.Utilities;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.util.Arrays;
import java.util.Objects;

public class AlbumInfoDialog extends Dialog<String> {

    private final AlbumSimplified album;

    public AlbumInfoDialog(AlbumSimplified album) {
        this.album = album;
        setIcon();
        setTitle("Info");
        setHeaderText(album.getName());
        getDialogPane().setContent(getMainVBOX());

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(event -> close());
    }

    private VBox getMainVBOX() {
        VBox vbox = new VBox();
        vbox.setSpacing(15);
        Label albumNameLabel = new Label("Available Spotify markets:");
        albumNameLabel.setStyle("-fx-font-weight: bold;");

        vbox.getChildren().addAll(albumNameLabel, getAvailableMarketsScrollPane());


        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                Album deezerAlbum = Utilities.findDeezerRelease(album);
                if (deezerAlbum == null) return null;
                Platform.runLater(() -> {
                    vbox.getChildren().addAll(getSeparator(), getDeezerInfoVBox(deezerAlbum));
                    getDialogPane().getScene().getWindow().sizeToScene();
                });
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        return vbox;
    }

    private VBox getDeezerInfoVBox(Album deezerAlbum) {
        HBox hbox = new HBox();

        Label albumName = new Label(deezerAlbum.getTitle());
        Label albumArtist = new Label(deezerAlbum.getArtist().getName());
        Label albumInfo = new Label(deezerAlbum.getNbTracks() == null ? "New Album!" : deezerAlbum.getNbTracks() + " tracks");
        Button copyLinkButton = new Button("Copy link");
        copyLinkButton.setOnAction(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(deezerAlbum.getLink());
            clipboard.setContent(content);
            copyLinkButton.setText("Copied!");
            copyLinkButton.setDisable(true);
        });

        ImageView albumImage = new ImageView(new Image(deezerAlbum.getCoverMedium()));
        hbox.getChildren().addAll(
                albumImage,
                new VBox(albumName, albumArtist, albumInfo, copyLinkButton)
        );


        Label deezerLabel = new Label("Deezer info:");
        deezerLabel.setStyle("-fx-font-weight: bold;");
        VBox vBox = new VBox(deezerLabel, hbox);
        vBox.setSpacing(15);
        return vBox;
    }

    private ScrollPane getAvailableMarketsScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        Label albumNameTextField = new Label();
        String[] markets = Arrays.stream(album.getAvailableMarkets()).map(CountryCode::getName).sorted().toArray(String[]::new);
        albumNameTextField.setText(String.join("\n", markets));
        scrollPane.setContent(albumNameTextField);
        scrollPane.setPrefViewportHeight(100);
        scrollPane.setPrefViewportWidth(200);

        // add right click menu
        albumNameTextField.setOnContextMenuRequested(event -> {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem copyMenuItem = new MenuItem("Copy");
            copyMenuItem.setOnAction(event1 -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(albumNameTextField.getText());
                clipboard.setContent(content);
            });
            contextMenu.getItems().add(copyMenuItem);
            contextMenu.show(albumNameTextField, event.getScreenX(), event.getScreenY());
        });

        return scrollPane;
    }

    private static Separator getSeparator() {
        Separator separator = new Separator();
        separator.setPrefWidth(200);
        separator.setPrefHeight(10);
        separator.setOrientation(Orientation.HORIZONTAL);
        return separator;
    }

    private void setIcon() {
        ((Stage) (getDialogPane().getScene().getWindow())).getIcons()
                .add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/images/icon.png"))));
    }

}
