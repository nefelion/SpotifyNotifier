package me.nefelion.spotifynotifier.gui;

import me.nefelion.spotifynotifier.Player;
import me.nefelion.spotifynotifier.ReleasedAlbum;
import me.nefelion.spotifynotifier.TheEngine;
import me.nefelion.spotifynotifier.Utilities;
import me.nefelion.spotifynotifier.records.CurrentPlaying;
import me.nefelion.spotifynotifier.records.TempAlbumInfo;
import me.nefelion.spotifynotifier.records.TimeCheckpoint;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.DAYS;

public class AlbumsGUI extends StandardGUI {

    private final TheEngine theEngine = TheEngine.getInstance();
    private final JLabel labelArtistName;
    private final JLabel labelName;
    private final JLabel labelTypeDate;
    private final JLabel labelBottomInfo;
    private final JLabel labelCurrentPlaying;
    private final JTextArea textAreaID;
    private final JList<String> albumList;
    private final List<Integer> albumListOffset = new LinkedList<>();
    private final DefaultListModel<String> modelAlbumList = new DefaultListModel<>();
    private final List<ReleasedAlbum> releasedAlbums;
    private final List<ReleasedAlbum> filteredReleasedAlbums;
    private final JButton buttonSpotify;
    private final JButton buttonMoreBy;
    private final JButton buttonFollow;
    private final JButton buttonRelated;
    private final JButton buttonRandom;
    private final JButton buttonStopCurrentPlaying;
    private final JCheckBox checkBoxAlbums;
    private final JCheckBox checkBoxSingles;
    private final JCheckBox checkBoxFeaturing;
    private final HashMap<String, TempAlbumInfo> mapTempInfo = new HashMap<>();
    private final JLabel labelCover = new JLabel();
    private final JList<String> trackList;
    private final DefaultListModel<String> modelTrackList = new DefaultListModel<>();
    private final String title;
    private List<ArtistSimplified> allArtistsAndPerformers;
    private TempAlbumInfo info;
    private int lastSelectedIndex = 0;
    private CurrentPlaying currentPlaying;

    public AlbumsGUI(List<ReleasedAlbum> albums, String title) {
        super();

        this.title = title;
        releasedAlbums = new ArrayList<>(albums);
        filteredReleasedAlbums = getAlbumsSortedByReleaseDate(releasedAlbums);
        albumList = getInitialAlbumList();
        trackList = getInitialTrackList();
        textAreaID = getInitialTextAreaID();
        labelArtistName = getInitialLabelArtistName();
        labelName = getInitialLabelName();
        labelTypeDate = getInitialLabelTypeDate();
        labelBottomInfo = getInitialLabelBottomInfo();
        labelCurrentPlaying = getInitialLabelCurrentPlaying();
        buttonSpotify = getInitialButtonSpotify();
        buttonMoreBy = getInitialButtonMoreBy();
        buttonFollow = getInitialButtonFollow();
        buttonRelated = getInitialButtonRelated();
        buttonRandom = getInitialButtonRandom();
        buttonStopCurrentPlaying = getInitialButtonStopCurrentPlaying();
        checkBoxAlbums = getInitialCheckBoxAlbums();
        checkBoxSingles = getInitialCheckBoxSingles();
        checkBoxFeaturing = getInitialCheckBoxFeaturing();

        buildGUI(this.title + " (" + albums.size() + ")");
        refreshFrame();
    }

    public AlbumsGUI(ReleasedAlbum album, String title) {
        this(List.of(album), title);
    }

    private void buildGUI(String title) {
        setContainer();
        setFrame();
        setTitle(title);
        refreshAlbumList();
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
        container.add(getInitialPanelGUIName());
        container.add(getInitialPanelCheckBox());
        container.add(getScrollPane(albumList));
        container.add(getInitialPanelArtistName());
        container.add(getInitialNameSpotify());
        container.add(getInitialPanelReleaseType());
        container.add(getInitialPanelID());
        container.add(getInitialPanelPlayback());
        container.add(getInitialPanelImage());
        container.add(getInitialPanelBottom());
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

    private JLabel getInitialLabelName() {
        final JLabel labelName;
        labelName = new JLabel();
        labelName.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        labelName.setToolTipText("Copy link");
        labelName.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                String copiedStuff = getSelectedAlbum().getLink();
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(
                                new StringSelection(copiedStuff),
                                null
                        );
                Utilities.showMessageDialog("Link to '" + getSelectedAlbum().getAlbumName()
                        + "' has been copied to clipboard.", "Done", JOptionPane.INFORMATION_MESSAGE);
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        return labelName;
    }

    private JLabel getInitialLabelTypeDate() {
        final JLabel labelTypeDate;
        labelTypeDate = new JLabel();
        labelTypeDate.setFont(labelTypeDate.getFont().deriveFont(Font.PLAIN));
        return labelTypeDate;
    }

    private JLabel getInitialLabelBottomInfo() {
        final JLabel labelBottomInfo;
        labelBottomInfo = new JLabel();
        labelBottomInfo.setFont(labelBottomInfo.getFont().deriveFont(Font.PLAIN));
        return labelBottomInfo;
    }

    private JLabel getInitialLabelCurrentPlaying() {
        final JLabel labelCurrentPlaying;
        labelCurrentPlaying = new JLabel("current-playing-track");
        labelCurrentPlaying.setFont(labelCurrentPlaying.getFont().deriveFont(Font.PLAIN));
        labelCurrentPlaying.setVisible(false);
        labelCurrentPlaying.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        labelCurrentPlaying.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ReleasedAlbum album = currentPlaying.releasedAlbum();
                AlbumsGUI gui = new AlbumsGUI(album, album.getArtistString() + " - " + album.getAlbumName());
                gui.show();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        return labelCurrentPlaying;
    }

    private JTextArea getInitialTextAreaID() {
        final JTextArea IDArea;
        IDArea = new JTextArea();
        IDArea.setEditable(false);
        IDArea.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(
                                new StringSelection(IDArea.getText()),
                                null
                        );
                Utilities.showMessageDialog(IDArea.getText() + " copied to clipboard.", "Copied ID", JOptionPane.INFORMATION_MESSAGE);
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        return IDArea;
    }

    private JPanel getInitialPanelBottom() {
        JPanel panelFollow = createZeroHeightJPanel();
        panelFollow.add(labelBottomInfo);
        panelFollow.add(buttonFollow);
        panelFollow.add(buttonRelated);
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
        panelReleaseType.add(labelTypeDate);
        return panelReleaseType;
    }

    private JPanel getInitialPanelID() {
        JPanel panelID = createZeroHeightJPanel();
        JLabel label = new JLabel("ID");
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
        panelID.add(label);
        panelID.add(textAreaID);
        return panelID;
    }

    private JPanel getInitialPanelPlayback() {
        JPanel panelPlayback = createZeroHeightJPanel();
        panelPlayback.add(labelCurrentPlaying);
        panelPlayback.add(buttonStopCurrentPlaying);
        return panelPlayback;
    }

    private JPanel getInitialNameSpotify() {
        JPanel panelNameSpotify = createZeroHeightJPanel();
        panelNameSpotify.add(labelName);
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
        checkBoxPanel.add(buttonRandom);
        return checkBoxPanel;
    }

    private JPanel getInitialPanelGUIName() {
        JPanel checkBoxPanel = createZeroHeightJPanel();
        JLabel label = new JLabel(title);
        if (backButton != null) checkBoxPanel.add(backButton);
        checkBoxPanel.add(label);
        return checkBoxPanel;
    }

    private JCheckBox getInitialCheckBoxFeaturing() {
        long numberOfFeaturing = releasedAlbums.stream().filter(ReleasedAlbum::isFeaturing).count();

        return getSetCheckBox(numberOfFeaturing, "Featuring");
    }

    private JCheckBox getInitialCheckBoxSingles() {
        long numberOfSingles = releasedAlbums.stream().filter(p -> p.getAlbumType().equalsIgnoreCase("SINGLE") && !p.isFeaturing()).count();

        return getSetCheckBox(numberOfSingles, "Singles");
    }

    private JCheckBox getInitialCheckBoxAlbums() {
        long numberOfAlbums = releasedAlbums.stream().filter(p -> p.getAlbumType().equalsIgnoreCase("ALBUM") && !p.isFeaturing()).count();

        return getSetCheckBox(numberOfAlbums, "Albums");
    }

    private JCheckBox getSetCheckBox(long number, String s) {
        JCheckBox checkBoxFeaturing;
        checkBoxFeaturing = new JCheckBox(s + " (" + number + ")", number != 0);
        checkBoxFeaturing.setEnabled(number != 0);
        checkBoxFeaturing.addItemListener(e -> refreshAlbumList());
        checkBoxFeaturing.setFont(checkBoxFeaturing.getFont().deriveFont(Font.PLAIN));
        return checkBoxFeaturing;
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
            PickOneGUI gui = new PickOneGUI(nameList);
            gui.show();
            int index = gui.getPickedIndex();
            if (index != -1) theEngine.printAllArtistAlbums(idList.get(index));
        });
        setSmallButtonMargins(buttonMoreBy);
        return buttonMoreBy;
    }

    private JButton getInitialButtonRelated() {
        final JButton buttonRelated = getDisabledButton("Related artists");
        buttonRelated.addActionListener(e -> {
            List<Artist> relatedArtists = Arrays.stream(theEngine.getRelatedArtists(getSelectedAlbum().getArtistId()))
                    .filter(p -> !theEngine.isFollowed(p.getId())).collect(Collectors.toList());
            List<String> nameList = relatedArtists.stream().map(Artist::getName).collect(Collectors.toList());
            PickOneGUI gui = new PickOneGUI(nameList);
            gui.show();
            int index = gui.getPickedIndex();
            if (index != -1) theEngine.printAllArtistAlbums(relatedArtists.get(index).getId());
        });
        setSmallButtonMargins(buttonRelated);
        return buttonRelated;
    }

    private JButton getInitialButtonRandom() {
        final JButton buttonRandom = getDisabledButton("Random");
        buttonRandom.addActionListener(e -> {
            if (filteredReleasedAlbums.size() < 2) return;

            int randIndex = new Random().nextInt(modelAlbumList.size());
            while (albumList.getSelectedIndex() == randIndex || albumListOffset.contains(randIndex + 1))
                randIndex = new Random().nextInt(modelAlbumList.size());

            albumList.ensureIndexIsVisible(randIndex);
            albumList.setSelectedIndex(randIndex);
        });
        setSmallButtonMargins(buttonRandom);
        buttonRandom.setEnabled(true);
        return buttonRandom;
    }

    private JButton getInitialButtonStopCurrentPlaying() {
        final JButton buttonStopCurrentPlaying = getDisabledButton("Stop");
        buttonStopCurrentPlaying.addActionListener(e -> stopPlayback());
        setSmallButtonMargins(buttonStopCurrentPlaying);
        buttonStopCurrentPlaying.setVisible(false);
        buttonStopCurrentPlaying.setEnabled(true);
        return buttonStopCurrentPlaying;
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
            if (e.getValueIsAdjusting()) return;
            playSelected();
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
            if (e.getValueIsAdjusting()) return;
            if (filteredReleasedAlbums.isEmpty()) {
                enableButtons(false, buttonSpotify, buttonMoreBy, buttonFollow, buttonRelated);
                return;
            } else enableButtons(true, buttonSpotify, buttonMoreBy, buttonFollow, buttonRelated);

            lastSelectedIndex = albumList.getSelectedIndex();
            loadInfo(albumList.getSelectedIndex());
            refreshButtonFollow();
            refreshFrame();
        });
        return albumList;
    }

    private void refreshFrame() {

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

            //mapTempInfo.put(id, new TempAlbumInfo(album, cover, trackList));
        }


        info = mapTempInfo.get(id);
        labelArtistName.setText(Arrays.stream(info.album().getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(", ")));
        labelName.setText(info.album().getName().length() > 65 ? info.album().getName().substring(0, 60) + "..." : info.album().getName());
        labelTypeDate.setText(info.album().getAlbumType().getType().toUpperCase() + " released " + info.album().getReleaseDate());
        textAreaID.setText(info.album().getId());
        //labelCover.setIcon(new ImageIcon(info.cover()));
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
        else enableButtons(false, buttonSpotify, buttonMoreBy, buttonFollow, buttonRelated);
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

    private int getIndexWithoutOffset(int n) {
        OptionalInt offset = IntStream.range(0, albumListOffset.size())
                .filter(i -> albumListOffset.get(i) <= n).reduce((first, second) -> second);

        return n - (offset.isPresent() ? offset.getAsInt() + 1 : 0);
    }

    private void playSelected() {
        if (trackList.getSelectedIndex() < 1) return;
        stopPlayback();
        TrackSimplified selectedTrack = info.trackList().get(trackList.getSelectedIndex() - 1);
        String url = selectedTrack.getPreviewUrl();
        if (url == null) return;
        Player player = new Player(url);
        player.lockToStandardGUI(this.hashCode());
        player.play();
        currentPlaying = new CurrentPlaying(getSelectedAlbum(), selectedTrack, player);

        buttonStopCurrentPlaying.setVisible(true);
        labelCurrentPlaying.setText(getSelectedAlbum().getArtistString() + "  —  " + selectedTrack.getName());
        labelCurrentPlaying.setVisible(true);
    }

    private void stopPlayback() {
        if (currentPlaying != null) currentPlaying.player().stop();
        buttonStopCurrentPlaying.setVisible(false);
        labelCurrentPlaying.setVisible(false);
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