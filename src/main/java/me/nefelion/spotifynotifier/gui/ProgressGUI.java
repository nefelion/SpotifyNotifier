package me.nefelion.spotifynotifier.gui;

import javax.swing.*;
import java.awt.*;

public class ProgressGUI extends GUIFrame {

    private final JProgressBar progressBar;
    private int value;

    public ProgressGUI(int min, int max) {
        super();
        setTitle("ProgressBar");

        progressBar = new JProgressBar(min, max);
        value = min;
        frame.add(progressBar);
        frame.setPreferredSize(new Dimension(300, 80));
        frame.pack();
    }

    public void setValue(int value) {
        progressBar.setValue(value);
        progressBar.setStringPainted(true);
    }

    public void increment() {
        setValue(++value);
    }

    @Override
    public void show() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
