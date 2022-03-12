package me.nefelion.spotifynotifier.TESTING_JAVAFX.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import me.nefelion.spotifynotifier.ReleasedAlbum;
import me.nefelion.spotifynotifier.TheEngine;
import me.nefelion.spotifynotifier.records.TempAlbumInfo;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;


public class ControllerAlbums {
    private static final String HIGHLIGHTED_CONTROL_INNER_BACKGROUND = "derive(palegreen, 50%)";


    private MediaPlayer player;
    private HashMap<String, TempAlbumInfo> infoHashMap = new HashMap<>();
    private List<ReleasedAlbum> newAlbums;
    private List<ReleasedAlbum> oldAlbums;
    private TrackSimplified currentSelectedTrack;

    @FXML
    private VBox GMainVBOX;
    @FXML
    private Accordion GAccordionAlbums;
    @FXML
    private TitledPane GTitledPaneNewReleases, GTitledPaneAllReleases, GTitledPaneTracklist, GTitledPaneInfo;
    @FXML
    private TableView<ReleasedAlbum> GTableNewReleases, GTableOldReleases;
    @FXML
    private TableColumn<ReleasedAlbum, String> GOldReleases_Type, GOldReleases_Date, GOldReleases_Artist, GOldReleases_Release,
            GNewReleases_Type, GNewReleases_Date, GNewReleases_Artist, GNewReleases_Release;
    @FXML
    private ListView<TrackSimplified> GListTracklist;
    @FXML
    private ImageView GCoverImageView;
    @FXML
    private Text GTextCurrentPlaying, GTextVolume;
    @FXML
    private Button GButtonStopPlaying;
    @FXML
    private Slider GSliderVolume;


    @FXML
    private void initialize() {
        initializeGTitledPaneNewReleases();
        initializeGTitledPaneAllReleases();

        initializeGNewReleasesColumns();
        initializeGOldReleasesColumns();

        initializeGListTracklist();

        initializeGTableNewReleases();
        initializeGTableOldReleases();

        GSliderVolume.valueProperty().addListener((observable, oldValue, newValue) -> {
            setVolume();
        });

    }

    @FXML
    private void onActionGButtonStopPlaying(ActionEvent actionEvent) {
        stopCurrent();
    }

    private void initializeGTableOldReleases() {
        addAlbumSelectionListenerFor(GTableOldReleases);
    }

    private void initializeGTableNewReleases() {
        addAlbumSelectionListenerFor(GTableNewReleases);
    }

    private void addAlbumSelectionListenerFor(TableView<ReleasedAlbum> gTableOldReleases) {
        gTableOldReleases.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            String id = getSelectedAlbum().getId();

            if (!infoHashMap.containsKey(id)) {
                GCoverImageView.setImage(null);
                GListTracklist.setPlaceholder(new Label("Loading..."));

                GListTracklist.setItems(null);
                Thread taskThread = new Thread(this::downloadInfoForSelectedAlbum);
                taskThread.start();
            } else loadInfo();


            if (!GTitledPaneTracklist.isExpanded() && !GTitledPaneInfo.isExpanded()) {
                GTitledPaneTracklist.setExpanded(true);
            }
        });
    }

    private void loadInfo() {
        TempAlbumInfo info = infoHashMap.get(getSelectedAlbum().getId());
        GCoverImageView.setImage(info.cover());
        GListTracklist.setItems(FXCollections.observableArrayList(info.trackList()));
    }

    private void downloadInfoForSelectedAlbum() {
        GMainVBOX.setCursor(Cursor.WAIT);
        String id = getSelectedAlbum().getId();
        Album album = TheEngine.getInstance().getAlbum(id);
        List<TrackSimplified> trackList = TheEngine.getInstance().getTracks(album.getId());
        try {
            URL url = new URL(album.getImages()[1].getUrl());
            InputStream input = url.openStream();
            Platform.runLater(() -> {
                infoHashMap.put(id, new TempAlbumInfo(album, new Image(input), trackList));
                if (Objects.equals(getSelectedAlbum().getId(), id)) loadInfo();
                //downloadInfoForBuffers();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        GMainVBOX.setCursor(Cursor.DEFAULT);
    }

    private void downloadInfoForBuffers() {
        int selectedIndex = GTableNewReleases.getSelectionModel().selectedIndexProperty().get();

        Thread thread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                int index = selectedIndex + i - 5;
                if (index < 0) continue;
                if (index == GTableNewReleases.getItems().size()) return;
                String id = GTableNewReleases.getItems().get(index).getId();
                if (infoHashMap.containsKey(id)) continue;
                Album album = TheEngine.getInstance().getAlbum(id);
                List<TrackSimplified> trackList = TheEngine.getInstance().getTracks(album.getId());
                try {
                    URL url = new URL(album.getImages()[1].getUrl());
                    InputStream input = url.openStream();
                    Platform.runLater(() -> {
                        infoHashMap.put(id, new TempAlbumInfo(album, new Image(input), trackList));
                        if (Objects.equals(getSelectedAlbum().getId(), id)) loadInfo();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    private ReleasedAlbum getSelectedAlbum() {
        return GTableNewReleases.getSelectionModel().getSelectedItem();
    }


    private void initializeGListTracklist() {
        GListTracklist.setPlaceholder(new Label("Select an album"));
        GListTracklist.setCellFactory((param) -> new ListCell<>() {
            @Override
            protected void updateItem(TrackSimplified item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    return;
                }
                setText(item.getTrackNumber() + ". " + item.getName());

                if (Arrays.stream(item.getArtists()).anyMatch(p -> p.getId().equals(getSelectedAlbum().getArtistId()))) {
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });

        GListTracklist.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) return;
            currentSelectedTrack = newSelection;
            playSelected();
        });
    }

    private void playSelected() {
        GMainVBOX.setCursor(Cursor.WAIT);
        stopCurrent();
        Thread taskThread = new Thread(() -> {
            TrackSimplified selected = GListTracklist.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            if (selected.getPreviewUrl() == null) {
                Platform.runLater(() -> {
                    GTextCurrentPlaying.setText("Spotify blocks this track from being played.");
                    GTextCurrentPlaying.setVisible(true);
                    GMainVBOX.setCursor(Cursor.DEFAULT);
                });
                return;
            }

            MediaPlayer newPlayer = new MediaPlayer(new Media(selected.getPreviewUrl()));
            Platform.runLater(() -> {
                stopCurrent();
                player = newPlayer;
                if (selected.getId().equals(currentSelectedTrack.getId())) play();
            });
        });
        taskThread.start();
    }

    private synchronized void play() {
        setVolume();
        player.play();
        GMainVBOX.setCursor(Cursor.DEFAULT);
        GTextCurrentPlaying.setText(currentSelectedTrack.getName());
        setPlayerVisible(true);
    }


    private synchronized void stopCurrent() {
        if (player != null) {
            player.stop();
            setPlayerVisible(false);
        }
    }

    private void setPlayerVisible(boolean b) {
        GTextCurrentPlaying.setVisible(b);
        GButtonStopPlaying.setVisible(b);
        GTextVolume.setVisible(b);
        GSliderVolume.setVisible(b);
    }


    private void setVolume() {
        double sliderValue = GSliderVolume.getValue() / GSliderVolume.getMax();
        sliderValue += 0.18 * (1 - sliderValue);
        double volume = Math.pow(sliderValue, 2.71828205201156);
        player.setVolume(volume);
    }

    private void initializeGTitledPaneNewReleases() {
        GTitledPaneNewReleases.setDisable(true);
    }

    private void initializeGTitledPaneAllReleases() {
        GTitledPaneAllReleases.setDisable(true);
    }

    private void initializeGNewReleasesColumns() {
        GNewReleases_Type.setCellValueFactory(new PropertyValueFactory<>("albumType"));
        GNewReleases_Date.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
        GNewReleases_Artist.setCellValueFactory(new PropertyValueFactory<>("artistString"));
        GNewReleases_Release.setCellValueFactory(new PropertyValueFactory<>("albumName"));


        GNewReleases_Artist.setCellFactory((TableColumn<ReleasedAlbum, String> releasedAlbumStringTableColumn) -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (isEmpty()) setText("");
                else {
                    setText(item);

                    int currentIndex = indexProperty()
                            .getValue() < 0 ? 0
                            : indexProperty().getValue();

                    ReleasedAlbum album = releasedAlbumStringTableColumn
                            .getTableView().getItems()
                            .get(currentIndex);

                    //setTextFill(Color.GREEN);
                    //if (!infoHashMap.containsKey(album.getId()))setStyle("-fx-font-weight: bold;");
                }
            }
        });


    }

    private void initializeGOldReleasesColumns() {
        GOldReleases_Type.setCellValueFactory(new PropertyValueFactory<>("albumType"));
        GOldReleases_Date.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
        GOldReleases_Artist.setCellValueFactory(new PropertyValueFactory<>("artistString"));
        GOldReleases_Release.setCellValueFactory(new PropertyValueFactory<>("albumName"));
    }


    public void setNewAlbums(List<ReleasedAlbum> newAlbums) {
        newAlbums.sort(Comparator.comparing(ReleasedAlbum::getLocalDate, Comparator.reverseOrder()));
        this.newAlbums = newAlbums;

        GTableNewReleases.setItems(FXCollections.observableArrayList(newAlbums));
        GTitledPaneNewReleases.setText(GTitledPaneNewReleases.getText() + " (" + newAlbums.size() + ")");

        GTitledPaneNewReleases.setDisable(false);
        Platform.runLater(() -> GTitledPaneNewReleases.setExpanded(true));
    }

    public void setOldAlbums(List<ReleasedAlbum> oldAlbums) {
        oldAlbums.sort(Comparator.comparing(ReleasedAlbum::getLocalDate, Comparator.reverseOrder()));
        this.oldAlbums = oldAlbums;

        GTableOldReleases.setItems(FXCollections.observableArrayList(oldAlbums));
        GTitledPaneAllReleases.setText(GTitledPaneAllReleases.getText() + " (" + oldAlbums.size() + ")");

        GTitledPaneAllReleases.setDisable(false);
        if (newAlbums == null || newAlbums.isEmpty())
            Platform.runLater(() -> GTitledPaneAllReleases.setExpanded(true));
    }


}
