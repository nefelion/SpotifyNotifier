package me.nefelion.spotifynotifier.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;

public class ControllerProgress {

    @FXML
    private ProgressBar GProgressBar;
    @FXML
    private Text GTextPercentage, GTextInfo;

    @FXML
    private void initialize() {
    }

    public void setProgress(double progress) {
        GProgressBar.setProgress(progress);
        if (progress >= 0) GTextPercentage.setText((int) (progress * 100) + "%");
        else GTextPercentage.setText("");
    }

    public void setInfo(String info) {
        GTextInfo.setText(info);
    }

}
