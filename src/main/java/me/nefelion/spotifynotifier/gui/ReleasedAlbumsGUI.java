package me.nefelion.spotifynotifier.gui;

import me.nefelion.spotifynotifier.ReleasedAlbum;
import me.nefelion.spotifynotifier.TheEngine;
import me.nefelion.spotifynotifier.records.TempAlbumInfo;
import me.nefelion.spotifynotifier.records.TimeCheckpoint;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.DAYS;

public class ReleasedAlbumsGUI extends StandardGUI {

    private final TheEngine theEngine;
    private final JLabel labelArtistName;
    private final JLabel nameLabel = new JLabel();
    private final JLabel typeLabel = new JLabel();
    private final JTextArea textAreaID;
    private final JList<String> albumList;
    private final List<Integer> albumListOffset = new LinkedList<>();
    private final DefaultListModel<String> modelAlbumList = new DefaultListModel<>();
    private final List<ReleasedAlbum> releasedAlbums;
    private final List<ReleasedAlbum> filteredReleasedAlbums;
    private final JButton buttonSpotify;
    private final JButton buttonMoreBy;
    private final JButton buttonFollow;
    private final JCheckBox checkBoxAlbums;
    private final JCheckBox checkBoxSingles;
    private final JCheckBox checkBoxFeaturing;
    private final HashMap<String, TempAlbumInfo> mapTempInfo = new HashMap<>();
    private final JLabel labelCover = new JLabel();
    private final JList<String> trackList;
    private final DefaultListModel<String> modelTrackList = new DefaultListModel<>();
    private List<ArtistSimplified> allArtistsAndPerformers;
    private TempAlbumInfo info;
    private int lastSelectedIndex = 0;

    public ReleasedAlbumsGUI(int defaultCloseOperation, TheEngine theEngine, List<ReleasedAlbum> albums, String title) {
        super();

        this.theEngine = theEngine;
        releasedAlbums = new ArrayList<>(albums);
        filteredReleasedAlbums = getAlbumsSortedByReleaseDate(releasedAlbums);
        albumList = getInitialAlbumList();
        trackList = getInitialTrackList();
        textAreaID = getInitialTextAreaID();
        labelArtistName = getInitialLabelArtistName();
        buttonSpotify = getInitialButtonSpotify();
        buttonMoreBy = getInitialButtonMoreBy();
        buttonFollow = getInitialButtonFollow();
        checkBoxAlbums = getInitialCheckBoxAlbums();
        checkBoxSingles = getInitialCheckBoxSingles();
        checkBoxFeaturing = getInitialCheckBoxFeaturing();

        buildGUI(defaultCloseOperation, title);
        refreshFrame();
    }

    private void buildGUI(int defaultCloseOperation, String title) {
        frame.setDefaultCloseOperation(defaultCloseOperation);
        setContainer();
        setFrame();
        setTitle(title);
    }

    private void setFrame() {
        frame.add(container);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private void setContainer() {
        fillContainerWithPanels();
        container.setPreferredSize(new Dimension(610, 750));
    }

    private void fillContainerWithPanels() {
        container.add(getInitialPanelCheckBox());
        container.add(getListScrollPane(albumList));
        container.add(getInitialPanelArtistName());
        container.add(getInitialNameSpotify());
        container.add(getInitialPanelReleaseType());
        container.add(getInitialPanelID());
        container.add(getInitialPanelImage());
        container.add(getInitialPanelFollow());
    }

    private List<ReleasedAlbum> getAlbumsSortedByReleaseDate(List<ReleasedAlbum> albums) {
        final List<ReleasedAlbum> filteredReleasedAlbums;
        filteredReleasedAlbums = new ArrayList<>(albums);
        filteredReleasedAlbums.sort((a, b) -> b.getLocalDate().compareTo(a.getLocalDate()));
        return filteredReleasedAlbums;
    }

    private JLabel getInitialLabelArtistName() {
        final JLabel labelArtistName;
        labelArtistName = new JLabel();
        labelArtistName.setForeground(Color.GRAY);
        return labelArtistName;
    }

    private JTextArea getInitialTextAreaID() {
        final JTextArea IDArea;
        IDArea = new JTextArea();
        IDArea.setEditable(false);
        return IDArea;
    }

    private JPanel getInitialPanelFollow() {
        JPanel panelFollow = createZeroHeightJPanel();
        panelFollow.add(buttonFollow);
        return panelFollow;
    }

    private JPanel getInitialPanelImage() {
        JPanel panelImage = createZeroHeightJPanel();
        panelImage.add(labelCover);
        panelImage.add(getTrackListScrollPane());
        return panelImage;
    }

    private JPanel getInitialPanelReleaseType() {
        JPanel panelReleaseType = createZeroHeightJPanel();
        panelReleaseType.add(typeLabel);
        return panelReleaseType;
    }

    private JPanel getInitialPanelID() {
        JPanel panelID = createZeroHeightJPanel();
        panelID.add(new JLabel("ID"));
        panelID.add(textAreaID);
        return panelID;
    }

    private JPanel getInitialNameSpotify() {
        JPanel panelNameSpotify = createZeroHeightJPanel();
        panelNameSpotify.add(nameLabel);
        panelNameSpotify.add(buttonSpotify);
        panelNameSpotify.add(buttonMoreBy);
        return panelNameSpotify;
    }

    private JPanel getInitialPanelArtistName() {
        JPanel panelArtistName = createZeroHeightJPanel();
        panelArtistName.add(labelArtistName);
        return panelArtistName;
    }

    private JPanel getInitialPanelCheckBox() {
        JPanel checkBoxPanel = createZeroHeightJPanel();
        checkBoxPanel.add(checkBoxAlbums);
        checkBoxPanel.add(checkBoxSingles);
        checkBoxPanel.add(checkBoxFeaturing);
        setCheckBoxes();
        return checkBoxPanel;
    }

    private JCheckBox getInitialCheckBoxFeaturing() {
        final JCheckBox checkBoxFeaturing;
        checkBoxFeaturing = new JCheckBox("Featuring (0)", false);
        checkBoxFeaturing.setEnabled(false);
        checkBoxFeaturing.addItemListener(e -> refreshAlbumList());
        return checkBoxFeaturing;
    }

    private JCheckBox getInitialCheckBoxSingles() {
        final JCheckBox checkBoxSingles;
        checkBoxSingles = new JCheckBox("Singles (0)", false);
        checkBoxSingles.setEnabled(false);
        checkBoxSingles.addItemListener(e -> refreshAlbumList());
        return checkBoxSingles;
    }

    private JCheckBox getInitialCheckBoxAlbums() {
        final JCheckBox albumsCheckBox;
        albumsCheckBox = new JCheckBox("Albums (0)", false);
        albumsCheckBox.setEnabled(false);
        albumsCheckBox.addItemListener(e -> refreshAlbumList());
        return albumsCheckBox;
    }

    private JScrollPane getTrackListScrollPane() {
        JScrollPane scrollTrackList = new JScrollPane(trackList);
        scrollTrackList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTrackList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTrackList.getVerticalScrollBar().setUnitIncrement(16);
        scrollTrackList.setPreferredSize(new Dimension(300, 300));
        return scrollTrackList;
    }

    private JButton getInitialButtonFollow() {
        final JButton buttonFollow = getDisabledButton("button-follow");
        buttonFollow.addActionListener(e -> {
            String id = getSelectedAlbum().getArtistId();
            if (theEngine.isFollowed(id)) theEngine.unfollowArtistID(id);
            else theEngine.followArtistID(id);
            refreshButtonFollow();
        });
        setSmallButtonMargins(buttonFollow);
        return buttonFollow;
    }

    private void refreshButtonFollow() {
        buttonFollow.setText(getButtonFollowState());
    }

    private String getButtonFollowState() {
        try {
            return (theEngine.isFollowed(getSelectedAlbum().getArtistId()) ? "Unfollow" : "Follow") + " " + getSelectedAlbum().getFollowedArtistName();
        } catch (IndexOutOfBoundsException e) {
            return "-";
        }
    }

    private JButton getInitialButtonMoreBy() {
        final JButton buttonMoreBy = getDisabledButton("More from...");
        buttonMoreBy.addActionListener(e -> {
            List<String> idList = allArtistsAndPerformers.stream().map(ArtistSimplified::getId).collect(Collectors.toList());
            List<String> nameList = allArtistsAndPerformers.stream().map(ArtistSimplified::getName).collect(Collectors.toList());
            PickArtistGUI gui = new PickArtistGUI(nameList);
            gui.show();
            int index = gui.getPickedIndex();
            if (index != -1) theEngine.printAllArtistAlbums(idList.get(index));
        });
        setSmallButtonMargins(buttonMoreBy);
        return buttonMoreBy;
    }

    private JButton getInitialButtonSpotify() {
        final JButton buttonSpotify = getDisabledButton("Spotify");
        buttonSpotify.addActionListener(e -> {
            Runtime rt = Runtime.getRuntime();
            String url = "https://open.spotify.com/album/" + getSelectedAlbum().getId();
            try {
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(-1009);
            }
        });
        setSmallButtonMargins(buttonSpotify);
        return buttonSpotify;
    }

    private ReleasedAlbum getSelectedAlbum() {
        return filteredReleasedAlbums.get(getIndexWithoutOffset(albumList.getSelectedIndex()));
    }

    private JButton getDisabledButton(String text) {
        final JButton buttonSpotify;
        buttonSpotify = new JButton(text);
        buttonSpotify.setEnabled(false);
        return buttonSpotify;
    }

    private JList<String> getInitialTrackList() {
        final JList<String> trackList;
        trackList = new JList<>(modelTrackList);
        trackList.setCellRenderer(new TrackListRenderer());
        trackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trackList.addListSelectionListener(e -> {
            if (trackList.getSelectedIndex() == 0) trackList.setSelectedIndex(1);
        });
        return trackList;
    }

    private JList<String> getInitialAlbumList() {
        final JList<String> albumList;
        albumList = new JList<>(modelAlbumList);
        albumList.setCellRenderer(new AlbumListRenderer());
        albumList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        albumList.addListSelectionListener(e -> {
            if (albumList.isSelectionEmpty()) return;
            if (!albumList.getSelectedValue().startsWith("\t")) {
                albumList.setSelectedIndex(albumList.getSelectedIndex() + (!e.getValueIsAdjusting() && lastSelectedIndex > albumList.getSelectedIndex() ? -1 : 1));
                if (albumList.getSelectedIndex() == 0) albumList.setSelectedIndex(1);
            }
            if (filteredReleasedAlbums.isEmpty()) {
                enableButtons(false, buttonSpotify, buttonMoreBy, buttonFollow);
                return;
            } else enableButtons(true, buttonSpotify, buttonMoreBy, buttonFollow);

            lastSelectedIndex = albumList.getSelectedIndex();
            refreshButtonFollow();
            refreshFrame();
        });
        return albumList;
    }

    private void refreshFrame() {
        if (albumList.getSelectedIndex() >= 0) loadInfo(albumList.getSelectedIndex());

        frame.repaint();
        frame.revalidate();
    }

    private void enableButtons(boolean enable, JButton... buttons) {
        for (JButton button : buttons) {
            button.setEnabled(enable);
        }
    }

    private void loadInfo(int indexInList) {
        String id = filteredReleasedAlbums.get(getIndexWithoutOffset(indexInList)).getId();
        labelCover.removeAll();
        if (!mapTempInfo.containsKey(id)) {
            Album album = theEngine.getAlbum(id);
            List<TrackSimplified> trackList = theEngine.getTracks(album.getId());
            BufferedImage cover = null;
            try {
                cover = ImageIO.read(new URL(album.getImages()[1].getUrl()));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1010);
            }

            mapTempInfo.put(id, new TempAlbumInfo(album, cover, trackList));
        }


        info = mapTempInfo.get(id);
        labelArtistName.setText(Arrays.stream(info.album().getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(", ")));
        nameLabel.setText(info.album().getName().length() > 65 ? info.album().getName().substring(0, 60) + "..." : info.album().getName());
        typeLabel.setText(info.album().getAlbumType().getType().toUpperCase() + " released " + info.album().getReleaseDate());
        textAreaID.setText(info.album().getId());
        labelCover.setIcon(new ImageIcon(info.cover()));
        modelTrackList.clear();
        int totalTracks = info.album().getTracks().getTotal();
        modelTrackList.add(0, totalTracks + " " + (totalTracks == 1 ? "track" : "tracks"));
        modelTrackList.addAll(info.trackList().stream()
                .map(t -> t.getTrackNumber() + ".  " + t.getName())
                .collect(Collectors.toList()));
        allArtistsAndPerformers = getArtistsAndPerformers(info.trackList());
    }

    private void refreshAlbumList() {
        reloadFilteredReleasedAlbums();
        recreateAlbumAndOffsetLists();

        if (!modelAlbumList.isEmpty()) albumList.setSelectedIndex(1);
        else enableButtons(false, buttonSpotify, buttonMoreBy, buttonFollow);
    }

    private void recreateAlbumAndOffsetLists() {
        modelAlbumList.clear();
        albumListOffset.clear();

        final List<TimeCheckpoint> checkpoints = new LinkedList<>(Arrays.asList(
                new TimeCheckpoint(-2, "Future"),
                new TimeCheckpoint(-1, "Tomorrow"),
                new TimeCheckpoint(0, "Today"),
                new TimeCheckpoint(1, "Yesterday"),
                new TimeCheckpoint(7, "Last 7 days"),
                new TimeCheckpoint(30, "Last 30 days"),
                new TimeCheckpoint(90, "Last 90 days"),
                new TimeCheckpoint(365, "Last 365 days"),
                new TimeCheckpoint(99999, "Before time")
        ));

        int lastUsedCheckpoint = -1;
        int listIndex = 0;
        for (ReleasedAlbum album : filteredReleasedAlbums) {
            for (int i = 0; i < checkpoints.size(); i++) {
                long daysAgo = DAYS.between(album.getLocalDate(), LocalDate.now());
                if (daysAgo > checkpoints.get(i).days()) continue;
                if (i == lastUsedCheckpoint) break;


                modelAlbumList.add(listIndex++, checkpoints.get(i).string());
                albumListOffset.add(listIndex);
                lastUsedCheckpoint = i;
                break;
            }

            modelAlbumList.add(listIndex++, "\t     " + album.toString());
        }
    }

    private void reloadFilteredReleasedAlbums() {
        filteredReleasedAlbums.clear();
        for (ReleasedAlbum album : releasedAlbums) {
            if (album.isFeaturing()) {
                if (!checkBoxFeaturing.isSelected()) continue;
            } else {
                if (!checkBoxAlbums.isSelected() && album.getAlbumType().equalsIgnoreCase("ALBUM")) continue;
                if (!checkBoxSingles.isSelected() && album.getAlbumType().equalsIgnoreCase("SINGLE")) continue;
            }
            filteredReleasedAlbums.add(album);
        }
        filteredReleasedAlbums.sort((a, b) -> b.getLocalDate().compareTo(a.getLocalDate()));
    }

    private void setCheckBoxes() {
        long numberOfAlbums = releasedAlbums.stream().filter(p -> p.getAlbumType().equalsIgnoreCase("ALBUM") && !p.isFeaturing()).count();
        long numberOfSingles = releasedAlbums.stream().filter(p -> p.getAlbumType().equalsIgnoreCase("SINGLE") && !p.isFeaturing()).count();
        long numberOfFeaturing = releasedAlbums.stream().filter(ReleasedAlbum::isFeaturing).count();

        if (numberOfAlbums > 0) {
            checkBoxAlbums.setText("Albums (" + numberOfAlbums + ")");
            checkBoxAlbums.setSelected(true);
            checkBoxAlbums.setEnabled(true);
            checkBoxAlbums.repaint();
        }
        if (numberOfSingles > 0) {
            checkBoxSingles.setText("Singles (" + numberOfSingles + ")");
            checkBoxSingles.setSelected(true);
            checkBoxSingles.setEnabled(true);
        }
        if (numberOfFeaturing > 0) {
            checkBoxFeaturing.setText("Featuring (" + numberOfFeaturing + ")");
            checkBoxFeaturing.setSelected(true);
            checkBoxFeaturing.setEnabled(true);
        }
    }

    private int getIndexWithoutOffset(int n) {
        OptionalInt offset = IntStream.range(0, albumListOffset.size())
                .filter(i -> albumListOffset.get(i) <= n).reduce((first, second) -> second);

        return n - (offset.isPresent() ? offset.getAsInt() + 1 : 0);
    }

    private List<ArtistSimplified> getArtistsAndPerformers(List<TrackSimplified> tracklist) {
        List<ArtistSimplified> artistsAndPerformers = new LinkedList<>();
        HashSet<String> nameSet = new HashSet<>();
        for (TrackSimplified track : tracklist)
            for (ArtistSimplified artist : track.getArtists())
                if (nameSet.add(artist.getId()))
                    artistsAndPerformers.add(artist);

        return artistsAndPerformers.stream().sorted(Comparator.comparing(ArtistSimplified::getName)).collect(Collectors.toList());
    }

    private class AlbumListRenderer extends DefaultListCellRenderer {

        JLabel label;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value.toString().startsWith("\t")) {
                if (filteredReleasedAlbums.get(getIndexWithoutOffset(index)).isFeaturing())
                    label.setForeground(Color.gray);
                int fontStyle = Font.BOLD;
                int fontSize = 12;
                if (mapTempInfo.containsKey(filteredReleasedAlbums.get(getIndexWithoutOffset(index)).getId()))
                    fontStyle = Font.PLAIN;
                //if (ChronoUnit.DAYS.between(getLocalDate(releasedAlbums.get(index).getReleaseDate()), LocalDate.now()) < 30) {fontSize = 14;}
                label.setFont((new Font("Dialog", fontStyle, fontSize)));
            } else {
                label.setFont(new Font("Dialog", Font.BOLD, 14));
                label.setForeground(Color.gray);
            }

            return label;
        }
    }

    private class TrackListRenderer extends DefaultListCellRenderer {

        JLabel label;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (index == 0) {
                label.setFont(new Font("Dialog", Font.BOLD, 14));
                label.setForeground(Color.gray);
            } else if (getSelectedAlbum().isFeaturing()) {
                if (Arrays.stream(info.trackList().get(index - 1).getArtists()).noneMatch(p -> Objects.equals(p.getId(), getSelectedAlbum().getArtistId()))) {
                    label.setFont(new Font("Dialog", Font.PLAIN, 12));
                    label.setForeground(Color.gray);
                }

            }

            return label;
        }
    }

}