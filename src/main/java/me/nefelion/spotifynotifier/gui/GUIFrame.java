package me.nefelion.spotifynotifier.gui;

import javax.swing.*;

public abstract class GUIFrame {
    private static final String GUI_PREFIX_NAME = "SN: ";
    protected final JFrame frame = new JFrame();
    protected JPanel container = new JPanel();

    public GUIFrame() {
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        frame.add(container);
    }

    public void setTitle(String title) {
        frame.setTitle(title);
    }

    public abstract void show();

    public void close() {
        frame.dispose();
    }


}
