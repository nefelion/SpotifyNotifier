package me.nefelion.spotifynotifier.gui;

import javax.swing.*;

public abstract class GUIFrame extends GUI {
    protected final JFrame frame = new JFrame();
    protected JPanel container = new JPanel();

    public GUIFrame() {
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        frame.add(container);
    }

    @Override
    public void setTitle(String title) {
        frame.setTitle(title);
    }

    @Override
    public void close() {
        frame.dispose();
    }

}
