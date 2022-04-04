package me.nefelion.spotifynotifier.gui.controllers;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import me.nefelion.spotifynotifier.FollowedArtist;
import me.nefelion.spotifynotifier.ReleasedAlbum;
import me.nefelion.spotifynotifier.ReleasesProcessor;
import me.nefelion.spotifynotifier.TheEngine;
import me.nefelion.spotifynotifier.data.TempData;
import me.nefelion.spotifynotifier.gui.apps.util.UtilShowAlbums;
import me.nefelion.spotifynotifier.records.TempAlbumInfo;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


public class ControllerAlbums {
    public static final Stack<VBox> VBOXSTACK = new Stack<>();
    private static MediaPlayer player;
    private final HashMap<String, TempAlbumInfo> infoHashMap = new HashMap<>();
    private ControllerOutline controllerOutline;
    private List<ReleasedAlbum> newAlbums, allAlbums, filteredNewAlbums, filteredAllAlbums;
    private ReleasedAlbum currentSelectedAlbum;
    private TrackSimplified currentSelectedTrack;
    private boolean oneArtist;

    @FXML
    private VBox GMainVBOX;
    @FXML
    private Accordion GAccordionAlbums, GAccordionTracklist;
    @FXML
    private TitledPane GTitledPaneNewReleases, GTitledPaneAllReleases, GTitledPaneTracklist, GTitledPaneInfo;
    @FXML
    private TableView<ReleasedAlbum> GTableNewReleases, GTableAllReleases;
    @FXML
    private TableColumn<ReleasedAlbum, String> GAllReleases_Type, GAllReleases_Date, GAllReleases_Artist, GAllReleases_Release,
            GNewReleases_Type, GNewReleases_Date, GNewReleases_Artist, GNewReleases_Release;
    @FXML
    private ListView<TrackSimplified> GListTracklist;
    @FXML
    private ImageView GCoverImageView;
    @FXML
    private Text GTextCurrentPlaying, GTextVolume, GTextLoadingArtist;
    @FXML
    private Button GButtonStopPlaying, GButtonBack, GButtonRandom;
    @FXML
    private Slider GSliderVolume;
    @FXML
    private CheckBox GCheckboxAlbums, GCheckboxSingles, GCheckboxFeaturing;
    @FXML
    private ProgressBar GProgressBar;

    public void setControllerOutline(ControllerOutline controllerOutline) {
        this.controllerOutline = controllerOutline;
    }

    @FXML
    private void initialize() {
        initializeGTitledPaneNewReleases();
        initializeGTitledPaneAllReleases();

        initializeGNewReleasesColumns();
        initializeGAllReleasesColumns();

        initializeGListTracklist();

        initializeGTableNewReleases();
        initializeGTableAllReleases();

        initializeGTextVolume();
        initializeGSliderVolume();

        initializeGTextCurrentPlaying();
        initializeGButtonStopPlaying();

        initializeGProgressBar();
        initializeGButtonBack();
        initializeGButtonRandom();

        GTitledPaneInfo.setDisable(true);
    }

    @FXML
    private void onActionGButtonStopPlaying(ActionEvent actionEvent) {
        stopCurrent();
    }

    @FXML
    private void onActionGButtonBack(ActionEvent actionEvent) {
        stopCurrent();
        controllerOutline.setAlbumsVBOX(VBOXSTACK.pop());
    }

    @FXML
    private void recalculateFilteredAlbums() {
        if (newAlbums != null) {
            recalculateFilteredAlbums(filteredNewAlbums, newAlbums);
            refreshReleases("New releases", filteredNewAlbums, GTitledPaneNewReleases, GTableNewReleases);
        }
        if (allAlbums != null) {
            recalculateFilteredAlbums(filteredAllAlbums, allAlbums);
            refreshReleases("All releases", filteredAllAlbums, GTitledPaneAllReleases, GTableAllReleases);
        }

    }

    private void recalculateFilteredAlbums(List<ReleasedAlbum> filteredAlbums, List<ReleasedAlbum> albums) {
        filteredAlbums.clear();
        for (ReleasedAlbum album : albums) {
            if (album.isFeaturing()) {
                if (!GCheckboxFeaturing.isSelected()) continue;
            } else {
                if (!GCheckboxAlbums.isSelected() && album.getAlbumType().equalsIgnoreCase("ALBUM")) continue;
                if (!GCheckboxSingles.isSelected() && album.getAlbumType().equalsIgnoreCase("SINGLE")) continue;
            }
            filteredAlbums.add(album);
        }
    }

    private void refreshReleases(String albumType, List<ReleasedAlbum> filteredAlbums, TitledPane gTitledPaneReleases, TableView<ReleasedAlbum> gTableReleases) {
        gTableReleases.setItems(FXCollections.observableArrayList(filteredAlbums));
        gTitledPaneReleases.setText(albumType + " (" + filteredAlbums.size() + ")");
    }

    private void initializeGButtonRandom() {
        Tooltip tooltip = new Tooltip("Random release");
        tooltip.setShowDelay(Duration.seconds(0));
        GButtonRandom.setTooltip(tooltip);

        GButtonRandom.setOnAction(e -> {
            if (filteredAllAlbums.size() < 2) return;
            TableView<ReleasedAlbum> GTable;

            if (GTitledPaneNewReleases.isExpanded()) GTable = GTableNewReleases;
            else if (GTitledPaneAllReleases.isExpanded()) GTable = GTableAllReleases;
            else return;

            int size = GTable.getItems().size();
            int randIndex = new Random().nextInt(size);
            while (GTable.getSelectionModel().getSelectedIndex() == randIndex)
                randIndex = new Random().nextInt(size);

            GTable.getSelectionModel().select(randIndex);
            GTable.scrollTo(randIndex);
        });
    }

    private void initializeGButtonBack() {
        if (VBOXSTACK.isEmpty()) GButtonBack.setVisible(false);
    }

    private void initializeGProgressBar() {
        showProgressBar(false);
        GProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    }

    private void initializeGTextVolume() {
        GTextVolume.setVisible(false);
    }

    private void initializeGButtonStopPlaying() {
        GButtonStopPlaying.setVisible(false);
    }

    private void initializeGTextCurrentPlaying() {
        GTextCurrentPlaying.setVisible(false);
    }

    private void initializeGSliderVolume() {
        GSliderVolume.setValue(TempData.getInstance().getVolumeSliderValue());
        GSliderVolume.valueProperty().addListener((observable, oldValue, newValue) -> setVolume());
        GSliderVolume.setVisible(false);
    }

    private void initializeGTableNewReleases() {
        addAlbumSelectionListenerFor(GTableNewReleases);
        setTableViewRowFactory(GTableNewReleases);
    }

    private void initializeGTableAllReleases() {
        addAlbumSelectionListenerFor(GTableAllReleases);
        setTableViewRowFactory(GTableAllReleases);
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

                if (Arrays.stream(item.getArtists()).anyMatch(p -> p.getId().equals(currentSelectedAlbum.getArtistId()))) {
                    setStyle("-fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });

        GListTracklist.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) return;
            currentSelectedTrack = newSelection;
            playSelected();
        });
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
    }

    private void initializeGAllReleasesColumns() {
        GAllReleases_Type.setCellValueFactory(new PropertyValueFactory<>("albumType"));
        GAllReleases_Date.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
        GAllReleases_Artist.setCellValueFactory(new PropertyValueFactory<>("artistString"));
        GAllReleases_Release.setCellValueFactory(new PropertyValueFactory<>("albumName"));
    }


    public void setNewAlbums(List<ReleasedAlbum> newAlbums) {
        if (newAlbums == null) return;
        if (newAlbums.isEmpty()) {
            GTitledPaneNewReleases.getStylesheets().clear();
            return;
        }

        newAlbums.sort(Comparator.comparing(ReleasedAlbum::getLocalDate, Comparator.reverseOrder()));
        this.newAlbums = newAlbums;
        this.filteredNewAlbums = new ArrayList<>(newAlbums);

        refreshReleases("New releases", filteredNewAlbums, GTitledPaneNewReleases, GTableNewReleases);

        GTitledPaneNewReleases.setDisable(false);

        GAccordionAlbums.setExpandedPane(GTitledPaneNewReleases);
        GAccordionTracklist.setExpandedPane(GTitledPaneTracklist);
        Platform.runLater(() -> GTableNewReleases.getSelectionModel().select(0));

    }

    public void setAllAlbums(List<ReleasedAlbum> allAlbums) {
        initializeCheckboxesFor(allAlbums);

        if (allAlbums.isEmpty()) {
            GTitledPaneTracklist.setDisable(true);
            GTitledPaneInfo.setDisable(true);
            return;
        }

        allAlbums.sort(Comparator.comparing(ReleasedAlbum::getLocalDate, Comparator.reverseOrder()));
        this.allAlbums = allAlbums;
        this.filteredAllAlbums = new ArrayList<>(allAlbums);
        this.oneArtist = allAlbums.stream().filter(p -> !p.getArtistId().equals(allAlbums.get(0).getArtistId())).findAny().isEmpty();

        refreshReleases("All releases", filteredAllAlbums, GTitledPaneAllReleases, GTableAllReleases);


        GTitledPaneAllReleases.setDisable(false);
        if (newAlbums == null || newAlbums.isEmpty()) {
            GAccordionAlbums.setExpandedPane(GTitledPaneAllReleases);
            GAccordionTracklist.setExpandedPane(GTitledPaneTracklist);
            Platform.runLater(() -> GTableAllReleases.getSelectionModel().select(0));
        }
    }

    private void initializeCheckboxesFor(List<ReleasedAlbum> albums) {
        long numberOfAlbums = albums.stream().filter(p -> p.getAlbumType().equalsIgnoreCase("ALBUM") && !p.isFeaturing()).count();
        long numberOfSingles = albums.stream().filter(p -> p.getAlbumType().equalsIgnoreCase("SINGLE") && !p.isFeaturing()).count();
        long numberOfFeaturing = albums.stream().filter(ReleasedAlbum::isFeaturing).count();

        GCheckboxAlbums.setText("Albums (" + numberOfAlbums + ")");
        GCheckboxSingles.setText("Singles (" + numberOfSingles + ")");
        GCheckboxFeaturing.setText("Featuring (" + numberOfFeaturing + ")");

        GCheckboxAlbums.setSelected(numberOfAlbums != 0);
        GCheckboxAlbums.setDisable(numberOfAlbums == 0);
        GCheckboxSingles.setSelected(numberOfSingles != 0);
        GCheckboxSingles.setDisable(numberOfSingles == 0);
        GCheckboxFeaturing.setSelected(numberOfFeaturing != 0);
        GCheckboxFeaturing.setDisable(numberOfFeaturing == 0);
    }


    private void addAlbumSelectionListenerFor(TableView<ReleasedAlbum> gTable) {
        gTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (getSelectedAlbum() == null) return;
            String id = getSelectedAlbum().getId();

            if (newSelection != null) {
                if (gTable.equals(GTableNewReleases))
                    GTableAllReleases.getSelectionModel().clearSelection();
                else if (gTable.equals(GTableAllReleases))
                    GTableNewReleases.getSelectionModel().clearSelection();
            }

            if (getSelectedAlbum().equals(currentSelectedAlbum)) return;
            currentSelectedAlbum = getSelectedAlbum();

            if (!infoHashMap.containsKey(id)) {
                GCoverImageView.setImage(null);
                GListTracklist.setPlaceholder(new Label("Loading..."));

                GListTracklist.setItems(null);
                Thread taskThread = new Thread(() -> downloadInfoForAlbum(getSelectedAlbum()));
                taskThread.start();
            } else loadInfo();


            if (!GTitledPaneTracklist.isExpanded() && !GTitledPaneInfo.isExpanded()) {
                GTitledPaneTracklist.setExpanded(true);
            }
        });
    }

    private void loadInfo() {
        TempAlbumInfo info = infoHashMap.get(getSelectedAlbum().getId());
        if (info == null) return;
        GCoverImageView.setImage(info.cover());
        GListTracklist.setItems(FXCollections.observableArrayList(info.trackList()));
    }

    private synchronized void downloadInfoForAlbum(ReleasedAlbum releasedAlbum) {
        GMainVBOX.setCursor(Cursor.WAIT);
        String id = releasedAlbum.getId();
        Album album = TheEngine.getInstance().getAlbum(id);
        List<TrackSimplified> trackList = TheEngine.getInstance().getTracks(album.getId());
        //AudioFeatures[] features = TheEngine.getInstance().getAudioFeatures(trackList.stream().map(TrackSimplified::getId).collect(Collectors.toList()));
        try {
            URL url = new URL(album.getImages()[1].getUrl());
            InputStream input = url.openStream();
            Platform.runLater(() -> {
                infoHashMap.put(id, new TempAlbumInfo(album, new Image(input), trackList));
                if (Objects.equals(getSelectedAlbum().getId(), id)) loadInfo();
                //downloadInfoForBuffers();
            });
        } catch (IOException e) {
            System.err.println("CAN'T DOWNLOAD COVER");
        }
        GMainVBOX.setCursor(Cursor.DEFAULT);
    }

    private ReleasedAlbum getSelectedAlbum() {
        return GTableNewReleases.getSelectionModel().getSelectedItem() == null
                ? GTableAllReleases.getSelectionModel().getSelectedItem()
                : GTableNewReleases.getSelectionModel().getSelectedItem();
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
                player.setOnEndOfMedia(() -> setPlayerVisible(false));
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
        TempData.getInstance().setVolumeSliderValue(GSliderVolume.getValue());

        double sliderPercentage = GSliderVolume.getValue() / GSliderVolume.getMax();

        sliderPercentage += 0.18 * (1 - sliderPercentage);
        double volume = Math.pow(sliderPercentage, 2.71828205201156);
        player.setVolume(volume);
    }

    private void setTableViewRowFactory(TableView<ReleasedAlbum> GTable) {

        GTable.setRowFactory(tableView -> {
            TableRow<ReleasedAlbum> row = new TableRow<>() {
                @Override
                protected void updateItem(ReleasedAlbum item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) return;

                    if (item.isFeaturing()) {
                        if (!getStyleClass().contains("featuring"))
                            getStyleClass().add("featuring");
                    } else getStyleClass().remove("featuring");
                }
            };

            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem showOnSpotifyMenuItem = new MenuItem("Show on Spotify");
            final MenuItem copySpotifyLinkMenuItem = new MenuItem("Copy Spotify link");
            final MenuItem showReleasesMenuItem = new MenuItem("Show releases by...");
            final MenuItem followUnfollowMenuItem = new MenuItem();
            showOnSpotifyMenuItem.setOnAction(event -> {
                ReleasedAlbum album = row.getItem();
                Runtime rt = Runtime.getRuntime();
                String url = "https://open.spotify.com/album/" + album.getId();
                try {
                    rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            copySpotifyLinkMenuItem.setOnAction(event -> {
                String copiedStuff = getSelectedAlbum().getLink();
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(
                                new StringSelection(copiedStuff),
                                null
                        );
            });
            showReleasesMenuItem.setOnAction(event -> {
                ReleasedAlbum album = row.getItem();
                showReleases(new FollowedArtist(album.getFollowedArtistName(), album.getArtistId()));
            });

            showReleasesMenuItem.setDisable(true);
            contextMenu.getItems().add(showOnSpotifyMenuItem);
            contextMenu.getItems().add(copySpotifyLinkMenuItem);
            contextMenu.getItems().add(showReleasesMenuItem);
            contextMenu.getItems().add(followUnfollowMenuItem);
            contextMenu.getItems().forEach(p -> p.setVisible(false));

            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );


            tableView.getSelectionModel().getSelectedIndices().addListener((InvalidationListener) (event) -> {
                getContextMenuLambdaBody(row, showReleasesMenuItem, followUnfollowMenuItem, contextMenu);
            });
            contextMenu.setOnHiding((event) -> {
                getContextMenuLambdaBody(row, showReleasesMenuItem, followUnfollowMenuItem, contextMenu);
            });


            return row;
        });

    }

    private void getContextMenuLambdaBody(TableRow<ReleasedAlbum> row, MenuItem showReleasesMenuItem, MenuItem followUnfollowMenuItem, ContextMenu contextMenu) {
        ReleasedAlbum album = row.getItem();
        if (album == null) {
            showReleasesMenuItem.setDisable(true);
            followUnfollowMenuItem.setDisable(true);
            return;
        }

        showReleasesMenuItem.setDisable(false);
        contextMenu.getItems().forEach(p -> p.setVisible(true));
        showReleasesMenuItem.setOnAction((ev) -> showPickArtistDialog(album));

        boolean isArtistFollowed = TheEngine.getInstance().isFollowed(album.getArtistId());
        followUnfollowMenuItem.setDisable(false);
        followUnfollowMenuItem.setText((isArtistFollowed ? "Unfollow " : "Follow ") + album.getFollowedArtistName());
        followUnfollowMenuItem.setOnAction((ev) -> {
            if (isArtistFollowed) TheEngine.getInstance().unfollowArtistID(album.getArtistId());
            else TheEngine.getInstance().followArtistID(album.getArtistId());
        });
    }

    private void showPickArtistDialog(ReleasedAlbum album) {
        Task<Boolean> task = new Task<>() {
            @Override
            public Boolean call() {
                if (!infoHashMap.containsKey(album.getId())) downloadInfoForAlbum(album);
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            List<ArtistSimplified> artistsAndPerformers = getArtistsAndPerformers(infoHashMap.get(album.getId()).trackList());
            artistsAndPerformers.removeIf(p -> oneArtist && p.getId().equals(allAlbums.get(0).getArtistId()));

            Stage stage = new Stage(StageStyle.UTILITY);
            stage.setTitle("Pick an artist");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            ListView<ArtistSimplified> list = initializeListOfArtistsToPick(stage);
            list.setItems(FXCollections.observableArrayList(artistsAndPerformers));
            list.setPrefHeight(artistsAndPerformers.size() * 24 + 2);
            Scene scene = new Scene(list);
            stage.setScene(scene);
            stage.showAndWait();
        });
        new Thread(task).start();


    }

    private ListView<ArtistSimplified> initializeListOfArtistsToPick(Stage stage) {
        ListView<ArtistSimplified> list = new ListView<>();

        list.setPlaceholder(new Label("Nothing to be found here"));

        list.setCellFactory((param) -> new ListCell<>() {
            @Override
            protected void updateItem(ArtistSimplified item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    return;
                }
                setText(item.getName());
            }
        });

        list.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) return;
            showReleases(newSelection);
            stage.close();
        });
        list.setMaxHeight(500);
        return list;
    }

    private void showReleases(ArtistSimplified artistSimplified) {
        showReleases(new FollowedArtist(artistSimplified.getName(), artistSimplified.getId()));
    }

    private void showReleases(FollowedArtist artist) {
        stopCurrent();
        ReleasesProcessor processor = new ReleasesProcessor(artist);
        disableAllElements(true);
        showProgressBar(true);

        Task<Boolean> task = new Task<>() {
            @Override
            public Boolean call() {
                processor.setCurrentArtistConsumer(GTextLoadingArtist::setText);
                processor.process();
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            showReleases(processor);
            disableAllElements(false);
            showProgressBar(false);
        });
        new Thread(task).start();
    }

    public void showReleases(ReleasesProcessor processor) {
        stopCurrent();
        VBox oldVbox = controllerOutline.getAlbumsVBOX();
        if (oldVbox != null) VBOXSTACK.push(oldVbox);
        controllerOutline.setAlbumsVBOX(UtilShowAlbums.getAlbumsVBOX(processor.getNewAlbums(), processor.getAllAlbums()));
    }

    private void showProgressBar(boolean b) {
        GProgressBar.setVisible(b);
        GTextLoadingArtist.setText("");
        GTextLoadingArtist.setVisible(b);
    }

    private void disableAllElements(boolean b) {
        GSliderVolume.setDisable(b);
        GTableAllReleases.setDisable(b);
        GTableNewReleases.setDisable(b);
        GListTracklist.setDisable(b);
    }

    public VBox getGMainVBOX() {
        return GMainVBOX;
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
}