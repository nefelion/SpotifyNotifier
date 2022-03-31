package me.nefelion.spotifynotifier.gui.controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import me.nefelion.spotifynotifier.FollowedArtist;
import me.nefelion.spotifynotifier.ReleasesProcessor;
import me.nefelion.spotifynotifier.TheEngine;
import me.nefelion.spotifynotifier.Utilities;
import me.nefelion.spotifynotifier.data.FileData;
import me.nefelion.spotifynotifier.data.FileManager;
import me.nefelion.spotifynotifier.data.TempData;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;
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
    private ListView<FollowedArtist> GListFollowed;
    @FXML
    private ListView<Artist> GListSpotify;
    @FXML
    private TextField GTextFieldSearchFollowed, GTextFieldSearchSpotify;
    @FXML
    private Label GLabelCurrentArtist, GLabelProcessedArtists, GLabelLoadedReleases, GLabelNewReleases, GLabelPercentage,
            GLabelLastChecked;
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
        initializeGLabelLastChecked();
        startTimer();
    }

    private void startTimer() {
        ActionListener taskPerformer = evt -> Platform.runLater(this::refreshGLabelLastChecked);
        new Timer(1000, taskPerformer).start();
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
        resetInfoBoard();
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
            FileData fileData = TempData.getInstance().getFileData();
            fileData.setLastChecked(Utilities.now());
            FileManager.saveFileData(fileData);
            showReleases(processor);

            resetInfoBoard();
            GVboxInfo.setVisible(false);
            refreshGLabelLastChecked();
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

        GListFollowed.setCellFactory(listView -> {
            ListCell<FollowedArtist> cell = new ListCell<>() {
                @Override
                protected void updateItem(FollowedArtist item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) setText(null);
                    else setText(item.getName());
                }
            };

            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem showReleasesMenuItem = new MenuItem("Show releases");
            final MenuItem showOnSpotifyMenuItem = new MenuItem("Show on Spotify");
            final MenuItem unfollowMenuItem = new MenuItem("Unfollow");

            showReleasesMenuItem.setOnAction(event -> {
                FollowedArtist artist = cell.getItem();
                showFollowedArtistReleases(artist);
            });
            showOnSpotifyMenuItem.setOnAction(event -> {
                FollowedArtist artist = cell.getItem();
                Runtime rt = Runtime.getRuntime();
                String url = "https://open.spotify.com/artist/" + artist.getID();
                try {
                    rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            unfollowMenuItem.setOnAction(event -> {
                FollowedArtist artist = cell.getItem();
                TheEngine.getInstance().unfollowArtistID(artist.getID());
                refreshGListFollowedArtists();
            });

            contextMenu.getItems().add(showReleasesMenuItem);
            contextMenu.getItems().add(showOnSpotifyMenuItem);
            contextMenu.getItems().add(unfollowMenuItem);

            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            cell.contextMenuProperty().bind(
                    Bindings.when(cell.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );


            return cell;
        });

        GListFollowed.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                FollowedArtist artist = GListFollowed.getSelectionModel().getSelectedItem();
                if (artist == null) return;
                showFollowedArtistReleases(artist);
            }
        });


        refreshGListFollowedArtists();
    }

    private void initializeGListSearchSpotifyArtists() {
        GListSpotify.setPlaceholder(placeholderLabelGListSpotify);

        GListSpotify.setCellFactory(listView -> {
            ListCell<Artist> cell = new ListCell<>() {
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
            };

            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem followMenuItem = new MenuItem("Follow");
            final MenuItem showReleasesMenuItem = new MenuItem("Show releases");


            showReleasesMenuItem.setOnAction(event -> {
                Artist artist = cell.getItem();
                showArtistsReleases(artist);
            });
            followMenuItem.setOnAction(event -> {
                Artist artist = cell.getItem();
                followArtist(artist);
            });

            contextMenu.getItems().add(followMenuItem);
            contextMenu.getItems().add(showReleasesMenuItem);


            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            cell.contextMenuProperty().bind(
                    Bindings.when(cell.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );


            return cell;
        });

        GListSpotify.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                Artist artist = GListSpotify.getSelectionModel().getSelectedItem();
                followArtist(artist);
            }
        });
    }

    private void followArtist(Artist artist) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                GMainVBOX.setCursor(Cursor.WAIT);
                TheEngine.getInstance().followArtistID(artist.getId());
                return true;
            }
        };
        task.setOnSucceeded(t -> {
            refreshGListFollowedArtists();
            GMainVBOX.setCursor(Cursor.DEFAULT);
        });
        new Thread(task).start();
    }


    private void initializeGVboxInfo() {
        GVboxInfo.setVisible(false);
        resetInfoBoard();
    }

    private void initializeGLabelLastChecked() {
        refreshGLabelLastChecked();
    }

    private void refreshGLabelLastChecked() {
        GLabelLastChecked.setText("Last checked: " + Utilities.getTimeAgo(TempData.getInstance().getFileData().getLastChecked()));
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
        GListFollowed.setItems(FXCollections.observableList(followedArtists
                .stream()
                .filter(p -> p.getName().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList())));

        placeholderLabelGListFollowed.setText(followedArtists.isEmpty()
                ? "You do not follow any artists yet"
                : "Nothing matches a search for '" + search + "'");
    }

    private void updateGListSearchSpotifyArtistsWith(List<Artist> list) {
        String search = GTextFieldSearchSpotify.getText().trim();

        Optional<Integer> optional = list.stream().max(Comparator.comparingInt(Artist::getPopularity)).map(Artist::getPopularity);
        optional.ifPresent(integer -> maxGListSpotifyPopularity = (double) integer);
        placeholderLabelGListSpotify.setText("Can't find '" + search + "' on Spotify");
        GListSpotify.setItems(FXCollections.observableArrayList(list));
    }

    private void resetInfoBoard() {
        GLabelPercentage.setText("");
        GProgressBar.setProgress(0.0);
        GLabelCurrentArtist.setText("...");
        GLabelProcessedArtists.setText("0");
        GLabelLoadedReleases.setText("0");
        GLabelNewReleases.setText("0");
    }

    private void showReleases(ReleasesProcessor processor) {
        controllerOutline.showAlbums(processor);
        controllerOutline.selectTab(ControllerOutline.TAB.ALBUMS);
    }

    private void showArtistsReleases(Artist artist) {
        showFollowedArtistReleases(new FollowedArtist(artist.getName(), artist.getId()));
    }

    private void showFollowedArtistReleases(FollowedArtist artist) {
        GMainVBOX.setCursor(Cursor.WAIT);
        ReleasesProcessor processor = new ReleasesProcessor(artist);
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                processor.process();
                return true;
            }
        };
        task.setOnSucceeded(b -> {
            showReleases(processor);
            GMainVBOX.setCursor(Cursor.DEFAULT);
        });
        new Thread(task).start();
    }


}
