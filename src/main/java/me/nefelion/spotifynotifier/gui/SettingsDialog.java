package me.nefelion.spotifynotifier.gui;

import com.neovisionaries.i18n.CountryCode;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.nefelion.spotifynotifier.Main;
import me.nefelion.spotifynotifier.data.FileData;
import me.nefelion.spotifynotifier.data.FileManager;
import me.nefelion.spotifynotifier.data.TempData;

import java.util.Objects;

public class SettingsDialog extends Dialog<ButtonType> {

    private ButtonType buttonTypeSave;
    private TextField textFieldCountryCode;
    private CheckBox checkBoxIgnoreVarious;

    public SettingsDialog() {
        setIcon();
        setTitle("Settings");
        setHeaderText("Settings");
        setContentText("Please choose your settings:");
        setButtons();
        getDialogPane().setContent(getVBox());
    }

    private VBox getVBox() {
        VBox vbox = new VBox();

        Label labelCountry = new Label("Country:");
        textFieldCountryCode = new TextField();
        textFieldCountryCode.setText(TempData.getInstance().getFileData().getCountryCode().getName());
        textFieldCountryCode.setPromptText("Country");
        textFieldCountryCode.setPrefWidth(200);
        textFieldCountryCode.setStyle("-fx-background-color: #00FF00; -fx-text-fill: black; -fx-border-color: black;");
        textFieldCountryCode.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z ]*")) {
                textFieldCountryCode.setText(newValue.replaceAll("[^a-zA-Z ]", ""));
            }
        });
        textFieldCountryCode.setOnKeyTyped(e -> {
            if (CountryCode.findByName(textFieldCountryCode.getText()).isEmpty()) {
                getDialogPane().lookupButton(buttonTypeSave).setDisable(true);
                textFieldCountryCode.setStyle("-fx-background-color: #FF0000; -fx-text-fill: black; -fx-border-color: black;");
            } else {
                getDialogPane().lookupButton(buttonTypeSave).setDisable(false);
                textFieldCountryCode.setStyle("-fx-background-color: #00FF00; -fx-text-fill: black; -fx-border-color: black;");
            }
        });

        // add small text for description
        Label labelCountryDescription = new Label("""
                Using the 'Remind' function will remind you of an album when it appears in the country you specify here.
                """);
        labelCountryDescription.setStyle("-fx-font-size: 10; -fx-text-fill: gray;");
        labelCountryDescription.setWrapText(true);
        labelCountryDescription.setPrefWidth(200);
        labelCountryDescription.setPrefHeight(50);


        Separator separator = new Separator();
        separator.setPrefWidth(200);
        separator.setPrefHeight(10);
        separator.setOrientation(Orientation.HORIZONTAL);

        checkBoxIgnoreVarious = new CheckBox("Ignore 'Various Artists' releases");
        checkBoxIgnoreVarious.setSelected(TempData.getInstance().getFileData().isIgnoreVariousArtists());

        vbox.getChildren().addAll(new VBox(labelCountry, textFieldCountryCode, labelCountryDescription), separator, checkBoxIgnoreVarious);
        vbox.setSpacing(15);
        return vbox;
    }

    private void setButtons() {
        buttonTypeSave = new ButtonType("SAVE", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(buttonTypeSave, buttonTypeCancel);

        getDialogPane().lookupButton(buttonTypeSave).addEventFilter(ActionEvent.ACTION, event -> save());
    }

    private void save() {
        FileData fd = TempData.getInstance().getFileData();

        if (!CountryCode.findByName(textFieldCountryCode.getText()).isEmpty())
            fd.setCountryCodeNumeric(CountryCode.findByName(textFieldCountryCode.getText()).get(0).getNumeric());

        fd.setIgnoreVariousArtists(checkBoxIgnoreVarious.isSelected());

        FileManager.saveFileData(fd);
    }

    private void setIcon() {
        ((Stage) (getDialogPane().getScene().getWindow())).getIcons()
                .add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/images/icon.png"))));
    }
}
