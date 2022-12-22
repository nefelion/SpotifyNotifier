package me.nefelion.spotifynotifier.gui;

import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.nefelion.spotifynotifier.Main;
import me.nefelion.spotifynotifier.settings.DiscordMessageSettings;

import java.util.Objects;

public class DiscordMessageConfigDialog extends Dialog<ButtonType> {

    private final DiscordMessageSettings discordMessagesSettings;
    private TextArea textAreaTemplateAlbum, textAreaTemplateAlbumSpecificDay;
    private TextField textFieldTodayReleasesPrefix, textFieldTomorrowReleasesPrefix;

    public DiscordMessageConfigDialog(DiscordMessageSettings settings) {
        this.discordMessagesSettings = settings;
        setIcon();
        setTitle("Discord Message Config");
        setHeaderText("Here you can customise your Discord messages:");
        setButtons();
        getDialogPane().setContent(getMainVBOX());
    }

    private VBox getMainVBOX() {
        VBox vbox = new VBox();
        vbox.setSpacing(15);

        vbox.getChildren().addAll(
                getAlbumVBOX(),
                getSeparator(),
                getSpecificDayVBOX()
        );
        return vbox;
    }

    private VBox getAlbumVBOX() {
        VBox vbox = new VBox();

        Label eachAlbumFormatting = new Label("Album formatting:");
        textAreaTemplateAlbum = new TextArea(discordMessagesSettings.getTemplateAlbum());
        textAreaTemplateAlbum.setPrefRowCount(5);

        vbox.setSpacing(5);
        vbox.getChildren().addAll(eachAlbumFormatting, textAreaTemplateAlbum);
        return vbox;
    }

    private Node getSpecificDayVBOX() {
        VBox vbox = new VBox();

        Label todayPrefix = new Label("Today's releases prefix:");
        textFieldTodayReleasesPrefix = new TextField(discordMessagesSettings.getPrefixLineToday());

        Label tomorrowPrefix = new Label("Tomorrow's releases prefix:");
        textFieldTomorrowReleasesPrefix = new TextField(discordMessagesSettings.getPrefixLineTomorrow());

        Label eachAlbumFormatting = new Label("Today's/tomorrow's releases album formatting:");
        textAreaTemplateAlbumSpecificDay = new TextArea(discordMessagesSettings.getTemplateAlbumSpecificDay());
        textAreaTemplateAlbumSpecificDay.setPrefRowCount(5);

        vbox.setSpacing(5);
        vbox.getChildren().addAll(
                todayPrefix, textFieldTodayReleasesPrefix, getSeparator(),
                tomorrowPrefix, textFieldTomorrowReleasesPrefix, getSeparator(),
                eachAlbumFormatting, textAreaTemplateAlbumSpecificDay);
        return vbox;
    }

    private static Separator getSeparator() {
        Separator separator = new Separator();
        separator.setPrefWidth(200);
        separator.setPrefHeight(10);
        separator.setOrientation(Orientation.HORIZONTAL);
        return separator;
    }

    private void setButtons() {
        ButtonType buttonTypeSave = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeRestoreDefaults = new ButtonType("RESTORE DEFAULTS", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType buttonTypeCancel = new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(buttonTypeSave, buttonTypeRestoreDefaults, buttonTypeCancel);

        getDialogPane().lookupButton(buttonTypeRestoreDefaults).addEventFilter(ActionEvent.ACTION, event -> {
            restoreDefaults();
            event.consume();
        });
        getDialogPane().lookupButton(buttonTypeSave).addEventFilter(ActionEvent.ACTION, event -> save());
    }

    private void restoreDefaults() {
        textAreaTemplateAlbum.setText(DiscordMessageSettings.DEFAULT_TEMPLATE_ALBUM);
        textAreaTemplateAlbumSpecificDay.setText(DiscordMessageSettings.DEFAULT_TEMPLATE_ALBUM_SPECIFIC_DAY);
        textFieldTodayReleasesPrefix.setText(DiscordMessageSettings.DEFAULT_PREFIX_LINE_TODAY);
        textFieldTomorrowReleasesPrefix.setText(DiscordMessageSettings.DEFAULT_PREFIX_LINE_TOMORROW);
    }

    private void save() {
        discordMessagesSettings.setTemplateAlbum(textAreaTemplateAlbum.getText());
        discordMessagesSettings.setTemplateAlbumSpecificDay(textAreaTemplateAlbumSpecificDay.getText());
        discordMessagesSettings.setPrefixLineToday(textFieldTodayReleasesPrefix.getText());
        discordMessagesSettings.setPrefixLineTomorrow(textFieldTomorrowReleasesPrefix.getText());
    }

    private void setIcon() {
        ((Stage) (getDialogPane().getScene().getWindow())).getIcons()
                .add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/images/icon.png"))));
    }
}
