package me.nefelion.spotifynotifier.gui;

import com.neovisionaries.i18n.CountryCode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.nefelion.spotifynotifier.Main;
import me.nefelion.spotifynotifier.data.TempData;

import java.util.Objects;

public class SettingsDialog extends Dialog<ButtonType> {

    private ButtonType buttonTypeSave;

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
        TextField textFieldCountryCode = new TextField();
        textFieldCountryCode.setText(TempData.getInstance().getFileData().getCountryCode().getName());
        TempData.getInstance().setTypedCountry(textFieldCountryCode.getText());
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
                TempData.getInstance().setTypedCountry(textFieldCountryCode.getText());
                getDialogPane().lookupButton(buttonTypeSave).setDisable(false);
                textFieldCountryCode.setStyle("-fx-background-color: #00FF00; -fx-text-fill: black; -fx-border-color: black;");
            }
        });


        vbox.getChildren().addAll(labelCountry, textFieldCountryCode);
        return vbox;
    }

    private void setButtons() {
        buttonTypeSave = new ButtonType("SAVE", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(buttonTypeSave, buttonTypeCancel);
    }

    private void setIcon() {
        ((Stage) (getDialogPane().getScene().getWindow())).getIcons()
                .add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/images/icon.png"))));
    }
}
