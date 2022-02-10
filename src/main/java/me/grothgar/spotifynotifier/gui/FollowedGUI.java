package me.grothgar.spotifynotifier.gui;

import me.grothgar.spotifynotifier.FileManager;
import me.grothgar.spotifynotifier.FollowedArtist;
import me.grothgar.spotifynotifier.TheEngine;
import me.grothgar.spotifynotifier.Utilities;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class FollowedGUI extends StandardGUI implements ListSelectionListener {


    private final JLabel nameLabel;
    private final JTextArea IDArea;
    private final DefaultListModel<String> listModel;
    private final JList<String> jList;
    private final List<FollowedArtist> followedArtists;
    private final JButton buttonSpotify;
    private final JButton buttonRemoveArtist;
    private final JButton buttonReleases;
    private final JButton buttonCheck;
    private final JButton buttonAllReleases;
    private FollowedArtist currentArtist;
    private final JLabel lastCheckedLabel;
    private final JProgressBar progressBar;

    public FollowedGUI(TheEngine theEngine, boolean exitOnClose, List<FollowedArtist> followedArtists) {
        super();
        this.followedArtists = followedArtists;
        if (exitOnClose) frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        nameLabel = new JLabel();
        IDArea = new JTextArea();
        lastCheckedLabel = new JLabel();
        progressBar = new JProgressBar();
        progressBar.setVisible(false);


        // itemy
        listModel = new DefaultListModel<>();
        jList = new JList<>(listModel);
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.addListSelectionListener(this);
        if (!followedArtists.isEmpty()) currentArtist = followedArtists.get(0);
        IDArea.setEditable(false);
        lastCheckedLabel.setForeground(Color.GRAY);


        // buttony
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

        JButton buttonAddNewArtist = new JButton("Add");
        buttonAddNewArtist.addActionListener(e -> {
            theEngine.followArtistID(Utilities.showInputDialog("Enter Artist ID to add", "Add Artist ID"));
            refreshList();
            refresh();
        });

        buttonRemoveArtist = new JButton("Remove");
        buttonRemoveArtist.setEnabled(false);
        buttonRemoveArtist.addActionListener(e -> {
            theEngine.unfollowArtistID(currentArtist.getID());
            refreshList();
            refresh();
        });

        buttonReleases = new JButton("Show releases");
        buttonReleases.setEnabled(false);
        buttonReleases.addActionListener(e -> {
            theEngine.printAllArtistAlbums(currentArtist.getID());
        });

        buttonCheck = new JButton("Check now");
        buttonCheck.setEnabled(false);
        buttonCheck.addActionListener(e -> {
            theEngine.checkForNewReleases(false);
            refresh();
        });

        buttonAllReleases = new JButton("Show all recent releases");
        buttonAllReleases.setEnabled(false);
        buttonAllReleases.addActionListener(e -> {
           theEngine.printAllRecentAlbums();
        });


        // kontenery, JPanel
        JPanel nameAndSpotifyPanel = new JPanel();
        nameAndSpotifyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        JPanel IDPanel = new JPanel();
        IDPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        JPanel restButtonsPanel = new JPanel();
        restButtonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        JPanel lastCheckedPanel = new JPanel();
        lastCheckedPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        JPanel checkPanel = new JPanel();
        checkPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        JPanel progressPanel = new JPanel();
        progressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));


        nameAndSpotifyPanel.add(nameLabel);
        nameAndSpotifyPanel.add(buttonSpotify);
        IDPanel.add(new JLabel("ID"));
        IDPanel.add(IDArea);
        restButtonsPanel.add(buttonAddNewArtist);
        restButtonsPanel.add(buttonRemoveArtist);
        restButtonsPanel.add(buttonReleases);
        lastCheckedPanel.add(lastCheckedLabel);
        checkPanel.add(buttonCheck);
        checkPanel.add(buttonAllReleases);
        progressPanel.add(progressBar);


        // scroll do listy
        JScrollPane scrollList = new JScrollPane(jList);
        scrollList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollList.getVerticalScrollBar().setUnitIncrement(16);


        // pakowanie
        container.add(scrollList);
        container.add(nameAndSpotifyPanel);
        container.add(IDPanel);
        container.add(restButtonsPanel);
        container.add(lastCheckedPanel);
        container.add(checkPanel);
        container.add(progressPanel);

        Dimension d = container.getPreferredSize();
        d.height = 600;
        d.width += 50;
        container.setPreferredSize(d);

        frame.add(container);
        frame.pack();
        frame.setLocationRelativeTo(null);

        // refresh every 1000ms
        int delay = 1000;
        ActionListener taskPerformer = evt -> refresh();
        new Timer(delay, taskPerformer).start();


        updateTitle();
        refreshList();
        refresh();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (followedArtists.isEmpty()) {
            enableButtons(false, buttonSpotify, buttonRemoveArtist, buttonReleases);
            return;
        } else enableButtons(true, buttonSpotify, buttonRemoveArtist, buttonReleases);
        currentArtist = followedArtists.get(Math.max(jList.getSelectedIndex(), 0));
        refresh();
    }


    private void refresh() {
        if (currentArtist != null) {
            nameLabel.setText(currentArtist.getName());
            IDArea.setText(currentArtist.getID());
            lastCheckedLabel.setText("Last checked: " + Utilities.getTimeAgo(FileManager.getFileData().getLastChecked()));
        }

        frame.repaint();
        frame.revalidate();
    }

    private void refreshList() {
        followedArtists.clear();
        followedArtists.addAll(FileManager.getFileData().getFollowedArtists());
        listModel.clear();
        listModel.addAll(followedArtists.stream().map(FollowedArtist::getName).collect(Collectors.toList()));
        enableButtons(!listModel.isEmpty(), buttonCheck, buttonAllReleases);

        updateTitle();
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
