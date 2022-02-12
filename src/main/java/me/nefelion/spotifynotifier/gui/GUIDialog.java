package me.nefelion.spotifynotifier.gui;

import javax.swing.*;

public abstract class GUIDialog extends GUI {
    protected final JDialog dialog = new JDialog();
    protected JPanel container = new JPanel();

    public GUIDialog() {
        dialog.setModal(true);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        dialog.add(container);
    }

    @Override
    public void setTitle(String title) {
        dialog.setTitle(title);
    }

    @Override
    public void close() {
        dialog.dispose();
    }


}
