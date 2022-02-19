package me.nefelion.spotifynotifier.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PickOneGUI extends PopupGUI {

    private final List<String> artistList;
    private int selectedIndex = -1;
    private int hoveredJListIndex = -1;

    public PickOneGUI(List<String> artistList) {
        this.artistList = artistList;
        createGUI();
    }

    private void createGUI() {
        container.add(getTrackListScrollPane(getInitialList(artistList)));
        dialog.add(container);
        dialog.pack();
        setTitle("Select one");
    }

    private JList<String> getInitialList(List<String> artistList) {
        final JList<String> jList;
        jList = new JList<>(artistList.toArray(new String[0]));
        jList.setCellRenderer(new ListRenderer());
        jList.addListSelectionListener(e -> {
            selectedIndex = jList.getSelectedIndex();
            dialog.dispose();
        });
        jList.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent me) {
                Point p = new Point(me.getX(), me.getY());
                int index = jList.locationToIndex(p);
                if (index != hoveredJListIndex) {
                    hoveredJListIndex = index;
                    jList.repaint();
                }
            }
        });
        return jList;
    }

    private JScrollPane getTrackListScrollPane(JList<String> list) {
        JScrollPane scrollTrackList = new JScrollPane(list);
        scrollTrackList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTrackList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTrackList.getVerticalScrollBar().setUnitIncrement(16);
        scrollTrackList.setPreferredSize(new Dimension(200, 200));
        return scrollTrackList;
    }


    public int getPickedIndex() {
        return selectedIndex;
    }


    private class ListRenderer extends DefaultListCellRenderer {

        JLabel label;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Color backgroundColor = hoveredJListIndex == index ? Color.gray : Color.white;

            label.setBackground(backgroundColor);
            return label;
        }
    }

}
