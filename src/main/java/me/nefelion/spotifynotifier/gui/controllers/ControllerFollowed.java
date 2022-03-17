package me.nefelion.spotifynotifier.gui.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import me.nefelion.spotifynotifier.FollowedArtist;
import me.nefelion.spotifynotifier.TheEngine;
import me.nefelion.spotifynotifier.data.TempData;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ControllerFollowed {
    private static final String DEFAULT_CONTROL_INNER_BACKGROUND = "derive(-fx-base,80%)";
    private static final String HIGHLIGHTED_CONTROL_INNER_BACKGROUND = "derive(palegreen, 50%)";

    private final Label placeholderLabelGListFollowed, placeholderLabelGListSpotify;
    private double maxGListSpotifyPopularity = Double.MAX_VALUE;

    @FXML
    private VBox GMainVBOX;
    @FXML
    private ListView<String> GListFollowed;


    @FXML
    private ListView<Artist> GListSpotify;
    @FXML
    private TextField GTextFieldSearchFollowed, GTextFieldSearchSpotify;


    public ControllerFollowed() {
        placeholderLabelGListFollowed = initializePlaceholderLabelGListFollowedArtists();
        placeholderLabelGListSpotify = initializePlaceholderLabelGListSearchSpotifyArtists();
    }

    public VBox getGMainVBOX() {
        return GMainVBOX;
    }


    @FXML
    private void initialize() {
        initializeGListFollowedArtists();
        initializeGListSearchSpotifyArtists();
    }

    @FXML
    private void onKeyTypedSearchFollowed(KeyEvent keyEvent) {
        refreshGListFollowedArtists();
    }

    @FXML
    private void onActionTextFieldSearchSpotify(ActionEvent actionEvent) {
        searchSpotify();
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


    private void searchSpotify() {
        String search = GTextFieldSearchSpotify.getText().trim();
        if (search.isEmpty()) return;

        Thread taskThread = new Thread(() -> {
            //todo, to nie ma sensu, robisz run later po wątku
            Platform.runLater(() -> updateGListSearchSpotifyArtistsWith(TheEngine.getInstance().searchArtist(search)));
        });
        taskThread.start();
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
        GListSpotify.setItems(FXCollections.observableArrayList(list));
        Optional<Integer> optional = GListSpotify.getItems().stream().max(Comparator.comparingInt(Artist::getPopularity)).map(Artist::getPopularity);
        optional.ifPresent(integer -> maxGListSpotifyPopularity = (double) integer);

        placeholderLabelGListSpotify.setText("Can't find '" + search + "' on Spotify");
    }

}
