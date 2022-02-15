package me.nefelion.spotifynotifier.gui;

import me.nefelion.spotifynotifier.TempData;
import me.nefelion.spotifynotifier.TheEngine;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AddGUI extends StandardGUI {

    private final TheEngine theEngine = TheEngine.getInstance();
    private final JList<String> artistList;
    private final DefaultListModel<String> artistListModel = new DefaultListModel<>();
    private final JTextField fieldInput;
    private final List<Artist> results = new LinkedList<>();
    private final JButton buttonAdd;
    private final JButton buttonReleases = new JButton();
    private final HashSet<String> isFollowedHashset = new HashSet<>();

    public AddGUI() {
        super();
        artistList = getInitialArtistList();
        buttonAdd = getInitialButtonAdd();
        fieldInput = getInitialFieldInput();


        buildGUI();
    }

    private void buildGUI() {
        setContainer();
        setFrame();
        setTitle("Search for artists");
    }

    private void setFrame() {
        frame.add(container);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private void setContainer() {
        fillContainerWithPanels();
        Dimension d = container.getPreferredSize();
        d.height = 600;
        container.setPreferredSize(d);
    }

    private void fillContainerWithPanels() {
        container.add(getInitialUpperPanel());
        container.add(getInitialArtistListPanel());
    }

    private JPanel getInitialArtistListPanel() {
        JPanel artistListPanel = new JPanel();
        artistListPanel.add(getListScrollPane());
        artistListPanel.setLayout(new GridLayout(0, 1));
        return artistListPanel;
    }

    private JPanel getInitialUpperPanel() {
        JPanel upperPanel = createZeroHeightJPanel();
        upperPanel.add(fieldInput);
        upperPanel.add(getInitialButtonSearch());
        upperPanel.add(buttonReleases);
        upperPanel.add(buttonAdd);
        return upperPanel;
    }

    private JButton getInitialButtonSearch() {
        JButton buttonSearch = new JButton("Search");
        buttonSearch.addActionListener(e -> search());
        return buttonSearch;
    }

    private JScrollPane getListScrollPane() {
        JScrollPane scrollList = new JScrollPane(artistList);
        scrollList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollList.getVerticalScrollBar().setUnitIncrement(16);
        //scrollList.setPreferredSize(new Dimension(300, 500));

        return scrollList;
    }

    private JTextField getInitialFieldInput() {
        final JTextField fieldInput;
        fieldInput = new JTextField();
        fieldInput.setPreferredSize(new Dimension(150, 20));
        fieldInput.addActionListener(e -> search());
        return fieldInput;
    }

    private JButton getInitialButtonAdd() {
        final JButton buttonAdd;
        buttonAdd = new JButton();
        buttonAdd.setText("Add");
        buttonAdd.addActionListener(e -> add());
        buttonAdd.setEnabled(false);
        buttonReleases.setText("Releases");
        buttonReleases.addActionListener(e -> theEngine.printAllArtistAlbums(results.get(artistList.getSelectedIndex()).getId()));
        buttonReleases.setEnabled(false);
        return buttonAdd;
    }

    private JList<String> getInitialArtistList() {
        final JList<String> artistList;
        artistList = new JList<>(artistListModel);
        artistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        artistList.setCellRenderer(new ArtistListRenderer());
        artistList.addListSelectionListener(e -> {
            buttonAdd.setEnabled(false);
            buttonReleases.setEnabled(false);
            if (artistListModel.isEmpty()) return;
            if (results.isEmpty()) return;
            if (artistList.getSelectedIndex() < 0) return;

            buttonReleases.setEnabled(true);

            if (!isFollowedHashset.contains(results.get(artistList.getSelectedIndex()).getId())) {
                buttonAdd.setEnabled(true);
            }
        });
        return artistList;
    }

    private void search() {
        if (fieldInput.getText().trim().isEmpty()) return;
        results.clear();
        results.addAll(theEngine.searchArtist(fieldInput.getText()));
        isFollowedHashset.clear();
        isFollowedHashset.addAll(results
                .stream()
                .map(Artist::getId)
                .filter(id -> TempData.getInstance().getFileData().getFollowedArtists().stream().anyMatch(r -> r.getID().equals(id))).collect(Collectors.toList()));


        artistListModel.clear();
        if (results.isEmpty()) artistListModel.add(0, "\t'" + fieldInput.getText().trim() + "' not found");
        else artistListModel.addAll(results
                .stream()
                .map(m -> m.getName().substring(0, Math.min(25, m.getName().length())) + " (" + m.getPopularity() + ")")
                .collect(Collectors.toList()));

        fieldInput.setText("");
        refresh();
    }

    private void add() {
        theEngine.followArtistID(results.get(artistList.getSelectedIndex()).getId());
    }

    private void refresh() {
        frame.repaint();
        frame.revalidate();
    }

    private class ArtistListRenderer extends DefaultListCellRenderer {

        JLabel label;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value.toString().startsWith("\t")) {
                label.setForeground(Color.gray);
                label.setFont((new Font("Dialog", Font.PLAIN, 14)));
            } else if (isFollowedHashset.contains(results.get(index).getId())) {
                label.setForeground(new Color(0, 102, 0));
                label.setFont((new Font("Dialog", Font.PLAIN, 12)));
            }

            return label;
        }
    }

}
