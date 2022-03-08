package me.nefelion.spotifynotifier.gui;

import me.nefelion.spotifynotifier.FollowedArtist;
import me.nefelion.spotifynotifier.TempData;
import me.nefelion.spotifynotifier.TheEngine;
import me.nefelion.spotifynotifier.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FollowedGUI extends StandardGUI {

    private final TheEngine theEngine = TheEngine.getInstance();
    private final JLabel labelName;
    private final JTextArea areaID;
    private final DefaultListModel<String> modelList = new DefaultListModel<>();
    private final JList<String> artistList;
    private final List<FollowedArtist> followedArtists;
    private final JButton buttonSpotify;
    private final JButton buttonAdd;
    private final JButton buttonRemove;
    private final JButton buttonReleases;
    private final JButton buttonCheck;
    private final JButton buttonExplore;
    private final JButton buttonAllReleases;
    private final JLabel labelLastChecked;
    private final JLabel labelID = new JLabel("ID");
    private FollowedArtist currentArtist;

    public FollowedGUI(int defaultCloseOperation, List<FollowedArtist> followedArtists) {
        super();
        this.followedArtists = followedArtists;
        artistList = getInitialArtistList();
        areaID = getInitialAreaID();
        labelName = getInitialLabelName();
        labelLastChecked = getInitialLabelLastChecked();
        buttonSpotify = getInitialButtonSpotify();
        buttonAdd = getInitialButtonAdd();
        buttonRemove = getInitialButtonRemove();
        buttonReleases = getInitialButtonReleases();
        buttonCheck = getInitialButtonCheck();
        buttonExplore = getInitialButtonExplore();
        buttonAllReleases = getInitialButtonAllReleases();

        // refresh every 1000ms
        startTimer();

        buildGUI(defaultCloseOperation);

        refreshList();
        refresh();
    }

    private void startTimer() {
        int delay = 1000;
        ActionListener taskPerformer = evt -> {
            if (getCurrentGUIHashCode() != this.hashCode()) return;
            updateTitle();
            refreshList();
            refresh();
        };
        new Timer(delay, taskPerformer).start();
    }

    private void buildGUI(int defaultCloseOperation) {
        frame.setDefaultCloseOperation(defaultCloseOperation);
        setContainer();
        setFrame();
        updateTitle();
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
        container.add(getScrollPane(artistList));
        container.add(getInitialPanelNameAndReleases());
        container.add(getInitialPanelID());
        container.add(getInitialPanelSelectionButtons());
        container.add(getInitialPanelLastChecked());
        container.add(getInitialPanelCheck());
    }

    private JLabel getInitialLabelLastChecked() {
        final JLabel labelLastChecked;
        labelLastChecked = new JLabel(getLastCheckedString());
        labelLastChecked.setForeground(Color.GRAY);
        return labelLastChecked;
    }

    private JLabel getInitialLabelName() {
        final JLabel label;
        label = new JLabel();
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setToolTipText("Show on Spotify");
        label.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Runtime rt = Runtime.getRuntime();
                String url = "https://open.spotify.com/artist/" + currentArtist.getID();
                try {
                    rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } catch (IOException ex) {
                    System.exit(-1008);
                    ex.printStackTrace();
                }
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
        return label;
    }

    private JTextArea getInitialAreaID() {
        final JTextArea areaID;
        areaID = new JTextArea();
        areaID.setEditable(false);
        areaID.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(
                                new StringSelection(areaID.getText()),
                                null
                        );
                Utilities.showMessageDialog(areaID.getText() + " copied to clipboard.", "Copied ID", JOptionPane.INFORMATION_MESSAGE);
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
        return areaID;
    }

    private JPanel getInitialPanelCheck() {
        JPanel panelCheck = createZeroHeightJPanel();


        panelCheck.add(buttonCheck);
        panelCheck.add(buttonAllReleases);
        panelCheck.add(buttonExplore);
        return panelCheck;
    }

    private JPanel getInitialPanelLastChecked() {
        JPanel panelLastChecked = createZeroHeightJPanel();
        panelLastChecked.add(labelLastChecked);
        return panelLastChecked;
    }

    private JPanel getInitialPanelSelectionButtons() {
        JPanel panelSelectionButtons = createZeroHeightJPanel();
        panelSelectionButtons.add(buttonAdd);
        panelSelectionButtons.add(buttonRemove);
        return panelSelectionButtons;
    }

    private JPanel getInitialPanelID() {
        JPanel IDPanel = createZeroHeightJPanel();
        IDPanel.add(labelID);
        IDPanel.add(areaID);
        return IDPanel;
    }

    private JPanel getInitialPanelNameAndReleases() {
        JPanel panel = createZeroHeightJPanel();
        panel.add(labelName);
        panel.add(buttonReleases);
        return panel;
    }

    private JButton getInitialButtonAllReleases() {
        final JButton buttonAllReleases;
        buttonAllReleases = new JButton("Show all recent releases");
        buttonAllReleases.setEnabled(false);
        buttonAllReleases.addActionListener(e -> {
            theEngine.printAllRecentAlbums();
        });
        setSmallButtonMargins(buttonAllReleases);
        return buttonAllReleases;
    }

    private JButton getInitialButtonCheck() {
        final JButton buttonCheck;
        buttonCheck = new JButton("Check now");
        buttonCheck.setEnabled(false);
        buttonCheck.addActionListener(e -> {
            theEngine.checkForNewReleases(false);
            refresh();
        });
        setSmallButtonMargins(buttonCheck);
        return buttonCheck;
    }

    private JButton getInitialButtonExplore() {
        final JButton buttonExplore;
        buttonExplore = new JButton("Explore");
        buttonExplore.setEnabled(false);
        buttonExplore.addActionListener(e -> {
            theEngine.showRelatedAlbums();
            refresh();
        });
        setSmallButtonMargins(buttonExplore);
        return buttonExplore;
    }

    private JButton getInitialButtonReleases() {
        final JButton buttonReleases;
        buttonReleases = new JButton("Show releases");
        buttonReleases.setEnabled(false);
        buttonReleases.addActionListener(e -> {
            theEngine.printAllArtistAlbums(currentArtist.getID());
        });
        return buttonReleases;
    }

    private JButton getInitialButtonRemove() {
        final JButton buttonRemove;
        buttonRemove = new JButton("Remove selected");
        buttonRemove.setEnabled(false);
        buttonRemove.addActionListener(e -> {
            theEngine.unfollowArtistID(currentArtist.getID());
            currentArtist = null;
            refreshList();
            refresh();
        });
        return buttonRemove;
    }

    private JButton getInitialButtonAdd() {
        JButton buttonAddNewArtist = new JButton("Add / Search");
        buttonAddNewArtist.addActionListener(e -> {
            GUIFrame gui = new AddGUI();
            gui.show();
        });
        return buttonAddNewArtist;
    }

    private JButton getInitialButtonSpotify() {
        final JButton buttonSpotify;
        buttonSpotify = new JButton("Spotify");
        buttonSpotify.setEnabled(false);
        buttonSpotify.addActionListener(e -> {
            Runtime rt = Runtime.getRuntime();
            String url = "https://open.spotify.com/artist/" + currentArtist.getID();
            try {
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } catch (IOException ex) {
                System.exit(-1008);
                ex.printStackTrace();
            }
        });
        return buttonSpotify;
    }

    private JList<String> getInitialArtistList() {
        final JList<String> artistList;
        artistList = new JList<>(modelList);
        artistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        artistList.addListSelectionListener(e -> {
            if (followedArtists.isEmpty()) {
                enableButtons(false, buttonSpotify, buttonRemove, buttonReleases);
                return;
            } else enableButtons(true, buttonSpotify, buttonRemove, buttonReleases);
            currentArtist = followedArtists.get(Math.max(artistList.getSelectedIndex(), 0));
            refresh();
            artistList.ensureIndexIsVisible(artistList.getSelectedIndex());
        });
        return artistList;
    }

    private void refresh() {
        refreshLabelName();
        refreshLabelLastChecked();
        refreshAreaID();
        refreshComponentsVisibility();
        frame.repaint();
        frame.revalidate();
    }

    private void refreshComponentsVisibility() {
        buttonSpotify.setVisible(currentArtist != null);
        areaID.setVisible(currentArtist != null);
        labelName.setVisible(currentArtist != null);
        labelID.setVisible(currentArtist != null);
    }

    private void refreshAreaID() {
        if (currentArtist != null) areaID.setText(currentArtist.getID());
    }

    private void refreshLabelName() {
        if (currentArtist != null) labelName.setText(currentArtist.getName());
    }

    private void refreshLabelLastChecked() {
        labelLastChecked.setText(getLastCheckedString());
    }

    private String getLastCheckedString() {
        return "Last checked: " + Utilities.getTimeAgo(TempData.getInstance().getFileData().getLastChecked());
    }

    public void refreshList() {
        if (!modelList.isEmpty() && !followedArtistListModified()) return;

        followedArtists.clear();
        followedArtists.addAll(TempData.getInstance().getFileData().getFollowedArtists());
        modelList.clear();
        modelList.addAll(followedArtists.stream().map(FollowedArtist::getName).collect(Collectors.toList()));
        enableButtons(!modelList.isEmpty(), buttonCheck, buttonAllReleases, buttonExplore);

        updateTitle();
    }

    private boolean followedArtistListModified() {
        Set<?> set1 = Set.of(followedArtists.stream().map(FollowedArtist::getID).collect(Collectors.toList()));
        Set<?> set2 = Set.of(TempData.getInstance().getFileData().getFollowedArtists().stream().map(FollowedArtist::getID).collect(Collectors.toList()));
        return !set1.equals(set2);
    }

    private void updateTitle() {
        setTitle("Followed artists (" + followedArtists.size() + ")");
    }

    private void enableButtons(boolean enable, JButton... buttons) {
        for (JButton button : buttons) {
            button.setEnabled(enable);
        }
    }


}
