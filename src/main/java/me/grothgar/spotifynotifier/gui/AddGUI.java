package me.grothgar.spotifynotifier.gui;

import me.grothgar.spotifynotifier.TheEngine;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AddGUI extends StandardGUI {

    private final TheEngine theEngine;
    private final JList<String> artistList;
    private final DefaultListModel<String> artistListModel = new DefaultListModel<>();
    private final JTextField textField;
    private final List<Artist> results;
    private final JButton buttonAdd = new JButton();

    public AddGUI(TheEngine theEngine) {
        super();
        this.theEngine = theEngine;
        this.results = new LinkedList<>();
        setTitle("Search for artists");
        artistList = new JList<>(artistListModel);
        artistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        artistList.setCellRenderer(new ArtistListRenderer());
        artistList.addListSelectionListener(e -> {
            buttonAdd.setEnabled(false);
            if (artistListModel.isEmpty()) return;

            if (artistListModel.get(0).startsWith("\t") && artistList.getSelectedIndex() == 0) {
                artistList.setSelectedIndex(-1);
                refresh();
            } else {
                buttonAdd.setEnabled(true);
            }
        });


        JScrollPane scrollList = new JScrollPane(artistList);
        scrollList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollList.getVerticalScrollBar().setUnitIncrement(16);
        scrollList.setPreferredSize(new Dimension(300, 500));


        JButton buttonSearch = new JButton("Search");
        buttonSearch.addActionListener(e -> search());
        buttonAdd.setText("Add");
        buttonAdd.addActionListener(e -> add());
        buttonAdd.setEnabled(false);
        textField = new JTextField();
        textField.setPreferredSize(new Dimension(150, 20));
        textField.addActionListener(e -> search());


        JPanel buttonPanel = new JPanel();
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        JPanel artistListPanel = new JPanel();


        buttonPanel.add(textField);
        buttonPanel.add(buttonSearch);
        buttonPanel.add(buttonAdd);
        artistListPanel.add(scrollList);


        container.add(buttonPanel);
        container.add(artistListPanel);

        Dimension d = container.getPreferredSize();
        d.height = 600;
        container.setPreferredSize(d);

        frame.add(container);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private void search() {
        if (textField.getText().trim().isEmpty()) return;
        results.clear();
        results.addAll(theEngine.searchArtist(textField.getText()));
        artistListModel.clear();
        if (results.isEmpty()) artistListModel.add(0, "\t'" + textField.getText().trim() + "' not found");
        else artistListModel.addAll(results
                .stream()
                .map(m -> m.getName().substring(0, Math.min(25, m.getName().length())) + " (" + m.getPopularity() + ")")
                .collect(Collectors.toList()));

        textField.setText("");
        refresh();
    }

    private void add() {
        theEngine.followArtistID(results.get(artistList.getSelectedIndex()).getId());
    }

    private void refresh() {
        frame.repaint();
        frame.revalidate();
    }

    private static class ArtistListRenderer extends DefaultListCellRenderer {

        JLabel label;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value.toString().startsWith("\t")) {
                label.setForeground(Color.gray);
                int fontStyle = Font.PLAIN;
                int fontSize = 14;
                label.setFont((new Font("Dialog", fontStyle, fontSize)));
            }

            return label;
        }
    }

}
