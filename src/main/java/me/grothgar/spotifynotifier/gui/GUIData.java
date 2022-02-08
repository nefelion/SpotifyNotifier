package me.grothgar.spotifynotifier.gui;

import javax.swing.*;
import java.awt.*;

public class GUIData {
    private final JPanel container;
    private final String title;
    private final Dimension size;

    public GUIData(JPanel container, String title, Dimension size) {
        this.container = container;
        this.title = title;
        this.size = size;
    }

    public JPanel getContainer() {
        return container;
    }

    public String getTitle() {
        return title;
    }

    public Dimension getSize() {
        return size;
    }
}
