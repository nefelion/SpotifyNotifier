package me.nefelion.spotifynotifier.gui;

import com.neovisionaries.i18n.CountryCode;
import javafx.geometry.Orientation;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.nefelion.spotifynotifier.Main;
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
        Label albumNameLabel = new Label("Available markets:");
        albumNameLabel.setStyle("-fx-font-weight: bold;");

        vbox.getChildren().addAll(
                albumNameLabel, getAvailableMarketsScrollPane()
        );
        return vbox;
    }

    private ScrollPane getAvailableMarketsScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        Label albumNameTextField = new Label();
        albumNameTextField.setText(String.join("\n", Arrays.stream(album.getAvailableMarkets()).map(CountryCode::getAlpha2).toArray(String[]::new)));
        scrollPane.setContent(albumNameTextField);
        scrollPane.setPrefViewportHeight(100);
        scrollPane.setPrefViewportWidth(200);
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
