package me.grothgar.spotifynotifier.gui;

import javax.swing.*;

public abstract class GUIDialog {
    private static final String GUI_PREFIX_NAME = "SN: ";
    protected JPanel container = new JPanel();
    protected final JDialog dialog = new JDialog();

    public GUIDialog() {
        dialog.setModal(true);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        dialog.add(container);
    }

    public void setTitle(String title) {
        dialog.setTitle(title);
    }

    public abstract void show();

    public void close() {
        dialog.dispose();
    }


}
