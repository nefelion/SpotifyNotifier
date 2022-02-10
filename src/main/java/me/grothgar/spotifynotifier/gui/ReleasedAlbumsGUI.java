package me.grothgar.spotifynotifier.gui;

import me.grothgar.spotifynotifier.ReleasedAlbum;
import me.grothgar.spotifynotifier.TheEngine;
import me.grothgar.spotifynotifier.records.TempAlbumInfo;
import me.grothgar.spotifynotifier.records.TimeCheckpoint;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
    private final JLabel artistNameLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel typeLabel = new JLabel();
    private final JTextArea IDArea = new JTextArea();
    private final JList<String> albumList;
    private final List<Integer> albumListOffset = new LinkedList<>();
    private final DefaultListModel<String> albumListModel = new DefaultListModel<>();
    private final List<ReleasedAlbum> releasedAlbums;
    private final List<ReleasedAlbum> filteredReleasedAlbums;
    private final JButton buttonSpotify;
    private final JButton buttonMoreBy;
    private final JCheckBox albumsCheckBox;
    private final JCheckBox singlesCheckBox;
    private final JCheckBox featuringCheckBox;
    private final HashMap<String, TempAlbumInfo> tempInfoMap = new HashMap<>();
    private final JLabel coverLabel = new JLabel();
    private final JList<String> trackList;
    private final DefaultListModel<String> trackListModel = new DefaultListModel<>();
    private List<ArtistSimplified> allArtistsAndPerformers;
    private TempAlbumInfo info;
    private int lastSelectedIndex;

    public ReleasedAlbumsGUI(boolean exitOnClose, TheEngine theEngine, List<ReleasedAlbum> albums, String title) {
        super();
        this.setTitle(title);

        this.theEngine = theEngine;
        this.releasedAlbums = new ArrayList<>(albums);
        this.filteredReleasedAlbums = new ArrayList<>(albums);
        filteredReleasedAlbums.sort((a, b) -> b.getLocalDate().compareTo(a.getLocalDate()));
        if (exitOnClose) frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        // itemy
        albumList = new JList<>(albumListModel);
        albumList.setCellRenderer(new AlbumListRenderer());
        albumList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        albumList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (albumList.isSelectionEmpty()) return;
                if (!albumList.getSelectedValue().startsWith("\t")) {
                    albumList.setSelectedIndex(albumList.getSelectedIndex() + (!e.getValueIsAdjusting() && lastSelectedIndex > albumList.getSelectedIndex() ? -1 : 1));
                    if (albumList.getSelectedIndex() == 0) albumList.setSelectedIndex(1);
                }
                if (filteredReleasedAlbums.isEmpty()) {
                    enableButtons(false, buttonSpotify, buttonMoreBy);
                    return;
                } else enableButtons(true, buttonSpotify, buttonMoreBy);

                lastSelectedIndex = albumList.getSelectedIndex();
                refreshFrame();
            }
        });
        trackList = new JList<>(trackListModel);
        trackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trackList.setCellRenderer(new TrackListRenderer());
        trackList.addListSelectionListener(e -> {
            if (trackList.getSelectedIndex() == 0) trackList.setSelectedIndex(1);
        });
        lastSelectedIndex = 0;
        IDArea.setEditable(false);
        artistNameLabel.setForeground(Color.GRAY);


        // buttony
        buttonSpotify = new JButton("Spotify");
        buttonSpotify.setEnabled(false);
        buttonSpotify.addActionListener(e -> {
            Runtime rt = Runtime.getRuntime();
            String url = "https://open.spotify.com/album/" + filteredReleasedAlbums.get(withoutOffset(albumList.getSelectedIndex())).getId();
            try {
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(-1009);
            }
        });

        buttonMoreBy = new JButton("More from...");
        buttonMoreBy.setEnabled(false);
        buttonMoreBy.addActionListener(e -> {
            List<String> idList = allArtistsAndPerformers.stream().map(ArtistSimplified::getId).collect(Collectors.toList());
            List<String> nameList = allArtistsAndPerformers.stream().map(ArtistSimplified::getName).collect(Collectors.toList());
            PickArtistGUI gui = new PickArtistGUI(nameList);
            gui.show();
            int index = gui.getPickedIndex();
            if (index != -1) theEngine.printAllArtistAlbums(idList.get(index));
        });


        // checkboxy
        albumsCheckBox = new JCheckBox("Albums (0)", false);
        singlesCheckBox = new JCheckBox("Singles (0)", false);
        featuringCheckBox = new JCheckBox("Featuring (0)", false);
        albumsCheckBox.setEnabled(false);
        singlesCheckBox.setEnabled(false);
        featuringCheckBox.setEnabled(false);
        initializeCheckBoxes();
        albumsCheckBox.addItemListener(e -> refreshAlbumList());
        singlesCheckBox.addItemListener(e -> refreshAlbumList());
        featuringCheckBox.addItemListener(e -> refreshAlbumList());


        // scroll do listy
        JScrollPane scrollList = new JScrollPane(albumList);
        scrollList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollList.getVerticalScrollBar().setUnitIncrement(16);

        JScrollPane scrollTrackList = new JScrollPane(trackList);
        scrollTrackList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTrackList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTrackList.getVerticalScrollBar().setUnitIncrement(16);
        scrollTrackList.setPreferredSize(new Dimension(300, 300));


        // lista przed pakowaniem
        albumList.setSelectedIndex(0);
        refreshAlbumList();


        // kontenery, JPanel
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        JPanel artistNamePanel = new JPanel();
        artistNamePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        JPanel nameAndSpotifyPanel = new JPanel();
        nameAndSpotifyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        JPanel IDPanel = new JPanel();
        IDPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        JPanel releaseTypePanel = new JPanel();
        releaseTypePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        JPanel imagePanel = new JPanel();
        imagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));


        checkBoxPanel.add(albumsCheckBox);
        checkBoxPanel.add(singlesCheckBox);
        checkBoxPanel.add(featuringCheckBox);
        artistNamePanel.add(artistNameLabel);
        nameAndSpotifyPanel.add(nameLabel);
        nameAndSpotifyPanel.add(buttonSpotify);
        nameAndSpotifyPanel.add(buttonMoreBy);
        IDPanel.add(new JLabel("ID"));
        IDPanel.add(IDArea);
        releaseTypePanel.add(typeLabel);
        imagePanel.add(coverLabel);
        imagePanel.add(scrollTrackList);


        // pakowanie
        container.add(checkBoxPanel);
        container.add(scrollList);
        container.add(artistNamePanel);
        container.add(nameAndSpotifyPanel);
        container.add(releaseTypePanel);
        container.add(IDPanel);
        container.add(imagePanel);


        Dimension d = container.getPreferredSize();
        d.height = 750;
        d.width = 610;
        container.setPreferredSize(d);


        frame.add(container);
        frame.pack();
        frame.setLocationRelativeTo(null);

        refreshFrame();
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
        String id = filteredReleasedAlbums.get(withoutOffset(indexInList)).getId();
        coverLabel.removeAll();
        if (!tempInfoMap.containsKey(id)) {
            Album album = theEngine.getAlbum(id);
            List<TrackSimplified> trackList = theEngine.getTracks(album.getId());
            BufferedImage cover = null;
            try {
                cover = ImageIO.read(new URL(album.getImages()[1].getUrl()));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1010);
            }

            tempInfoMap.put(id, new TempAlbumInfo(album, cover, trackList));
        }


        info = tempInfoMap.get(id);
        artistNameLabel.setText(Arrays.stream(info.album().getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(", ")));
        nameLabel.setText(info.album().getName().length() > 65 ? info.album().getName().substring(0, 60) + "..." : info.album().getName());
        typeLabel.setText(info.album().getAlbumType().getType().toUpperCase() + " released " + info.album().getReleaseDate());
        IDArea.setText(info.album().getId());
        coverLabel.setIcon(new ImageIcon(info.cover()));
        trackListModel.clear();
        int totalTracks = info.album().getTracks().getTotal();
        trackListModel.add(0, totalTracks + " " + (totalTracks == 1 ? "track" : "tracks"));
        trackListModel.addAll(info.trackList().stream()
                .map(t -> t.getTrackNumber() + ".  " + t.getName())
                .collect(Collectors.toList()));
        allArtistsAndPerformers = getArtistsAndPerformers(info.trackList());
    }

    private void refreshAlbumList() {
        filteredReleasedAlbums.clear();
        for (ReleasedAlbum album : releasedAlbums) {
            if (album.isFeaturing()) {
                if (!featuringCheckBox.isSelected()) continue;
            } else {
                if (!albumsCheckBox.isSelected() && album.getAlbumType().equalsIgnoreCase("ALBUM")) continue;
                if (!singlesCheckBox.isSelected() && album.getAlbumType().equalsIgnoreCase("SINGLE")) continue;
            }
            filteredReleasedAlbums.add(album);
        }
        filteredReleasedAlbums.sort((a, b) -> b.getLocalDate().compareTo(a.getLocalDate()));
        albumListModel.clear();
        albumListOffset.clear();

        final List<TimeCheckpoint> checkpoints = new LinkedList<>(Arrays.asList(
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


                albumListModel.add(listIndex++, checkpoints.get(i).string());
                albumListOffset.add(listIndex);
                lastUsedCheckpoint = i;
                break;
            }

            albumListModel.add(listIndex++, "\t     " + album.toString());
        }


        if (!albumListModel.isEmpty()) albumList.setSelectedIndex(1);
        else enableButtons(false, buttonSpotify, buttonMoreBy);
    }

    private void initializeCheckBoxes() {
        long numberOfAlbums = releasedAlbums.stream().filter(p -> p.getAlbumType().equalsIgnoreCase("ALBUM") && !p.isFeaturing()).count();
        long numberOfSingles = releasedAlbums.stream().filter(p -> p.getAlbumType().equalsIgnoreCase("SINGLE") && !p.isFeaturing()).count();
        long numberOfFeaturing = releasedAlbums.stream().filter(ReleasedAlbum::isFeaturing).count();

        if (numberOfAlbums > 0) {
            albumsCheckBox.setText("Albums (" + numberOfAlbums + ")");
            albumsCheckBox.setSelected(true);
            albumsCheckBox.setEnabled(true);
            albumsCheckBox.repaint();
        }
        if (numberOfSingles > 0) {
            singlesCheckBox.setText("Singles (" + numberOfSingles + ")");
            singlesCheckBox.setSelected(true);
            singlesCheckBox.setEnabled(true);
        }
        if (numberOfFeaturing > 0) {
            featuringCheckBox.setText("Featuring (" + numberOfFeaturing + ")");
            featuringCheckBox.setSelected(true);
            featuringCheckBox.setEnabled(true);
        }
    }

    private int withoutOffset(int n) {
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
                if (filteredReleasedAlbums.get(withoutOffset(index)).isFeaturing()) label.setForeground(Color.gray);
                int fontStyle = Font.BOLD;
                int fontSize = 12;
                if (tempInfoMap.containsKey(filteredReleasedAlbums.get(withoutOffset(index)).getId()))
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
            } else if (filteredReleasedAlbums.get(withoutOffset(albumList.getSelectedIndex())).isFeaturing()) {
                if (Arrays.stream(info.trackList().get(index - 1).getArtists()).noneMatch(p -> Objects.equals(p.getId(), filteredReleasedAlbums.get(withoutOffset(albumList.getSelectedIndex())).getArtistId()))) {
                    label.setFont(new Font("Dialog", Font.PLAIN, 12));
                    label.setForeground(Color.gray);
                }

            }

            return label;
        }
    }

}
