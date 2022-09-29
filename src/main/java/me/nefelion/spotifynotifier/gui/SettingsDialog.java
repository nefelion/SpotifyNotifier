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
    private Slider sliderExploreIterations;

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
                getExploreIterationsVBOX(),
                getSeparator(),
                getButtonResetCredentials());
        return vbox;
    }

    private VBox getExploreIterationsVBOX() {
        VBox vbox = new VBox();
        vbox.setSpacing(5);

        Tooltip tooltip = new Tooltip("""
                How deep should the explore algorithm go?
                1 = only artists similar to followed artists
                2 = also artists similar to artists similar to followed artists
                """);
        tooltip.setShowDelay(javafx.util.Duration.ZERO);
        tooltip.setStyle("-fx-font-size: 14px;");

        Label label = new Label("Explore depth:");
        label.setTooltip(tooltip);
        label.setWrapText(true);

        sliderExploreIterations = new Slider(1, 2, TempData.getInstance().getFileData().getExploreIterations());
        sliderExploreIterations.setShowTickLabels(true);
        sliderExploreIterations.setShowTickMarks(true);
        sliderExploreIterations.setMajorTickUnit(1);
        sliderExploreIterations.setMinorTickCount(0);
        sliderExploreIterations.setBlockIncrement(1);
        sliderExploreIterations.setSnapToTicks(true);
        sliderExploreIterations.setTooltip(tooltip);

        vbox.getChildren().addAll(label, sliderExploreIterations);
        return vbox;
    }

    private Button getButtonResetCredentials() {
        Button button = new Button("Reset credentials and exit");
        button.setOnAction(this::resetCredentials);
        button.setStyle("-fx-text-fill: red;");


        Tooltip tooltip = new Tooltip("This option will delete your token details from the application and you will have to re-enter them.");
        tooltip.setShowDelay(javafx.util.Duration.ZERO);
        tooltip.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
        button.setTooltip(tooltip);
        return button;
    }

    private void resetCredentials(ActionEvent actionEvent) {
        ClientManager.resetCredentials();
        System.exit(0);
    }

    private VBox getCountryVBOX() {
        Label labelCountry = new Label("Country:");
        initializeTextFieldCountryCode();

        checkBoxOnlyAvailable = new CheckBox("Show only albums already available in this country");
        checkBoxOnlyAvailable.setSelected(TempData.getInstance().getFileData().isShowOnlyAvailable());

        VBox vBox = new VBox(labelCountry, textFieldCountryCode, checkBoxOnlyAvailable);
        vBox.setSpacing(5);

        Tooltip tooltip = new Tooltip("""
                Setting a country affects the Reminder functionality.
                With Reminder, you can select an album and it will reappear in New Releases when it is available in that country.
                """);
        tooltip.setShowDelay(javafx.util.Duration.ZERO);
        tooltip.setStyle("-fx-font-size: 14px;");
        Tooltip.install(vBox, tooltip);

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
        fd.setExploreIterations((int) sliderExploreIterations.getValue());

        FileManager.saveFileData(fd);
    }

    private void setIcon() {
        ((Stage) (getDialogPane().getScene().getWindow())).getIcons()
                .add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/images/icon.png"))));
    }
}
