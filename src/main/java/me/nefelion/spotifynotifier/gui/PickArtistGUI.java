package me.nefelion.spotifynotifier.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PickArtistGUI extends PopupGUI {

    private final List<String> artistList;
    private int selectedIndex = -1;

    public PickArtistGUI(List<String> artistList) {
        this.artistList = artistList;
        createGUI();
    }

    private void createGUI() {
        dialog.setMinimumSize(new Dimension(180, 0));
        dialog.setMaximumSize(new Dimension(500,500));
        dialog.add(getTrackListScrollPane(getInitialList(artistList)));
        dialog.pack();
        setTitle("Select an artist");
    }

    private JList<String> getInitialList(List<String> artistList) {
        final JList<String> jList;
        jList = new JList<>(artistList.toArray(new String[0]));
        jList.addListSelectionListener(e -> {
            selectedIndex = jList.getSelectedIndex();
            dialog.dispose();
        });
        return jList;
    }

    private JScrollPane getTrackListScrollPane(JList<String> list) {
        JScrollPane scrollTrackList = new JScrollPane(list);
        scrollTrackList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTrackList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTrackList.getVerticalScrollBar().setUnitIncrement(16);
        //scrollTrackList.setPreferredSize(new Dimension(300, 300));
        return scrollTrackList;
    }


    public int getPickedIndex() {
        return selectedIndex;
    }
}
