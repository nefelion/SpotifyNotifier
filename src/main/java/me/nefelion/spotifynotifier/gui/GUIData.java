package me.nefelion.spotifynotifier.gui;

import javax.swing.*;
import java.awt.*;

public class GUIData {
    private final JPanel container;
    private final String title;
    private final Dimension size;
    private final int hashCode;

    public GUIData(JPanel container, String title, Dimension size, int hashCode) {
        this.container = container;
        this.title = title;
        this.size = size;
        this.hashCode = hashCode;
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

    public int getHashCode() {
        return hashCode;
    }
}
