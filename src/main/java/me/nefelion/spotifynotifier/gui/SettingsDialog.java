package me.nefelion.spotifynotifier.gui;

import com.neovisionaries.i18n.CountryCode;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.nefelion.spotifynotifier.ClientManager;
import me.nefelion.spotifynotifier.Main;
import me.nefelion.spotifynotifier.data.FileData;
import me.nefelion.spotifynotifier.data.FileManager;
import me.nefelion.spotifynotifier.data.TempData;

import java.util.Objects;

public class SettingsDialog extends Dialog<ButtonType> {

    private ButtonType buttonTypeSave;
    private TextField textFieldCountryCode;
    private CheckBox checkBoxIgnoreVarious, checkBoxOnlyAvailable, checkBoxIgnoreNotWorldwide;

    public SettingsDialog() {
        setIcon();
        setTitle("Settings");
        setHeaderText("Settings");
        setContentText("Please choose your settings:");
        setButtons();
        getDialogPane().setContent(getMainVBOX());
    }

    private VBox getMainVBOX() {
        VBox vbox = new VBox();
        vbox.setSpacing(15);

        checkBoxIgnoreVarious = new CheckBox("Ignore 'Various Artists' releases");
        checkBoxIgnoreVarious.setSelected(TempData.getInstance().getFileData().isIgnoreVariousArtists());

        checkBoxIgnoreNotWorldwide = new CheckBox("Ignore not worldwide releases");
        checkBoxIgnoreNotWorldwide.setSelected(TempData.getInstance().getFileData().isIgnoreNotWorldwide());

        vbox.getChildren().addAll(
                getCountryVBOX(), getSeparator(), checkBoxIgnoreVarious, checkBoxIgnoreNotWorldwide,
                getSeparator(),
                getButtonResetCredentials());
        return vbox;
    }

    private Button getButtonResetCredentials() {
        Button button = new Button("Reset credentials and exit");
        button.setOnAction(this::resetCredentials);
        button.setStyle("-fx-text-fill: red;");
        return button;
    }

    private void resetCredentials(ActionEvent actionEvent) {
        ClientManager.resetCredentials();
        System.exit(0);
    }

    private VBox getCountryVBOX() {
        Label labelCountry = new Label("Country:");
        initializeTextFieldCountryCode();

        checkBoxOnlyAvailable = new CheckBox("Show only albums already available in my country");
        checkBoxOnlyAvailable.setSelected(TempData.getInstance().getFileData().isShowOnlyAvailable());

        VBox vBox = new VBox(labelCountry, textFieldCountryCode, checkBoxOnlyAvailable);
        vBox.setSpacing(5);
        return vBox;
    }

    private static Separator getSeparator() {
        Separator separator = new Separator();
        separator.setPrefWidth(200);
        separator.setPrefHeight(10);
        separator.setOrientation(Orientation.HORIZONTAL);
        return separator;
    }

    private void initializeTextFieldCountryCode() {
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
        fd.setShowOnlyAvailable(checkBoxOnlyAvailable.isSelected());
        fd.setIgnoreNotWorldwide(checkBoxIgnoreNotWorldwide.isSelected());

        FileManager.saveFileData(fd);
    }

    private void setIcon() {
        ((Stage) (getDialogPane().getScene().getWindow())).getIcons()
                .add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/images/icon.png"))));
    }
}
