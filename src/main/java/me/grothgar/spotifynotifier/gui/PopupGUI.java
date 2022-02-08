package me.grothgar.spotifynotifier.gui;

public abstract class PopupGUI extends GUIDialog {

    public PopupGUI() {
        super();
    }

    @Override
    public void show() {
        dialog.revalidate();
        dialog.repaint();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
