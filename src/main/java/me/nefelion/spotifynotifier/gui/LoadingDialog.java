package me.nefelion.spotifynotifier.gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.nefelion.spotifynotifier.Main;

import java.util.Objects;
import java.util.function.Consumer;

public class LoadingDialog extends Dialog<String> {

    private final Label label = new Label();
    private final ProgressBar progressBar = new ProgressBar();
    private final Consumer<Double> progressConsumer = progressBar::setProgress;
    private final Consumer<String> labelConsumer = label::setText;
    private Runnable cancelListener = this::close;

    public LoadingDialog() {

        ((Stage) (getDialogPane().getScene().getWindow())).getIcons()
                .add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/images/icon.png"))));


        setResizable(false);
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        setOnCloseRequest(event -> cancelListener.run());

        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(30);
        progressBar.setProgress(0);

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(progressBar, label);
        getDialogPane().setContent(vbox);
    }

    public void setProgressText(String s) {
        Platform.runLater(() -> label.setText(s));
    }

    public void setProgress(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    }

    public void feedProgress(double progress) {
        progressConsumer.accept(progress);
    }

    public void feedLabel(String s) {
        labelConsumer.accept(s);
    }

    public Consumer<Double> getProgressConsumer() {
        return progressConsumer;
    }

    public Consumer<String> getLabelConsumer() {
        return labelConsumer;
    }

    public void setCancelListener(Runnable cancelListener) {
        this.cancelListener = cancelListener;
    }
}
