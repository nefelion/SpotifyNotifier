package me.nefelion.spotifynotifier.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PickArtistGUI extends PopupGUI {

    JList<String> jList;
    int selectedIndex = -1;

    public PickArtistGUI(List<String> artistList) {
        setTitle("Select an artist");
        jList = new JList<>(artistList.toArray(new String[0]));
        jList.addListSelectionListener(e -> {
            selectedIndex = jList.getSelectedIndex();
            dialog.dispose();
        });
        dialog.setMinimumSize(new Dimension(180, 0));
        dialog.add(jList);

        dialog.pack();
    }

    public int getPickedIndex() {
        return selectedIndex;
    }
}
