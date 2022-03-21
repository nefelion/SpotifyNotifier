package me.nefelion.spotifynotifier.gui.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import me.nefelion.spotifynotifier.FollowedArtist;
import me.nefelion.spotifynotifier.ReleasesProcessor;
import me.nefelion.spotifynotifier.TheEngine;
import me.nefelion.spotifynotifier.data.TempData;
import me.nefelion.spotifynotifier.gui.AppShowAlbums;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ControllerFollowed {
    private static final String DEFAULT_CONTROL_INNER_BACKGROUND = "derive(-fx-base,80%)";
    private static final String HIGHLIGHTED_CONTROL_INNER_BACKGROUND = "derive(palegreen, 50%)";
    private final Label placeholderLabelGListFollowed, placeholderLabelGListSpotify;
    private ControllerOutline controllerOutline;
    private String lastSearch = "";
    private double maxGListSpotifyPopularity = Double.MAX_VALUE;
    private Task<Boolean> task;

    @FXML
    private VBox GMainVBOX, GVboxInfo;
    @FXML
    private ListView<String> GListFollowed;
    @FXML
    private ListView<Artist> GListSpotify;
    @FXML
    private TextField GTextFieldSearchFollowed, GTextFieldSearchSpotify;
    @FXML
    private Label GLabelCurrentArtist, GLabelProcessedArtists, GLabelLoadedReleases, GLabelNewReleases, GLabelPercentage;
    @FXML
    private ProgressBar GProgressBar;
    @FXML
    private Button GButtonCheckReleases, GButtonAbort;


    public ControllerFollowed() {
        placeholderLabelGListFollowed = initializePlaceholderLabelGListFollowedArtists();
        placeholderLabelGListSpotify = initializePlaceholderLabelGListSearchSpotifyArtists();
    }

    public VBox getGMainVBOX() {
        return GMainVBOX;
    }

    public void setControllerOutline(ControllerOutline controllerOutline) {
        this.controllerOutline = controllerOutline;
    }


    @FXML
    private void initialize() {
        initializeGListFollowedArtists();
        initializeGListSearchSpotifyArtists();
        initializeGVboxInfo();
    }

    @FXML
    private void onKeyTypedSearchFollowed(KeyEvent keyEvent) {
        refreshGListFollowedArtists();
    }

    @FXML
    private void onActionTextFieldSearchSpotify(ActionEvent actionEvent) {
        searchSpotify();
    }

    @FXML
    private void onActionCheckReleases(ActionEvent actionEvent) {
        GButtonCheckReleases.setDisable(true);
        GVboxInfo.setVisible(true);

        ReleasesProcessor processor = new ReleasesProcessor(TempData.getInstance().getFileData().getFollowedArtists());
        processor.setProgressConsumer((var) -> {
                    GLabelPercentage.setText((int) (var * 100) + "%");
                    GProgressBar.setProgress(var);
                })
                .setCurrentArtistConsumer(GLabelCurrentArtist::setText)
                .setProcessedArtistsConsumer(GLabelProcessedArtists::setText)
                .setReleasesConsumer(GLabelLoadedReleases::setText)
                .setNewReleasesConsumer(GLabelNewReleases::setText);

        task = new Task<>() {
            @Override
            public Boolean call() {
                processor.process();
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            GButtonCheckReleases.setDisable(false);
            controllerOutline.setAlbumsVBOX(AppShowAlbums.getAlbumsVBOX(processor.getNewAlbums(), processor.getAllAlbums()));
            controllerOutline.selectTab(ControllerOutline.TAB.ALBUMS);

            resetInfoBoard();
            GVboxInfo.setVisible(false);
        });
        new Thread(task).start();
    }

    public void onActionGButtonAbort(ActionEvent actionEvent) {
        task.setOnCancelled(e -> {
            GVboxInfo.setVisible(false);
            GButtonCheckReleases.setDisable(false);
        });
        task.cancel();
    }


    private Label initializePlaceholderLabelGListFollowedArtists() {
        final Label label;
        label = new Label();
        label.setTextFill(Color.GRAY);
        return label;
    }

    private Label initializePlaceholderLabelGListSearchSpotifyArtists() {
        final Label label;
        label = new Label("Search for the artists you want to follow");
        label.setTextFill(Color.GRAY);
        return label;
    }

    private void initializeGListFollowedArtists() {
        GListFollowed.setPlaceholder(placeholderLabelGListFollowed);
        refreshGListFollowedArtists();
    }

    private void initializeGListSearchSpotifyArtists() {
        GListSpotify.setPlaceholder(placeholderLabelGListSpotify);

        GListSpotify.setCellFactory((param) -> new ListCell<>() {
            @Override
            protected void updateItem(Artist item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("-fx-control-inner-background: " + DEFAULT_CONTROL_INNER_BACKGROUND + ";");
                } else {
                    setText(item.getName());
                    StringBuilder style = new StringBuilder();

                    if (TheEngine.getInstance().isFollowed(item.getId())) {
                        style.append("-fx-control-inner-background: " + HIGHLIGHTED_CONTROL_INNER_BACKGROUND + ";");
                    } else {
                        style.append("-fx-control-inner-background: " + DEFAULT_CONTROL_INNER_BACKGROUND + ";");
                    }

                    double vis = 0.5 + (item.getPopularity() / maxGListSpotifyPopularity) / 2;
                    style.append("-fx-text-fill: " + "rgba(0,0,0,").append(vis).append(");");

                    setStyle(style.toString());
                }

            }
        });
    }

    private void initializeGVboxInfo() {
        GVboxInfo.setVisible(false);
        resetInfoBoard();
    }


    private void searchSpotify() {
        String search = GTextFieldSearchSpotify.getText().trim();
        if (search.isEmpty() || search.equals(lastSearch)) return;
        lastSearch = search;

        List<Artist> artistList = new ArrayList<>();
        Task<Boolean> task = new Task<>() {
            @Override
            public Boolean call() {
                artistList.addAll(TheEngine.getInstance().searchArtist(search));
                return true;
            }
        };

        task.setOnSucceeded(e -> updateGListSearchSpotifyArtistsWith(artistList));
        new Thread(task).start();
    }

    private void refreshGListFollowedArtists() {
        String search = GTextFieldSearchFollowed.getText().trim();
        List<FollowedArtist> followedArtists = TempData.getInstance().getFileData().getFollowedArtists();
        GListFollowed.setItems(FXCollections.observableList(followedArtists.stream()
                .map(FollowedArtist::getName)
                .filter(p -> p.toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList())));

        placeholderLabelGListFollowed.setText(followedArtists.isEmpty()
                ? "You do not follow any artists yet"
                : "Nothing matches a search for '" + search + "'");
    }

    private void updateGListSearchSpotifyArtistsWith(List<Artist> list) {
        String search = GTextFieldSearchSpotify.getText().trim();
        Platform.runLater(() -> {
            placeholderLabelGListSpotify.setText("Can't find '" + search + "' on Spotify");
            GListSpotify.setItems(FXCollections.observableArrayList(list));
        });
        Optional<Integer> optional = GListSpotify.getItems().stream().max(Comparator.comparingInt(Artist::getPopularity)).map(Artist::getPopularity);
        optional.ifPresent(integer -> maxGListSpotifyPopularity = (double) integer);
    }

    private void resetInfoBoard() {
        GLabelPercentage.setText("");
        GProgressBar.setProgress(0.0);
        GLabelCurrentArtist.setText("...");
        GLabelProcessedArtists.setText("0");
        GLabelLoadedReleases.setText("0");
        GLabelNewReleases.setText("0");
    }

}
