package me.nefelion.spotifynotifier.gui;

import javax.swing.*;
import java.awt.*;

public abstract class GUI {

    public abstract void show();

    public abstract void close();

    public abstract void setTitle(String title);

    protected JPanel createZeroHeightJPanel() {
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        return checkBoxPanel;
    }

    protected JScrollPane getListScrollPane(JList<String> albumList) {
        JScrollPane scrollList = new JScrollPane(albumList);
        scrollList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollList.getVerticalScrollBar().setUnitIncrement(16);
        return scrollList;
    }
}
